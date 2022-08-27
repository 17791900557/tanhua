package com.tanhua.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tabhua.model.domain.Question;
import com.tabhua.model.domain.Settings;
import com.tabhua.model.domain.UserInfo;
import com.tabhua.model.vo.PageResult;
import com.tabhua.model.vo.SettingsVo;
import com.tanhua.dubbo.api.BlackListApi;
import com.tanhua.dubbo.api.QuestionApi;
import com.tanhua.dubbo.api.SettingsApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SettingsService {

    @DubboReference(check = false)
    private SettingsApi settingsApi;
    @DubboReference(check = false)
    private QuestionApi questionApi;
    @DubboReference(check = false)
    private BlackListApi blackListApi;

    /**
     * 查询通用设置
     *
     * @return
     */
    public SettingsVo settings() {
        SettingsVo settingsVo = new SettingsVo();
        Long id = UserHolder.getId();
        settingsVo.setId(id);
        String phone = UserHolder.getPhone();
        settingsVo.setPhone(phone);
        Question question = questionApi.getQuestion(id);
        String txt = question == null ? "你喜欢Java吗" : question.getTxt();

        settingsVo.setStrangerQuestion(txt);

        Settings settings = settingsApi.getOne(id);
        if (settings != null){
            settingsVo.setGonggaoNotification(settings.getGonggaoNotification());
            settingsVo.setLikeNotification(settings.getLikeNotification());
            settingsVo.setPinglunNotification(settings.getPinglunNotification());
        }

        return settingsVo;
    }

    /**
     * 陌生人问题设置
     * @param txt
     */
    public void save(String txt) {
        Long userId = UserHolder.getId();
        Question question = questionApi.getQuestion(userId);
        if (question == null){
             question = new Question();
             question.setTxt(txt);
             question.setUserId(userId);
             questionApi.save(question);
        }else {
            question.setTxt(txt);
            questionApi.update(question);
        }
    }

    //通知设置
    public void savesettings(Map map) {
        Long userId = UserHolder.getId();
        Settings settings = settingsApi.getOne(userId);
        if (settings == null){
            settings = new Settings();
            settings.setGonggaoNotification((Boolean) map.get("gonggaoNotification"));
            settings.setPinglunNotification((Boolean) map.get("pinglunNotification"));
            settings.setLikeNotification((Boolean) map.get("likeNotification"));
            settings.setUserId(userId);
            settingsApi.save(settings);
        }else {
            settings.setGonggaoNotification((Boolean) map.get("gonggaoNotification"));
            settings.setPinglunNotification((Boolean) map.get("pinglunNotification"));
            settings.setLikeNotification((Boolean) map.get("likeNotification"));
            settingsApi.update(settings);
        }

    }

    /**
     * 分页查询黑名单列表
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult blacklist(int page, int pageSize) {
        Long userId = UserHolder.getId();
        IPage<UserInfo> iPage = blackListApi.findByuserId(userId,page,pageSize);
        PageResult pageResult = new PageResult(page,pageSize, (int) iPage.getTotal(),iPage.getRecords());
        return pageResult;

    }

    /**
     * 解除黑名单
     * @param blackUserId
     */
    public void deleteBlacklist(Long blackUserId) {
        Long userId = UserHolder.getId();
        blackListApi.deleteBlacklist(userId,blackUserId);
    }
}
