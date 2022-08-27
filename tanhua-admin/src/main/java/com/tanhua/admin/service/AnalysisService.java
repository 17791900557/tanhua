package com.tanhua.admin.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tabhua.model.domain.Analysis;
import com.tabhua.model.vo.AnalysisSummaryVo;
import com.tanhua.admin.mapper.AnalysisMapper;
import com.tanhua.admin.mapper.LogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class AnalysisService {
    @Autowired
    private LogMapper logMapper;
    @Autowired
    private AnalysisMapper analysisMapper;




    public void analysis() throws ParseException {
        String todayStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String yestodayStr = DateUtil.yesterday().toString("yyyy-MM-dd"); //工具类
        //1、统计每日注册用户数
        Integer numRegistered = logMapper.queryByTypeAndLogTime("0102", todayStr);
        //2、统计每日登陆用户
        Integer numLogin = logMapper.queryByTypeAndLogTime("0101", todayStr);
        //3、统计活跃的用户数
        Integer numActive = logMapper.queryByLogTime(todayStr);
        //4、统计次日留存的用户数
        Integer numRetention1d = logMapper.queryNumRetention1d(todayStr, yestodayStr);
        //5、根据当前时间查询AnalysisByDay数据
        QueryWrapper<Analysis> qw = new QueryWrapper<>();
        Date todatDate = new SimpleDateFormat("yyyy-MM-dd").parse(todayStr);
        qw.eq("record_date", todatDate);

        Analysis analysis = analysisMapper.selectOne(qw);
        if (analysis == null) {
            //7、如果不存在，保存
            analysis = new Analysis();
            analysis.setRecordDate(todatDate);
            analysis.setNumRegistered(numRegistered);
            analysis.setNumLogin(numLogin);
            analysis.setNumActive(numActive);
            analysis.setNumRetention1d(numRetention1d);
            analysis.setCreated(new Date());
            analysisMapper.insert(analysis);
        } else {
            //8、如果存在，更新
            analysis.setNumRegistered(numRegistered);
            analysis.setNumLogin(numLogin);
            analysis.setNumActive(numActive);
            analysis.setNumRetention1d(numRetention1d);
            analysisMapper.updateById(analysis);
        }
    }

    /**
     * 查询信息概要
     * @param dateTime
     * @return
     */
    public Analysis select(DateTime dateTime) {
        LambdaQueryWrapper<Analysis> qw = new LambdaQueryWrapper<>();
        qw.eq(Analysis::getRecordDate,dateTime);
        Analysis analysis = analysisMapper.selectOne(qw);
        return analysis;
    }
}
