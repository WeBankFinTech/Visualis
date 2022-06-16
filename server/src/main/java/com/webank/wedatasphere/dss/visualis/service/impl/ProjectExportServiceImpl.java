package com.webank.wedatasphere.dss.visualis.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.webank.wedatasphere.dss.visualis.enums.ModuleEnum;
import com.webank.wedatasphere.dss.visualis.service.IProjectExportService;
import com.webank.wedatasphere.dss.visualis.model.optmodel.ExportedProject;
import com.webank.wedatasphere.dss.visualis.model.optmodel.PlainDashboard;
import com.webank.wedatasphere.dss.visualis.model.optmodel.PlainDashboardPortal;
import com.webank.wedatasphere.dss.visualis.model.optmodel.PlainDisplay;
import org.apache.linkis.adapt.LinkisUtils;
import org.apache.linkis.bml.client.BmlClient;
import org.apache.linkis.bml.client.BmlClientFactory;
import org.apache.linkis.bml.protocol.BmlUploadResponse;
import org.apache.linkis.server.Message;
import edp.core.exception.ServerException;
import edp.davinci.dao.*;
import edp.davinci.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProjectExportServiceImpl implements IProjectExportService {

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
    public Message exportProject(Map<String, String> params, String userName) throws Exception {
        ExportedProject exportedProject = null;
        Long projectId = Long.parseLong(params.get("projectId"));
        Boolean partial = Boolean.parseBoolean(params.get("partial"));
        Map<String, Set<Long>> moduleIdsMap = getModuleIdsMap(params);

        log.info("export project, user: {}, project: {}, partial:{}", userName, projectId, partial);
        exportedProject = export(projectId, moduleIdsMap, partial);
        String exported = LinkisUtils.gson().toJson(exportedProject);

        BmlClient bmlClient = BmlClientFactory.createBmlClient(userName);
        BmlUploadResponse bmlUploadResponse = bmlClient.uploadShareResource(userName, exportedProject.getName(),
                "visualis_exported_" + UUID.randomUUID(), new ByteArrayInputStream(exported.getBytes(StandardCharsets.UTF_8)));

        if (bmlUploadResponse == null || !bmlUploadResponse.isSuccess()) {
            throw new ServerException("cannot upload exported data to BML");
        }

        log.info("{} is exporting the project, uploaded to BML the resourceID is {} and the version is {}",
                userName, bmlUploadResponse.resourceId(), bmlUploadResponse.version());

        Message message = Message.ok()
                .data("resourceId", bmlUploadResponse.resourceId())
                .data("version", bmlUploadResponse.version());
        return message;
    }

    /**
     * 获取需要导出对象集合
     *
     * @param params
     * @return
     */
    public Map<String, Set<Long>> getModuleIdsMap(Map<String, String> params) {

        Map<String, Set<Long>> map = Maps.newHashMap();
        String widgetIdsStr = params.get(ModuleEnum.WIDGET_IDS.getName());
        String displayIdsStr = params.get(ModuleEnum.DISPLAY_IDS.getName());
        String dashboardPortalIdsStr = params.get(ModuleEnum.DASHBOARD_PORTAL_IDS.getName());
        String viewIdsStr = params.get(ModuleEnum.VIEW_IDS.getName());

        Set<Long> widgetIds = Sets.newHashSet();
        Set<Long> displayIds = Sets.newHashSet();
        Set<Long> dashboardPortalIds = Sets.newHashSet();
        Set<Long> viewIds = Sets.newHashSet();

        if (StringUtils.isNotEmpty(widgetIdsStr)) {
            widgetIds = Arrays.stream(StringUtils.split(widgetIdsStr, ","))
                    .map(Long::parseLong).collect(Collectors.toSet());
        }
        if (StringUtils.isNotEmpty(displayIdsStr)) {
            displayIds = Arrays.stream(StringUtils.split(displayIdsStr, ","))
                    .map(Long::parseLong).collect(Collectors.toSet());
        }
        if (StringUtils.isNotEmpty(dashboardPortalIdsStr)) {
            dashboardPortalIds = Arrays.stream(StringUtils.split(dashboardPortalIdsStr, ","))
                    .map(Long::parseLong).collect(Collectors.toSet());
        }
        if (StringUtils.isNotEmpty(viewIdsStr)) {
            viewIds = Arrays.stream(StringUtils.split(viewIdsStr, ","))
                    .map(Long::parseLong).collect(Collectors.toSet());
        }
        map.put(ModuleEnum.WIDGET_IDS.getName(), widgetIds);
        map.put(ModuleEnum.DISPLAY_IDS.getName(), displayIds);
        map.put(ModuleEnum.DASHBOARD_PORTAL_IDS.getName(), dashboardPortalIds);
        map.put(ModuleEnum.VIEW_IDS.getName(), viewIds);
        log.info("The objects to be exported are: {}", map);
        return map;
    }


    @Override
    public Long getProjectId(Map<String, Set<Long>> moduleIdsMap) {
        Set<Long> widgets = moduleIdsMap.get(ModuleEnum.WIDGET_IDS.getName());
        Set<Long> displays = moduleIdsMap.get(ModuleEnum.DISPLAY_IDS.getName());
        Set<Long> dashboards = moduleIdsMap.get(ModuleEnum.DASHBOARD_PORTAL_IDS.getName());
        Set<Long> views = moduleIdsMap.get(ModuleEnum.VIEW_IDS.getName());
        if (!widgets.isEmpty()) {
            return projectMapper.getProjectIdByWidgetId(widgets.iterator().next());
        } else if (!displays.isEmpty()) {
            return projectMapper.getProjectByDisplayId(displays.iterator().next());
        } else if (!dashboards.isEmpty()) {
            return projectMapper.getProjectIdByDashboardId(dashboards.iterator().next());
        } else if (!views.isEmpty()) {
            return projectMapper.getProjectIdByViewId(views.iterator().next());
        } else {
            log.error("widgets displays dashboards are all empty");
            return -1L;
        }
    }


    @Override
    public ExportedProject export(Long projectId, Map<String, Set<Long>> moduleIdsMap, boolean partial) {
        ExportedProject exportedProject = new ExportedProject();
        Project project = projectMapper.getById(projectId);
        log.info("execute export method! export project is {}.", project.getName());
        exportedProject.setName(project.getName());

        setDisplays(projectId, moduleIdsMap, partial, exportedProject);

        setDashboardPortals(projectId, moduleIdsMap, partial, exportedProject);

        setWidgets(projectId, moduleIdsMap, partial, exportedProject);

        // 新增view导出功能
        setViews(projectId, moduleIdsMap, partial, exportedProject);

        return exportedProject;
    }

    private void setViews(Long projectId, Map<String, Set<Long>> moduleIdsMap, boolean partial, ExportedProject exportedProject) {
        if (partial) {
            Set<Long> longs = moduleIdsMap.get(ModuleEnum.VIEW_IDS.getName());
            if (longs.size() > 0) {
                exportedProject.setViews(viewMapper.getByIds(longs));
                Set<Long> sourceIds = exportedProject.getViews().stream().map(View::getSourceId).collect(Collectors.toSet());
                List<Source> sources = sourceMapper.getByProject(projectId).stream().filter(s -> sourceIds.contains(s.getId())).collect(Collectors.toList());
                exportedProject.setSources(sources);
            }
        } else {
            exportedProject.setSources(sourceMapper.getByProject(projectId));
            List<View> exportedViews = Lists.newArrayList();
            for (Source source : exportedProject.getSources()) {
                exportedViews.addAll(viewMapper.getBySourceId(source.getId()));
            }
            exportedProject.setViews(exportedViews);
        }
        log.info("exporting project, export views: {}", exportedProject);
    }

    private void setWidgets(Long projectId, Map<String, Set<Long>> moduleIdsMap, boolean partial, ExportedProject exportedProject) {
        if (partial) {
            Set<Long> longs = moduleIdsMap.get(ModuleEnum.WIDGET_IDS.getName());
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


    private void setDashboardPortals(Long projectId, Map<String, Set<Long>> moduleIdsMap, boolean partial, ExportedProject exportedProject) {
        List<PlainDashboardPortal> exportedDashboardPortals = Lists.newArrayList();
        List<DashboardPortal> dashboardPortals = Lists.newArrayList();
        if (partial) {
            Set<Long> idsSet = moduleIdsMap.get(ModuleEnum.DASHBOARD_PORTAL_IDS.getName());
            if (idsSet.size() > 0) {
                idsSet.stream().map(dashboardPortalMapper::getById).forEach(dashboardPortals::add);
            }

        } else {
            dashboardPortals = dashboardPortalMapper.getByProject(projectId);
        }
        for (DashboardPortal dashboardPortal : dashboardPortals) {
            PlainDashboardPortal plainDashboardPortal = new PlainDashboardPortal();
            List<PlainDashboard> exportedDashboards = Lists.newArrayList();
            List<Dashboard> dashboards = dashboardMapper.getByPortalId(dashboardPortal.getId());
            for (Dashboard dashboard : dashboards) {
                PlainDashboard exportedDashboard = new PlainDashboard();
                exportedDashboard.setDashboard(dashboard);
                List<MemDashboardWidget> memDashboardWidgets = memDashboardWidgetMapper.getByDashboardId(dashboard.getId());
                memDashboardWidgets.forEach(m -> moduleIdsMap.get(ModuleEnum.WIDGET_IDS.getName()).add(m.getWidgetId()));
                exportedDashboard.setMemDashboardWidgets(memDashboardWidgets);
                exportedDashboards.add(exportedDashboard);
            }
            plainDashboardPortal.setDashboardPortal(dashboardPortal);
            plainDashboardPortal.setDashboards(exportedDashboards);
            exportedDashboardPortals.add(plainDashboardPortal);
        }
        exportedProject.setDashboardPortals(exportedDashboardPortals);
        log.info("exporting project, export dashboardPortals: {}", exportedProject);
    }

    private void setDisplays(Long projectId, Map<String, Set<Long>> moduleIdsMap, boolean partial, ExportedProject exportedProject) {
        List<PlainDisplay> exportedDisplays = Lists.newArrayList();
        List<Display> displays = Lists.newArrayList();
        if (partial) {
            Set<Long> idsSet = moduleIdsMap.get(ModuleEnum.DISPLAY_IDS.getName());
            if (idsSet.size() > 0) {
                idsSet.stream().map(displayMapper::getById).forEach(displays::add);
            }
        } else {
            displays = displayMapper.getByProject(projectId);
        }
        for (Display display : displays) {
            PlainDisplay plainDisplay = new PlainDisplay();
            plainDisplay.setDisplay(display);
            plainDisplay.setDisplaySlide(displaySlideMapper.selectByDisplayId(display.getId()).get(0));
            List<MemDisplaySlideWidget> memDisplaySlideWidgets = memDisplaySlideWidgetMapper.getMemDisplaySlideWidgetListBySlideId(plainDisplay.getDisplaySlide().getId());
            memDisplaySlideWidgets.forEach(m -> moduleIdsMap.get(ModuleEnum.WIDGET_IDS.getName()).add(m.getWidgetId()));///
            plainDisplay.setMemDisplaySlideWidgets(memDisplaySlideWidgets);
            exportedDisplays.add(plainDisplay);
        }
        log.info("exporting project, export displays: {}", exportedProject);
        exportedProject.setDisplays(exportedDisplays);
    }

}
