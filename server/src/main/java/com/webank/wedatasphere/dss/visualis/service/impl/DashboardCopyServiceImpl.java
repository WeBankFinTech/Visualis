package com.webank.wedatasphere.dss.visualis.service.impl;

import com.google.common.collect.Lists;
import com.webank.wedatasphere.dss.visualis.model.optmodel.IdCatalog;
import com.webank.wedatasphere.dss.visualis.service.IDashboardCopyService;
import org.apache.linkis.server.Message;
import edp.davinci.dao.*;
import edp.davinci.model.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class DashboardCopyServiceImpl implements IDashboardCopyService {

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
    public Message copyDashboard(HttpServletRequest req, Long sourceId, Long targetId) throws Exception {
        DashboardPortal dashboardPortal = dashboardPortalMapper.getById(targetId);
        IdCatalog idCatalog = new IdCatalog();
        dashboardMapper.deleteByPortalId(targetId);
        for (Dashboard dashboard : dashboardMapper.getByPortalId(sourceId)) {
            Long originalId = dashboard.getId();
            dashboard.setId(null);
            dashboard.setDashboardPortalId(targetId);
            dashboardMapper.insert(dashboard);
            idCatalog.getDashboard().put(originalId, dashboard.getId());
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

            for (MemDashboardWidget memDashboardWidget : memDashboardWidgetMapper.getByDashboardId(originalId)) {
                memDashboardWidget.setId(null);
                memDashboardWidget.setDashboardId(dashboard.getId());
                memDashboardWidget.setWidgetId(copyWidget(memDashboardWidget.getWidgetId(), dashboardPortal.getProjectId()));
                memDashboardWidgetMapper.insert(memDashboardWidget);
            }
        }
        Message message = Message.ok();
        return message;
    }


    private Long copyWidget(Long widgetId, Long projectId) {
        Widget widget = widgetMapper.getById(widgetId);
        View view = viewMapper.getById(widget.getViewId());
        Source source = sourceMapper.getById(view.getSourceId());
        Long existingSource = sourceMapper.getByNameWithProjectId(source.getName(), projectId);
        if (existingSource == null) {
            source.setId(null);
            source.setProjectId(projectId);
            sourceMapper.insert(source);
            existingSource = source.getId();
        }
        Long existingView = viewMapper.getByNameWithProjectId(view.getName(), projectId);
        if (existingView == null) {
            view.setId(null);
            view.setSourceId(existingSource);
            view.setProjectId(projectId);
            viewMapper.insert(view);
            existingView = view.getId();
        }
        Long existingWidget = widgetMapper.getByNameWithProjectId(widget.getName(), projectId);
        if (existingWidget == null) {
            widget.setId(null);
            widget.setViewId(existingView);
            widget.setProjectId(projectId);
            widgetMapper.insert(widget);
            existingWidget = widget.getId();
        }
        return existingWidget;
    }


}
