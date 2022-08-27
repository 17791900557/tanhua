package com.tanhua.dubbo.api;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tabhua.model.domain.User;
import com.tanhua.dubbo.mappers.UserMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService
public class UserApiImpl implements UserApi {

    @Autowired
    private UserMapper userMapper;

    /**
     * 查询用户
     * @param phone
     * @return
     */
    public User findByPhone(String phone) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone,phone);
        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 创建用户
     * @param user
     * @return
     */
    @Override
    public Long save(User user) {
        userMapper.insert(user);
        return user.getId();
    }

    /**
     * 修改手机号
     * @param newPhone
     * @param userID
     */
    @Override
    public void updatePhone(String newPhone, Long userID) {
        User user = new User() ;
        user.setPhone(newPhone);
        user.setId(userID);
        userMapper.updateById(user);

    }

    /**
     * 更新用户信息
     * @param user
     */
    @Override
    public void update(User user) {
        userMapper.updateById(user);
    }

    /**
     * 根据id查询
     * @param userId
     * @return
     */
    @Override
    public User findById(Long userId) {
        User user = userMapper.selectById(userId);
        return user;
    }



    /**
     * 根据环信id查询user
     * @param huanxinId
     * @return
     */
    @Override
    public User findByHuanxin(String huanxinId) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getHxUser,huanxinId);
        return userMapper.selectOne(qw);
    }

}
