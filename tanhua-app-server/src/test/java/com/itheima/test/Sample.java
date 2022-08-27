package com.itheima.test;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tabhua.model.domain.Announcement;
import com.tabhua.model.domain.UserInfo;
import com.tabhua.model.mongo.RecommendUser;
import com.tabhua.model.vo.AnnouncementVo;
import com.tanhua.dubbo.api.*;
import com.tanhua.server.AppserverApplication;
import com.tanhua.autoconfig.template.SmsTemplate;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppserverApplication.class)
public class Sample {
    @Autowired
    private SmsTemplate smsTemplate;
    @DubboReference
    private UserApi userApi;
    @DubboReference
    private BlackListApi blackListApi;
    @DubboReference
    private RecommendUserApi recommendUserApi;
    @DubboReference
    private UserInfoApi userInfoApi;
    @DubboReference
    private AnnouncementApi announcementApi;

    @Test
    public void testSend() {
        smsTemplate.sendSms("17791900557", "1234");
    }

    @Test
    public void testGetUser() {
        IPage<UserInfo> byuserId = blackListApi.findByuserId(106l, 1, 10);

        for (UserInfo record : byuserId.getRecords()) {
            System.out.println(record);
        }
    }
    @Test
    public void announcements() {
        IPage<Announcement> byuserId = announcementApi.announcements(1,1);

        for (Announcement record : byuserId.getRecords()) {
            System.out.println(record);
        }
    }

    @Test
    public void eecommend() {
        RecommendUser recommendUser = recommendUserApi.queryWithMaxScore(106L);

        System.out.println(recommendUser);
    }


    @Test
    public void eecommend1() {
        List list = new ArrayList<>();
        list.add(1l);
        list.add(2l);
        list.add(3l);
        UserInfo userInfo = new UserInfo();
        userInfo.setAge(22);
        Map byIds = userInfoApi.findByIds(list, userInfo);
        byIds.forEach((k,v)-> System.out.println(k+"--"+v));
    }
}

