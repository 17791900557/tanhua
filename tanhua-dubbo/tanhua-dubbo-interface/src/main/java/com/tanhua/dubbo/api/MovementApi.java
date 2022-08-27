package com.tanhua.dubbo.api;

import com.tabhua.model.domain.UserInfo;
import com.tabhua.model.mongo.Movement;
import com.tabhua.model.vo.PageResult;
import org.bson.types.ObjectId;

import java.util.List;

public interface MovementApi {
    //发布动态
    void publishMovements(Movement movement);

    //查询我的动态
    PageResult getAllMovements(int page, int pageSize, Long userId);

    //查询好友动态
    List<Movement> friendMovements(int page, int pagesize, Long userId);

    //随机获取数据
    List<Movement> randomMovements(Integer pagesize);

    //根据pid查询
    List<Movement> findByPids(List<Long> pids);

    //查询单条
    Movement findMovementById(String id);

    //查询动态根据条件
    PageResult getMovements(Integer page, Integer pagesize, Long userId, Integer state);
}

