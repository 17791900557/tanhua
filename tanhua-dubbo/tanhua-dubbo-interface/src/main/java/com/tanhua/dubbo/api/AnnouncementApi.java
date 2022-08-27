package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tabhua.model.domain.Announcement;

public interface AnnouncementApi {
    //公告查询
    IPage<Announcement> announcements(Integer page, Integer pagesize);
}
