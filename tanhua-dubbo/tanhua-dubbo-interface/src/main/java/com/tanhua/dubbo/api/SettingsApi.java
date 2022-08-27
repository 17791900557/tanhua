package com.tanhua.dubbo.api;

import com.tabhua.model.domain.Settings;

public interface SettingsApi {
    //根据Userid查开关
    Settings getOne(Long userId);

    //根据Userid保存设置
    void save(Settings settings);

    //根据Userid更新设置
    void update(Settings settings);
}
