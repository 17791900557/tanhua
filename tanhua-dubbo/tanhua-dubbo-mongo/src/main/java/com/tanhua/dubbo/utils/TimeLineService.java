package com.tanhua.dubbo.utils;

import com.tabhua.model.mongo.Friend;
import com.tabhua.model.mongo.MovementTimeLine;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TimeLineService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Async
    public void saveTimeLine(Long userId, ObjectId movementId) {
        Criteria criteria = Criteria.where("userId").is(userId);
        Query query = Query.query(criteria);
        List<Friend> friends = mongoTemplate.find(query,Friend.class);

        for (Friend friend : friends) {
            MovementTimeLine mv = new MovementTimeLine();
            mv.setCreated(friend.getCreated());
            mv.setFriendId(friend.getFriendId());
            mv.setUserId(friend.getUserId());
            mv.setMovementId(movementId);
            mongoTemplate.save(mv);
        }
    }
}
