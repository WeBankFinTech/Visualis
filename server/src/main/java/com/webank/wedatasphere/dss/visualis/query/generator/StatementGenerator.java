package com.webank.wedatasphere.dss.visualis.query.generator;

import com.webank.wedatasphere.dss.visualis.query.model.VirtualView;
import edp.davinci.dto.viewDto.DistinctParam;
import edp.davinci.dto.viewDto.ViewExecuteParam;
import edp.davinci.model.User;

public interface StatementGenerator {

    String generate(VirtualView virtualView, ViewExecuteParam executeParam, User user);

    String generateDistinct(VirtualView virtualView, DistinctParam param, User user);
}
