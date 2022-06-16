package com.webank.wedatasphere.dss.visualis.service.impl;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.webank.wedatasphere.dss.visualis.enums.ModuleEnum;
import com.webank.wedatasphere.dss.visualis.model.optmodel.IdCatalog;
import com.webank.wedatasphere.dss.visualis.query.model.VirtualView;
import com.webank.wedatasphere.dss.visualis.query.utils.QueryUtils;
import com.webank.wedatasphere.dss.visualis.restful.ProjectRestfulApi;
import com.webank.wedatasphere.dss.visualis.service.IProjectCopyService;
import com.webank.wedatasphere.dss.visualis.service.IProjectExportService;
import com.webank.wedatasphere.dss.visualis.service.IProjectImportService;
import com.webank.wedatasphere.dss.visualis.model.optmodel.ExportedProject;
import com.webank.wedatasphere.dss.visualis.model.optmodel.PlainDashboardPortal;
import com.webank.wedatasphere.dss.visualis.model.optmodel.PlainDisplay;
import org.apache.linkis.adapt.LinkisUtils;
import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.server.Message;
import edp.davinci.model.View;
import edp.davinci.model.Widget;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ProjectCopyServiceImpl implements IProjectCopyService {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectRestfulApi.class);

    @Autowired
    private IProjectExportService projectExportService;

    @Autowired
    private IProjectImportService projectImportServer;


    @Override
    public Message copy(HttpServletRequest req, Map<String, String> params) throws Exception {
        LOG.info("begin to copy in visualis params is {}", params);

        Map<String, Set<Long>> moduleIdsMap = projectExportService.getModuleIdsMap(params);

        String projectVersion = params.getOrDefault("projectVersion", "v1");
        String flowVersion = params.get("flowVersion");
        if (StringUtils.isEmpty(flowVersion)) {
            LOG.error("flowVersion is null, can not copy flow to a newest version");
            flowVersion = "v00001";
        }
        String contextIdStr = params.get("contextID");
        if (StringUtils.isEmpty(contextIdStr)) {
            throw new ErrorException(20012, "contextId is null, visualis can not do copy");
        }
        Long projectId = projectExportService.getProjectId(moduleIdsMap);

        ExportedProject exportedProject = projectExportService.export(projectId, moduleIdsMap, true);

        copyWidget(contextIdStr, moduleIdsMap, exportedProject);

        copyDisplay(moduleIdsMap, exportedProject);

        copyDashboardPortal(moduleIdsMap, exportedProject);

        copyView(moduleIdsMap, exportedProject);

        String projectJson = LinkisUtils.gson().toJson(exportedProject);
        String versionSuffix = projectVersion + "_" + flowVersion;
        IdCatalog idCatalog = projectImportServer.importOpt(projectJson, projectId, versionSuffix);
        Message message = Message.ok()
                .data("widget", idCatalog.getWidget())
                .data("dashboard", idCatalog.getDashboard())
                .data("dashboardPortal", idCatalog.getDashboardPortal())
                .data("display", idCatalog.getDisplay())
                .data("view", idCatalog.getView());
        return message;
    }

    private void copyView(Map<String, Set<Long>> moduleIdsMap, ExportedProject exportedProject) {
        Set<Long> viewIds = moduleIdsMap.get(ModuleEnum.VIEW_IDS.getName());
        if (!viewIds.isEmpty()) {
            View view = exportedProject.getViews().get(0);
            exportedProject.setViews(Lists.newArrayList(view));
        }
    }

    private void copyDashboardPortal(Map<String, Set<Long>> moduleIdsMap, ExportedProject exportedProject) {
        Set<Long> dashboardPortalIds = moduleIdsMap.get(ModuleEnum.DASHBOARD_PORTAL_IDS.getName());
        if (!dashboardPortalIds.isEmpty()) {
            PlainDashboardPortal plainDashboardPortal = exportedProject.getDashboardPortals().get(0);
            exportedProject.setDashboardPortals(Lists.newArrayList(plainDashboardPortal));
        }
    }

    private void copyDisplay(Map<String, Set<Long>> moduleIdsMap, ExportedProject exportedProject) {
        Set<Long> displayIds = moduleIdsMap.get(ModuleEnum.DISPLAY_IDS.getName());
        if (!displayIds.isEmpty()) {
            PlainDisplay display = exportedProject.getDisplays().get(0);
            exportedProject.setDisplays(Lists.newArrayList(display));
        }
    }

    @SuppressWarnings("unchecked")
    private void copyWidget(String contextIdStr, Map<String, Set<Long>> moduleIdsMap, ExportedProject exportedProject) throws ErrorException {
        Set<Long> widgetIds = moduleIdsMap.get(ModuleEnum.WIDGET_IDS.getName());
        //将widget新的contextId和名字进行替换
        if (!widgetIds.isEmpty()) {
            List<Widget> widgetLists = Lists.newArrayList();
            for (Widget widgetItem : exportedProject.getWidgets()) {
                if (widgetItem != null) {
                    Widget newWidget = widgetItem;
                    // 获取config内容，转换成map
                    Map<String, Object> configMap = LinkisUtils.gson().fromJson(newWidget.getConfig(), Map.class);
                    // 获取上下文id
                    String encodedContextId = QueryUtils.encodeContextId(contextIdStr);
                    String nodeName = (String) configMap.get("nodeName");
                    // 从map中获取VirtualView
                    if (StringUtils.isNotBlank(nodeName)) {
                        VirtualView virtualView = Iterables.getFirst(QueryUtils.getFromContext(encodedContextId, nodeName), null);
                        if (virtualView != null) {
                            configMap.put("view", virtualView);
                        }
                    }

                    if (configMap.get("view") != null && !(configMap.get("view") instanceof VirtualView)) {
                        Object viewVal = configMap.get("view");
                        // 判断拿到的结构是否是map结构，可能存在不是map的情况
                        if (viewVal != null && viewVal.toString().matches("^([-+])?\\d+(\\.\\d+)?$")) {
                            widgetLists.add(newWidget);
                        }
                    } else {
                        // 拿到viewMap
                        Map<String, Object> viewMap = (Map<String, Object>) configMap.get("view");
                        Map<String, Object> sourceMap = (Map<String, Object>) viewMap.get("source");
                        Map<String, Object> dataSourceContentMap = (Map<String, Object>) sourceMap.get("dataSourceContent");
                        dataSourceContentMap.put("contextId", contextIdStr);
                        sourceMap.put("dataSourceContent", dataSourceContentMap);
                        viewMap.put("source", sourceMap);
                        configMap.put("view", viewMap);
                    }
                    configMap.put("contextId", QueryUtils.encodeContextId(contextIdStr));
                    newWidget.setConfig(LinkisUtils.gson().toJson(configMap));
                    widgetLists.add(newWidget);
                }
                exportedProject.setWidgets(widgetLists);
            }
        }
    }
}
