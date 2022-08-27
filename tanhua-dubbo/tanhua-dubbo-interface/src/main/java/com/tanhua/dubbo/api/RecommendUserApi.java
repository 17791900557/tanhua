package com.tanhua.dubbo.api;

import com.tabhua.model.mongo.RecommendUser;
import com.tabhua.model.vo.PageResult;

import java.util.List;

public interface RecommendUserApi {
    //今日最佳
    RecommendUser queryWithMaxScore(Long userId);

    //分页查询推荐列表
   
    //根据两个id查询推荐数据
    RecommendUser queryByUserId(Long userId, Long toUserId);

    //分页查询
    PageResult queryRecommendUserList(Integer page, Integer pagesize, Long userId);

    //查询推荐用户
    List<RecommendUser> queryCardsList(Long id, int i);

    //分页查询
    List<RecommendUser> findByIds(Integer page, Integer pagesize,List<Long> userIds, Long userId);
}
