package com.tanhua.server.controller;

import com.tabhua.model.mongo.Movement;
import com.tabhua.model.vo.MovementsVo;
import com.tabhua.model.vo.PageResult;
import com.tabhua.model.vo.VisitorsVo;
import com.tanhua.server.service.CommentsService;
import com.tanhua.server.service.MovementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/movements")
public class MovementController {

    @Autowired
    private MovementService movementService;
    @Autowired
    private CommentsService commentsService;

    /**
     * 发布动态
     *
     * @param movement
     * @param file
     * @return
     */

    @PostMapping
    public ResponseEntity movements(MultipartFile file[], Movement movement) throws IOException {
        movementService.publishMovements(movement, file);

        return ResponseEntity.ok(null);

    }

    /**
     * 我的动态
     *
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @GetMapping("/all")
    public ResponseEntity all(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "10") int pagesize,
                              Long userId) {
        PageResult pageResult = movementService.all(page, pagesize, userId);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 好友动态
     *
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping
    public ResponseEntity friendMovements(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int pagesize) {
        PageResult pageResult = movementService.friendMovements(page, pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 推荐动态
     *
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("/recommend")
    public ResponseEntity recommend(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer pagesize) {
        PageResult pr = movementService.findRecommendMovements(page, pagesize);
        return ResponseEntity.ok(pr);
    }

    /**
     * 查询单条动态
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity findMovementById(@PathVariable String id) {
        MovementsVo vo = movementService.findMovementById(id);
        return ResponseEntity.ok(vo);
    }

    /**
     * 动态点赞
     * @param movementId
     * @return
     */
    @GetMapping("/{id}/like")
    public ResponseEntity like(@PathVariable(value = "id") String movementId) {
        Integer likeCount = commentsService.like(movementId);
        return ResponseEntity.ok(likeCount);
    }
    /**
     * 取消点赞
     */
    @GetMapping("/{id}/dislike")
    public ResponseEntity dislike(@PathVariable("id") String movementId) {
        Integer likeCount = commentsService.dislikeComment(movementId);
        return ResponseEntity.ok(likeCount);
    }

    /**
     * 保存喜欢
     */
    @GetMapping("/{id}/love")
    public ResponseEntity love(@PathVariable(value = "id") String movementId) {
        Integer likeCount = commentsService.love(movementId);
        return ResponseEntity.ok(likeCount);
    }
    /**
     * 取消喜欢
     */
    @GetMapping("/{id}/unlove")
    public ResponseEntity unlove(@PathVariable("id") String movementId) {
        Integer likeCount = commentsService.disloveComment(movementId);
        return ResponseEntity.ok(likeCount);
    }

    /**
     * 谁看过我
     */
    @GetMapping("/visitors")
    public ResponseEntity queryVisitorsList(){
        List<VisitorsVo> list = movementService.queryVisitorsList();
        return ResponseEntity.ok(list);
    }
}
