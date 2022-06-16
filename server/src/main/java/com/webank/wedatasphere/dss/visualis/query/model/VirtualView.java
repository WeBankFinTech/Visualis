package com.webank.wedatasphere.dss.visualis.query.model;

import edp.davinci.model.View;
import lombok.Data;

import java.util.Map;

@Data
public class VirtualView extends View {
    VirtualSource source;
    Map<String, Object> params;
}
