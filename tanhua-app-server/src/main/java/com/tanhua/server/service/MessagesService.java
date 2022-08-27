package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tabhua.model.domain.Announcement;
import com.tabhua.model.domain.User;
import com.tabhua.model.domain.UserInfo;
import com.tabhua.model.enums.CommentType;
import com.tabhua.model.mongo.Comment;
import com.tabhua.model.mongo.Friend;
import com.tabhua.model.vo.*;
import com.tanhua.autoconfig.template.HuanXinTemplate;
import com.tanhua.commoms.utils.Constants;
import com.tanhua.dubbo.api.*;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MessagesService {
    @DubboReference(check = false)
    private UserInfoApi userInfoApi;
    @DubboReference(check = false)
    private UserApi userApi;
    @Autowired
    private HuanXinTemplate huanXinTemplate;
    @DubboReference(check = false)
    private FriendApi friendApi;
    @DubboReference(check = false)
    private AnnouncementApi announcementApi;
    @DubboReference(check = false)
    private CommentApi commentApi;


    /**
     * 根据环信id查询用户详情
     */
    public UserInfoVo findUserInfoByHuanxin(String huanxinId) {
        //1、根据环信id查询用户
        User user = userApi.findByHuanxin(huanxinId);
        //2、根据用户id查询用户详情
        UserInfo userInfo = userInfoApi.findById(user.getId());
        UserInfoVo vo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo,vo); //copy同名同类型的属性
        if(userInfo.getAge() != null) {
            vo.setAge(userInfo.getAge().toString());
        }
        return vo;
    }


    /**
     * 添加好友
     *
     * @param friendId
     */
    public void contacts(Long friendId) {
        //1、将好友关系注册到环信
        Boolean aBoolean = huanXinTemplate.addContact(Constants.HX_USER_PREFIX + UserHolder.getId(),
                Constants.HX_USER_PREFIX + friendId);
        if(!aBoolean) {
            throw new BusinessException(ErrorResult.error());
        }
        //2、如果注册成功，记录好友关系到mongodb
        friendApi.save(UserHolder.getId(),friendId);
    }
    /**
     * 删除好友
     *
     * @param friendId
     */
    public void removeContacts(Long friendId) {
        //1、将好友关系注册到环信
        Boolean aBoolean = huanXinTemplate.deleteContact(Constants.HX_USER_PREFIX + UserHolder.getId(),
                Constants.HX_USER_PREFIX + friendId);
        if(!aBoolean) {
            throw new BusinessException(ErrorResult.error());
        }
        //2、如果删除成功，记录好友关系到mongodb
        friendApi.delete(UserHolder.getId(),friendId);
    }

    /**
     * 查询联系人列表
     *
     * @param page
     * @param pagesize
     * @param keyword
     * @return
     */
    public PageResult findFriends(Integer page, Integer pagesize, String keyword) {
        //1、调用API查询当前用户的好友数据 -- List<Friend>
        List<Friend> list = friendApi.findByUserId(UserHolder.getId(), page, pagesize);
        if (CollUtil.isEmpty(list)) {
            return new PageResult();
        }
        //2、提取数据列表中的好友id
        List<Long> userIds = CollUtil.getFieldValues(list, "friendId", Long.class);
        //3、调用UserInfoAPI查询好友的用户详情
        UserInfo info = new UserInfo();
        info.setNickname(keyword);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, info);
        //4、构造VO对象
        List<ContactVo> vos = new ArrayList<>();
        for (Friend friend : list) {
            UserInfo userInfo = map.get(friend.getFriendId());
            if (userInfo != null) {
                ContactVo vo = ContactVo.init(userInfo);
                vos.add(vo);
            }
        }
        return new PageResult(page, pagesize, 0, vos);
    }

    /**
     * 查询通知列表
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult announcements(Integer page, Integer pagesize) {
        IPage<Announcement> iPage = announcementApi.announcements(page,pagesize);
        if (iPage == null){
            return new PageResult();
        }
        List<Announcement> records = iPage.getRecords();
        List<AnnouncementVo> list = records.stream().map((item)->{
            AnnouncementVo vo = new AnnouncementVo();
            vo.setId(item.getId().toString());
            vo.setCreateDate(new DateTime(item.getCreated()).toString("yyyy-MM-dd HH:mm"));
            vo.setUpdated(new DateTime(item.getUpdated()).toString("yyyy-MM-dd HH:mm"));
            vo.setDescription(item.getDescription());
            vo.setTitle(item.getTitle());
            return vo;
        }).collect(Collectors.toList());



        return new PageResult(page,pagesize, (int) iPage.getTotal(),list);
    }

    /**
     * 查询点赞列表
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult likes(Integer page, Integer pagesize) {

        List<Comment> list = commentApi.getComment(page,pagesize,UserHolder.getId(), CommentType.LIKE);
        List<Long> userId = CollUtil.getFieldValues(list, "userId", Long.class);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userId,null);
        List<CommentVo> vos = new ArrayList<>();
        for (Comment comment : list) {
            UserInfo userInfo = map.get(comment.getUserId());
            CommentVo vo = CommentVo.init(userInfo, comment);
            vos.add(vo);
        }
        return new PageResult(page,pagesize,0,vos);
    }

    /**
     * 查询评论列表
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult comments(Integer page, Integer pagesize) {

        List<Comment> list = commentApi.getComment(page,pagesize,UserHolder.getId(), CommentType.COMMENT);
        List<Long> userId = CollUtil.getFieldValues(list, "userId", Long.class);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userId,null);
        List<CommentVo> vos = new ArrayList<>();
        for (Comment comment : list) {
            UserInfo userInfo = map.get(comment.getUserId());
            CommentVo vo = CommentVo.init(userInfo, comment);
            vos.add(vo);
        }
        return new PageResult(page,pagesize,0,vos);
    }

    /**
     * 喜欢列表
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult loves(Integer page, Integer pagesize) {
        List<Comment> list = commentApi.getComment(page,pagesize,UserHolder.getId(), CommentType.LOVE);
        List<Long> userId = CollUtil.getFieldValues(list, "userId", Long.class);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userId,null);
        List<CommentVo> vos = new ArrayList<>();
        for (Comment comment : list) {
            UserInfo userInfo = map.get(comment.getUserId());
            CommentVo vo = CommentVo.init(userInfo, comment);
            vos.add(vo);
        }
        return new PageResult(page,pagesize,0,vos);
    }
}

