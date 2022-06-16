package com.webank.wedatasphere.dss.visualis.service;

import com.webank.wedatasphere.dss.visualis.model.optmodel.IdCatalog;
import org.apache.linkis.server.Message;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface IProjectImportService {
    Message importProject(HttpServletRequest req, Map<String, String> params) throws Exception;

    IdCatalog importOpt(String projectJson, Long projectId, String versionSuffix);
}
