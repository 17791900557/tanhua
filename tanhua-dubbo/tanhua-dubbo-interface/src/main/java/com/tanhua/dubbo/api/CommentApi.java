package com.tanhua.dubbo.api;

import com.tabhua.model.enums.CommentType;
import com.tabhua.model.mongo.Comment;
import com.tabhua.model.vo.PageResult;

import java.util.List;

public interface CommentApi {


    //查询评论
     PageResult getComments(int page, int pagesize, String movementId, CommentType commentType);

    //保存返回总数
    Integer save(Comment comment1);

    //查询本条动态改用户是否已经点赞
    Boolean hasComment(String movementId, Long id, CommentType like);
    //删除返回总数
    Integer delete(Comment comment);

    //评论点赞
    Integer pingLunCount(String commentId);

    //评论取消点赞
    Integer pingLunDelete(String commentId);


    //点赞列表
    List<Comment> getComment(Integer page, Integer pagesize, Long id, CommentType like);

    //视频点赞保存返回总数
    Integer videoSave(Comment comment);

    //视频取消点赞
    Integer dislike(Comment comment);

}