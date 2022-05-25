package com.webank.wedatasphere.dss.visualis.model.optmodel;

import edp.davinci.model.*;
import lombok.Data;

import java.util.List;

@Data
public class ExportedProject {
    String name;
    List<Source> sources;
    List<View> views;
    List<Widget> widgets;
    List<PlainDisplay> displays;
    List<PlainDashboardPortal> dashboardPortals;

}