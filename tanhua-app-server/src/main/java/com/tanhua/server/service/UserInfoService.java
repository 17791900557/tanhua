package com.tanhua.server.service;


import cn.hutool.core.collection.CollUtil;
import com.tabhua.model.domain.User;
import com.tabhua.model.domain.UserInfo;
import com.tabhua.model.mongo.RecommendUser;
import com.tabhua.model.mongo.UserLike;
import com.tabhua.model.mongo.Visitors;
import com.tabhua.model.vo.*;
import com.tanhua.autoconfig.template.AipFaceTemplate;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.commoms.utils.Constants;
import com.tanhua.dubbo.api.*;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.*;


@Service
public class UserInfoService {
    @DubboReference(check = false)
    private UserInfoApi userInfoApi;
    @Autowired
    private OssTemplate ossTemplate;
    @Autowired
    private AipFaceTemplate aipFaceTemplate;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @DubboReference(check = false)
    private UserApi userApi;
    @DubboReference(check = false)
    private UserLikeApi userLikeApi;
    @DubboReference(check = false)
    private VisitorsApi visitorsApi;

    /**
     * 保存新用户信息
     *
     * @param userInfo
     */
    public void save(UserInfo userInfo) {
        userInfoApi.save(userInfo);
    }

    /**
     * 保存用户头像
     *
     * @param headPhoto
     * @param id
     */
    public void updateHead(MultipartFile headPhoto, Long id) throws IOException {
        String url = ossTemplate.upload(headPhoto.getOriginalFilename(), headPhoto.getInputStream());

        boolean detect = aipFaceTemplate.detect(url);

        if (!detect) {
            throw new BusinessException(ErrorResult.faceError());
        } else {
            UserInfo userInfo = new UserInfo();
            userInfo.setId(id);
            userInfo.setAvatar(url);
            userInfoApi.update(userInfo);
        }

    }

    /**
     * 根据Id查询用户信息
     *
     * @param userId
     * @return
     */
    public UserInfoVo findById(Long userId) {
        UserInfo userInfo = userInfoApi.findById(userId);
        UserInfoVo vo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo, vo);
        if (userInfo.getAge() != null) {
            vo.setAge(userInfo.getAge().toString());
        }
        return (vo);
    }

    /**
     * 更新用户信息
     *
     * @param userInfo
     */
    public void updateById(UserInfo userInfo) {
        userInfoApi.update(userInfo);
    }

    /**
     * 更新用户头像
     *
     * @param headPhoto
     * @param id
     */
    public void updateImage(MultipartFile headPhoto, Long id) throws IOException {
        String url = ossTemplate.upload(headPhoto.getOriginalFilename(), headPhoto.getInputStream());
        boolean detect = aipFaceTemplate.detect(url);
        if (!detect) {
            throw new BusinessException(ErrorResult.faceError());
        } else {
            UserInfo userInfo = new UserInfo();
            userInfo.setId(id);
            userInfo.setAvatar(url);
            userInfoApi.update(userInfo);
        }
    }

    //修改电话号码发送短信
    public void sendVerificationCode(String phone) {

        String code = "123456";

        redisTemplate.opsForValue().set(phone, code, Duration.ofMinutes(5));

    }

    /**
     * 修改手机号验证码校验
     *
     * @param code
     * @return
     */

    public Boolean checkVerificationCode(String code) {
        String phone = UserHolder.getPhone();
        String redisCode = redisTemplate.opsForValue().get(phone);
        if (StringUtils.isEmpty(redisCode) || !redisCode.equals(code)) {
            throw new BusinessException(ErrorResult.loginError());
        }
        redisTemplate.delete(phone);
        return true;
    }

    /**
     * 修改手机号
     *
     * @param newPhone
     */
    public void updatePhone(String newPhone) {
        Long userID = UserHolder.getId();
        User user = userApi.findByPhone(newPhone);
        if (user != null) {
            throw new BusinessException(ErrorResult.mobileError());
        }
        userApi.updatePhone(newPhone, userID);
    }

    /**
     * 查询喜欢或者不喜欢
     *
     * @param friendId
     * @return
     */
    public Boolean alreadyLove(Long friendId) {
        String key = Constants.USER_LIKE_KEY + UserHolder.getId();
        String key1 = Constants.USER_NOT_LIKE_KEY + UserHolder.getId();
        Boolean like = redisTemplate.opsForSet().isMember(key, String.valueOf(friendId));
        Boolean unLike = redisTemplate.opsForSet().isMember(key1, String.valueOf(friendId));
        if (like) {
            return true;
        } else if (unLike) {
            return false;
        }
        return null;
    }


    /**
     * 互相喜欢，喜欢，粉丝 - 统计
     *
     * @return
     */
    public CountsVo counts() {
        String key = Constants.USER_LIKE_KEY + UserHolder.getId();
        Set<String> set = redisTemplate.opsForSet().members(key);
        String[] array = set.toArray(new String[]{});
        List<Long> ids = new ArrayList<>();
        for (String s : array) {
            ids.add(Long.valueOf(s));
        }

        CountsVo vos = new CountsVo();
        //喜欢总数
        vos.setLoveCount(set.size());
        //互相喜欢总数
        List<UserLike> userLikes = userLikeApi.eachLoveCount(UserHolder.getId(), ids, true);
        vos.setEachLoveCount(userLikes.size());
        //粉丝
        List<UserLike> fanCounts = userLikeApi.fanCount(UserHolder.getId(), true);
        vos.setFanCount(fanCounts.size());
        return vos;
    }

    /**
     * 谁看过我
     *
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult visitors(Integer page, Integer pagesize) {
        Long userId = UserHolder.getId();
        String key = Constants.VISITORS_USER;
        String hashKey = userId.toString();
        String value = String.valueOf(System.currentTimeMillis());
        redisTemplate.opsForHash().put(key, hashKey, value);
        List<Visitors> list = visitorsApi.visitors(page, pagesize, userId);
        List<Long> userIds = CollUtil.getFieldValues(list, "visitorUserId", Long.class);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, null);
        List<fangKeVo> vos = new ArrayList<>();
        for (Visitors visitors : list) {
            UserInfo userInfo = map.get(visitors.getVisitorUserId());
            if (userInfo != null) {
                fangKeVo vo = fangKeVo.init(userInfo);
                vo.setMatchRate(visitors.getScore().intValue());
                if (vo.getMatchRate() == null) {
                    vo.setMatchRate(77);
                }
                vos.add(vo);
            }
        }
        return new PageResult(page, pagesize, 0, vos);
    }

    /**
     * 分页查询互相喜欢
     *
     * @param page
     * @param pagesize
     * @return
     */
    @DubboReference
    private RecommendUserApi recommendUserApi;

    public PageResult eachLoveCount(Integer page, Integer pagesize) {
        Long userId = UserHolder.getId();
        String key = Constants.USER_LIKE_KEY + userId;
        Set<String> set = redisTemplate.opsForSet().members(key);
        String[] array = set.toArray(new String[]{});
        List<Long> ids = new ArrayList<>();
        for (String s : array) {
            ids.add(Long.valueOf(s));
        }
        List<UserLike> userLikes = userLikeApi.eachLoveCount(userId, ids, true);
        List<Long> userIds = CollUtil.getFieldValues(userLikes, "userId", Long.class);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, null);
        List<RecommendUser> recommendUsers = recommendUserApi.findByIds(page, pagesize, userIds, userId);
        List<fangKeVo> vos = new ArrayList<>();
        for (RecommendUser recommendUser : recommendUsers) {
            UserInfo userInfo = map.get(recommendUser.getUserId());
            if (userInfo != null) {
                fangKeVo vo = fangKeVo.init(userInfo);
                vo.setMatchRate(recommendUser.getScore().intValue());
                if (vo.getMatchRate() == null) {
                    vo.setMatchRate(77);
                }
                vos.add(vo);
            }
        }
        return new PageResult(page, pagesize, 0, vos);
    }

    //我喜欢分页查询
    public PageResult loveCount(Integer page, Integer pagesize) {
        Long userId = UserHolder.getId();
        String key = Constants.USER_LIKE_KEY + userId;
        Set<String> set = redisTemplate.opsForSet().members(key);
        String[] array = set.toArray(new String[]{});
        List<Long> ids = new ArrayList<>();
        for (String s : array) {
            ids.add(Long.valueOf(s));
        }
        List<RecommendUser> recommendUsers = recommendUserApi.findByIds(page, pagesize, ids, userId);
        Map<Long, UserInfo> map = userInfoApi.findByIds(ids, null);
        List<fangKeVo> vos = new ArrayList<>();
        for (RecommendUser recommendUser : recommendUsers) {
            UserInfo userInfo = map.get(recommendUser.getUserId());
            if (userInfo != null) {
                fangKeVo vo = fangKeVo.init(userInfo);
                vo.setMatchRate(recommendUser.getScore().intValue());
                if (vo.getMatchRate() == null) {
                    vo.setMatchRate(77);
                }
                vos.add(vo);
            }
        }
        return new PageResult(page, pagesize, 0, vos);

    }

    //粉丝分页列表
    public PageResult fanCount(Integer page, Integer pagesize) {
        Long userId = UserHolder.getId();
        String key = Constants.USER_LIKE_KEY + userId;
        Set<String> set = redisTemplate.opsForSet().members(key);
        String[] array = set.toArray(new String[]{});
        List<Long> ids = new ArrayList<>();
        for (String s : array) {
            ids.add(Long.valueOf(s));
        }
        List<UserLike> fanCounts = userLikeApi.fanCount(userId, true);
        List<Long> userIds = CollUtil.getFieldValues(fanCounts, "userId", Long.class);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, null);
        List<fangKeVo> vos = new ArrayList<>();
        for (Long id : userIds) {
            UserInfo userInfo = map.get(id);
            if (userInfo != null) {
                fangKeVo vo = fangKeVo.init(userInfo);
                if (ids.contains(id)) {
                    vo.setAlreadyLove(true);
                } else {
                    vo.setAlreadyLove(false);
                }
                vos.add(vo);
            }
        }

        return new PageResult(page, pagesize, 0, vos);
    }

    //列表取消喜欢
    public void offLike(Long likeUserId) {
        Long userId = UserHolder.getId();
        Boolean b = userLikeApi.offLike(likeUserId, userId);
        if (b){
            String key = Constants.USER_LIKE_KEY + userId;
            String value = likeUserId.toString();
            redisTemplate.opsForSet().remove(key, value);
        }else {
            throw new BusinessException(ErrorResult.error());
        }
    }

    //粉丝喜欢
    public void fansLike(Long likeUserId) {
        Boolean aBoolean = userLikeApi.saveOrUpdate(likeUserId, UserHolder.getId(), true);
        if (!aBoolean) {
            throw new BusinessException(ErrorResult.error());
        }
        String key = Constants.USER_LIKE_KEY + UserHolder.getId();
        String value = String.valueOf(likeUserId);
        redisTemplate.opsForSet().add(key, value);
    }
}
