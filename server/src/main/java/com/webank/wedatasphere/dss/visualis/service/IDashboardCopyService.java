package com.webank.wedatasphere.dss.visualis.service;

import org.apache.linkis.server.Message;

import javax.servlet.http.HttpServletRequest;

public interface IDashboardCopyService {
    Message copyDashboard(HttpServletRequest req, Long sourceId, Long targetId) throws Exception;
}
