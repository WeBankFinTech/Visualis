package com.webank.wedatasphere.dss.visualis.service.impl;

import com.google.common.collect.Lists;
import com.webank.wedatasphere.dss.visualis.model.optmodel.*;
import com.webank.wedatasphere.dss.visualis.service.IProjectImportService;
import org.apache.linkis.adapt.LinkisUtils;
import org.apache.linkis.bml.client.BmlClient;
import org.apache.linkis.bml.client.BmlClientFactory;
import org.apache.linkis.bml.protocol.BmlDownloadResponse;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import edp.core.exception.ServerException;
import edp.davinci.dao.*;
import edp.davinci.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ProjectImportServerImpl implements IProjectImportService {

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
    public Message importProject(HttpServletRequest req, Map<String, String> params) throws Exception {
        String userName = SecurityFilter.getLoginUsername(req);
        String resourceId = params.get("resourceId");
        String version = params.get("version");
        Long projectId = Long.parseLong(params.get("projectId"));
        String projectVersion = params.get("projectVersion");
        String flowVersion = params.get("flowVersion");
        String versionSuffix = projectVersion + "_" + flowVersion;
        BmlClient bmlClient = BmlClientFactory.createBmlClient(userName);
        BmlDownloadResponse bmlDownloadResponse = bmlClient.downloadShareResource(userName, resourceId, version);
        if (bmlDownloadResponse == null || !bmlDownloadResponse.isSuccess()) {
            throw new ServerException("cannot download exported data from BML");
        }
        try {
            String projectJson = IOUtils.toString(bmlDownloadResponse.inputStream());
            IdCatalog idCatalog = importOpt(projectJson, projectId, versionSuffix);
            Message message = Message.ok()
                    .data("widget", idCatalog.getWidget())
                    .data("dashboard", idCatalog.getDashboard())
                    .data("dashboardPortal", idCatalog.getDashboardPortal())
                    .data("view", idCatalog.getView())
                    .data("display", idCatalog.getDisplay());

            return message;
        } finally {
            IOUtils.closeQuietly(bmlDownloadResponse.inputStream());
        }
    }

    public IdCatalog importOpt(String projectJson, Long projectId, String versionSuffix) {
        ExportedProject exportedProject = LinkisUtils.gson().fromJson(projectJson, ExportedProject.class);
        IdCatalog idCatalog = new IdCatalog();

        importSource(projectId, versionSuffix, exportedProject, idCatalog);

        importView(projectId, versionSuffix, exportedProject, idCatalog);

        importWidget(projectId, versionSuffix, exportedProject, idCatalog);

        importDisplay(projectId, versionSuffix, exportedProject, idCatalog);

        importDashboard(projectId, versionSuffix, exportedProject, idCatalog);

        return idCatalog;
    }

    private void importSource(Long projectId, String versionSuffix, ExportedProject exportedProject, IdCatalog idCatalog) {
        List<Source> sources = exportedProject.getSources();
        if (sources == null) {
            return;
        }
        for(Source source: sources) {
            Long oldId = source.getId();
            source.setProjectId(projectId);
            // 查询是否存在该source
            Source sourceSelect = sourceMapper.getById(oldId);
            // 如果存在，就复用
            if(sourceSelect != null) {
                Long existingId = (Long) sourceSelect.getId();
                idCatalog.getSource().put(oldId, existingId);
            } else {
            // 如果不存在
                source.setProjectId(projectId);
                sourceMapper.insert(source);
                idCatalog.getSource().put(oldId, source.getId());
            }
        }
//        for (Source source : sources) {
//            Long oldId = source.getId();
//            source.setProjectId(projectId);
//            source.setName(updateName(source.getName(), versionSuffix));
//            Long existingId = sourceMapper.getByNameWithProjectId(source.getName(), projectId);
//            if (existingId != null) {
//                idCatalog.getSource().put(oldId, existingId);
//            } else {
//                sourceMapper.insert(source);
//                idCatalog.getSource().put(oldId, source.getId());
//            }
//        }
    }

    private void importDashboard(Long projectId, String versionSuffix, ExportedProject exportedProject, IdCatalog idCatalog) {
        List<PlainDashboardPortal> dashboardPortals = exportedProject.getDashboardPortals();
        if (dashboardPortals == null) {
            return;
        }
        for (PlainDashboardPortal plainDashboardPortal : dashboardPortals) {
            DashboardPortal dashboardPortal = plainDashboardPortal.getDashboardPortal();
            Long oldPortalId = dashboardPortal.getId();
            dashboardPortal.setProjectId(projectId);
            dashboardPortal.setName(updateName(dashboardPortal.getName(), versionSuffix));

            // 导入dashboardPortal，判断是否存在已有同名Portal
            Long existingPortalId = dashboardPortalMapper.getByNameWithProjectId(dashboardPortal.getName(), projectId);
            if (existingPortalId != null) {
                // 存在portalId
                dashboardPortal.setId(existingPortalId);
                dashboardMapper.deleteByPortalId(dashboardPortal.getId());
                memDashboardWidgetMapper.deleteByPortalId(dashboardPortal.getId());
                idCatalog.getDashboardPortal().put(oldPortalId, existingPortalId);
            } else {
                // 不存在portalId
                dashboardPortalMapper.insert(dashboardPortal);
                // 这个id是否是新的portalId，
                idCatalog.getDashboardPortal().put(oldPortalId, dashboardPortal.getId());
            }
            //导入dashboard
            for (PlainDashboard plainDashboard : plainDashboardPortal.getDashboards()) {
                Dashboard dashboard = plainDashboard.getDashboard();
                Long oldDashboardId = dashboard.getId();
                dashboard.setDashboardPortalId(dashboardPortal.getId());

                dashboardMapper.insert(dashboard);
                idCatalog.getDashboard().put(oldDashboardId, dashboard.getId());
                //导入dashboard与widget关系
                for (MemDashboardWidget memDashboardWidget : plainDashboard.getMemDashboardWidgets()) {
                    Long oldMemId = memDashboardWidget.getId();
                    memDashboardWidget.setDashboardId(dashboard.getId());
                    memDashboardWidget.setWidgetId(idCatalog.getWidget().get(memDashboardWidget.getWidgetId()));
                    memDashboardWidgetMapper.insert(memDashboardWidget);
                    idCatalog.getMemDashboardWidget().put(oldMemId, memDashboardWidget.getId());
                }
            }
            //是否存在parentId
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
    }

    private void importDisplay(Long projectId, String versionSuffix, ExportedProject exportedProject, IdCatalog idCatalog) {
        List<PlainDisplay> displays = exportedProject.getDisplays();
        if (displays == null) {
            return;
        }
        for (PlainDisplay plainDisplay : displays) {
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
    }

    private void importWidget(Long projectId, String versionSuffix, ExportedProject exportedProject, IdCatalog idCatalog) {
        List<Widget> widgets = exportedProject.getWidgets();
        if (widgets == null) {
            return;
        }
        for (Widget widget : widgets) {
            Long oldId = widget.getId();
            widget.setProjectId(projectId);
            widget.setName(updateName(widget.getName(), versionSuffix));
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

    private void importView(Long projectId, String versionSuffix, ExportedProject exportedProject, IdCatalog idCatalog) {
        List<View> views = exportedProject.getViews();
        if (views == null) {
            return;
        }
        for (View view : views) {
            Long oldId = view.getId();
            view.setProjectId(projectId);
            view.setName(updateName(view.getName(), versionSuffix));
            if (idCatalog.getSource().get(view.getSourceId()) != null) {
                view.setSourceId(idCatalog.getSource().get(view.getSourceId()));
            }
            Long existingId = viewMapper.getByNameWithProjectId(view.getName(), projectId);
            if (existingId != null) {
                idCatalog.getView().put(oldId, existingId);
            } else {
                viewMapper.insert(view);
                idCatalog.getView().put(oldId, view.getId());
            }
        }
    }

    public static String updateName(String name, String versionSuffix) {
        if (StringUtils.isBlank(versionSuffix)) {
            return name;
        }
        String shortName = getShortName(name);
        return shortName + "_" + versionSuffix;
    }

    private static String getShortName(String longName) {
        String shortName;
        String version = getSuffixVersion(longName);
        if (null == version) {
            shortName = longName;
        } else {
            shortName = longName.substring(0, longName.length() - version.length() - 1);
        }
        return shortName;
    }

    private static String getSuffixVersion(String longName) {
        String version;
        Pattern suffixVersionPattern = Pattern.compile("[v]\\d_[v][0-9]{6}");
        Matcher matcherVersionPattern = suffixVersionPattern.matcher(longName);
        if (matcherVersionPattern.find()) {
            version = matcherVersionPattern.group();
        } else {
            version = null;
        }
        return version;
    }

}
