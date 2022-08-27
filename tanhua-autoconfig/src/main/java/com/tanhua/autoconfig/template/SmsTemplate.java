package com.tanhua.autoconfig.template;


import com.aliyun.dysmsapi20170525.models.SendSmsRequest;

import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.dysmsapi20170525.models.SendSmsResponseBody;
import com.aliyun.teaopenapi.models.*;
import com.tanhua.autoconfig.properties.SmsProperties;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class SmsTemplate {
    private SmsProperties properties;

    public SmsTemplate(SmsProperties properties){
        this.properties = properties;
    }
    public void sendSms(String phone, String code) {
        log.info(properties.getSingName());
        try {
            Config config = new Config()
                    // 您的 AccessKey ID
                    .setAccessKeyId(properties.getAccessKey())
                    // 您的 AccessKey Secret
                    .setAccessKeySecret(properties.getSecret());
            // 访问的域名
            config.endpoint = "dysmsapi.aliyuncs.com";
            com.aliyun.dysmsapi20170525.Client client = new com.aliyun.dysmsapi20170525.Client(config);

            SendSmsRequest sendSmsRequest = new SendSmsRequest()
                    .setSignName(properties.getSingName())
                    .setTemplateCode(properties.getTemplateCode())
                    .setPhoneNumbers(phone)
                    .setTemplateParam("{\"code\":\""+code+"\"}");

            // 复制代码运行请自行打印 API 的返回值
            SendSmsResponse respones = client.sendSms(sendSmsRequest);
            SendSmsResponseBody body = respones.getBody();
            log.info(body.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

