package com.webank.wedatasphere.dss.visualis.service.impl;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.webank.wedatasphere.dss.visualis.configuration.CommonConfig;
import com.webank.wedatasphere.dss.visualis.model.optmodel.*;
import com.webank.wedatasphere.dss.visualis.service.IProjectReaderService;
import com.webank.wedatasphere.dss.visualis.utils.VisualisUtils;
import com.webank.wedatasphere.dss.visualis.utils.export.WidgetMigration;
import org.apache.linkis.adapt.LinkisUtils;
import org.apache.linkis.server.Message;
import edp.davinci.dao.*;
import edp.davinci.model.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class ProjectReaderServiceImpl implements IProjectReaderService {

    private static final Logger log = LoggerFactory.getLogger(ProjectReaderServiceImpl.class);


    @Autowired
    private ViewMapper viewMapper;

    @Autowired
    private WidgetMapper widgetMapper;

    @Autowired
    private DisplayMapper displayMapper;

    @Autowired
    private DisplaySlideMapper displaySlideMapper;

    @Autowired
    private MemDisplaySlideWidgetMapper memDisplaySlideWidgetMapper;

    @Autowired
    private DashboardMapper dashboardMapper;

    @Autowired
    private DashboardPortalMapper dashboardPortalMapper;

    @Autowired
    private MemDashboardWidgetMapper memDashboardWidgetMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private SourceMapper sourceMapper;

    @Override
    public Message read(HttpServletRequest req, String fileName, Long projectId) throws Exception {
        String versionSuffix = "";
        String projectJson = FileUtils.readFileToString(new File(CommonConfig.EXPORT_PROJECT_DIR().getValue() + fileName));
        IdCatalog idCatalog = importProject(projectJson, projectId, versionSuffix);
        Message message = Message.ok()
                .data("widget", idCatalog.getWidget())
                .data("dashboard", idCatalog.getDashboard())
                .data("dashboardPortal", idCatalog.getDashboardPortal())
                .data("view", idCatalog.getView())
                .data("display", idCatalog.getDisplay());
        return message;
    }

    public IdCatalog importProject(String projectJson, Long projectId, String versionSuffix) throws Exception {
        ExportedProject exportedProject = LinkisUtils.gson().fromJson(projectJson, ExportedProject.class);
        IdCatalog idCatalog = new IdCatalog();
        for (Source source : exportedProject.getSources()) {
            Long oldId = source.getId();
            source.setProjectId(projectId);
            source.setName(updateName(source.getName(), versionSuffix));
            Long existingId = sourceMapper.getByNameWithProjectId(source.getName(), projectId);
            if (existingId != null) {
                idCatalog.getSource().put(oldId, existingId);
            } else {
                sourceMapper.insert(source);
                idCatalog.getSource().put(oldId, source.getId());
            }
        }
        for (View view : exportedProject.getViews()) {
            Long oldId = view.getId();
            view.setProjectId(projectId);
            view.setName(updateName(view.getName(), versionSuffix));
            String variables = StringUtils.substringBefore(view.getSql(), "{");
            view.setSql(StringUtils.substringBeforeLast(StringUtils.substringAfter(view.getSql(), "{"), "}"));
            if (StringUtils.isNotBlank(variables)) {
                JsonArray variableArray = new JsonArray();
                String[] variableTokens = variables.split("--@team");
                for (String variableToken : variableTokens) {
                    if (StringUtils.isNotBlank(variableToken)) {
                        String[] exp = variableToken.split("=");
                        JsonObject variableObj = new JsonObject();
                        variableObj.addProperty("name", StringUtils.substringBefore(variableToken, "=").trim());
                        variableObj.addProperty("alias", "");
                        variableObj.addProperty("type", "query");
                        variableObj.addProperty("valueType", "string");
                        variableObj.addProperty("udf", false);
                        JsonArray defaultValues = new JsonArray();
                        String[] values = StringUtils.substringsBetween(StringUtils.substringAfter(variableToken, "="), "'", "'");
                        for (String value : values) {
                            defaultValues.add(value);
                        }
                        variableObj.add("defaultValues", defaultValues);
                        variableObj.addProperty("key", Integer.toString(new Random().nextInt(100000)));
                        variableArray.add(variableObj);
                    }
                }
                view.setVariable(LinkisUtils.gson().toJson(variableArray));
            }
            JsonObject model = LinkisUtils.gson().fromJson(view.getModel(), JsonObject.class);
            for (Map.Entry<String, JsonElement> modelItem : model.entrySet()) {
                JsonObject newItem = modelItem.getValue().getAsJsonObject().deepCopy();
                if (modelItem.getValue().getAsJsonObject().get("sqlType") == null) {
                    newItem.addProperty("sqlType", "STRING");
                } else {
                    newItem.addProperty("sqlType", modelItem.getValue().getAsJsonObject().get("sqlType").getAsString().toUpperCase());
                }
                model.add(modelItem.getKey(), newItem);
            }
            view.setModel(LinkisUtils.gson().toJson(model));
            if (idCatalog.getSource().get(view.getSourceId()) != null) {
                view.setSourceId(idCatalog.getSource().get(view.getSourceId()));
            } else {
                Source hiveSource = sourceMapper.getById(VisualisUtils.getHiveDataSourceId());
                Long projectHiveSourceId = sourceMapper.getByNameWithProjectId(hiveSource.getName(), projectId);
                if (projectHiveSourceId == null) {
                    hiveSource.setId(null);
                    hiveSource.setProjectId(projectId);
                    sourceMapper.insert(hiveSource);
                    projectHiveSourceId = hiveSource.getId();
                }
                idCatalog.getSource().put(view.getSourceId(), projectHiveSourceId);
                view.setSourceId(projectHiveSourceId);
            }
            Long existingId = viewMapper.getByNameWithProjectId(view.getName(), projectId);
            if (existingId != null) {
                idCatalog.getView().put(oldId, existingId);
            } else {
                viewMapper.insert(view);
                idCatalog.getView().put(oldId, view.getId());
            }
        }
        for (Widget widget : exportedProject.getWidgets()) {
            Long oldId = widget.getId();
            widget.setProjectId(projectId);
            widget.setName(updateName(widget.getName(), versionSuffix));
            widget.setViewId(idCatalog.getView().get(widget.getViewId()));
            Long existingId = widgetMapper.getByNameWithProjectId(widget.getName(), projectId);
            if (existingId != null) {
                idCatalog.getWidget().put(oldId, existingId);
            } else {
                widget.setConfig(WidgetMigration.migrate(widget.getConfig(), widget.getViewId()));
                widgetMapper.insert(widget);
                idCatalog.getWidget().put(oldId, widget.getId());
            }
        }
        for (PlainDisplay plainDisplay : exportedProject.getDisplays()) {
            Display display = plainDisplay.getDisplay();
            Long oldDisplayId = display.getId();
            display.setProjectId(projectId);
            display.setName(updateName(display.getName(), versionSuffix));
            Long existingId = displayMapper.getByNameWithProjectId(display.getName(), projectId);
            if (existingId != null) {
                display.setId(existingId);
                displaySlideMapper.deleteByDisplayId(display.getId());
                memDisplaySlideWidgetMapper.deleteByDisplayId(display.getId());
                idCatalog.getDisplay().put(oldDisplayId, display.getId());
            } else {
                displayMapper.insert(display);
                idCatalog.getDisplay().put(oldDisplayId, display.getId());
            }
            DisplaySlide displaySlide = plainDisplay.getDisplaySlide();
            Long oldSlideId = displaySlide.getId();
            displaySlide.setDisplayId(display.getId());
            displaySlideMapper.insert(displaySlide);
            idCatalog.getDisplaySlide().put(oldSlideId, displaySlide.getId());
            for (MemDisplaySlideWidget memDisplaySlideWidget : plainDisplay.getMemDisplaySlideWidgets()) {
                Long oldMemId = memDisplaySlideWidget.getId();
                memDisplaySlideWidget.setDisplaySlideId(displaySlide.getId());
                memDisplaySlideWidget.setWidgetId(idCatalog.getWidget().get(memDisplaySlideWidget.getWidgetId()));
                memDisplaySlideWidgetMapper.insert(memDisplaySlideWidget);
                idCatalog.getMemDisplaySlideWidget().put(oldMemId, memDisplaySlideWidget.getId());
            }
        }
        for (PlainDashboardPortal plainDashboardPortal : exportedProject.getDashboardPortals()) {
            DashboardPortal dashboardPortal = plainDashboardPortal.getDashboardPortal();
            Long oldPortalId = dashboardPortal.getId();
            dashboardPortal.setProjectId(projectId);
            dashboardPortal.setName(updateName(dashboardPortal.getName(), versionSuffix));
            Long existingPortalId = dashboardPortalMapper.getByNameWithProjectId(dashboardPortal.getName(), projectId);
            if (existingPortalId != null) {
                dashboardPortal.setId(existingPortalId);
                dashboardMapper.deleteByPortalId(dashboardPortal.getId());
                memDashboardWidgetMapper.deleteByPortalId(dashboardPortal.getId());
                idCatalog.getDashboardPortal().put(oldPortalId, existingPortalId);
            } else {
                dashboardPortalMapper.insert(dashboardPortal);
                idCatalog.getDashboardPortal().put(oldPortalId, dashboardPortal.getId());
            }
            for (PlainDashboard plainDashboard : plainDashboardPortal.getDashboards()) {
                Dashboard dashboard = plainDashboard.getDashboard();
                Long oldDashboardId = dashboard.getId();
                dashboard.setDashboardPortalId(dashboardPortal.getId());
                dashboard.setConfig("");
                dashboardMapper.insert(dashboard);
                idCatalog.getDashboard().put(oldDashboardId, dashboard.getId());
                for (MemDashboardWidget memDashboardWidget : plainDashboard.getMemDashboardWidgets()) {
                    Long oldMemId = memDashboardWidget.getId();
                    memDashboardWidget.setDashboardId(dashboard.getId());
                    //memDashboardWidget.setConfig("");
                    memDashboardWidget.setWidgetId(idCatalog.getWidget().get(memDashboardWidget.getWidgetId()));
                    memDashboardWidgetMapper.insert(memDashboardWidget);
                    idCatalog.getMemDashboardWidget().put(oldMemId, memDashboardWidget.getId());
                }
            }
            for (PlainDashboard plainDashboard : plainDashboardPortal.getDashboards()) {
                Dashboard dashboard = plainDashboard.getDashboard();
                Long parentId = dashboard.getParentId() == 0 ? 0L : idCatalog.getDashboard().get(dashboard.getParentId());
                dashboard.setParentId(parentId);
                if (StringUtils.isNotBlank(dashboard.getFullParentId())) {
                    List<Long> ids = Lists.newArrayList();
                    for (String old : dashboard.getFullParentId().split(",")) {
                        ids.add(idCatalog.getDashboard().get(Long.parseLong(old)));
                    }
                    dashboard.setFullParentId(StringUtils.join(ids, ","));
                }
                dashboardMapper.update(dashboard);
            }
        }
        return idCatalog;
    }

    private String updateName(String name, String versionSuffix) {
        if (StringUtils.isBlank(versionSuffix)) {
            return name;
        }
        return name + "_" + versionSuffix;
    }

}
