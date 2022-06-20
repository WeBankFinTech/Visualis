package com.webank.wedatasphere.dss.visualis.service;

import com.webank.wedatasphere.dss.visualis.model.optmodel.ExportedProject;
import com.webank.wedatasphere.dss.visualis.model.optmodel.IdCatalog;
import edp.davinci.core.common.ResultMap;

import java.util.Map;
import java.util.Set;

public interface DssWidgetService {

    /**
     * When the DSS workflow executes, the ContextID is updated,
     * return ture update success, otherwise update false.
     */
    ResultMap rename(Map<String, Object> params) throws Exception;

    ResultMap smartCreateFromSql(String userName, Map<String, Object> params) throws Exception;

    ResultMap updateContextId(Long widgetId, String contextId) throws Exception;

    ResultMap getWidgetData(String userName, Long widgetId) throws Exception;

    ResultMap compareWithSnapshot(String userName, String type, Long id) throws Exception;

    void exportWidgets(Long projectId, Map<String, Set<Long>> moduleIdsMap, boolean partial, ExportedProject exportedProject) throws Exception;

    void importWidget(Long projectId, String versionSuffix, ExportedProject exportedProject, IdCatalog idCatalog) throws Exception;

    void copyWidget(String contextIdStr, Map<String, Set<Long>> moduleIdsMap, ExportedProject exportedProject) throws Exception;
}
