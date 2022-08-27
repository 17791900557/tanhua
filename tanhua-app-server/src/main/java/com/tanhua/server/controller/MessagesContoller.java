package com.tanhua.server.controller;

import com.tabhua.model.vo.PageResult;
import com.tabhua.model.vo.UserInfoVo;
import com.tanhua.server.service.MessagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/messages")
public class MessagesContoller {

    @Autowired
    private MessagesService messagesService;

    /**
     * 根据环信id查询用户信息
     *
     * @param huanxinId
     * @return
     */
    @GetMapping("/userinfo")
    public ResponseEntity userinfo(String huanxinId) {
        UserInfoVo vo = messagesService.findUserInfoByHuanxin(huanxinId);
        return ResponseEntity.ok(vo);
    }


    /**
     * 添加好友
     * @param map
     * @return
     */
    @PostMapping("/contacts")
    public ResponseEntity contacts(@RequestBody Map map) {
        Long friendId = Long.valueOf(map.get("userId").toString());
        messagesService.contacts(friendId);
        return ResponseEntity.ok(null);
    }
    /**
     * 查询联系人列表
     * @param
     * @return
     */
    @GetMapping("/contacts")
    public ResponseEntity contacts(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pagesize,
                                   String keyword) {
        PageResult pr = messagesService.findFriends(page,pagesize,keyword);
        return ResponseEntity.ok(pr);
    }

    /**
     * 查询公告
     */

    @GetMapping("/announcements")
    public ResponseEntity announcements(@RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer pagesize){
      PageResult pageResult = messagesService.announcements(page,pagesize);

      return ResponseEntity.ok(pageResult);
    }

    /**
     * 查询点赞列表
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("/likes")
    public ResponseEntity likes(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer pagesize){
        PageResult pageResult = messagesService.likes(page,pagesize);

        return ResponseEntity.ok(pageResult);
    }

    /**
     * 查询评论列表
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("/comments")
    public ResponseEntity comments(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer pagesize){
        PageResult pageResult = messagesService.comments(page,pagesize);

        return ResponseEntity.ok(pageResult);
    }

    /**
     * 查询喜欢列表
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("/loves")
    public ResponseEntity loves(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pagesize){
        PageResult pageResult = messagesService.loves(page,pagesize);

        return ResponseEntity.ok(pageResult);
    }


}
