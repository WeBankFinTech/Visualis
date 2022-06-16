package com.webank.wedatasphere.dss.visualis.query.initializer;

import com.webank.wedatasphere.dss.visualis.query.model.VirtualView;
import com.webank.wedatasphere.dss.visualis.query.utils.QueryUtils;
import com.webank.wedatasphere.dss.visualis.ujes.UJESJob;
import edp.davinci.model.User;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UrlSourceInitializer implements SourceInitializer {

    private static final Logger logger = LoggerFactory.getLogger(UrlSourceInitializer.class);

    @Override
    public SourceInitJob init(VirtualView virtualView, User user) {
        SourceInitJob sourceInitJob = null;
        CookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
        HttpGet httpGet = new HttpGet(virtualView.getSource().getDataSourceContent().get("url"));
        String dolphinResult = "";
        try{
            CloseableHttpResponse response = httpClient.execute(httpGet);
            dolphinResult = EntityUtils.toString(response.getEntity(), "UTF-8");
        }catch(IOException e){
            logger.error("failed to download url data source, reason:" , e);
        }
        if(StringUtils.isNotBlank(dolphinResult)){
            String tempViewName = "tmp_url_" + virtualView.getName();
            sourceInitJob = new SourceInitJob(QueryUtils.getCreateTempViewScala(tempViewName, dolphinResult), UJESJob.SCALA_TYPE());
            String selectFrom = "select * from " + tempViewName;
            virtualView.setSql(selectFrom);
        }
        return sourceInitJob;
    }

    @Override
    public String getType() {
        return "url";
    }

}
