package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tabhua.model.domain.UserInfo;
import com.tabhua.model.enums.CommentType;
import com.tabhua.model.mongo.Comment;
import com.tabhua.model.mongo.FocusUser;
import com.tabhua.model.mongo.Video;
import com.tabhua.model.vo.CommentVo;
import com.tabhua.model.vo.ErrorResult;
import com.tabhua.model.vo.PageResult;
import com.tabhua.model.vo.VideoVo;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.commoms.utils.Constants;
import com.tanhua.dubbo.api.CommentApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.VideoApi;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SmallVideosService {
    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private FastFileStorageClient client;

    @Autowired
    private FdfsWebServer webServer;

    @DubboReference(check = false)
    private VideoApi videoApi;

    @DubboReference(check = false)
    private UserInfoApi userInfoApi;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @DubboReference(check = false)
    private CommentApi commentApi;

    /**
     * 上传视频
     *
     * @param videoThumbnail 图片
     * @param videoFile      视频
     */
    public void saveVideos(MultipartFile videoThumbnail, MultipartFile videoFile) throws IOException {
        Long userId = UserHolder.getId();
        if (videoThumbnail.isEmpty() || videoFile.isEmpty()) {
            throw new BusinessException(ErrorResult.error());
        }
        //1、封面图上传到阿里云OSS，获取地址
        String picUrl = ossTemplate.upload(videoThumbnail.getOriginalFilename(), videoThumbnail.getInputStream());
        //2、视频上传到fdfs上，获取请求地址
        //获取文件的后缀名
        String filename = videoFile.getOriginalFilename();  //ssss.avi
        String sufix = filename.substring(filename.lastIndexOf(".") + 1);
        StorePath storePath = client.uploadFile(videoFile.getInputStream(),
                videoFile.getSize(), sufix, null);//文件输入流，文件长度，文件后缀，元数据
        String videoUrl = webServer.getWebServerUrl() + storePath.getFullPath();
        //3、创建Video对象，并设置属性
        Video video = new Video();
        video.setUserId(userId);
        video.setPicUrl(picUrl);
        video.setVideoUrl(videoUrl);
        video.setText("我就是我，不一样的烟火");
        //4、调用API完成保存
        String videoId = videoApi.save(video);
        if (videoId.isEmpty()) {
            throw new BusinessException(ErrorResult.error());
        }
    }

    /**
     * 查询视频列表
     *
     * @param page
     * @param pagesize
     * @return
     */
    @Cacheable(value = "videos",key = "T(com.tanhua.server.interceptor.UserHolder).getId()+'_'+#page")
    public PageResult queryVideoList(Integer page, Integer pagesize) {
        //1、调用API查询分页数据 PageResult<Video>
        PageResult result = videoApi.findAll(page, pagesize);
        //2、获取分页对象中list集合  List<Video>
        List<Video> items = (List<Video>) result.getItems();
        //3、一个Video转化成一个VideoVo对象
        List<VideoVo> list = new ArrayList<>();
        for (Video item : items) {
            UserInfo info = userInfoApi.findById(item.getUserId());
            VideoVo vo = VideoVo.init(info, item);
            //加入了作者关注功能，从redis判断是否存在关注的key，如果存在设置hasFocus=1
            if (redisTemplate.hasKey(Constants.FOCUS_USER + UserHolder.getId() + "_" + item.getUserId())) {
                vo.setHasFocus(1);
            }
            if (redisTemplate.opsForHash().hasKey(Constants.VIDEO_LIKE_HASHKEY+item.getId(),Constants.VIDEO_LIKE_HASHKEY+UserHolder.getId())){
                vo.setHasLiked(1);
            }
            list.add(vo);
        }
        //4、替换PageResult中的list列表
        result.setItems(list);
        //5、构造返回值
        return result;
    }

    //关注视频作者
    public void userFocus(Long followUserId) {
        Long userId = UserHolder.getId();
        FocusUser focusUser = new FocusUser();
        focusUser.setUserId(userId);
        focusUser.setFollowUserId(followUserId);
        focusUser.setCreated(System.currentTimeMillis());
        videoApi.userFocus(focusUser);
        String key = Constants.FOCUS_USER + userId + "_" + followUserId;
        String hashKey = String.valueOf(followUserId);
        redisTemplate.opsForHash().put(key, hashKey, "1");
    }

    //取消关注
    public void userUnFocus(Long followUserId) {
        Long userId = UserHolder.getId();
        videoApi.deleteFollowUser(followUserId, userId);
        String key = Constants.FOCUS_USER + userId + "_" + followUserId;
        String hashKey = String.valueOf(followUserId);
        redisTemplate.opsForHash().delete(key, hashKey);

    }

    //视频点赞
    public Integer like(String videoId) {
        Long userId = UserHolder.getId();
        //判断是否已经点赞
        Boolean aBoolean = commentApi.hasComment(videoId, userId, CommentType.LIKE);
        if (aBoolean){
            throw new BusinessException(ErrorResult.likeError());
        }
        //3、调用API保存数据到Mongodb
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(videoId));
        comment.setCommentType(CommentType.LIKE.getType());
        comment.setUserId(userId);
        comment.setCreated(System.currentTimeMillis());
        Integer count = commentApi.videoSave(comment);//查询点赞总数
        //拼接redis
        String key = Constants.VIDEO_LIKE_HASHKEY + videoId;
        String hashKey = Constants.VIDEO_LIKE_HASHKEY+ userId;

        redisTemplate.opsForHash().put(key, hashKey, "1");
        return count;

    }

    //取消点赞
    public Integer dislike(String videoId) {
        Long userId = UserHolder.getId();
        //判断是否已经点赞
        Boolean aBoolean = commentApi.hasComment(videoId, userId, CommentType.LIKE);
        if (!aBoolean){
            throw new BusinessException(ErrorResult.likeError());
        }
        //3、调用API保存数据到Mongodb
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(videoId));
        comment.setCommentType(CommentType.LIKE.getType());
        comment.setUserId(userId);
        comment.setCreated(System.currentTimeMillis());
        Integer count = commentApi.dislike(comment);//查询点赞总数
        //拼接redis
        String key = Constants.VIDEO_LIKE_HASHKEY + videoId;
        String hashKey = Constants.VIDEO_LIKE_HASHKEY+ userId;
        redisTemplate.opsForHash().delete(key,hashKey);
        return count;
    }

    /**
     * 视频评论
     * @param videoId
     * @param comment
     */
    public Integer saveComments(String videoId, String comment) {
        Long userId = UserHolder.getId();
        Comment comment1 = new Comment();
        comment1.setPublishId(new ObjectId(videoId));
        comment1.setCommentType(CommentType.COMMENT.getType());
        comment1.setContent(comment);
        comment1.setUserId(userId);
        comment1.setCreated(System.currentTimeMillis());
        Integer count = commentApi.videoSave(comment1);
        return count;
    }

    //视频评论列表
    public PageResult videoComments(String videoId, Integer page, Integer pagesize) {
        PageResult pageResult = commentApi.getComments(page, pagesize, videoId, CommentType.COMMENT);
        List<Comment> items = (List<Comment>) pageResult.getItems();
        if (CollUtil.isEmpty(items)){
            return new PageResult();
        }
        List<Long> userIds = CollUtil.getFieldValues(items, "userId", Long.class);
        List<CommentVo> vos = new ArrayList<>();
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, null);
        for (Comment item : items) {
            UserInfo userInfo = map.get(item.getUserId());
            if (userInfo != null){
                CommentVo vo  = CommentVo.init(userInfo,item);
                if (redisTemplate.opsForHash().hasKey(Constants.MOVEMENT_LIKE_HASHKEY+item.getId(),Constants.COMMENT_LIKE_HASHKEY+UserHolder.getId())){
                    vo.setHasLiked(1);
                }
                vos.add(vo);
            }
        }
        pageResult.setItems(vos);
        return pageResult;
    }

    //视频评论点赞
    public Integer pingLunLike(String commentId) {
        //拼接redis
        String key = Constants.COMMENT_LIKE_HASHKEY + commentId;
        String hashKey = Constants.COMMENT_LIKE_HASHKEY + UserHolder.getId();
        if (!redisTemplate.opsForHash().hasKey(key,hashKey)) {
            redisTemplate.opsForHash().put(key, hashKey, "1");
        }
        Integer count = commentApi.pingLunCount(commentId);
        return count;
    }

    //评论取消点赞
    public Integer pingLunDisLike(String commentId) {
        //拼接redis
        String key = Constants.COMMENT_LIKE_HASHKEY + commentId;
        String hashKey = Constants.COMMENT_LIKE_HASHKEY + UserHolder.getId();

        if (redisTemplate.opsForHash().hasKey(key,hashKey)) {
            redisTemplate.opsForHash().delete(key,hashKey);
        }
        Integer count = commentApi.pingLunDelete(commentId);
        return count;
    }
}
