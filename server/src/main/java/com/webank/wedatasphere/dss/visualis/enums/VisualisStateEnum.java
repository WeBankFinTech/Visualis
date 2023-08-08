package com.webank.wedatasphere.dss.visualis.enums;

public enum VisualisStateEnum {

    INITED("Inited", "Inited"),

    TIMEOUT("Timeout", "Timeout"),

    CANCELLED("Cancelled", "Cancelled"),

    SCHEDULED("Scheduled", "Scheduled"),

    RUNNING("Running", "Running"),

    SUCCESS("Succeed", "Succeed"),

    FAILED("Failed", "Failed");

    private String name;
    private String value;

    VisualisStateEnum(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public String getName() {
        return this.name;
    }
}
