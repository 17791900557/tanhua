package com.tanhua.dubbo.api;

import com.tabhua.model.mongo.UserLike;

import java.util.List;
import java.util.Set;

public interface UserLikeApi {
    //保存或更新
    Boolean saveOrUpdate(Long likeUserId, Long userId, boolean isLike);

    //互相喜欢



    //粉丝
    List<UserLike> fanCount(Long id, boolean b);


    List<UserLike> eachLoveCount(Long id, List<Long> ids, boolean b);


    //翻页列表取消喜欢
    Boolean  offLike(Long likeUserId, Long userId);
}
