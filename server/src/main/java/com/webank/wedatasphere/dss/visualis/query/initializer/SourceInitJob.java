package com.webank.wedatasphere.dss.visualis.query.initializer;

import lombok.Data;

@Data
public class SourceInitJob {

    String scriptContent;
    String scriptType;

    public SourceInitJob(String scriptContent, String scriptType) {
        this.scriptContent = scriptContent;
        this.scriptType = scriptType;
    }

}
