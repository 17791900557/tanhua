package com.tanhua.dubbo.api;

import cn.hutool.core.collection.CollUtil;
import com.tabhua.model.mongo.RecommendUser;
import com.tabhua.model.mongo.UserLike;
import com.tabhua.model.vo.PageResult;
import com.tanhua.dubbo.api.RecommendUserApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
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
@Slf4j
public class RecommendUserApiImpl implements RecommendUserApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 查询今日佳人
     *
     * @param userId
     * @return
     */
    @Override
    public RecommendUser queryWithMaxScore(Long userId) {
        //根据touserId,查询根据score评分排序，获取第一条
        //构建Criteria
        Criteria criteria = Criteria.where("userId").is(userId);
        //构建Query
        Query query = Query.query(criteria).with(Sort.by(Sort.Order.desc("score")))
                .limit(1);
        //调用查询

        return mongoTemplate.findOne(query, RecommendUser.class);
    }

    /**
     * 分页查询今日推荐
     *
     * @param page
     * @param pagesize
     * @param
     * @return
     */
    @Override
    public PageResult queryRecommendUserList(Integer page, Integer pagesize, Long toUserId) {
        //1、构建Criteria对象
        Criteria criteria = Criteria.where("toUserId").is(toUserId);
        //2、创建Query对象
        Query query = Query.query(criteria).with(Sort.by(Sort.Order.desc("score"))).limit(pagesize)
                .skip((page - 1) * pagesize);
        //3、调用mongoTemplate查询
        List<RecommendUser> list = mongoTemplate.find(query, RecommendUser.class);
        long count = mongoTemplate.count(query, RecommendUser.class);
        //4、构建返回值PageResult
        return new PageResult(page, pagesize, (int) count, list);
    }

    /**
     * 查询推荐用户
     *
     * @param userId
     * @param count
     * @return
     */
    @Override
    public List<RecommendUser> queryCardsList(Long userId, int count) {
        List<UserLike> likeList = mongoTemplate.find(Query.query(Criteria.where("userId").is(userId)), UserLike.class);
        List<Long> likeUserId = CollUtil.getFieldValues(likeList, "likeUserId", Long.class);
        Criteria criteria = Criteria.where("toUserId").is(userId).and("userId").nin(likeUserId);
        //使用统计函数查询
        TypedAggregation<RecommendUser> aggregation = TypedAggregation.newAggregation(RecommendUser.class,
                Aggregation.match(criteria),
                Aggregation.sample(count));
        AggregationResults<RecommendUser> list = mongoTemplate.aggregate(aggregation, RecommendUser.class);

        return list.getMappedResults();
    }

    //分页查询分数
    @Override
    public List<RecommendUser> findByIds(Integer page, Integer pagesize, List<Long> userIds, Long userId) {
        Criteria criteria = Criteria.where("toUserId").is(userId)
                .and("userId").in(userIds);
        Query query = Query.query(criteria).skip((page - 1) * pagesize).limit(pagesize)
                .with(Sort.by(Sort.Order.desc("score")));
        List<RecommendUser> recommendUsers = mongoTemplate.find(query, RecommendUser.class);
        return recommendUsers;
    }

    /**
     * 查询推荐数据
     *
     * @param userId
     * @param
     * @return
     */
    @Override
    public RecommendUser queryByUserId(Long userId, Long toUserId) {
        Criteria criteria = Criteria.where("toUserId").is(toUserId).and("userId").is(userId);
        Query query = Query.query(criteria);
        RecommendUser user = mongoTemplate.findOne(query, RecommendUser.class);
        if (user == null) {
            user = new RecommendUser();
            user.setUserId(userId);
            user.setToUserId(toUserId);
            //构建缘分值
            user.setScore(95d);
        }
        return user;
    }
}
