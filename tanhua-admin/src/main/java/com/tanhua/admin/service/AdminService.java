package com.tanhua.admin.service;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.MD5Utils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tabhua.model.domain.Admin;
import com.tabhua.model.vo.AdminVo;
import com.tanhua.admin.exception.BusinessException;
import com.tanhua.admin.interceptor.AdminHolder;
import com.tanhua.admin.mapper.AdminMapper;

import com.tanhua.commoms.utils.Constants;
import com.tanhua.commoms.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 登陆验证
     *
     * @param map
     * @return
     */

    public Map login(Map map) {
        String username = (String) map.get("username");
        String password = (String) map.get("password");
        String verificationCode = (String) map.get("verificationCode");
        String uuid = (String) map.get("uuid");

        String code = redisTemplate.opsForValue().get(Constants.CAP_CODE + uuid);
        if (StringUtils.isEmpty(verificationCode) || !verificationCode.equals(code)) {
            throw new BusinessException("验证码错误");
        }
        redisTemplate.delete(Constants.CAP_CODE + uuid);
        LambdaQueryWrapper<Admin> qw = new LambdaQueryWrapper();
        qw.eq(Admin::getUsername, username);
        Admin admin = adminMapper.selectOne(qw);
        password = SecureUtil.md5(password);
        if (admin == null || !password.equals(admin.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        //生成token
        Map tokenMap = new HashMap();
        tokenMap.put("id", admin.getId());
        tokenMap.put("username", admin.getUsername());
        String token = JwtUtils.getToken(tokenMap);
        //构造返回值
        Map retMap = new HashMap();
        retMap.put("token", token);
        return retMap;

    }

    /**
     * 获取管理员信息
     * @return
     */
    public AdminVo profile() {
        Long userId = AdminHolder.getUserId();
        Admin admin = adminMapper.selectById(userId);
        AdminVo vo = AdminVo.init(admin);
        return vo;
    }
}
