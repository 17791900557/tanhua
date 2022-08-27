package com.tanhua.server.controller;

import com.tabhua.model.vo.PageResult;
import com.tabhua.model.vo.SettingsVo;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.service.SettingsService;
import org.apache.dubbo.config.support.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    /**
     * 查询通用设置
     *
     * @return
     */
    @GetMapping("/settings")
    public ResponseEntity settings() {

        SettingsVo settingsVo = settingsService.settings();
        return ResponseEntity.ok(settingsVo);
    }

    /**
     * 设置陌生人问题
     *
     * @param map
     * @return
     */
    @PostMapping("/questions")
    public ResponseEntity questions(@RequestBody Map map) {
        String content = (String) map.get("content");
        settingsService.save(content);
        return ResponseEntity.ok(null);
    }

    /**
     * 通知设置
     *
     * @param map
     * @return
     */
    @PostMapping("/notifications/setting")
    public ResponseEntity setting(@RequestBody Map map) {
        settingsService.savesettings(map);
        return ResponseEntity.ok(null);
    }

    /**
     * 分页查询黑名单列表
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/blacklist")
    public ResponseEntity blacklist(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int pageSize) {
        PageResult pageResult = settingsService.blacklist(page, pageSize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 解除黑名单
     *
     * @param blackUserId
     * @return
     */
    @DeleteMapping("/blacklist/{uid}")
    public ResponseEntity deleteBlacklist(@PathVariable("uid") Long blackUserId) {
        settingsService.deleteBlacklist(blackUserId);
        return ResponseEntity.ok(null);
    }
}
