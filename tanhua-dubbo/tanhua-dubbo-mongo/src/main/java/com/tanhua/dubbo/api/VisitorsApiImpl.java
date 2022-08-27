package com.tanhua.dubbo.api;

import com.tabhua.model.mongo.Visitors;
import com.tabhua.model.vo.VisitorsVo;
import org.apache.dubbo.config.annotation.DubboService;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@DubboService
public class VisitorsApiImpl implements VisitorsApi{
    @Autowired
    private MongoTemplate mongoTemplate;

    //保存访客数据
    @Override
    public void save(Visitors visitors) {
        Query query = Query.query(Criteria.where("userId").is(visitors.getUserId())
                .and("visitorUserId").is(visitors.getVisitorUserId())
                .and("visitDate").is(visitors.getVisitDate()));
        if (!mongoTemplate.exists(query,Visitors.class)){
            mongoTemplate.save(visitors);
        }
    }

    /**
     * 访问列表
     * @param date
     * @param id
     * @return
     */
    @Override
    public List<Visitors> queryVisitorsList(Long date, Long id) {

        Criteria criteria = Criteria.where("userId").is(id);
        if (date != null){
            criteria.and("date").gt(date);
        }
        Query query = Query.query(criteria).limit(5).with(Sort.by(Sort.Order.desc("date")));
        return mongoTemplate.find(query,Visitors.class);
    }

    /**
     * 分页查询访客列表
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @Override
    public List<Visitors> visitors(Integer page, Integer pagesize, Long userId) {
        Criteria criteria = Criteria.where("userId").is(userId);
        Query query = Query.query(criteria).with(Sort.by(Sort.Order.desc("date")))
                .skip((page-1)*pagesize)
                .limit(pagesize);

        return mongoTemplate.find(query,Visitors.class);
    }
}
