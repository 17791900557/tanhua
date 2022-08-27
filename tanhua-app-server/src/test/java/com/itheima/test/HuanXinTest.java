package com.itheima.test;

import com.easemob.im.server.EMProperties;
import com.easemob.im.server.EMService;
import com.tabhua.model.domain.User;
import com.tanhua.autoconfig.template.HuanXinTemplate;
import com.tanhua.commoms.utils.Constants;
import com.tanhua.dubbo.api.UserApi;
import com.tanhua.server.AppserverApplication;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppserverApplication.class)
public class HuanXinTest {

    @Autowired
    private HuanXinTemplate huanXinTemplate;
    @DubboReference
    private UserApi userApi;


    @Test
    public void testd(){
        huanXinTemplate.createUser("hx122","123456");
    }

    @Test
    public void register() {
        for (int i = 1; i <122; i++) {
            User user = userApi.findById(Long.valueOf(i));
            if(user != null) {
                Boolean create = huanXinTemplate.createUser("hx" + user.getId(), Constants .INIT_PASSWORD);
                if (create){
                    user.setHxUser("hx" + user.getId());
                    user.setHxPassword(Constants.INIT_PASSWORD);
                    userApi.update(user);
                }
            }
        }
    }
}
