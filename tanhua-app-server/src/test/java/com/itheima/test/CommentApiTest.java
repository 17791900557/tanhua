package com.itheima.test;

import com.tabhua.model.enums.CommentType;
import com.tabhua.model.mongo.Comment;
import com.tanhua.dubbo.api.CommentApi;
import com.tanhua.server.AppserverApplication;
import org.apache.dubbo.config.annotation.DubboReference;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppserverApplication.class)
public class CommentApiTest {

    @DubboReference
    private CommentApi commentApi;

    @Test
    public void testSave() {
        Comment comment = new Comment();
        comment.setCommentType(CommentType.COMMENT.getType());
        comment.setUserId(106l);
        comment.setCreated(System.currentTimeMillis());
        comment.setContent("测试评论");
        comment.setPublishId(new ObjectId("62fe375c638ddf042292150f"));
        commentApi.save(comment);
    }
}