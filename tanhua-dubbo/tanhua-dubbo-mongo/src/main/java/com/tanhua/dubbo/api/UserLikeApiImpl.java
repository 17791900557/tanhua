package com.tanhua.dubbo.api;

import com.tabhua.model.mongo.UserLike;
import net.sf.jsqlparser.expression.LongValue;
import org.apache.dubbo.config.annotation.DubboService;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.Set;

@DubboService
public class UserLikeApiImpl  implements UserLikeApi{
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 保存或者更新喜欢
     * @param likeUserId
     * @param userId
     * @param isLike
     * @return
     */
    @Override
    public Boolean saveOrUpdate(Long likeUserId, Long userId, boolean isLike) {
        //查询数据是否存在
        try {
            Criteria criteria = Criteria.where("userId").is(userId)
                    .and("likeUserId").is(likeUserId);
            Query query = Query.query(criteria);
            UserLike userLike = mongoTemplate.findOne(query, UserLike.class);
            if (userLike == null){
                userLike = new UserLike();
                userLike.setIsLike(isLike);
                userLike.setUserId(userId);
                userLike.setLikeUserId(likeUserId);
                userLike.setCreated(System.currentTimeMillis());
                userLike.setUpdated(System.currentTimeMillis());
                mongoTemplate.save(userLike);
            }else {
                Update up = Update.update("isLike", isLike)
                        .set("updated", System.currentTimeMillis());
                mongoTemplate.updateFirst(query,up,UserLike.class);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    /**
     * 互相喜欢
     * @param id
     * @param ids
     * @param isLike
     * @return
     */
    @Override
    public List<UserLike> eachLoveCount(Long id, List<Long> ids, boolean isLike) {
        Criteria criteria = Criteria.where("likeUserId").is(id).and("userId").in(ids)
                .and("isLike").is(isLike);
        Query query = Query.query(criteria);
        List<UserLike> userLikes = mongoTemplate.find(query, UserLike.class);
        return userLikes;
    }

    /**
     * 分页列表取消喜欢
     * @param likeUserId
     * @param userId
     */
    @Override
    public  Boolean  offLike(Long likeUserId, Long userId) {
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("likeUserId").is(likeUserId);
        Query query = Query.query(criteria);
        boolean exists = mongoTemplate.exists(query, UserLike.class);
        if (exists) {
            mongoTemplate.remove(query, UserLike.class);
        }else {
            return false;
        }
        return true;
    }

    /**
     * 粉丝
     * @param id
     * @param b
     * @return
     */
    @Override
    public  List<UserLike> fanCount(Long id, boolean b) {
        Query query = Query.query(Criteria.where("likeUserId").is(id).and("isLike").is(b));
        List<UserLike> userLikes = mongoTemplate.find(query, UserLike.class);
        return userLikes;
    }

}
