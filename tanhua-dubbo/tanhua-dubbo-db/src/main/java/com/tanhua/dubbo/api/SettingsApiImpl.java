package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tabhua.model.domain.Settings;
import com.tanhua.dubbo.mappers.SettingsMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService
public class SettingsApiImpl implements SettingsApi{

    @Autowired
    private SettingsMapper settingsMapper;

    /**
     * 根据userid查设置
     * @param userId
     * @return
     */
    @Override
    public Settings getOne(Long userId) {
        LambdaQueryWrapper<Settings> qw = new LambdaQueryWrapper<>();
        qw.eq(Settings::getUserId,userId);
        return settingsMapper.selectOne(qw);
    }

    /**
     * 根据userid保存设置
     * @param settings
     */
    @Override
    public void save(Settings settings) {
        settingsMapper.insert(settings);
    }

    /**
     * 根据userid更新设置
     * @param settings
     */
    @Override
    public void update(Settings settings) {
        settingsMapper.updateById(settings);
    }
}
