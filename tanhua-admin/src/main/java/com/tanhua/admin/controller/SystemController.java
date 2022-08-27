package com.tanhua.admin.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.tabhua.model.vo.AdminVo;
import com.tanhua.admin.interceptor.AdminHolder;
import com.tanhua.admin.service.AdminService;
import com.tanhua.commoms.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/system/users")
public class SystemController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    /**
     * 获取验证码图片
     *
     * @param uuid
     */
    @GetMapping("/verification")
    public void verification(String uuid, HttpServletResponse response) throws IOException {
        //1生成验证码对象
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(299, 97);
        //获取验证码存入redis
        String code = captcha.getCode();
        redisTemplate.opsForValue().set(Constants.CAP_CODE + uuid, code);
        //将验证码图片相应给前端
        captcha.write(response.getOutputStream());
    }


    /**
     * 登陆验证
     *
     * @param map
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity login(@RequestBody Map map) {
        Map restMap = adminService.login(map);
        return ResponseEntity.ok(restMap);
    }


    /**
     * 获取管理员信息
     * @return
     */
    @PostMapping("/profile")
    public ResponseEntity profile() {
        AdminVo vo = adminService.profile();
        return ResponseEntity.ok(vo);
    }
}
