package com.webank.wedatasphere.dss.visualis.model.optmodel;

import edp.davinci.model.DashboardPortal;
import lombok.Data;

import java.util.List;

@Data
public class PlainDashboardPortal {
    DashboardPortal dashboardPortal;
    List<PlainDashboard> dashboards;
}
