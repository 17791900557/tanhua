package com.tanhua.server.controller;


import com.tabhua.model.domain.UserInfo;
import com.tabhua.model.vo.CountsVo;
import com.tabhua.model.vo.PageResult;
import com.tabhua.model.vo.UserInfoVo;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UsersController {

    @Autowired
    private UserInfoService userInfoService;

    /**
     * 查询用户资料
     * 1.请求头taken
     * 2.请求参数userId
     *
     * @return
     */
    @GetMapping
    public ResponseEntity users(@RequestHeader("Authorization") String token, Long userID) {

        Long id = UserHolder.getId();
        if (userID == null) {
            userID = id;
        }
        UserInfoVo userInfo = userInfoService.findById(userID);

        return ResponseEntity.ok(userInfo);
    }

    /**
     * 更新用户信息
     *
     * @param userInfo
     * @param token
     * @return
     */
    @PutMapping
    public ResponseEntity update(@RequestBody UserInfo userInfo,
                                 @RequestHeader("Authorization") String token) {

//        boolean verifyToken = JwtUtils.verifyToken(token);
//        if (!verifyToken) {
//            return ResponseEntity.status(401).body(null);
//        }
        Long id = UserHolder.getId();
        userInfo.setId(id);

        userInfoService.updateById(userInfo);

        return ResponseEntity.ok(null);

    }

    /**
     * 修改头像
     *
     * @param headPhoto
     * @return
     * @throws IOException
     */
    @PostMapping("/header")
    public ResponseEntity updateImage(MultipartFile headPhoto) throws IOException {
        Long id = UserHolder.getId();

        userInfoService.updateImage(headPhoto, id);
        return ResponseEntity.ok(null);
    }

    /**
     * 修改手机号码发送验证码
     *
     * @return
     */
    @PostMapping("/phone/sendVerificationCode")
    public ResponseEntity sendVerificationCode() {
        String phone = UserHolder.getPhone();
        userInfoService.sendVerificationCode(phone);
        return ResponseEntity.ok(null);
    }

    /**
     * 修改手机验证码校验
     *
     * @param map
     * @return
     */
    @PostMapping("/phone/checkVerificationCode")
    public ResponseEntity<Map<String, Object>> checkVerificationCode(@RequestBody Map map) {
        String code = (String) map.get("verificationCode");
        Boolean bool = userInfoService.checkVerificationCode(code);
        Map<String, Object> result = new HashMap<>();
        result.put("verification", bool);
        return ResponseEntity.ok(result);
    }

    /**
     * 修改手机号
     *
     * @param map
     * @return
     */
    @PostMapping("/phone")
    public ResponseEntity updatePhone(@RequestBody Map map) {
        String newPhone = (String) map.get("phone");
        userInfoService.updatePhone(newPhone);
        return ResponseEntity.ok(null);
    }


    /**
     * 查询喜欢或者不喜欢
     *
     * @param friendId
     * @return
     */
    @GetMapping("/{uid}/alreadyLove")
    public ResponseEntity alreadyLove(@PathVariable(value = "uid") Long friendId) {
        Boolean b = userInfoService.alreadyLove(friendId);
        if (b != null) {
            return ResponseEntity.ok(b);
        }
        return ResponseEntity.ok(false);
    }

    /**
     * 互相喜欢，喜欢，粉丝 - 统计
     *
     * @return
     */

    @GetMapping("/counts")
    public ResponseEntity counts() {
        CountsVo counts = userInfoService.counts();
        return ResponseEntity.ok(counts);
    }

    /**
     * 分页列表
     * @param type
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("/friends/{type}")
    public ResponseEntity friends(@PathVariable Integer type,
                                  @RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer pagesize) {

        if (type == 1) {//互相关注
            PageResult pageResult = userInfoService.eachLoveCount(page, pagesize);
            return ResponseEntity.ok(pageResult);
        } else if (type == 2) {//我喜欢
            PageResult pageResult = userInfoService.loveCount(page, pagesize);
            return ResponseEntity.ok(pageResult);
        } else if (type == 3) {//粉丝
            PageResult pageResult = userInfoService.fanCount(page, pagesize);
            return ResponseEntity.ok(pageResult);
        } else if (type == 4){//谁看过我
            PageResult pageResult = userInfoService.visitors(page, pagesize);
            return ResponseEntity.ok(pageResult);
        }
        return ResponseEntity.ok(null);
    }

    /**
     * 分页取消喜欢
     * @param likeUserId
     * @return
     */

    @DeleteMapping("/like/{uid}")
    public ResponseEntity offLike(@PathVariable("uid") Long likeUserId){
        userInfoService.offLike(likeUserId);
        return ResponseEntity.ok(null);
    }


    /**
     * 粉丝喜欢
     * @param likeUserId
     * @return
     */
    @PostMapping("/fans/{uid}")
    public ResponseEntity fansLike(@PathVariable (value = "uid") Long likeUserId){
        userInfoService.fansLike(likeUserId);
        return ResponseEntity.ok(null);
    }

}
