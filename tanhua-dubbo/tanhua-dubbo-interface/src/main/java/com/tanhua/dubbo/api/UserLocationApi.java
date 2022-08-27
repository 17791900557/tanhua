package com.tanhua.dubbo.api;

import java.util.List;

public interface UserLocationApi {
    //更新或保存地理位置
    Boolean updateLocation(Long id, Double longitude, Double latitude, String address);

    //查询附近所有人的id
    List<Long> queryNearUser(Long id, Double valueOf);
}
