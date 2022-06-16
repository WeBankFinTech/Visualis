package com.webank.wedatasphere.dss.visualis.service;

import com.webank.wedatasphere.dss.visualis.model.optmodel.ExportedProject;
import org.apache.linkis.server.Message;

import java.util.Map;
import java.util.Set;

public interface IProjectExportService {
    Message exportProject(Map<String, String> params, String username) throws Exception;

    ExportedProject export(Long projectId, Map<String, Set<Long>> moduleIdsMap, boolean partial);

    Map<String, Set<Long>> getModuleIdsMap(Map<String, String> params);

    Long getProjectId(Map<String, Set<Long>> moduleIdsMap);
}
