package com.webank.wedatasphere.dss.visualis.service;


import com.webank.wedatasphere.dss.visualis.model.DWCResultInfo;
import org.apache.linkis.server.Message;

import javax.servlet.http.HttpServletRequest;


public interface IViewService {

    Message getViewData(HttpServletRequest req, Long id) throws Exception;

    Message createView(HttpServletRequest req, DWCResultInfo dwcResultInfo);

    Message getAvailableEngineTypes(HttpServletRequest req, Long id);

    Message submitQuery(HttpServletRequest req, Long id) throws Exception;

    Message isHiveDataSource(HttpServletRequest req, Long id) throws Exception;
}
