package com.webank.wedatasphere.dss.visualis.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.webank.wedatasphere.dss.visualis.service.Utils;
import com.webank.wedatasphere.dss.visualis.service.DssWidgetService;
import com.webank.wedatasphere.dss.visualis.content.WidgetContant;
import com.webank.wedatasphere.dss.visualis.exception.VGErrorException;
import com.webank.wedatasphere.dss.visualis.model.optmodel.ExportedProject;
import com.webank.wedatasphere.dss.visualis.model.optmodel.IdCatalog;
import com.webank.wedatasphere.dss.visualis.query.model.VirtualView;
import com.webank.wedatasphere.dss.visualis.query.service.VirtualViewQueryServiceImpl;
import com.webank.wedatasphere.dss.visualis.query.utils.QueryUtils;
import com.webank.wedatasphere.dss.visualis.utils.StringConstant;
import edp.core.exception.ServerException;
import edp.core.model.PaginateWithQueryColumns;
import edp.core.utils.CollectionUtils;
import edp.davinci.common.utils.ScriptUtils;
import edp.davinci.core.common.ResultMap;
import edp.davinci.dao.*;
import edp.davinci.dto.dashboardDto.DashboardWithPortal;
import edp.davinci.dto.displayDto.DisplayWithProject;
import edp.davinci.dto.viewDto.ViewExecuteParam;
import edp.davinci.dto.widgetDto.WidgetCreate;
import edp.davinci.model.*;
import edp.davinci.service.ViewService;
import edp.davinci.service.WidgetService;
import org.apache.linkis.adapt.LinkisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.cs.common.utils.CSCommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static edp.davinci.common.utils.ScriptUtils.getExecuptParamScriptEngine;

@Slf4j
@Service("dssWidgetService")
public class WidgetServiceImpl implements DssWidgetService {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String migratedOldTime = "2000-05-26 18:34:01";

    @Autowired
    WidgetMapper widgetMapper;

    @Autowired
    SourceMapper sourceMapper;

    @Autowired
    ViewMapper viewMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    DashboardMapper dashboardMapper;

    @Autowired
    DisplayMapper displayMapper;

    @Autowired
    WidgetService widgetService;

    @Autowired
    ViewService viewService;

    @Autowired
    VirtualViewQueryServiceImpl virtualViewQueryService;

    @Override
    public ResultMap rename(Map<String, Object> params) throws Exception {

        ResultMap resultMap = new ResultMap();

        Long widgetId = ((Integer) params.getOrDefault("id", -1)).longValue();
        String widgetName = ((String) params.getOrDefault("name", ""));

        Widget widget = widgetMapper.getById(widgetId);
        widget.setName(widgetName);
        Map<String, Object> configMap = LinkisUtils.gson().fromJson(widget.getConfig(), Map.class);
        configMap.put("nodeName", widgetName);
        if (configMap.get("view") != null && (configMap.get("view") instanceof Map)) {
            Map<String, Object> viewMap = (Map<String, Object>) configMap.get("view");
            Map<String, Object> sourceMap = (Map<String, Object>) viewMap.get("source");
            Map<String, Object> dataSourceContentMap = (Map<String, Object>) sourceMap.get("dataSourceContent");
            dataSourceContentMap.put("nodeName", widgetName);
            sourceMap.put("dataSourceContent", dataSourceContentMap);
            viewMap.put("source", sourceMap);
            configMap.put("view", viewMap);
        }
        widget.setConfig(LinkisUtils.gson().toJson(configMap));
        widgetMapper.update(widget);

        return resultMap.success();
    }

    @Override
    public ResultMap smartCreateFromSql(String userName, Map<String, Object> params) throws Exception {

        ResultMap resultMap = new ResultMap();
        Map<String, Object> resultDataMap = new HashMap<>();

        User user = userMapper.selectByUsername(userName);

        String widgetName = ((String) params.getOrDefault("widgetName", ""));
        String viewName = ((String) params.getOrDefault("viewName", ""));
        String viewSql = ((String) params.getOrDefault("viewSql", ""));
        Long viewId = ((Integer) params.getOrDefault("viewId", -1)).longValue();
        Long projectId = ((Integer) params.getOrDefault("projectId", -1)).longValue();
        String nodeName = ((String) params.getOrDefault(CSCommonUtils.NODE_NAME_STR, ""));
        String contextId = ((String) params.getOrDefault(CSCommonUtils.CONTEXT_ID_STR, ""));
        String encodedContextId = QueryUtils.encodeContextId(contextId);
        String description = (String) params.getOrDefault("description", "");

        WidgetCreate widgetCreate = new WidgetCreate();
        widgetCreate.setName(widgetName);
        widgetCreate.setProjectId(projectId);
        widgetCreate.setPublish(true);
        widgetCreate.setType(1L);
        widgetCreate.setDescription(description);
        String widgetConfig = "";

        if (viewId < 0) {
            if (StringUtils.isBlank(nodeName)) {
                String contextInfo = "\"contextId\":\"" + "\", \"nodeName\":\"" + widgetName + "\",";
                widgetConfig = StringUtils.replace(WidgetContant.WIDGET_CHART_CONFIG_TEMPLE, "${model_content}", "\"\"");
                widgetConfig = StringUtils.replace(widgetConfig, "${context_info}", contextInfo);
            } else {
                VirtualView virtualView = QueryUtils.getExactFromContext(encodedContextId, nodeName);
                if (virtualView == null) {
                    throw new ServerException("节点[" + nodeName + "]: 没有产生查询结果集，或非法绑定View节点");
                }
                String contextInfo = "\"contextId\":\"" + "\", \"nodeName\":\"" + widgetName + "\",\"refNodeName\":\"" + nodeName + "\",";
                widgetConfig = StringUtils.replace(WidgetContant.WIDGET_CHART_CONFIG_TEMPLE, "${model_content}", "\"\"");
                widgetConfig = StringUtils.replace(widgetConfig, "${context_info}", contextInfo);
            }
        } else {
            View view = viewMapper.getById(viewId);
            widgetCreate.setViewId(viewId);
            widgetConfig = StringUtils.replace(WidgetContant.WIDGET_CHART_CONFIG_TEMPLE, "${model_content}", view.getModel());
            widgetConfig = StringUtils.replace(widgetConfig, "${context_info}", "");
        }
        widgetCreate.setConfig(widgetConfig);
        Widget newWidget = widgetService.createWidget(widgetCreate, user);

        resultDataMap.put("widgetId", newWidget.getId());
        resultDataMap.put("widgetName", newWidget.getName());
        resultDataMap.put("viewId", viewId);

        return resultMap.success().payload(resultDataMap);
    }

    @Override
    public ResultMap updateContextId(Long widgetId, String contextId) throws Exception {
        Widget widget = widgetMapper.getById(widgetId);
        if (widget == null) {
            throw new ServerException("Widget does not exist");
        }
        Map<String, Object> configMap = LinkisUtils.gson().fromJson(widget.getConfig(), Map.class);
        if (configMap.get("contextId") == null) {
            throw new ServerException("This Widget does not have contextId");
        }
        String encodedContextId = QueryUtils.encodeContextId(contextId);
        String nodeName = (String) configMap.get("refNodeName");
        if (StringUtils.isNotBlank(nodeName)) {
            try {
                VirtualView virtualView = Iterables.getFirst(QueryUtils.getFromContext(encodedContextId, nodeName), null);
                if (virtualView != null) {
                    configMap.put("view", virtualView);
                }
            } catch (ErrorException e) {
                log.error("Get visualView error by ContextID: {} and nodeName: {}", contextId, nodeName);
                throw new VGErrorException(20003, "get visualView error, due to error error contextId and nodeName.");
            }
        }

        Object viewObj = configMap.get("view");
        if (viewObj != null && (viewObj instanceof Map)) {
            Map<String, Object> viewMap = (Map<String, Object>) viewObj;
            if (viewMap.size() > 0) {
                Map<String, Object> sourceMap = (Map<String, Object>) viewMap.get("source");
                Map<String, Object> dataSourceContentMap = (Map<String, Object>) sourceMap.get("dataSourceContent");
                dataSourceContentMap.put("contextId", contextId);
                sourceMap.put("dataSourceContent", dataSourceContentMap);
                viewMap.put("source", sourceMap);
                configMap.put("view", viewMap);
            }
        }
        log.info("widget:{}", widget);
        configMap.put("contextId", QueryUtils.encodeContextId(contextId));
        widget.setConfig(LinkisUtils.gson().toJson(configMap));
        widgetMapper.update(widget);

        return new ResultMap().success();
    }

    @Override
    public ResultMap getWidgetData(String userName, Long widgetId) throws Exception {

        ResultMap resultMap = new ResultMap();
        Map<String, Object> resultDataMap = new HashMap<>();

        User user = userMapper.selectByUsername(userName);
        Widget widget = widgetMapper.getById(widgetId);

        JSONObject configObject = JSONObject.parseObject(widget.getConfig());
        if (configObject.get("query") == null) {
            log.warn("querying an empty widget");
            resultDataMap.put("columns", Lists.newArrayList());
            resultDataMap.put("resultList", Lists.newArrayList());
            return resultMap.success().payload(resultDataMap);
        }
        ViewExecuteParam viewExecuteParam = ScriptUtils.getViewExecuteParam(getExecuptParamScriptEngine(), null, widget.getConfig(), null);
        PaginateWithQueryColumns paginate;
        if (viewExecuteParam.getView() == null) {
            paginate = (PaginateWithQueryColumns) viewService.getData(widget.getViewId(), viewExecuteParam, user, false);
        } else {
            //for production published
            String encodedContextId = configObject.getString("contextId");
            if (StringUtils.isNotBlank(encodedContextId)) {
                String newContextId = QueryUtils.decodeContextId(encodedContextId);
                String oldContextId = viewExecuteParam.getView().getSource().getDataSourceContent().get("contextId");
                if (!newContextId.equals(oldContextId)) {
                    viewExecuteParam.getView().getSource().getDataSourceContent().put("contextId", newContextId);
                    configObject.put("view", JSONObject.toJSON(viewExecuteParam.getView()));
                    configObject.getJSONObject("view").put("model",
                            JSONObject.parse(viewExecuteParam.getView().getModel()));
                    widget.setConfig(JSONObject.toJSONString(configObject));
                    widgetMapper.update(widget);
                }
            }
            paginate = (PaginateWithQueryColumns) virtualViewQueryService.getData(viewExecuteParam, user, false);
        }
        resultDataMap.put("columns", paginate.getColumns()==null?new ArrayList<>():paginate.getColumns());
        resultDataMap.put("resultList", paginate.getResultList());
        return resultMap.success().payload(resultDataMap);
    }

    @Override
    public ResultMap compareWithSnapshot(String userName, String type, Long id) throws Exception {

        ResultMap resultMap = new ResultMap();
        Map<String, Object> resultDataMap = new HashMap<>();

        User user = userMapper.selectByUsername(userName);

        Project project = null;
        Set<Widget> widgets = Sets.newHashSet();
        if ("dashboard".equals(type)) {
            DashboardWithPortal dashboardWithPortal = dashboardMapper.getDashboardWithPortalAndProject(id);
            if (dashboardWithPortal == null) {
                return resultMap.fail();
            }
            project = dashboardWithPortal.getProject();
            widgets.addAll(widgetMapper.getByDashboard(id));
        } else if ("portal".equals(type)) {
            List<Dashboard> dashboards = dashboardMapper.getByPortalId(id);
            if (CollectionUtils.isEmpty(dashboards)) {
                return resultMap.fail();
            }
            for (Dashboard dashboard : dashboards) {
                DashboardWithPortal dashboardWithPortal = dashboardMapper.getDashboardWithPortalAndProject(dashboard.getId());
                project = dashboardWithPortal.getProject();
                widgets.addAll(widgetMapper.getByDashboard(dashboard.getId()));
            }
        } else {
            DisplayWithProject displayWithProject = displayMapper.getDisplayWithProjectById(id);
            if (displayWithProject == null) {
                return resultMap.fail();
            }
            project = displayWithProject.getProject();
            widgets.addAll(widgetMapper.getByDisplayId(id));
        }
        List<Map<String, String>> widgetsMetaData = Lists.newArrayList();
        for (Widget widget : widgets) {
            Map<String, String> widgetMeta = Maps.newHashMap();
            widgetMeta.put("name", widget.getName());
            if (widget.getUpdateTime() == null) {
                widgetMeta.put("updated", migratedOldTime);
            } else {
                widgetMeta.put("updated", simpleDateFormat.format(widget.getUpdateTime()));
            }
            widgetMeta.put("columns", StringUtils.join(getWidgetUsedColumns(widget.getConfig()), ";"));
            widgetsMetaData.add(widgetMeta);
        }
        resultDataMap.put("projectName", project.getName());
        resultDataMap.put("widgetsMetaData", widgetsMetaData);
        return resultMap.success().payload(resultDataMap);
    }

    private Set<String> getWidgetUsedColumns(String config) {
        Set<String> columns = Sets.newHashSet();
        JsonObject configJson = LinkisUtils.gson().fromJson(config, JsonElement.class).getAsJsonObject();
        configJson.getAsJsonArray("rows").forEach(e -> columns.add(getRealColumn(e.getAsJsonObject().get("name").getAsString())));
        configJson.getAsJsonArray("cols").forEach(e -> columns.add(getRealColumn(e.getAsJsonObject().get("name").getAsString())));
        configJson.getAsJsonArray("metrics").forEach(e -> columns.add(getRealColumn(e.getAsJsonObject().get("name").getAsString())));
        return columns;
    }

    private String getRealColumn(String wrappedColumn) {
        return wrappedColumn.split("@")[0];
    }


    @Override
    public void exportWidgets(Long projectId, Map<String, Set<Long>> moduleIdsMap, boolean partial, ExportedProject exportedProject) {
        if (partial) {
            Set<Long> longs = moduleIdsMap.get(StringConstant.WIDGET_IDS);
            if (longs.size() > 0) {
                exportedProject.setWidgets(widgetMapper.getByIds(longs));
                exportedProject.setViews(Lists.newArrayList(viewMapper.selectByWidgetIds(longs)));
                Set<Long> sourceIds = exportedProject.getViews().stream().map(View::getSourceId).collect(Collectors.toSet());
                List<Source> sources = sourceMapper.getByProject(projectId).stream().filter(s -> sourceIds.contains(s.getId())).collect(Collectors.toList());
                exportedProject.setSources(sources);
            }

        } else {
            exportedProject.setWidgets(widgetMapper.getByProject(projectId));
            exportedProject.setSources(sourceMapper.getByProject(projectId));
            List<View> exportedViews = Lists.newArrayList();
            for (Source source : exportedProject.getSources()) {
                exportedViews.addAll(viewMapper.getBySourceId(source.getId()));
            }
            exportedProject.setViews(exportedViews);
        }
        log.info("exporting project, export widgets: {}", exportedProject);
    }

    @Override
    public void importWidget(Long projectId, String versionSuffix, ExportedProject exportedProject, IdCatalog idCatalog) {
        List<Widget> widgets = exportedProject.getWidgets();
        if (widgets == null) {
            return;
        }
        for (Widget widget : widgets) {
            Long oldId = widget.getId();
            widget.setProjectId(projectId);
            widget.setName(Utils.updateName(widget.getName(), versionSuffix));
            widget.setViewId(idCatalog.getView().get(widget.getViewId()));
            Long existingId = widgetMapper.getByNameWithProjectId(widget.getName(), projectId);
            if (existingId != null) {
                idCatalog.getWidget().put(oldId, existingId);
            } else {
                widgetMapper.insert(widget);
                idCatalog.getWidget().put(oldId, widget.getId());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void copyWidget(String contextIdStr, Map<String, Set<Long>> moduleIdsMap, ExportedProject exportedProject) throws Exception {
        Set<Long> widgetIds = moduleIdsMap.get(StringConstant.WIDGET_IDS);
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
                    String nodeName = (String) configMap.get(StringConstant.NODE_NAME);
                    // 从map中获取VirtualView
                    if (StringUtils.isNotBlank(nodeName)) {
                        VirtualView virtualView = Iterables.getFirst(QueryUtils.getFromContext(encodedContextId, nodeName), null);
                        if (virtualView != null) {
                            configMap.put(StringConstant.VIEW, virtualView);
                        }
                    }

//                    if (configMap.get(StringConstant.VIEW) != null && !(configMap.get(StringConstant.VIEW) instanceof VirtualView)) {
                    // 这个地方用工具apachecommons判断是否是数字
                    if (configMap.get(StringConstant.VIEW) != null && configMap.get(StringConstant.VIEW).toString().matches("^([-+])?\\d+(\\.\\d+)?$")) {
                        Object viewVal = configMap.get(StringConstant.VIEW);
                        // 判断拿到的结构是否是map结构，可能存在不是map的情况
                        if (viewVal != null && viewVal.toString().matches("^([-+])?\\d+(\\.\\d+)?$")) {
                            widgetLists.add(newWidget);
                        }
                    } else {
                        // 拿到viewMap
                        Map<String, Object> viewMap = (Map<String, Object>) configMap.get(StringConstant.VIEW);
                        Map<String, Object> sourceMap = (Map<String, Object>) viewMap.get(StringConstant.SOURCE);
                        Map<String, Object> dataSourceContentMap = (Map<String, Object>) sourceMap.get(StringConstant.DATASOURCE_CONTENT);
                        dataSourceContentMap.put(StringConstant.CONTEXT_ID, contextIdStr);
                        sourceMap.put(StringConstant.DATASOURCE_CONTENT, dataSourceContentMap);
                        viewMap.put(StringConstant.SOURCE, sourceMap);
                        configMap.put(StringConstant.VIEW, viewMap);
                    }
//                    configMap.put(StringConstant.CONTEXT_ID, QueryUtils.encodeContextId(contextIdStr));
                    newWidget.setConfig(LinkisUtils.gson().toJson(configMap));
                    widgetLists.add(newWidget);
                }
                exportedProject.setWidgets(widgetLists);
            }
        }
    }
}
