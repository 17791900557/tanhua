package com.tanhua.dubbo.api;

import com.tabhua.model.mongo.Visitors;
import com.tabhua.model.vo.VisitorsVo;

import java.util.List;

public interface VisitorsApi {
    //保存访客记录
    void save(Visitors visitors);

    //访问列表
    List<Visitors> queryVisitorsList(Long date, Long id);

    //分页查询
    List<Visitors> visitors(Integer page, Integer pagesize, Long userId);
}
