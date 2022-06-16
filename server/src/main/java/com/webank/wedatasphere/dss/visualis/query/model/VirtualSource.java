package com.webank.wedatasphere.dss.visualis.query.model;

import edp.davinci.model.Source;
import lombok.Data;

import java.util.Map;

@Data
public class VirtualSource extends Source {

    String engineType;
    String dataSourceType;
    Map<String, String> dataSourceContent;
    String creator;

}
