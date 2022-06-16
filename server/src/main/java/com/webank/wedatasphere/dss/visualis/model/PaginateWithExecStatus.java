package com.webank.wedatasphere.dss.visualis.model;

import org.apache.linkis.scheduler.queue.SchedulerEventState;
import edp.core.model.PaginateWithQueryColumns;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PaginateWithExecStatus extends PaginateWithQueryColumns {
    private String execId = "";
    private String status = SchedulerEventState.Inited().toString();
    private float progress = -1;

    public String getExecId() {
        return execId;
    }

    public void setExecId(String execId) {
        this.execId = execId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }
}
