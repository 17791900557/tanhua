package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.tabhua.model.domain.User;
import com.tabhua.model.vo.ErrorResult;
import com.tanhua.autoconfig.template.HuanXinTemplate;
import com.tanhua.commoms.utils.Constants;
import com.tanhua.commoms.utils.JwtUtils;
import com.tanhua.dubbo.api.UserApi;
import com.tanhua.autoconfig.template.SmsTemplate;
import com.tanhua.server.exception.BusinessException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private SmsTemplate smsTemplate;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @DubboReference(check = false)
    private UserApi userApi;
    @Autowired
    private HuanXinTemplate huanXinTemplate;
    @Autowired
    private UserFreezeService userFreezeService;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private MqMessageService mqMessageService;

    /**
     * 登陆短信验证
     *
     * @param phone
     */
    public void sendSms(String phone) {
        //生成短信验证码
        // String code = RandomStringUtils.randomNumeric(6);
        User user = userApi.findByPhone(phone);
        if (user != null) {
            userFreezeService.checkUserStatus("1", user.getId());
        }
        String code = "123456";
        //调用方法发送短信
        // smsTemplate.sendSms(phone, code);
        //将验证码存入redis
        redisTemplate.opsForValue().set("CHECK_CODE_" + phone, code, Duration.ofMinutes(5));

    }

    ;

    /**
     * 验证登录
     *
     * @param phone
     * @param verificationCode
     * @return
     */
    public Map loginVerification(String phone, String verificationCode) {
        String redisCode = redisTemplate.opsForValue().get("CHECK_CODE_" + phone);
        if (StringUtils.isEmpty(redisCode) || !redisCode.equals(verificationCode)) {
            throw new BusinessException(ErrorResult.loginError());
        }
        redisTemplate.delete("CHECK_CODE_" + phone);
        User user = userApi.findByPhone(phone);
        boolean isNew = false;
        String type = "0101";//登陆
        if (user == null) {
            type = "0102";//注册
            user = new User();
            user.setPhone(phone);
            user.setPassword(DigestUtils.md5Hex("12345"));
            Long userId = userApi.save(user);
            user.setId(userId);
            isNew = true;
            //注册环信用户
            String hxUser = "hx" + user.getId();
            Boolean create = huanXinTemplate.createUser(hxUser, Constants.INIT_PASSWORD);
            if (create) {
                user.setHxUser(hxUser);
                user.setHxPassword(Constants.INIT_PASSWORD);
                userApi.update(user);
            }
        }
         //发送消息MQ统计数据
//        try {
//            Map map = new HashMap();
//            map.put("userId",user.getId().toString());
//            map.put("type",type);
//            map.put("logTime",new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
//            String message = JSON.toJSONString(map);
//            amqpTemplate.convertAndSend("tanhua.log.exchange", "log.user",message);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        mqMessageService.sendLogService(user.getId(),type,"user",null);
        //生成jwt token
        Map tokenMap = new HashMap();
        tokenMap.put("id", user.getId());
        tokenMap.put("phone", phone);
        String token = JwtUtils.getToken(tokenMap);

        //构造返回值
        Map retMap = new HashMap();
        retMap.put("token", token);
        retMap.put("isNew", isNew);
        return retMap;
    }
}
