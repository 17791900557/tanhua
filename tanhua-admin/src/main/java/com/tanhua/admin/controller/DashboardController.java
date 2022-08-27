package com.tanhua.admin.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.tabhua.model.domain.Analysis;
import com.tabhua.model.vo.AnalysisSummaryVo;
import com.tanhua.admin.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private AnalysisService analysisService;

    /**
     * 概要统计信息
     */
    @GetMapping("/summary")
    public AnalysisSummaryVo getSummary() {
        AnalysisSummaryVo analysisSummaryVo = new AnalysisSummaryVo();
        DateTime dateTime = DateUtil.parseDate("2020-09-16");

        Analysis analysis = analysisService.select(dateTime);
        //今日新增
        analysisSummaryVo.setNewUsersToday(Long.valueOf(analysis.getNumRegistered()));
        //今日登录
        analysisSummaryVo.setLoginTimesToday(Long.valueOf(analysis.getNumLogin()));
        //活越用户
        analysisSummaryVo.setActiveUsersToday(Long.valueOf(analysis.getNumActive()));
        //
        //累计用户数
        analysisSummaryVo.setCumulativeUsers(Long.valueOf(1000));
        analysisSummaryVo.setNewUsersTodayRate(computeRate(
                analysisSummaryVo.getNewUsersToday(),
                Long.valueOf(analysisService.select(DateUtil.parseDate("2020-09-15")).getNumRegistered())
        ));

        //今日登录次数涨跌率，单位百分数，正数为涨，负数为跌
        analysisSummaryVo.setLoginTimesTodayRate(computeRate(
                analysisSummaryVo.getLoginTimesToday(),
                Long.valueOf(analysisService.select(DateUtil.parseDate("2020-09-15")).getNumLogin())
        ));

        return analysisSummaryVo;
    }

    private static BigDecimal computeRate(Long current, Long last) {
        BigDecimal result;
        if (last == 0) {
            // 当上一期计数为零时，此时环比增长为倍数增长
            result = new BigDecimal((current - last) * 100);
        } else {
            result = BigDecimal.valueOf((current - last) * 100).divide(BigDecimal.valueOf(last), 2, BigDecimal.ROUND_HALF_DOWN);
        }
        return result;
    }

    private static String offsetDay(Date date, int offSet) {
        return DateUtil.offsetDay(date, offSet).toDateStr();
    }
}