package com.webank.wedatasphere.dss.visualis.enums;

import java.util.Arrays;

public enum ModuleEnum {

    DASHBOARD_PORTAL_IDS("dashboardPortalIds", "DASHBOARD ids"),

    DISPLAY_IDS("displayIds", "DISPLAY ids"),

    WIDGET_IDS("widgetIds", "WIDGET ids"),

    VIEW_IDS("viewIds", "VIEW ids");

    private String name;
    private String desc;

    ModuleEnum(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public static ModuleEnum getEnum(String name) {
        return Arrays.stream(ModuleEnum.values()).filter(e -> e.getName().equals(name)).findFirst().orElseThrow(NullPointerException::new);
    }

    public String getName() {
        return name;
    }
}
