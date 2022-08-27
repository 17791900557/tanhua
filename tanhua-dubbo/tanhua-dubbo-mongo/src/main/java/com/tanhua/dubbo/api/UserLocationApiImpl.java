package com.tanhua.dubbo.api;

import cn.hutool.core.collection.CollUtil;
import com.tabhua.model.mongo.UserLocation;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@DubboService
public class UserLocationApiImpl implements UserLocationApi {
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 更新或保存地理位置
     *
     * @param id
     * @param longitude
     * @param latitude
     * @param address
     * @return
     */
    @Override
    public Boolean updateLocation(Long id, Double longitude, Double latitude, String address) {
        try {
            Query query = Query.query(Criteria.where("userId").is(id));
            UserLocation location = mongoTemplate.findOne(query, UserLocation.class);
            if (location == null) {
                location = new UserLocation();
                location.setUserId(id);
                location.setUpdated(System.currentTimeMillis());
                location.setCreated(System.currentTimeMillis());
                location.setAddress(address);
                location.setLastUpdated(System.currentTimeMillis());
                location.setLocation(new GeoJsonPoint(longitude, latitude));
                mongoTemplate.save(location);
            } else {
                Update update = Update.update("location", new GeoJsonPoint(longitude, latitude))
                        .set("updated", System.currentTimeMillis())
                        .set("lastUpdated", location.getUpdated());
                mongoTemplate.updateFirst(query, update, UserLocation.class);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }

    }

    /**
     * 查询附近所有人的id
     *
     * @param id
     * @param valueOf
     * @return
     */
    @Override
    public List<Long> queryNearUser(Long id, Double valueOf) {
        //根据id查询位置信息
        Query query = Query.query(Criteria.where("userId").is(id));
        UserLocation user = mongoTemplate.findOne(query, UserLocation.class);
        if (user == null) {
            return null;
        }
        //获取位置信息  原点
        GeoJsonPoint point = user.getLocation();
        //绘制半径
        Distance distance = new Distance(valueOf / 1000, Metrics.KILOMETERS);
        Circle circle = new Circle(point, distance);
        Query location = Query.query(Criteria.where("location").withinSphere(circle));
        List<UserLocation> userLocations = mongoTemplate.find(location, UserLocation.class);
        List<Long> userIds = CollUtil.getFieldValues(userLocations, "userId", Long.class);
        return userIds;
    }
}
