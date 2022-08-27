package com.tanhua.server.controller;

import com.tabhua.model.vo.PageResult;
import com.tanhua.server.service.CommentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/comments")
@RestController
public class CommentsController {

    @Autowired
    private CommentsService commentsService;

    /**
     * 发表评论
     * @param map
     * @return
     */
    @PostMapping
    public ResponseEntity saveComments(@RequestBody Map map){
        String movementId = (String) map.get("movementId");
        String comment = (String) map.get("comment");
        commentsService.saveComments(movementId,comment);
        return ResponseEntity.ok(null);
    }

    /**
     * 评论列表
     * @param page
     * @param pagesize
     * @param movementId
     * @return
     */
    @GetMapping
    public ResponseEntity getComments(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int pagesize,
                                      String movementId){
        PageResult pr = commentsService.getComments(page,pagesize,movementId);
        return ResponseEntity.ok(pr);

    }

    /**
     * 评论点赞
     * @param commentId
     * @return
     */
    @GetMapping("/{id}/like")
    public ResponseEntity like (@PathVariable("id") String commentId){
       Integer count = commentsService.pingLunLike(commentId);
       return ResponseEntity.ok(count);
    }
    /**
     * 评论取消点赞
     * @param commentId
     * @return
     */
    @GetMapping("/{id}/dislike")
    public ResponseEntity dislike (@PathVariable("id") String commentId){
        Integer count = commentsService.pingLunDisLike(commentId);
        return ResponseEntity.ok(count);
    }

}
