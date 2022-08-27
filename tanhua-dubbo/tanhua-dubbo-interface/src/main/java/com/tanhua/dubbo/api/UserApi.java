package com.tanhua.dubbo.api;

import com.tabhua.model.domain.User;

public interface UserApi {
    //根据手机号码查询用户
    User findByPhone(String phone);

    //保存用户
    Long save(User user);

    //修改手机号
    void updatePhone(String newPhone, Long userID);

    //更新用户信息
    void update(User user);

    //根据id查询
    User findById(Long userId);


    User findByHuanxin(String huanxinId);

    //根据环信id查询

}
