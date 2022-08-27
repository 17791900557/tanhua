package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.tabhua.model.vo.ErrorResult;
import com.tanhua.commoms.utils.Constants;
import com.tanhua.server.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
public class UserFreezeService {
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    public  void checkUserStatus (String state,Long userId){
        String key = Constants.USER_FREEZE + userId;
        String value = redisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(value)){
            Map map = JSON.parseObject(value, Map.class);
            String freezingRange = (String) map.get("freezingRange");
            if (state.equals(freezingRange)){
                throw new BusinessException(ErrorResult.builder().errMessage("用户被冻结").build());
            }
        }
    }
}
