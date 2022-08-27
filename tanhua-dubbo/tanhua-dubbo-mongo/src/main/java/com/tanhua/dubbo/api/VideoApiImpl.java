package com.tanhua.dubbo.api;

import com.tabhua.model.mongo.FocusUser;
import com.tabhua.model.mongo.Video;
import com.tabhua.model.vo.PageResult;
import com.tanhua.dubbo.utils.IdWorker;
import org.apache.dubbo.config.annotation.DubboService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@DubboService
public class VideoApiImpl implements VideoApi {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private IdWorker idWorker;

    /**
     * 发布视频
     *
     * @param video
     * @return
     */
    @Override
    public String save(Video video) {
        video.setId(ObjectId.get());
        video.setCreated(System.currentTimeMillis());
        video.setVid(idWorker.getNextId("video"));
        mongoTemplate.save(video);
        return video.getId().toHexString();
    }

    /**
     * 视频列表
     *
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult findAll(Integer page, Integer pagesize) {
        //1、查询总数
        long count = mongoTemplate.count(new Query(), Video.class);
        //2、分页查询数据列表
        Query query = new Query().limit(pagesize).skip((page - 1) * pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        List<Video> list = mongoTemplate.find(query, Video.class);
        //3、构建返回
        return new PageResult(page, pagesize, (int) count, list);
    }

    /**
     * 关注用户
     *
     * @param focusUser
     * @return
     */
    @Override
    public void userFocus(FocusUser focusUser) {
        mongoTemplate.save(focusUser);
    }

    //取消关注
    @Override
    public void deleteFollowUser(Long followUserId, Long userId) {
        Query query = Query.query(Criteria.where("followUserId").is(followUserId)
                .and("userId").is(userId));
        mongoTemplate.remove(query, FocusUser.class);
    }

    /**
     * 查询视频列表
     *
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @Override
    public PageResult findVideosByUserId(Integer page, Integer pagesize, Long userId) {
        Criteria criteria = Criteria.where("userId").is(userId);
        Query query1 = Query.query(criteria);
        int count = (int) mongoTemplate.count(query1, Video.class);
        Query query =Query.query(criteria).skip((page-1)*pagesize).limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        List<Video> videos = mongoTemplate.find(query, Video.class);
        return new PageResult(page,pagesize,count,videos);
    }
}

