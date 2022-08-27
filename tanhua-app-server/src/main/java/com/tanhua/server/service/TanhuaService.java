package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.tabhua.model.domain.Question;
import com.tabhua.model.domain.User;
import com.tabhua.model.domain.UserInfo;
import com.tabhua.model.dto.RecommendUserDto;
import com.tabhua.model.mongo.RecommendUser;
import com.tabhua.model.mongo.Visitors;
import com.tabhua.model.vo.ErrorResult;
import com.tabhua.model.vo.NearUserVo;
import com.tabhua.model.vo.PageResult;
import com.tabhua.model.vo.TodayBest;
import com.tanhua.autoconfig.template.HuanXinTemplate;
import com.tanhua.commoms.utils.Constants;
import com.tanhua.dubbo.api.*;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TanhuaService {

    @DubboReference(check = false)
    private RecommendUserApi recommendUserApi;
    @Autowired
    private HuanXinTemplate huanXinTemplate;
    @DubboReference(check = false)
    private QuestionApi questionApi;
    @DubboReference(check = false)
    private UserInfoApi userInfoApi;
    @Value("${tanhua.default.recommend.users}")
    private String recommendUsers;
    @DubboReference(check = false)
    private UserLikeApi userLikeApi;
    @Autowired
    private RedisTemplate<String ,String> redisTemplate;
    @DubboReference(check = false)
    private UserLocationApi locationApi;
    @DubboReference(check = false)
    private VisitorsApi visitorsApi;

    /**
     * 今日佳人
     *
     * @return
     */
    public TodayBest todayBest() {
        Long userId = UserHolder.getId();
        RecommendUser recommendUser = recommendUserApi.queryWithMaxScore(userId);
        if (recommendUser == null) {
            recommendUser = new RecommendUser();
            recommendUser.setUserId(1l);
            recommendUser.setScore(99d);
        }

        UserInfo userInfo = userInfoApi.findById(recommendUser.getToUserId());
        TodayBest todayBest = TodayBest.init(userInfo, recommendUser);
        return todayBest;
    }

    /**
     * 查询推荐好友列表
     *
     * @param dto
     * @return
     */
    public PageResult recommendation(RecommendUserDto dto) {
        //1、获取用户id
        Long userId = UserHolder.getId();
        //2、调用recommendUserApi分页查询数据列表（PageResult -- RecommendUser）
        PageResult pr = recommendUserApi.queryRecommendUserList(dto.getPage(), dto.getPagesize(), userId);
        //3、获取分页中的RecommendUser数据列表
        List<RecommendUser> items = (List<RecommendUser>) pr.getItems();
        //4、判断列表是否为空
        if (items == null || items.size() <= 0) {
            return pr;
        }
        //5、提取所有推荐的用户id列表
        List<Long> ids = CollUtil.getFieldValues(items, "userId", Long.class);
        UserInfo userInfo = new UserInfo();
        userInfo.setAge(dto.getAge());
        userInfo.setGender(dto.getGender());
        //6、构建查询条件，批量查询所有的用户详情
        Map<Long, UserInfo> map = userInfoApi.findByIds(ids, userInfo);
        //7、循环推荐的数据列表，构建vo对象
        List<TodayBest> list = new ArrayList<>();
        for (RecommendUser item : items) {
            UserInfo info = map.get(item.getUserId());
            if (info != null) {
                TodayBest vo = TodayBest.init(info, item);
                list.add(vo);
            }
        }
        //8、构造返回值
        pr.setItems(list);
        return pr;
    }

    /**
     * 查询佳人信息
     *
     * @param userId
     * @return
     */
//查看佳人详情
    public TodayBest personalInfo(Long userId) {
        //1、根据用户id查询，用户详情
        UserInfo userInfo = userInfoApi.findById(userId);
        //2、根据操作人id和查看的用户id，查询两者的推荐数据
        RecommendUser user = recommendUserApi.queryByUserId(userId, UserHolder.getId());
        //构造访客数据调用api保存
        Visitors visitors = new Visitors();
        visitors.setUserId(userId);
        visitors.setVisitorUserId(UserHolder.getId());
        visitors.setScore(user.getScore());
        visitors.setFrom("首页");
        visitors.setDate(System.currentTimeMillis());
        visitors.setVisitDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        visitorsApi.save(visitors);


        //3、构造返回值
        return TodayBest.init(userInfo, user);
    }

    /**
     * 查询陌生人问题
     *
     * @param userId
     * @return
     */
    public String strangerQuestions(Long userId) {
        Question question = questionApi.getQuestion(userId);

        return question == null ? "你喜欢Java吗" : question.getTxt();
    }

    /**
     * 回复陌生人消息
     *
     * @param userId
     * @param reply
     */
    public void replyQuestions(Long userId, String reply) {
        //1、构造消息数据
        Long currentUserId = UserHolder.getId();
        UserInfo userInfo = userInfoApi.findById(currentUserId);
        Map map = new HashMap();
        map.put("userId", currentUserId);
        map.put("huanXinId", Constants.HX_USER_PREFIX + currentUserId);
        map.put("nickname", userInfo.getNickname());
        map.put("strangerQuestion", strangerQuestions(userId));
        map.put("reply", reply);
        String message = JSON.toJSONString(map);
        //2、调用template对象，发送消息
        Boolean aBoolean = huanXinTemplate.sendMsg(Constants.HX_USER_PREFIX + userId, message);//1、接受方的环信id，2、消息
        if (!aBoolean) {
            throw new BusinessException(ErrorResult.error());
        }
    }

    /**
     * 推荐用户列表
     *
     * @return
     */
    public List<TodayBest> queryCardsList() {
        //根据用户id查询推荐的人
        List<RecommendUser> list = recommendUserApi.queryCardsList(UserHolder.getId(), 10);

        //如果没有推荐的人构造默认
        if (CollUtil.isEmpty(list)) {
            list = new ArrayList<>();
            String[] userIds = recommendUsers.split(",");
            for (String userId : userIds) {
                RecommendUser recommendUser = new RecommendUser();
                recommendUser.setToUserId(Convert.toLong(userId));
                recommendUser.setUserId(UserHolder.getId());
                recommendUser.setScore(RandomUtil.randomDouble(60, 90));
                list.add(recommendUser);
            }
        }

        List<Long> ids = CollUtil.getFieldValues(list, "userId", Long.class);
        Map<Long, UserInfo> map = userInfoApi.findByIds(ids, null);
        List<TodayBest> todayBestList = new ArrayList<>();
        for (RecommendUser recommendUser : list) {
            UserInfo userInfo = map.get(recommendUser.getUserId());
            if (userInfo != null) {
                TodayBest todayBest = TodayBest.init(userInfo, recommendUser);
                todayBestList.add(todayBest);
            }
        }
        return todayBestList;
    }

    /**
     * 探花喜欢
     *
     * @param likeUserId
     */
    @Autowired
    private MessagesService messagesService;
    public void likeUser(Long likeUserId) {

        //调用api保存或者更新
        Boolean save = userLikeApi.saveOrUpdate(likeUserId, UserHolder.getId(), true);
        if (!save){
            throw new BusinessException(ErrorResult.error());
        }
        //在redis中删除或保存
        redisTemplate.opsForSet().remove(Constants.USER_NOT_LIKE_KEY+UserHolder.getId(),String.valueOf(likeUserId));
        redisTemplate.opsForSet().add(Constants.USER_LIKE_KEY+UserHolder.getId(),String.valueOf(likeUserId));
        //判断是否双向喜欢
        Boolean aBoolean = isLike(likeUserId, UserHolder.getId());
        if (aBoolean){
            messagesService.contacts(likeUserId);
        }
    }
    //判断是否互相喜欢
    public Boolean isLike(Long userId,Long likeUserId){
        String key =Constants.USER_LIKE_KEY+userId;
        return redisTemplate.opsForSet().isMember(key,String.valueOf(likeUserId));
    }

    /**
     * 探花不喜欢
     * @param likeUserId
     */
    public void notLikeUser(Long likeUserId) {
        //调用api保存或者更新
        Boolean save = userLikeApi.saveOrUpdate(likeUserId, UserHolder.getId(), false);
        if (!save){
            throw new BusinessException(ErrorResult.error());
        }
        //在redis中删除或保存
        redisTemplate.opsForSet().add(Constants.USER_NOT_LIKE_KEY+UserHolder.getId(),String.valueOf(likeUserId));
        redisTemplate.opsForSet().remove(Constants.USER_LIKE_KEY+UserHolder.getId(),String.valueOf(likeUserId));
        //删除好友关系
        messagesService.removeContacts(likeUserId);



    }

    /**
     * 搜附近
     * @param gender
     * @param distance
     * @return
     */
    public List<NearUserVo> queryNearUser(String gender, String distance) {
        List<Long> ids = locationApi.queryNearUser(UserHolder.getId(),Double.valueOf(distance));
        if (CollUtil.isEmpty(ids)){
            return new ArrayList<>();
        }
        //调用userInfoApi查询
        UserInfo info = new UserInfo();
        info.setGender(gender);
        Map<Long, UserInfo> map = userInfoApi.findByIds(ids, info);
        List<NearUserVo> vos = new ArrayList<>();
        for (Long id : ids) {
            if (id == UserHolder.getId()){
                continue;
            }//排除当前用户
            UserInfo userInfo = map.get(id);
            if (userInfo != null){
               NearUserVo vo = NearUserVo.init(userInfo);
               vos.add(vo);
            }
        }
        return vos;
    }
}
