package com.tanhua.dubbo.api;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tabhua.model.domain.BlackList;
import com.tabhua.model.domain.UserInfo;
import com.tanhua.dubbo.mappers.BlackListMapper;
import com.tanhua.dubbo.mappers.UserInfoMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService
public class BlackListApiImpl implements BlackListApi {

    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private BlackListMapper blackListMapper;

    /**
     * 分页查询黑名单列表
     *
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public IPage<UserInfo> findByuserId(Long userId, int page, int pageSize) {
        //构建内置分页参宿
        Page pages = new Page(page,pageSize);
        return userInfoMapper.findBlackList(pages,userId);
    }

    /**
     * 解除黑名单
     * @param userId
     * @param blackUserId
     */
    @Override
    public void deleteBlacklist(Long userId, Long blackUserId) {
        LambdaQueryWrapper<BlackList> qw = new LambdaQueryWrapper<>();
        qw.eq(BlackList::getUserId,userId);
        qw.eq(BlackList::getBlackUserId,blackUserId);
        blackListMapper.delete(qw);
    }



}