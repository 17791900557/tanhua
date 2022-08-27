package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tabhua.model.domain.UserInfo;

public interface BlackListApi {
    //分页查询黑名单数据
    IPage<UserInfo> findByuserId(Long userId, int page, int pageSize);

    //解除黑名单
    void deleteBlacklist(Long userId, Long blackUserId);
}
