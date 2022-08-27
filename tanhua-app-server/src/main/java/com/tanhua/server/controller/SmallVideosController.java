package com.tanhua.server.controller;

import com.tabhua.model.vo.PageResult;
import com.tanhua.server.service.SmallVideosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/smallVideos")
public class SmallVideosController {

    @Autowired
    private SmallVideosService videosService;

    /**
     * 发布视频
     *  接口路径：POST
     *  请求参数：
     *      videoThumbnail：封面图
     *      videoFile：视频文件
     */
    @PostMapping
    public ResponseEntity saveVideos(MultipartFile videoThumbnail, MultipartFile videoFile) throws IOException {
        videosService.saveVideos(videoThumbnail,videoFile);
        return ResponseEntity.ok(null);
    }
    /**
     * 视频列表
     */
    @GetMapping

    public ResponseEntity queryVideoList(@RequestParam(defaultValue = "1")  Integer page,
                                         @RequestParam(defaultValue = "10") Integer pagesize) {
        PageResult result = videosService.queryVideoList(page, pagesize);
        return ResponseEntity.ok(result);
    }

    /**
     * 关注视频作者
     */
    @PostMapping("/{id}/userFocus")
    public ResponseEntity userFocus(@PathVariable("id") Long followUserId) {
        videosService.userFocus(followUserId);
        return ResponseEntity.ok(null);
    }

    /**
     * 取消关注视频作者
     */
    @PostMapping("/{id}/userUnFocus")
    public ResponseEntity userUnFocus(@PathVariable("id") Long followUserId) {
        videosService.userUnFocus(followUserId);
        return ResponseEntity.ok(null);
    }

    /**
     * 视频点赞
     * @param videoId
     * @return
     */
    @PostMapping("/{id}/like")
    public ResponseEntity videosLike(@PathVariable(value = "id")String videoId){
        Integer likeCount = videosService.like(videoId);
        return ResponseEntity.ok(likeCount);
    }
    /**
     * 视频取消点赞
     * @param videoId
     * @return
     */
    @PostMapping("/{id}/dislike")
    public ResponseEntity dislike(@PathVariable(value = "id")String videoId){
        Integer likeCount = videosService.dislike(videoId);
        return ResponseEntity.ok(likeCount);
    }

    /**
     * 视频评论
     * @param videoId
     * @param map
     * @return
     */
    @PostMapping("/{id}/comments")
    public ResponseEntity comments(@PathVariable(value = "id") String videoId,
                                   @RequestBody Map map){

        String comment = (String) map.get("comment");
        Integer count = videosService.saveComments(videoId,comment);
        return ResponseEntity.ok(null);
    }

    /**
     * 视频评论列表
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity videoComments(@PathVariable(value = "id")String videoId,
                                        @RequestParam(defaultValue = "1")Integer page,
                                        @RequestParam(defaultValue = "5")Integer pagesize){
        PageResult pageResult = videosService.videoComments(videoId,page,pagesize);

        return ResponseEntity.ok(pageResult);
    }

    /**
     * 评论点赞
     * @param commentId
     * @return
     */
    @PostMapping("/comments/{id}/like")
    public ResponseEntity like (@PathVariable("id") String commentId){
        Integer count = videosService.pingLunLike(commentId);
        return ResponseEntity.ok(count);
    }

    /**
     * 评论取消点赞
     * @param commentId
     * @return
     */

   @PostMapping("/comments/{id}/dislike")
   public ResponseEntity pingLunDisLike (@PathVariable("id") String commentId){
       Integer count = videosService.pingLunDisLike(commentId);
       return ResponseEntity.ok(count);
   }

}