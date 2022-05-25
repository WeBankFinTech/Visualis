package com.webank.wedatasphere.dss.visualis.model.optmodel;

import edp.davinci.model.Dashboard;
import edp.davinci.model.MemDashboardWidget;
import lombok.Data;

import java.util.List;

@Data
public class PlainDashboard {
    Dashboard dashboard;
    List<MemDashboardWidget> memDashboardWidgets;
}
