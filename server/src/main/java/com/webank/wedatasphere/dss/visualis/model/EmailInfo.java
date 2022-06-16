package com.webank.wedatasphere.dss.visualis.model;

public class EmailInfo {
    private String cc;
    private String to;
    private String bcc;
    private String status;
    private String priority;
    private String alertList;
    private int alertInterval = 60;

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getAlertList() {
        return alertList;
    }

    public void setAlertList(String alertList) {
        this.alertList = alertList;
    }

    public int getAlertInterval() {
        return alertInterval;
    }

    public void setAlertInterval(int alertInterval) {
        this.alertInterval = alertInterval;
    }
}
