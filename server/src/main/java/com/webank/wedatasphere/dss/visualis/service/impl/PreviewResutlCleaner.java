package com.webank.wedatasphere.dss.visualis.service.impl;

import com.webank.wedatasphere.dss.visualis.configuration.CommonConfig;
import edp.davinci.dao.PreviewResultMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PreviewResutlCleaner {

    @Autowired
    PreviewResultMapper previewResultMapper;

    // 秒数 分钟 小时 日期 月份 星期 年份(可为空)
    // 每周5下午2点半清理
//    @Scheduled(cron = "0 30 14 ? * 5")
    @Scheduled(fixedRate = 3000)
    public void scheduleGetUserList() {
        if (CommonConfig.PREVIEW_RESULT_CLEAN_STRATEGY().getValue().equals("scheduled")) {
            log.warn("Start cleaning up historical data of screenshots.[开始清理截图数据!]");
            previewResultMapper.deleteArchivedResult();
        }
    }
}
