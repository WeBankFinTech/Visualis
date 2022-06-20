package com.webank.wedatasphere.dss.visualis.service.impl;

import com.google.common.collect.Lists;
import com.webank.wedatasphere.dss.visualis.service.DssDashboradService;
import com.webank.wedatasphere.dss.visualis.service.Utils;
import com.webank.wedatasphere.dss.visualis.model.optmodel.ExportedProject;
import com.webank.wedatasphere.dss.visualis.model.optmodel.IdCatalog;
import com.webank.wedatasphere.dss.visualis.model.optmodel.PlainDashboard;
import com.webank.wedatasphere.dss.visualis.model.optmodel.PlainDashboardPortal;
import com.webank.wedatasphere.dss.visualis.utils.StringConstant;
import edp.davinci.dao.DashboardMapper;
import edp.davinci.dao.DashboardPortalMapper;
import edp.davinci.dao.MemDashboardWidgetMapper;
import edp.davinci.model.Dashboard;
import edp.davinci.model.DashboardPortal;
import edp.davinci.model.MemDashboardWidget;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service("dssDashboradService")
public class DashboardServiceImpl implements DssDashboradService {

    @Autowired
    private DashboardMapper dashboardMapper;

    @Autowired
    private DashboardPortalMapper dashboardPortalMapper;

    @Autowired
    private MemDashboardWidgetMapper memDashboardWidgetMapper;

    @Override
    public void exportDashboardPortals(Long projectId, Map<String, Set<Long>> moduleIdsMap, boolean partial, ExportedProject exportedProject) {
        List<PlainDashboardPortal> exportedDashboardPortals = Lists.newArrayList();
        List<DashboardPortal> dashboardPortals = Lists.newArrayList();
        if (partial) {
            Set<Long> idsSet = moduleIdsMap.get(StringConstant.DASHBOARD_PORTAL_IDS);
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
                memDashboardWidgets.forEach(m -> moduleIdsMap.get(StringConstant.WIDGET_IDS).add(m.getWidgetId()));
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

    @Override
    public void importDashboard(Long projectId, String versionSuffix, ExportedProject exportedProject, IdCatalog idCatalog) {
        List<PlainDashboardPortal> dashboardPortals = exportedProject.getDashboardPortals();
        if (dashboardPortals == null) {
            return;
        }
        for (PlainDashboardPortal plainDashboardPortal : dashboardPortals) {
            DashboardPortal dashboardPortal = plainDashboardPortal.getDashboardPortal();
            Long oldPortalId = dashboardPortal.getId();
            dashboardPortal.setProjectId(projectId);
            dashboardPortal.setName(Utils.updateName(dashboardPortal.getName(), versionSuffix));

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
                    for (String old : dashboard.getFullParentId().split(StringConstant.COMMA)) {
                        ids.add(idCatalog.getDashboard().get(Long.parseLong(old)));
                    }
                    dashboard.setFullParentId(StringUtils.join(ids, StringConstant.COMMA));
                }
                dashboardMapper.update(dashboard);
            }
        }
    }

    @Override
    public void copyDashboardPortal(Map<String, Set<Long>> moduleIdsMap, ExportedProject exportedProject) {
        Set<Long> dashboardPortalIds = moduleIdsMap.get(StringConstant.DASHBOARD_PORTAL_IDS);
        if (!dashboardPortalIds.isEmpty()) {
            PlainDashboardPortal plainDashboardPortal = exportedProject.getDashboardPortals().get(0);
            exportedProject.setDashboardPortals(Lists.newArrayList(plainDashboardPortal));
        }
    }


}
