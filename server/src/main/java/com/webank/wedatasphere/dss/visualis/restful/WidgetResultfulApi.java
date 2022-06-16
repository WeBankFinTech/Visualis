package com.webank.wedatasphere.dss.visualis.restful;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.webank.wedatasphere.dss.visualis.query.model.VirtualView;
import com.webank.wedatasphere.dss.visualis.query.service.VirtualViewQueryService;
import com.webank.wedatasphere.dss.visualis.query.utils.QueryUtils;
import com.webank.wedatasphere.dss.visualis.res.ModelItem;
import com.webank.wedatasphere.dss.visualis.res.ResultHelper;
import com.webank.wedatasphere.dss.visualis.service.WidgetResultService;
import org.apache.linkis.adapt.LinkisUtils;
import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.cs.common.utils.CSCommonUtils;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import edp.core.annotation.MethodLog;
import edp.core.exception.ServerException;
import edp.core.model.PaginateWithQueryColumns;
import edp.core.model.QueryColumn;
import edp.core.utils.CollectionUtils;
import edp.davinci.common.utils.ScriptUtils;
import edp.davinci.core.common.Constants;
import edp.davinci.dao.*;
import edp.davinci.dto.dashboardDto.DashboardWithPortal;
import edp.davinci.dto.displayDto.DisplayWithProject;
import edp.davinci.dto.viewDto.ViewExecuteParam;
import edp.davinci.dto.viewDto.ViewExecuteSql;
import edp.davinci.dto.widgetDto.WidgetCreate;
import edp.davinci.model.*;
import edp.davinci.service.ViewService;
import edp.davinci.service.WidgetService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static edp.davinci.common.utils.ScriptUtils.getExecuptParamScriptEngine;


@Slf4j
@RestController
@RequestMapping(path = Constants.RESTFUL_BASE_PATH + "widget", produces = MediaType.APPLICATION_JSON_VALUE)
@ComponentScan(basePackages = {"edp", "com.webank.wedatasphere.dss"})
public class WidgetResultfulApi {

    private static String WIDGET_CONFIG_TEMPLATE = "{${context_info}\"data\":[],\"cols\":[],\"rows\":[]," +
            "\"metrics\":[],\"filters\":[],\"color\":{\"title\":\"颜色\",\"type\":\"category\"," +
            "\"value\":{\"all\":\"#509af2\"},\"items\":[]},\"chartStyles\":{\"pivot\":{\"fontFamily\":\"PingFang SC\"," +
            "\"fontSize\":\"12\",\"color\":\"#666\",\"lineStyle\":\"solid\",\"lineColor\":\"#D9D9D9\"," +
            "\"headerBackgroundColor\":\"#f7f7f7\"}},\"selectedChart\":1,\"pagination\":{\"pageNo\":0," +
            "\"pageSize\":0,\"withPaging\":false,\"totalCount\":0},\"renderType\":\"clear\",\"orders\":[]," +
            "\"mode\":\"pivot\",\"model\":${model_content},\"controls\":[],\"computed\":[],\"cache\":false,\"expired\":300,\"autoLoadData\":true}";

    private static String WIDGET_CHART_CONFIG_TEMPLE = "{${context_info}\"data\":[],\"pagination\":{\"pageNo\":0," +
            "\"pageSize\":0,\"totalCount\":0,\"withPaging\":false}," +
            "\"cols\":[],\"rows\":[],\"metrics\":[],\"secondaryMetrics\":[]," +
            "\"filters\":[],\"chartStyles\":{\"pivot\":{\"fontFamily\":\"PingFangSC\"," +
            "\"fontSize\":\"12\",\"color\":\"#666\",\"lineStyle\":\"solid\"," +
            "\"lineColor\":\"#D9D9D9\",\"headerBackgroundColor\":\"#f7f7f7\"}," +
            "\"table\":{\"fontFamily\":\"PingFangSC\",\"fontSize\":\"12\"," +
            "\"color\":\"#666\",\"lineStyle\":\"solid\",\"lineColor\":\"#D9D9D9\"," +
            "\"headerBackgroundColor\":\"#f7f7f7\",\"headerConfig\":[],\"columnsConfig\":[]," +
            "\"leftFixedColumns\":[],\"rightFixedColumns\":[],\"headerFixed\":true," +
            "\"autoMergeCell\":false,\"bordered\":true,\"size\":\"small\",\"withPaging\":true," +
            "\"pageSize\":\"5000\",\"withNoAggregators\":false}},\"selectedChart\":1," +
            "\"orders\":[],\"mode\":\"chart\",\"model\":${model_content},\"controls\":[],\"computed\":[]," +
            "\"cache\":false,\"expired\":300,\"autoLoadData\":true,\"query\":null}";

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String migratedOldTime = "2000-05-26 18:34:01";

    @Autowired
    private ViewMapper viewMapper;

    @Autowired
    private WidgetMapper widgetMapper;

    @Autowired
    private DisplayMapper displayMapper;

    @Autowired
    private DashboardMapper dashboardMapper;

    @Autowired
    private ViewService viewService;

    @Autowired
    private VirtualViewQueryService virtualViewQueryService;


    @Autowired
    private WidgetService widgetService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    WidgetResultService widgetResultService;

    @SuppressWarnings("unchecked")
    @MethodLog
    @RequestMapping(path = "rename", method = RequestMethod.POST)
    public Message rename(HttpServletRequest req, @RequestBody Map<String, Object> json) throws Exception {
        String userName = SecurityFilter.getLoginUsername(req);
        Long widgetId = ((Integer) json.getOrDefault("id", -1)).longValue();
        String widgetName = ((String) json.getOrDefault("name", ""));

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

        Message message = Message.ok();
        return message;
    }

    // 工作流创建widget时调用Step 2
    @MethodLog
    @RequestMapping(path = "setcontext", method = RequestMethod.POST)
    public Message setcontext(HttpServletRequest req, @RequestBody Map<String, Object> json) throws Exception {
        Long widgetId = ((Integer) json.getOrDefault("id", -1)).longValue();
        String contextId = ((String) json.getOrDefault(CSCommonUtils.CONTEXT_ID_STR, ""));
        try {
            widgetResultService.updateContextId(widgetId, contextId);
        } catch (ErrorException e) {
            throw e;
        }
        return Message.ok();
    }
    // 工作流创建widget时调用Step 1
    @MethodLog
    @RequestMapping(path = "smartcreate", method = RequestMethod.POST)
    public Message smartCreateFromSql(HttpServletRequest req, @RequestBody Map<String, Object> json) throws ErrorException {
        String userName = SecurityFilter.getLoginUsername(req);
        User user = userMapper.selectByUsername(userName);

        String widgetName = ((String) json.getOrDefault("widgetName", ""));
        String viewName = ((String) json.getOrDefault("viewName", ""));
        String viewSql = ((String) json.getOrDefault("viewSql", ""));
        Long viewId = ((Integer) json.getOrDefault("viewId", -1)).longValue();
        Long projectId = ((Integer) json.getOrDefault("projectId", -1)).longValue();
        String nodeName = ((String) json.getOrDefault(CSCommonUtils.NODE_NAME_STR, ""));
        String contextId = ((String) json.getOrDefault(CSCommonUtils.CONTEXT_ID_STR, ""));
        String encodedContextId = QueryUtils.encodeContextId(contextId);
        String description = (String) json.getOrDefault("description", "");


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
                widgetConfig = StringUtils.replace(WIDGET_CHART_CONFIG_TEMPLE, "${model_content}", "\"\"");
                widgetConfig = StringUtils.replace(widgetConfig, "${context_info}", contextInfo);
            } else {
                VirtualView virtualView = QueryUtils.getExactFromContext(encodedContextId, nodeName);
                if (virtualView == null) {
                    throw new ServerException("节点" + nodeName + "不是上游节点，或没有产生查询结果集。");
                }
                String contextInfo = "\"contextId\":\"" + "\", \"nodeName\":\"" + widgetName + "\",\"refNodeName\":\"" + nodeName + "\",";
                widgetConfig = StringUtils.replace(WIDGET_CHART_CONFIG_TEMPLE, "${model_content}", "\"\"");
                widgetConfig = StringUtils.replace(widgetConfig, "${context_info}", contextInfo);
            }
        } else {
            View view = viewMapper.getById(viewId);
            widgetCreate.setViewId(viewId);
            widgetConfig = StringUtils.replace(WIDGET_CHART_CONFIG_TEMPLE, "${model_content}", view.getModel());
            widgetConfig = StringUtils.replace(widgetConfig, "${context_info}", "");
        }
        widgetCreate.setConfig(widgetConfig);
        Widget newWidget = widgetService.createWidget(widgetCreate, user);

        Message message = Message.ok();
        message.data("widgetId", newWidget.getId());
        message.data("widgetName", newWidget.getName());
        message.data("viewId", viewId);
        return message;
    }

    private Map<String, ModelItem> executeViewSql(User user, String viewSql, Long hiveSourceId) {
        ViewExecuteSql viewExecuteSql = new ViewExecuteSql();
        viewExecuteSql.setSql(viewSql);
        viewExecuteSql.setSourceId(hiveSourceId);
        PaginateWithQueryColumns paginateWithQueryColumns = viewService.executeSql(viewExecuteSql, user);

        Map<String, ModelItem> modelMap = Maps.newHashMap();
        for (QueryColumn queryColumn : paginateWithQueryColumns.getColumns()) {
            String visualType = ResultHelper.toVisualType(queryColumn.getType());
            String modelType = "number".equals(visualType) ? "value" : "category";
            ModelItem modelItem = new ModelItem(queryColumn.getType(), visualType, modelType);
            modelMap.put(queryColumn.getName(), modelItem);
        }
        return modelMap;
    }

    @MethodLog
    @RequestMapping(path = "{id}/getdata", method = RequestMethod.GET)
    public Message getWidgetData(HttpServletRequest req, @PathVariable("id") Long id) throws Exception {
        String userName = SecurityFilter.getLoginUsername(req);
        User user = userMapper.selectByUsername(userName);
        Widget widget = widgetMapper.getById(id);

        JSONObject configObject = JSONObject.parseObject(widget.getConfig());
        if (configObject.get("query") == null) {
            log.warn("querying an empty widget");
            Message message = Message.ok("This is an empty widget");
            message.data("columns", Lists.newArrayList());
            message.data("resultList", Lists.newArrayList());
            return message;
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

        Message message = Message.ok();
        message.data("columns", paginate.getColumns());
        message.data("resultList", paginate.getResultList());
        return message;
    }

    @MethodLog
    @RequestMapping(path = "{type}/{id}/metadata", method = RequestMethod.GET)
    public Message compareWithSnapshot(HttpServletRequest req, @PathVariable("type") String type, @PathVariable("id") Long id) {

        String userName = SecurityFilter.getLoginUsername(req);
        User user = userMapper.selectByUsername(userName);

        Project project = null;
        Set<Widget> widgets = Sets.newHashSet();
        if ("dashboard".equals(type)) {
            DashboardWithPortal dashboardWithPortal = dashboardMapper.getDashboardWithPortalAndProject(id);
            if (dashboardWithPortal == null) {
                Message message = Message.error("Dashboard does not exist.");
                return message;
            }
            project = dashboardWithPortal.getProject();
            widgets.addAll(widgetMapper.getByDashboard(id));
        } else if ("portal".equals(type)) {
            List<Dashboard> dashboards = dashboardMapper.getByPortalId(id);
            if (CollectionUtils.isEmpty(dashboards)) {
                Message message = Message.error("Display does not exist.");
                return message;
            }
            for (Dashboard dashboard : dashboards) {
                DashboardWithPortal dashboardWithPortal = dashboardMapper.getDashboardWithPortalAndProject(dashboard.getId());
                project = dashboardWithPortal.getProject();
                widgets.addAll(widgetMapper.getByDashboard(dashboard.getId()));
            }
        } else {
            DisplayWithProject displayWithProject = displayMapper.getDisplayWithProjectById(id);
            if (displayWithProject == null) {
                Message message = Message.error("Display does not exist.");
                return message;
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

        Message message = Message.ok();
        message.data("projectName", project.getName());
        message.data("widgetsMetaData", widgetsMetaData);
        return message;
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

}
