package com.tanhua.dubbo.api;


import cn.hutool.core.collection.CollUtil;
import com.tabhua.model.mongo.Movement;
import com.tabhua.model.mongo.MovementTimeLine;
import com.tabhua.model.vo.PageResult;
import com.tanhua.dubbo.utils.IdWorker;
import com.tanhua.dubbo.utils.TimeLineService;
import org.apache.dubbo.config.annotation.DubboService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@DubboService
public class MovementApiImpl implements MovementApi {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private TimeLineService timeLineService;

    /**
     * 发布动态
     *
     * @param movement
     */
    @Override
    public void publishMovements(Movement movement) {
        //设置Pid
        movement.setPid(idWorker.getNextId("movement"));
        //设置时间
        movement.setCreated(System.currentTimeMillis());
        mongoTemplate.save(movement);
        //查询好友数据
//        Criteria criteria = Criteria.where("userId").is(movement.getUserId());
//        Query query =Query.query(criteria);
//        List<Friend> friends = mongoTemplate.find(query, Friend.class);
//
//        for (Friend friend : friends) {
//            MovementTimeLine mv = new MovementTimeLine();
//            mv.setCreated(friend.getCreated());
//            mv.setFriendId(friend.getFriendId());
//            mv.setUserId(friend.getUserId());
//            mv.setMovementId(movement.getId());
//            mongoTemplate.save(mv);
//        }
        timeLineService.saveTimeLine(movement.getUserId(), movement.getId());
    }

    /**
     * 我的动态
     *
     * @param page
     * @param pageSize
     * @param userId
     * @return
     */
    @Override
    public PageResult getAllMovements(int page, int pageSize, Long userId) {
        Criteria criteria = Criteria.where("userId").is(userId);
        Query query = Query.query(criteria);
        //查询总数
        int count = (int) mongoTemplate.count(query, Movement.class);
        //分页查询
        query.with(Sort.by(Sort.Order.desc("created"))).skip((page - 1) * pageSize);
        List<Movement> movements = mongoTemplate.find(query, Movement.class);
        PageResult pageResult = new PageResult(page, pageSize, count, movements);
        return pageResult;
    }

    /**
     * 查询好友动态
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    public List<Movement> friendMovements(int page, int pagesize, Long userId) {
        Criteria criteria = Criteria.where("friendId").is(userId);
        Query query = Query.query(criteria).skip((page - 1)*pagesize)
                .with(Sort.by(Sort.Order.desc("created")));;
        List<MovementTimeLine> movementTimeLines = mongoTemplate.find(query, MovementTimeLine.class);
        List<ObjectId> movementIds = CollUtil.getFieldValues(movementTimeLines, "movementId", ObjectId.class);
        Query movementQuery = Query.query(Criteria.where("_id").in(movementIds));

        List<Movement> list = mongoTemplate.find(movementQuery, Movement.class);

        return list;
    }

    /**
     * 随机获取动态数据
     * @param pagesize
     * @return
     */
    @Override
    public List<Movement> randomMovements(Integer pagesize) {
        //1.创建统计对象，设置统计参数
        TypedAggregation aggregation = Aggregation.newAggregation(Movement.class,Aggregation.sample(pagesize));
        AggregationResults<Movement> aggregate = mongoTemplate.aggregate(aggregation, Movement.class);
        return aggregate.getMappedResults();
    }

    /**
     * 根据id查询
     * @param pids
     * @return
     */
    @Override
    public List<Movement> findByPids(List<Long> pids) {
        Query query = Query.query(Criteria.where("pid").in(pids));
        return  mongoTemplate.find(query,Movement.class);
    }

    /**
     *查询单挑动态
     * @param id
     * @return
     */
    @Override
    public Movement findMovementById(String id) {
        return mongoTemplate.findById(id,Movement.class);
    }

    /**
     * 根据条件查询
     * @param page
     * @param pagesize
     * @param userId
     * @param state
     * @return
     */
    @Override
    public PageResult getMovements(Integer page, Integer pagesize, Long userId, Integer state) {
        Query query = new Query();
        if (userId != null){
            query.addCriteria(Criteria.where("userId").is(userId));
        }
        if (state != null){
            query.addCriteria(Criteria.where("state").is(state));
        }
        int count = (int) mongoTemplate.count(query, Movement.class);
        query.with(Sort.by(Sort.Order.desc("created"))).skip((page-1)*pagesize).limit(pagesize);
        List<Movement> list = mongoTemplate.find(query, Movement.class);
        return new PageResult(page,pagesize,count,list);

    }
}
