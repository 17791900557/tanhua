package com.tanhua.server.controller;

import com.tabhua.model.domain.UserInfo;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserInfoService userInfoService;

    /**
     * 保存用户信息
     * UserInfo
     * 请求头中携带token用户的信息
     */
    @PostMapping("/loginReginfo")
    public ResponseEntity loginReginfo(@RequestBody UserInfo userInfo,
                                       @RequestHeader("Authorization") String token) {




        Long id = UserHolder.getId();
        userInfo.setId(id);

        userInfoService.save(userInfo);
        return ResponseEntity.ok(null);

    }

    /**
     * 上传用户头像
     *
     * @param headPhoto
     * @param token
     * @return
     */
    @PostMapping("/loginReginfo/head")
    public ResponseEntity head(MultipartFile headPhoto,
                               @RequestHeader("Authorization") String token) throws IOException {


        Long id = UserHolder.getId();

        //上传头像
        userInfoService.updateHead(headPhoto, id);
        return ResponseEntity.ok(null);
    }



}
