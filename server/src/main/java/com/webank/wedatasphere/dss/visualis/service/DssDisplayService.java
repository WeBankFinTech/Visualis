package com.webank.wedatasphere.dss.visualis.service;

import com.webank.wedatasphere.dss.visualis.model.optmodel.ExportedProject;
import com.webank.wedatasphere.dss.visualis.model.optmodel.IdCatalog;

import java.util.Map;
import java.util.Set;

public interface DssDisplayService {

    void exportDisplays(Long projectId, Map<String, Set<Long>> moduleIdsMap, boolean partial, ExportedProject exportedProject) throws Exception;

    void importDisplay(Long projectId, String versionSuffix, ExportedProject exportedProject, IdCatalog idCatalog) throws Exception;

    void copyDisplay(Map<String, Set<Long>> moduleIdsMap, ExportedProject exportedProject) throws Exception;
}
