package com.tanhua.dubbo.api;

import com.tabhua.model.enums.CommentType;
import com.tabhua.model.mongo.Comment;
import com.tabhua.model.mongo.Movement;
import com.tabhua.model.mongo.Video;
import com.tabhua.model.vo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@DubboService
public class CommentApiImpl implements CommentApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 查询评论
     *
     * @param page
     * @param pagesize
     * @param movementId
     * @return
     */
    public PageResult getComments(int page, int pagesize, String movementId, CommentType commentType) {
        Query query = Query.query(Criteria.where("publishId").is(new ObjectId(movementId)).and("commentType").is(commentType.getType()));
        int count = (int) mongoTemplate.count(query, Comment.class);
        query.skip((page - 1) * pagesize).limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        List<Comment> list = mongoTemplate.find(query, Comment.class);

        return new PageResult(page, pagesize, count, list);
    }

    /**
     * 查询总数
     *
     * @param comment
     */
    @Override
    public Integer save(Comment comment) {
        //查询动态
        Movement movement = mongoTemplate.findById(comment.getPublishId(), Movement.class);
        //向comment 插入被评论人id
        if (movement != null) {
            comment.setPublishUserId(movement.getUserId());
        }
        mongoTemplate.save(comment);
        //更新动态表中的点赞数平论数并返回
        Query query = Query.query(Criteria.where("id").is(comment.getPublishId()));
        Update update = new Update();
        if (comment.getCommentType() == CommentType.LIKE.getType()) {
            update.inc("likeCount", 1);
        } else if (comment.getCommentType() == CommentType.COMMENT.getType()) {
            update.inc("CommentCount", 1);
        } else {
            update.inc("loveCount", 1);
        }
        //设置更新参数，获取最新评论数并返回
        FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions();
        findAndModifyOptions.returnNew(true);
        Movement andModify = mongoTemplate.findAndModify(query, update, findAndModifyOptions, Movement.class);
        return andModify.statisCount(comment.getCommentType());
    }

    /**
     * 判断是否已经点赞
     *
     * @param movementId
     * @param userId
     * @param like
     * @return
     */
    @Override
    public Boolean hasComment(String movementId, Long userId, CommentType like) {
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("publishId").is(new ObjectId(movementId))
                .and("CommentType").is(like.getType());
        Query query = Query.query(criteria);
        boolean exists = mongoTemplate.exists(query, Comment.class);
        return exists;
    }

    /**
     * 删除返回总数
     *
     * @param comment
     * @return
     */
    @Override
    public Integer delete(Comment comment) {
        //1、删除Comment表数据
        Query query = Query.query(Criteria.where("publishId").is(comment.getPublishId())
                .and("userId").is(comment.getUserId())
                .and("commentType").is(comment.getCommentType()));
        mongoTemplate.remove(query, Comment.class);
        //2、修改动态表中的总数量
        Query movementQuery = Query.query(Criteria.where("id").is(comment.getPublishId()));
        Update update = new Update();
        if (comment.getCommentType() == CommentType.LIKE.getType()) {
            update.inc("likeCount", -1);
        } else if (comment.getCommentType() == CommentType.COMMENT.getType()) {
            update.inc("commentCount", -1);
        } else {
            update.inc("loveCount", -1);
        }
        //设置更新参数
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true);//获取更新后的最新数据
        Movement modify = mongoTemplate.findAndModify(movementQuery, update, options, Movement.class);
        //5、获取最新的评论数量，并返回
        return modify.statisCount(comment.getCommentType());
    }

    /**
     * 评论点赞
     *
     * @param commentId
     * @return
     */
    @Override
    public Integer pingLunCount(String commentId) {
        Query query = Query.query(Criteria.where("id").is(new ObjectId(commentId)));
        Update update = new Update();
        update.inc("likeCount", 1);
        //设置更新参数，获取最新评论数并返回
        FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions();
        findAndModifyOptions.returnNew(true);
        Comment andModify = mongoTemplate.findAndModify(query, update, findAndModifyOptions, Comment.class);
        return andModify.getLikeCount();
    }

    /**
     * 评论取消点赞
     *
     * @param commentId
     * @return
     */

    @Override
    public Integer pingLunDelete(String commentId) {
        Query query = Query.query(Criteria.where("id").is(new ObjectId(commentId)));
        Update update = new Update();
        update.inc("likeCount", -1);
        //设置更新参数，获取最新评论数并返回
        FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions();
        findAndModifyOptions.returnNew(true);
        Comment andModify = mongoTemplate.findAndModify(query, update, findAndModifyOptions, Comment.class);
        return andModify.getLikeCount();
    }

    /**
     * 点赞列表
     *
     * @param page
     * @param pagesize
     * @param id
     * @param like
     * @return
     */
    @Override
    public List<Comment> getComment(Integer page, Integer pagesize, Long id, CommentType like) {
        Criteria criteria = Criteria.where("publishUserId").is(id)
                .and("commentType").is(like.getType());
        Query query = Query.query(criteria).skip((page - 1) * pagesize).limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        return mongoTemplate.find(query, Comment.class);
    }

    //视频点赞返回总数
    @Override
    public Integer videoSave(Comment comment) {
        Video video = mongoTemplate.findById(comment.getPublishId(), Video.class);
        if (video != null) {
            comment.setPublishUserId(video.getUserId());
        }
        mongoTemplate.save(comment);
        //更新动态表中的点赞数平论数并返回
        Query query = Query.query(Criteria.where("id").is(comment.getPublishId()));
        Update update = new Update();
        if (comment.getCommentType() == CommentType.LIKE.getType()) {
            update.inc("likeCount", 1);
        } else if (comment.getCommentType() == CommentType.COMMENT.getType()) {
            update.inc("CommentCount", 1);
        } else {
            update.inc("loveCount", 1);
        }
        //设置更新参数，获取最新评论数并返回
        FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions();
        findAndModifyOptions.returnNew(true);
        Video andModify = mongoTemplate.findAndModify(query, update, findAndModifyOptions, Video.class);
        return andModify.statisCount(comment.getCommentType());
    }

    //视频取消点赞
    @Override
    public Integer dislike(Comment comment) {
        Video video = mongoTemplate.findById(comment.getPublishId(), Video.class);
        if (video != null) {
            comment.setPublishUserId(video.getUserId());
        }
        Query query = Query.query(Criteria.where("userId").is(comment.getUserId())
                .and("commentType").is(comment.getCommentType())
                .and("publishUserId").is(comment.getPublishUserId())
                .and("publishId").is(comment.getPublishId()));
        mongoTemplate.remove(query, Comment.class);
        Query query1 = Query.query(Criteria.where("id").is(comment.getPublishId()));
        Update update = new Update();
        if (comment.getCommentType() == CommentType.LIKE.getType()) {
            update.inc("likeCount", -1);
        } else if (comment.getCommentType() == CommentType.COMMENT.getType()) {
            update.inc("CommentCount", -1);
        } else {
            update.inc("loveCount", -1);
        }
        //设置更新参数，获取最新评论数并返回
        FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions();
        findAndModifyOptions.returnNew(true);
        Video andModify = mongoTemplate.findAndModify(query1, update, findAndModifyOptions, Video.class);
        return andModify.statisCount(comment.getCommentType());
    }



}