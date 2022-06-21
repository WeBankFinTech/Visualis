package com.webank.wedatasphere.dss.visualis.service;

import com.webank.wedatasphere.dss.visualis.model.optmodel.ExportedProject;
import com.webank.wedatasphere.dss.visualis.model.optmodel.IdCatalog;

import java.util.Map;
import java.util.Set;

public interface DssDashboradService {

    void exportDashboardPortals(Long projectId, Map<String, Set<Long>> moduleIdsMap, boolean partial, ExportedProject exportedProject);

    void importDashboard(Long projectId, String versionSuffix, ExportedProject exportedProject, IdCatalog idCatalog);

    void copyDashboardPortal(Map<String, Set<Long>> moduleIdsMap, ExportedProject exportedProject);
}
