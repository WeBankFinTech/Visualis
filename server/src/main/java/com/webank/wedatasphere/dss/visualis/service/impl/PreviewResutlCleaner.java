package com.webank.wedatasphere.dss.visualis.service.impl;

import edp.davinci.dao.PreviewResultMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

@Slf4j
@Component
public class PreviewResutlCleaner {

    @Autowired
    PreviewResultMapper previewResultMapper;

    //秒数 分钟 小时 日期 月份 星期 年份(可为空)
    // 每周5下午2点半清理
    @Scheduled(cron = "0 30 14 ? * 5")
    public void scheduleGetUserList() {
        log.warn("Start cleaning up historical data of screenshots.[开始清理截图数据!]");
        Calendar calendar = Calendar.getInstance();//得到一个Calendar的实例
        Date date=new Date();
        calendar.setTimeInMillis(date.getTime());
        calendar.add(Calendar.DATE, -1);
        Date yesterDay = calendar.getTime();
        previewResultMapper.deleteResult(yesterDay);
    }
}
