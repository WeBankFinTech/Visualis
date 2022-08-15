package com.webank.wedatasphere.dss.visualis.service;

import edp.davinci.model.PreviewResult;
import edp.davinci.model.User;

import java.util.List;

public interface AsynService {

    // 1. submit提交任务
    String sumbmitPreviewTask(User user, String component, Long id) throws Exception;

    // 2. state获取任务状态
    String state(String executeId, String component) throws Exception;

    // 3. getResult获取执行结果
    PreviewResult getResult(String executeId, String component) throws Exception;
}
