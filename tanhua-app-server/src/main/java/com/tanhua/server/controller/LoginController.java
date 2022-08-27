package com.tanhua.server.controller;


import com.tanhua.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class    LoginController {

    @Autowired
    private UserService userService;

    /**
     * 用户登陆验证码
     *
     * @param map
     */
    @PostMapping("/login")
    public ResponseEntity login(@RequestBody Map map) {
        String phone = (String) map.get("phone");
        log.info(phone);
        userService.sendSms(phone);
        return ResponseEntity.ok("发送成功");
    }

    /**
     * 验证登陆
     *
     * @param map
     * @return
     */
    @PostMapping("/loginVerification")
    public ResponseEntity loginVerification(@RequestBody Map map) {

        String phone = (String) map.get("phone");
        String verificationCode = (String) map.get("verificationCode");
        Map retMap = userService.loginVerification(phone, verificationCode);
        return ResponseEntity.ok(retMap);

    }

}