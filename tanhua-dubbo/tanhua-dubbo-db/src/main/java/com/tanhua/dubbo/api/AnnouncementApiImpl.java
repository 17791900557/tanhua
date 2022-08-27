package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tabhua.model.domain.Announcement;
import com.tanhua.dubbo.mappers.AnnouncementMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;


@DubboService
public class AnnouncementApiImpl implements AnnouncementApi{
    @Autowired
    private AnnouncementMapper announcementMapper;

    @Override
    public IPage<Announcement> announcements(Integer page, Integer pagesize) {
        Page pages = new Page(page,pagesize);

        IPage<Announcement> iPage =  announcementMapper.page(pages);

        return iPage;
    }
}
