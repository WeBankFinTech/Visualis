package com.webank.wedatasphere.dss.visualis.service;

import org.apache.linkis.server.Message;
import javax.servlet.http.HttpServletRequest;

public interface IBaseInfoService {
    Message getDefault(HttpServletRequest req);
}
