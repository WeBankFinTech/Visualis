package com.webank.wedatasphere.dss.visualis.model.optmodel;

import com.google.common.collect.Maps;

import java.util.Map;

public class IdCatalog {
    private Map<Long, Long> source = Maps.newHashMap();
    private Map<Long, Long> view = Maps.newHashMap();
    private Map<Long, Long> widget = Maps.newHashMap();
    private Map<Long, Long> display = Maps.newHashMap();
    private Map<Long, Long> displaySlide = Maps.newHashMap();
    private Map<Long, Long> memDisplaySlideWidget = Maps.newHashMap();
    private Map<Long, Long> dashboardPortal = Maps.newHashMap();
    private Map<Long, Long> dashboard = Maps.newHashMap();
    private Map<Long, Long> memDashboardWidget = Maps.newHashMap();

    public Map<Long, Long> getSource() {
        return source;
    }

    public void setSource(Map<Long, Long> source) {
        this.source = source;
    }

    public Map<Long, Long> getView() {
        return view;
    }

    public void setView(Map<Long, Long> view) {
        this.view = view;
    }

    public Map<Long, Long> getWidget() {
        return widget;
    }

    public void setWidget(Map<Long, Long> widget) {
        this.widget = widget;
    }

    public Map<Long, Long> getDisplay() {
        return display;
    }

    public void setDisplay(Map<Long, Long> display) {
        this.display = display;
    }

    public Map<Long, Long> getDisplaySlide() {
        return displaySlide;
    }

    public void setDisplaySlide(Map<Long, Long> displaySlide) {
        this.displaySlide = displaySlide;
    }

    public Map<Long, Long> getMemDisplaySlideWidget() {
        return memDisplaySlideWidget;
    }

    public void setMemDisplaySlideWidget(Map<Long, Long> memDisplaySlideWidget) {
        this.memDisplaySlideWidget = memDisplaySlideWidget;
    }

    public Map<Long, Long> getDashboardPortal() {
        return dashboardPortal;
    }

    public void setDashboardPortal(Map<Long, Long> dashboardPortal) {
        this.dashboardPortal = dashboardPortal;
    }

    public Map<Long, Long> getDashboard() {
        return dashboard;
    }

    public void setDashboard(Map<Long, Long> dashboard) {
        this.dashboard = dashboard;
    }

    public Map<Long, Long> getMemDashboardWidget() {
        return memDashboardWidget;
    }

    public void setMemDashboardWidget(Map<Long, Long> memDashboardWidget) {
        this.memDashboardWidget = memDashboardWidget;
    }
}
