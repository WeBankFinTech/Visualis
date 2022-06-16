package com.webank.wedatasphere.dss.visualis.query.initializer;

import com.webank.wedatasphere.dss.visualis.query.model.VirtualView;
import com.webank.wedatasphere.dss.visualis.query.utils.QueryUtils;
import com.webank.wedatasphere.dss.visualis.res.ResultHelper;
import com.webank.wedatasphere.dss.visualis.ujes.UJESJob;
import edp.davinci.model.User;
import org.springframework.stereotype.Component;


@Component
public class ResultSetSourceInitializer implements SourceInitializer{

    @Override
    public SourceInitJob init(VirtualView virtualView, User user) {
        String resultLocation = ResultHelper.getSchemaPath(virtualView.getSource().getDataSourceContent().get("resultLocation"));
        String tempViewName = "tmp_res_" + (virtualView.getName().hashCode() & Integer.MAX_VALUE);
        String selectFrom = "select * from " + tempViewName;
        virtualView.setSql(selectFrom);
        return new SourceInitJob(QueryUtils.getCreateTempViewScala(tempViewName, resultLocation), UJESJob.SCALA_TYPE());
    }

    @Override
    public String getType() {
        return "resultset";
    }

}
