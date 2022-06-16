package com.webank.wedatasphere.dss.visualis.query.initializer;

import com.webank.wedatasphere.dss.visualis.query.model.VirtualView;
import org.apache.linkis.common.exception.ErrorException;
import edp.davinci.model.User;

public interface SourceInitializer {

    SourceInitJob init(VirtualView virtualView, User user) throws ErrorException;
    String getType();
}
