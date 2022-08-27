package com.tanhua.dubbo.api;

import com.tabhua.model.mongo.Friend;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@DubboService
public class FriendApiImpl implements FriendApi{
    @Autowired
    private MongoTemplate mongoTemplate;
    /**
     * 添加好友关系
     * @param friendId
     * @param
     */
    @Override
    public void save(Long userId, Long friendId) {
        //1、保存自己的好友数据
        Query query1 = Query.query(Criteria.where("userId").is(userId).and("friendId").is(friendId));
        //1.1 判断好友关系是否存在
        if(!mongoTemplate.exists(query1, Friend.class)) {
            //1.2 如果不存在，保存
            Friend friend1 = new Friend();
            friend1.setUserId(userId);
            friend1.setFriendId(friendId);
            friend1.setCreated(System.currentTimeMillis());
            mongoTemplate.save(friend1);
        }
        //2、保存好友的数据
        Query query2 = Query.query(Criteria.where("userId").is(friendId).and("friendId").is(userId));
        //2.1 判断好友关系是否存在
        if(!mongoTemplate.exists(query2, Friend.class)) {
            //2.2 如果不存在，保存
            Friend friend1 = new Friend();
            friend1.setUserId(friendId);
            friend1.setFriendId(userId);
            friend1.setCreated(System.currentTimeMillis());
            mongoTemplate.save(friend1);
        }
    }

    //查询好友列表
    @Override
    public List<Friend> findByUserId(Long userId, Integer page, Integer pagesize) {
        Criteria criteria = Criteria.where("userId").is(userId);
        Query query = Query.query(criteria).skip((page - 1) * pagesize).limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        return mongoTemplate.find(query,Friend.class);
    }

    //删除好友列表
    @Override
    public void delete(Long id, Long friendId) {
        //1、保存自己的好友数据
        Query query1 = Query.query(Criteria.where("userId").is(id).and("friendId").is(friendId));
        //1.1 判断好友关系是否存在
        if(mongoTemplate.exists(query1, Friend.class)) {
            //1.2 如果存在删除
            mongoTemplate.remove(query1,Friend.class);
        }
        //2、保存好友的数据
        Query query2 = Query.query(Criteria.where("userId").is(friendId).and("friendId").is(id));
        //2.1 判断好友关系是否存在
        if(mongoTemplate.exists(query2, Friend.class)) {
            //1.2 如果存在删除
            mongoTemplate.remove(query2,Friend.class);
        }

    }
}
