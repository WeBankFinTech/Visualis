package com.webank.wedatasphere.dss.visualis.service;

import org.apache.linkis.server.Message;

import javax.servlet.http.HttpServletRequest;

public interface IDisplayCopyService {
    Message copyDisplay(HttpServletRequest req, Long sourceId, Long targetId) throws Exception;
}
