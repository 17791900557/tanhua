package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import com.tabhua.model.domain.UserInfo;
import com.tabhua.model.enums.CommentType;
import com.tabhua.model.mongo.Comment;
import com.tabhua.model.vo.CommentVo;
import com.tabhua.model.vo.ErrorResult;
import com.tabhua.model.vo.PageResult;
import com.tanhua.commoms.utils.Constants;
import com.tanhua.dubbo.api.CommentApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CommentsService {
    @DubboReference(check = false)
    private CommentApi commentApi;
    @DubboReference(check = false)
    private UserInfoApi userInfoApi;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private UserFreezeService userFreezeService;

    /**
     * 发表平论
     *
     * @param movementId
     * @param comment
     */
    public void saveComments(String movementId, String comment) {
        Long userId = UserHolder.getId();

        String key = Constants.USER_FREEZE+UserHolder.getId();
        Boolean aBoolean = redisTemplate.hasKey(key);
        if (aBoolean){
            userFreezeService.checkUserStatus("2",UserHolder.getId());
        }
        Comment comment1 = new Comment();
        comment1.setPublishId(new ObjectId(movementId));
        comment1.setCommentType(CommentType.COMMENT.getType());
        comment1.setContent(comment);
        comment1.setUserId(userId);
        comment1.setCreated(System.currentTimeMillis());
        Integer count = commentApi.save(comment1);
        log.info(count + "");
    }

    /**
     * 评论列表
     *
     * @param page
     * @param pagesize
     * @param movementId
     * @return
     */
    public PageResult getComments(int page, int pagesize, String movementId) {
        PageResult pr = commentApi.getComments(page, pagesize, movementId, CommentType.COMMENT);

        List<Comment> list = (List<Comment>) pr.getItems();
        int counts = pr.getCounts();
        List<CommentVo> vos = new ArrayList<>();
        if (CollUtil.isEmpty(list)) {
            return new PageResult();
        }
        List<Long> userIds = CollUtil.getFieldValues(list, "userId", Long.class);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, null);
        for (Comment comment : list) {
            UserInfo userInfo = map.get(comment.getUserId());
            if (userInfo != null) {
                CommentVo vo = CommentVo.init(userInfo, comment);
                String key = Constants.MOVEMENTS_INTERACT_KEY + comment.getId();
                String hashKey = Constants.COMMENT_LIKE_HASHKEY + UserHolder.getId();
                if (redisTemplate.opsForHash().hasKey(key,hashKey)) {
                   vo.setHasLiked(1);
                }
                vos.add(vo);
            }
        }
        return new PageResult(page, pagesize, counts, vos);
    }

    /**
     * 动态点赞
     *
     * @param movementId
     * @return
     */
    public Integer like(String movementId) {
        //查询用户是否已经点赞
        Boolean hasComment = commentApi.hasComment(movementId, UserHolder.getId(), CommentType.LIKE);
        //如果已经点赞抛出异常
        if (hasComment) {
            throw new BusinessException(ErrorResult.likeError());
        }
        //3、调用API保存数据到Mongodb
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(movementId));
        comment.setCommentType(CommentType.LIKE.getType());
        comment.setUserId(UserHolder.getId());
        comment.setCreated(System.currentTimeMillis());
        Integer count = commentApi.save(comment);//查询点赞总数
        //拼接redis
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;
        String hashKey = Constants.MOVEMENT_LIKE_HASHKEY + UserHolder.getId();

        redisTemplate.opsForHash().put(key, hashKey, "1");
        return count;
    }

    /**
     * 取消点赞
     *
     * @param movementId
     * @return
     */
    public Integer dislikeComment(String movementId) {
        //查询用户是否已经点赞
        Boolean hasComment = commentApi.hasComment(movementId, UserHolder.getId(), CommentType.LIKE);
        if (!hasComment) {
            throw new BusinessException(ErrorResult.disLikeError());
        }
        //3、调用API，删除数据，返回点赞数量
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(movementId));
        comment.setCommentType(CommentType.LIKE.getType());
        comment.setUserId(UserHolder.getId());
        Integer count = commentApi.delete(comment);
        //4、拼接redis的key，删除点赞状态
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;
        String hashKey = Constants.MOVEMENT_LIKE_HASHKEY + UserHolder.getId();
        redisTemplate.opsForHash().delete(key,hashKey);
        return count;
    }

    /**
     * 保存喜欢
     *
     * @param movementId
     * @return
     */
    public Integer love(String movementId) {
        Boolean hasComment = commentApi.hasComment(movementId, UserHolder.getId(), CommentType.LOVE);
        if (hasComment) {
            throw new BusinessException(ErrorResult.loveError());
        }
        //3、调用API保存数据到Mongodb
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(movementId));
        comment.setCommentType(CommentType.LOVE.getType());
        comment.setUserId(UserHolder.getId());
        comment.setCreated(System.currentTimeMillis());
        Integer count = commentApi.save(comment);//查询点赞总数
        //拼接redis
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;
        String hashKey = Constants.MOVEMENT_LOVE_HASHKEY + UserHolder.getId();

        redisTemplate.opsForHash().put(key, hashKey, "1");
        return count;

    }

    /**
     * 取消喜欢
     *
     * @param movementId
     * @return
     */
    public Integer disloveComment(String movementId) {
        //1、调用API查询用户是否已点赞
        Boolean hasComment = commentApi.hasComment(movementId, UserHolder.getId(), CommentType.LOVE);
        //2、如果未点赞，抛出异常
        if (!hasComment) {
            throw new BusinessException(ErrorResult.disloveError());
        }
        //3、调用API，删除数据，返回点赞数量
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(movementId));
        comment.setCommentType(CommentType.LOVE.getType());
        comment.setUserId(UserHolder.getId());
        Integer count = commentApi.delete(comment);
        //4、拼接redis的key，删除点赞状态
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;
        String hashKey = Constants.MOVEMENT_LOVE_HASHKEY + UserHolder.getId();
        redisTemplate.opsForHash().delete(key, hashKey);
        return count;
    }

    /**
     * 评论点赞
     *
     * @param commentId
     * @return
     */
    public Integer pingLunLike(String commentId) {
        //拼接redis
        String key = Constants.MOVEMENTS_INTERACT_KEY + commentId;
        String hashKey = Constants.COMMENT_LIKE_HASHKEY + UserHolder.getId();
        if (!redisTemplate.opsForHash().hasKey(key,hashKey)) {
            redisTemplate.opsForHash().put(key, hashKey, "1");
        }
        Integer count = commentApi.pingLunCount(commentId);
        return count;
    }

    /**
     * 评论取消点赞
     * @param commentId
     * @return
     */
    public Integer pingLunDisLike(String commentId) {
        //拼接redis
        String key = Constants.MOVEMENTS_INTERACT_KEY + commentId;
        String hashKey = Constants.COMMENT_LIKE_HASHKEY + UserHolder.getId();
        if (redisTemplate.opsForHash().hasKey(key,hashKey)) {
            redisTemplate.opsForHash().delete(key,hashKey);
        }
        Integer count = commentApi.pingLunDelete(commentId);
        return count;
    }
}

