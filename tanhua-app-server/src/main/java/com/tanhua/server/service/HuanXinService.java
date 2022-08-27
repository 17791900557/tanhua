package com.tanhua.server.service;

import com.tabhua.model.domain.User;
import com.tabhua.model.vo.HuanXinUserVo;
import com.tanhua.dubbo.api.UserApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class HuanXinService {
    @DubboReference(check = false)
    private UserApi userApi;

    /**
     * 获取环信账号密码
     *
     * @return
     */
    public HuanXinUserVo findHuanXinUser() {
        Long userId = UserHolder.getId();
        User user = userApi.findById(userId);
        if (user == null){
            return null;
        }
        return new HuanXinUserVo(user.getHxUser(), user.getHxPassword());
    }
}
