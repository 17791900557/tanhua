package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tabhua.model.domain.UserInfo;
import com.tabhua.model.mongo.Movement;
import com.tabhua.model.mongo.Visitors;
import com.tabhua.model.vo.ErrorResult;
import com.tabhua.model.vo.MovementsVo;
import com.tabhua.model.vo.PageResult;
import com.tabhua.model.vo.VisitorsVo;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.commoms.utils.Constants;
import com.tanhua.dubbo.api.MovementApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.VisitorsApi;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MovementService {

    @Autowired
    private OssTemplate ossTemplate;
    @DubboReference(check = false)
    private MovementApi movementApi;
    @DubboReference(check = false)
    private UserInfoApi userInfoApi;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private UserFreezeService userFreezeService;

    /**
     * 发布动态
     *
     * @param movement
     * @param file
     */
    public void publishMovements(Movement movement, MultipartFile[] file) throws IOException {


        String key = Constants.USER_FREEZE+UserHolder.getId();
        Boolean aBoolean = redisTemplate.hasKey(key);
        if (aBoolean){
            userFreezeService.checkUserStatus("3",UserHolder.getId());
        }

        //判断文字内容是否存在
        if (StringUtils.isEmpty(movement.getTextContent())) {
            throw new BusinessException(ErrorResult.contentError());
        }
        Long userId = UserHolder.getId();
        //将图片上传到阿里云oss
        List<String> urlList = new ArrayList<>();
        for (MultipartFile multipartFile : file) {
            String url = ossTemplate.upload(multipartFile.getOriginalFilename(), multipartFile.getInputStream());
            urlList.add(url);
        }
        movement.setMedias(urlList);
        movement.setUserId(userId);
        movementApi.publishMovements(movement);
    }

    /**
     * 我的动态
     *
     * @param page
     * @param pageSize
     * @param userId
     * @return
     */
    public PageResult all(int page, int pageSize, Long userId) {
        UserInfo userInfo = userInfoApi.findById(userId);
        PageResult pr = movementApi.getAllMovements(page, pageSize, userId);
        List<Movement> items = (List<Movement>) pr.getItems();
        if (items == null) {
            return pr;
        }
//        List<MovementsVo> vo = new ArrayList<>();
//        for (Movement movement : items) {
//            MovementsVo vo1 = MovementsVo.init(userInfo, movement);
//            vo.add(vo1);
//        }
//        pr.setItems(vo);
        return getPageResult(page, pageSize, items);
    }

    /**
     * 好友动态
     *
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult friendMovements(int page, int pagesize) {
        Long userId = UserHolder.getId();
        List<Movement> items = movementApi.friendMovements(page, pagesize, userId);
        return getPageResult(page, pagesize, items);
    }

    /**
     * 查询动态的公共方法
     *
     * @param page
     * @param pagesize
     * @param items
     * @return
     */
    private PageResult getPageResult(int page, int pagesize, List<Movement> items) {
        if (CollUtil.isEmpty(items)) {
            return new PageResult();
        }
        List<Long> friendUserIds = CollUtil.getFieldValues(items, "userId", Long.class);
        Map<Long, UserInfo> map = userInfoApi.findByIds(friendUserIds, null);
        List<MovementsVo> list = new ArrayList<>();
        for (Movement item : items) {
//            MovementsVo vo = MovementsVo.init(map.get(item.getUserId()),item);
//            list.add(vo);
            UserInfo userInfo = map.get(item.getUserId());
            if (userInfo != null) {
                MovementsVo vo = MovementsVo.init(userInfo, item);
                //显示是否已经点赞
                //拼接redis
                String key = Constants.MOVEMENTS_INTERACT_KEY + item.getId().toString();
                String hashKey = Constants.MOVEMENT_LIKE_HASHKEY + UserHolder.getId();
                String key1 = Constants.MOVEMENTS_INTERACT_KEY + item.getId().toString();
                String hashKey1 = Constants.MOVEMENT_LOVE_HASHKEY + UserHolder.getId();
                if (redisTemplate.opsForHash().hasKey(key, hashKey)) {
                    vo.setHasLiked(1);
                }
                if (redisTemplate.opsForHash().hasKey(key1, hashKey1)) {
                    vo.setHasLoved(1);
                }
                list.add(vo);
            }
        }
        return new PageResult(page, pagesize, 0, list);
    }

    /**
     * 推荐动态
     *
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult findRecommendMovements(Integer page, Integer pagesize) {
        //1.从redis中获取推荐数据
        String redisKey = Constants.MOVEMENTS_RECOMMEND + UserHolder.getId();
        String redisData = redisTemplate.opsForValue().get(redisKey);
        List<Movement> list = Collections.EMPTY_LIST;
        if (StringUtils.isEmpty(redisData)) {
            //随机获取动态数据
            list = movementApi.randomMovements(pagesize);
        } else {
            String[] split = redisData.split(",");
            if ((page - 1) * pagesize > split.length) {
                return new PageResult();
            }
            List<Long> pids = Arrays.stream(split)
                    .skip((page - 1) * pagesize)
                    .limit(pagesize)
                    .map(e -> Convert.toLong(e))
                    .collect(Collectors.toList());
            list = movementApi.findByPids(pids);
        }
        return getPageResult(page, pagesize, list);
    }

    @Autowired
    private MqMessageService mqMessageService;
    /**
     * 查询单条动态
     *
     * @param id
     * @return
     */
    public MovementsVo findMovementById(String id) {
        mqMessageService.sendLogService(UserHolder.getId(),"0202","movement",id);
        Movement movement = movementApi.findMovementById(id);
        if (movement != null) {
            UserInfo userInfo = userInfoApi.findById(movement.getUserId());
            MovementsVo vo = MovementsVo.init(userInfo, movement);
            return vo;
        } else {
            return null;
        }


    }


    @DubboReference
    private VisitorsApi visitorsApi;

    /**
     * 谁看过我
     *
     * @return
     */
    public List<VisitorsVo> queryVisitorsList() {
        String key = Constants.VISITORS_USER;
        String hashKey = String.valueOf(UserHolder.getId());
        String value = (String) redisTemplate.opsForHash().get(key, hashKey);
        Long Date = StringUtils.isEmpty(value) ? null : Long.valueOf(value);

        List<Visitors> list = visitorsApi.queryVisitorsList(Date, UserHolder.getId());

        if (CollUtil.isEmpty(list)){
            return new ArrayList<>();
        }
        List<Long> userIds = CollUtil.getFieldValues(list, "visitorUserId", Long.class);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, null);
        List<VisitorsVo> vos = new ArrayList<>();
        for (Visitors visitors : list) {
            UserInfo userInfo = map.get(visitors.getVisitorUserId());
            if (userInfo != null){
                VisitorsVo vo = VisitorsVo.init(userInfo,visitors);
                vos.add(vo);
            }
        }
        return vos;
    }
}
