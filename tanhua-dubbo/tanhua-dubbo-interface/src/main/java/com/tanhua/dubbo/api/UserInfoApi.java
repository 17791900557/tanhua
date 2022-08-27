package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tabhua.model.domain.UserInfo;

import java.util.List;
import java.util.Map;

public interface UserInfoApi {
    //保存用户信息
    public void save(UserInfo userInfo);

    //更新用户信息
    public void update(UserInfo userInfo);

    //分页查询黑名单信息
    UserInfo findById(Long userId);

    //批量查询用户信息
    Map<Long, UserInfo> findByIds(List<Long> ids, UserInfo userInfo);


    //分页查询用户信息
    IPage findAll(Integer page, Integer pagesize);
}
