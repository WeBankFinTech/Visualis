package com.webank.wedatasphere.dss.visualis.query.initializer;

import com.webank.wedatasphere.dss.visualis.query.model.VirtualView;
import com.webank.wedatasphere.dss.visualis.query.utils.QueryUtils;
import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.cs.common.exception.CSErrorException;
import edp.davinci.model.User;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ContextSourceInitializer implements SourceInitializer{

    private static final Logger logger = LoggerFactory.getLogger(ContextSourceInitializer.class);

    @Override
    public SourceInitJob init(VirtualView virtualView, User user) throws ErrorException {
        SourceInitJob sourceInitJob = null;
        try {
            QueryUtils.refreshFromContext(virtualView);
        } catch (CSErrorException e) {
            logger.error("Failed to refresh metadata:", e);
        }

        String tableName = virtualView.getSource().getDataSourceContent().get("tableName");
        String dbName = virtualView.getSource().getDataSourceContent().get("dbName");
        String fullName = StringUtils.isBlank(dbName) ? tableName : dbName + "." + tableName;
        String selectFrom = "select * from " + fullName;
        virtualView.setSql(selectFrom);
        return sourceInitJob;
    }

    @Override
    public String getType() {
        return "context";
    }
}
