package com.webank.wedatasphere.dss.visualis.service.impl;

import com.webank.wedatasphere.dss.visualis.service.IDisplayCopyService;
import org.apache.linkis.server.Message;
import edp.davinci.dao.*;
import edp.davinci.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class DisplayCopyServiceImpl implements IDisplayCopyService {


    private static final Logger log = LoggerFactory.getLogger(DisplayCopyServiceImpl.class);

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
    public Message copyDisplay(HttpServletRequest req, Long sourceId, Long targetId) throws Exception {
        Display display = displayMapper.getById(targetId);
        displaySlideMapper.deleteByDisplayId(targetId);
        DisplaySlide displaySlide = displaySlideMapper.selectByDisplayId(sourceId).get(0);
        displaySlide.setId(null);
        displaySlide.setDisplayId(targetId);
        displaySlideMapper.insert(displaySlide);

        for (MemDisplaySlideWidget memDisplaySlideWidget : memDisplaySlideWidgetMapper.getMemWithSlideByDisplayId(sourceId)) {
            memDisplaySlideWidget.setId(null);
            memDisplaySlideWidget.setDisplaySlideId(displaySlide.getId());
            if (memDisplaySlideWidget.getWidgetId() != null) {
                memDisplaySlideWidget.setWidgetId(copyWidget(memDisplaySlideWidget.getWidgetId(), display.getProjectId()));
            }
            memDisplaySlideWidgetMapper.insert(memDisplaySlideWidget);
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
