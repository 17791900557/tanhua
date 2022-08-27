package com.tanhua.dubbo.api;

import com.tabhua.model.mongo.FocusUser;
import com.tabhua.model.mongo.Video;
import com.tabhua.model.vo.PageResult;

public interface VideoApi {
    //发部视频
    String save(Video video);

    //视频列表
    PageResult findAll(Integer page, Integer pagesize);

    //关注用户
    void userFocus(FocusUser focusUser);

    //取消关注
    void deleteFollowUser(Long followUserId, Long userId);

    //查询视频列表
    PageResult findVideosByUserId(Integer page, Integer pagesize, Long userId);
}
