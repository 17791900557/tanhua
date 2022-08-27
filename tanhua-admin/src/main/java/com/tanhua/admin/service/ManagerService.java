package com.tanhua.admin.service;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.gson.annotations.JsonAdapter;
import com.tabhua.model.domain.UserInfo;
import com.tabhua.model.enums.CommentType;
import com.tabhua.model.mongo.Comment;
import com.tabhua.model.mongo.Movement;
import com.tabhua.model.mongo.Video;
import com.tabhua.model.vo.CommentVo;
import com.tabhua.model.vo.MovementsVo;
import com.tabhua.model.vo.PageResult;
import com.tanhua.commoms.utils.Constants;
import com.tanhua.dubbo.api.CommentApi;
import com.tanhua.dubbo.api.MovementApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.VideoApi;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ManagerService {
    @DubboReference
    private UserInfoApi userInfoApi;
    @DubboReference
    private VideoApi videoApi;
    @DubboReference
    private MovementApi movementApi;
    @DubboReference
    private CommentApi commentApi;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 查询用户列表
     *
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult findAllUsers(Integer page, Integer pagesize) {
        IPage<UserInfo> iPage = userInfoApi.findAll(page, pagesize);
        List<UserInfo> records = iPage.getRecords();
        for (UserInfo record : records) {
            String key = Constants.USER_FREEZE+record.getId();
            if (redisTemplate.hasKey(key)){
                record.setUserStatus("2");
            }
        }
        return new PageResult(page, pagesize, (int) iPage.getTotal(), iPage.getRecords());
    }

    /**
     * 查询用户详情
     *
     * @param userId
     * @return
     */
    public UserInfo findUserById(Long userId) {
        UserInfo user = userInfoApi.findById(userId);
        String key = Constants.USER_FREEZE+userId;
        if (redisTemplate.hasKey(key)){
            user.setUserStatus("2");
        }
        return user;
    }

    /**
     * 查询用户视频列表
     *
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */

    public PageResult findVideosByUserId(Integer page, Integer pagesize, Long userId) {

        PageResult pageResult = videoApi.findVideosByUserId(page, pagesize, userId);

        return pageResult;
    }

    /**
     * 查询动态
     *
     * @param page
     * @param pagesize
     * @param userId
     * @param state
     * @return
     */
    public PageResult findAllMovements(Integer page, Integer pagesize, Long userId, Integer state) {

        PageResult pageResult = movementApi.getMovements(page, pagesize, userId, state);
        List<Movement> items = (List<Movement>) pageResult.getItems();
        List<Long> userIds = CollUtil.getFieldValues(items, "userId", Long.class);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, null);
        List<MovementsVo> vos = new ArrayList<>();
        for (Movement item : items) {
            UserInfo userInfo = map.get(item.getUserId());
            if (userInfo != null) {
                MovementsVo vo = MovementsVo.init(userInfo, item);
                vos.add(vo);
            }
        }
        pageResult.setItems(vos);
        return pageResult;
    }

    /**
     * 查询动态详情
     *
     * @param movementId
     * @return
     */
    public MovementsVo findMessagesById(String movementId) {
        Movement movement = movementApi.findMovementById(movementId);
        Long userId = movement.getUserId();
        UserInfo userInfo = userInfoApi.findById(userId);
        MovementsVo movementsVo = MovementsVo.init(userInfo, movement);
        return movementsVo;


    }

    /**
     * 查询评论
     *
     * @param page
     * @param pagesize
     * @param movementId
     * @return
     */
    public PageResult findMessages(Integer page, Integer pagesize, String movementId) {
        PageResult comments = commentApi.getComments(page, pagesize, movementId, CommentType.COMMENT);
        List<Comment> items = (List<Comment>) comments.getItems();
        List<Long> userIds = CollUtil.getFieldValues(items, "userId", Long.class);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, null);
        List<CommentVo> vos = new ArrayList<>();
        for (Comment item : items) {
            UserInfo userInfo = map.get(item.getUserId());
            if (userInfo != null) {
                CommentVo vo = CommentVo.init(userInfo, item);
                vos.add(vo);
            }
        }
        comments.setItems(vos);
        return comments;
    }

    /**
     * 用户冻结
     *
     * @param params
     * @return
     */
    public Map userFreeze(Map params) {
        String userId = params.get("userId").toString();
        String key = Constants.USER_FREEZE + userId;

        Integer freezingTime = Integer.valueOf (params.get("freezingTime").toString());
        Integer days = 0;
        if (freezingTime == 1) {
            days = 3;
        }else if (freezingTime == 2){
            days = 7;
        }
        String value = JSON.toJSONString(params);
        if (days > 0){
            redisTemplate.opsForValue().set(key,value,days, TimeUnit.MINUTES);
        }else {
            redisTemplate.opsForValue().set(key,value);
        }
        Map retMap = new HashMap();
        retMap.put("message","冻结成功");
        return retMap;

    }

    /**
     * 用户解冻
     * @param params
     * @return
     */
    public Map userUnfreeze(Map params) {
        String userId = params.get("userId").toString();
        String key = Constants.USER_FREEZE+userId;

        redisTemplate.delete(key);
        Map retMap = new HashMap();
        retMap.put("message","解冻成功");
        return retMap;
    }
}
