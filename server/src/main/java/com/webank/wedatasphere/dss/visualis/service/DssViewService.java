package com.webank.wedatasphere.dss.visualis.service;

import com.webank.wedatasphere.dss.visualis.model.DWCResultInfo;
import com.webank.wedatasphere.dss.visualis.model.optmodel.ExportedProject;
import com.webank.wedatasphere.dss.visualis.model.optmodel.IdCatalog;
import edp.davinci.core.common.ResultMap;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DssViewService {

    List<String> getAvailableEngineTypes(HttpServletRequest req, Long id) throws Exception;

    ResultMap createView(HttpServletRequest req, DWCResultInfo dwcResultInfo) throws Exception;

    ResultMap getViewData(HttpServletRequest req, Long id) throws Exception;

    ResultMap submitQuery(HttpServletRequest req, Long id) throws Exception;

    ResultMap isHiveDataSource(HttpServletRequest req, Long id) throws Exception;

    void exportViews(Long projectId, Map<String, Set<Long>> moduleIdsMap, boolean partial, ExportedProject exportedProject) throws  Exception;

    void importViews(Long projectId, String versionSuffix, ExportedProject exportedProject, IdCatalog idCatalog) throws Exception;

    void copyView(Map<String, Set<Long>> moduleIdsMap, ExportedProject exportedProject) throws Exception;
}
