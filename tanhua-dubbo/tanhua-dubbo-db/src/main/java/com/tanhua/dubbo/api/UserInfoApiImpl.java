package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tabhua.model.domain.UserInfo;
import com.tanhua.dubbo.mappers.UserInfoMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DubboService
public class UserInfoApiImpl implements UserInfoApi {

    @Autowired
    private UserInfoMapper userInfoMapper;
    //保存用户
    @Override
    public void save(UserInfo userInfo) {
        userInfoMapper.insert(userInfo);

    }
    //用户更新
    @Override
    public void update(UserInfo userInfo) {
       userInfoMapper.updateById(userInfo);
    }

    //根据Id查找用户
    @Override
    public UserInfo findById(Long userId) {
        return userInfoMapper.selectById(userId);
    }

    //根据id集合批量查询
    @Override
    public Map<Long,UserInfo> findByIds(List<Long> ids, UserInfo userInfo) {
        LambdaQueryWrapper<UserInfo> qw = new LambdaQueryWrapper<>();
        qw.in(UserInfo::getId,ids);
        if(userInfo != null){
            if (userInfo.getAge() != null){
                qw.lt(UserInfo::getAge,userInfo.getAge());
            }
            if (userInfo.getGender() != null){
                qw.eq(UserInfo::getGender,userInfo.getGender());
            }
            if (!StringUtils.isEmpty(userInfo.getNickname())){
                qw.like(UserInfo::getNickname,userInfo.getNickname());
            }
        }
        List<UserInfo> list = userInfoMapper.selectList(qw);
        Map<Long, UserInfo> map = new HashMap<>();
        for (UserInfo info : list) {
            map.put(info.getId(),info);
        }
//        Map<Long, UserInfo> id = CollUtil.fieldValueMap(list, "id");
        return map;
    }

    //分页查询
    @Override
    public IPage findAll(Integer page, Integer pagesize) {
        Page<UserInfo> infoPage = new Page<>(page,pagesize);
        Page<UserInfo> infoPage1 = userInfoMapper.selectPage(infoPage, null);
        return infoPage1;
    }


}
