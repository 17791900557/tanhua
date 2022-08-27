package com.tanhua.dubbo.api;

import com.tabhua.model.mongo.Friend;

import java.util.List;

public interface FriendApi {
    //添加好友关系
    void save(Long userId, Long friendId);

    //查询好友列表
    List<Friend> findByUserId(Long id, Integer page, Integer pagesize);

    //删除好友关系
    void delete(Long id, Long friendId);
}
