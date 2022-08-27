package com.tanhua.admin.controller;

import com.tabhua.model.domain.UserInfo;
import com.tabhua.model.vo.MovementsVo;
import com.tabhua.model.vo.PageResult;
import com.tanhua.admin.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/manage")
public class ManagerController {


    @Autowired
    private ManagerService managerService;

    /**
     * 查询用户列表
     *
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("/users")
    public ResponseEntity users(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer pagesize) {
        PageResult result = managerService.findAllUsers(page, pagesize);
        return ResponseEntity.ok(result);
    }

    /**
     * 用户详情
     *
     * @return
     */
    @GetMapping("/users/{userID}")
    public ResponseEntity findUserById(@PathVariable(value = "userID") Long userId) {
        UserInfo userInfo = managerService.findUserById(userId);

        return ResponseEntity.ok(userInfo);
    }

    /**
     * 查询用户视频列表
     *
     * @param page
     * @param pagesize
     * @param uid
     * @return
     */

    @GetMapping("/videos")
    public ResponseEntity findVideosByUserId(@RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer pagesize,
                                             Long uid) {
        PageResult pageResult = managerService.findVideosByUserId(page, pagesize, uid);

        return ResponseEntity.ok(pageResult);

    }

    /**
     * 查询动态
     *
     * @param page
     * @param pagesize
     * @param uid
     * @param state
     * @return
     */

    @GetMapping("/messages")
    public ResponseEntity messages(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pagesize,
                                   Long uid, Integer state) {
        PageResult result = managerService.findAllMovements(page, pagesize, uid, state);
        return ResponseEntity.ok(result);
    }


    /**
     * 查询动态详情
     *
     * @param movementId
     * @return
     */
    @GetMapping("/messages/{id}")
    public ResponseEntity findMessagesById(@PathVariable(value = "id") String movementId) {
        MovementsVo map = managerService.findMessagesById(movementId);
        return ResponseEntity.ok(map);
    }

    /**
     * 查询评论
     * @param movementId
     * @param page
     * @param pagesize
     * @return
     */

    @GetMapping("/messages/comments")
    public ResponseEntity comments(@RequestParam(value = "messageID") String movementId,
                                   @RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pagesize) {
        PageResult pageResult = managerService.findMessages(page, pagesize, movementId);

        return ResponseEntity.ok(pageResult);
    }


    /**
     * 用户冻结
     * @param params
     * @return
     */
    @PostMapping("/users/freeze")
    public ResponseEntity freeze(@RequestBody Map params) {
        Map map =  managerService.userFreeze(params);
        return ResponseEntity.ok(map);
    }


    /**
     * 用户解冻
     * @param params
     * @return
     */
    @PostMapping("/users/unfreeze")
    public ResponseEntity unfreeze(@RequestBody  Map params) {
        Map map =  managerService.userUnfreeze(params);
        return ResponseEntity.ok(map);
    }

}
