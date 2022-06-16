package com.webank.wedatasphere.dss.visualis.service;


import org.apache.linkis.server.Message;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


public interface IProjectCopyService {
    Message copy(HttpServletRequest req, Map<String, String> params) throws Exception;
}
