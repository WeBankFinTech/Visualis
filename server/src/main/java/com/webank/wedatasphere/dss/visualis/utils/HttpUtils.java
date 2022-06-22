package com.webank.wedatasphere.dss.visualis.utils;

import com.webank.wedatasphere.dss.visualis.configuration.CommonConfig;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.apache.linkis.errorcode.client.ClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

public class HttpUtils {
    private static final String GATEWAY_URL = ClientConfiguration.getGatewayUrl();
    private static final String DATABASE_URL = GATEWAY_URL + CommonConfig.DB_URL_SUFFIX().getValue();
    private static final String TABLE_URL = GATEWAY_URL + CommonConfig.TABLE_URL_SUFFIX().getValue();
    private static final String COLUMN_URL = GATEWAY_URL + CommonConfig.COLUMN_URL_SUFFIX().getValue();
    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    public static String getDbs(String ticketId) {
        logger.info("开始进行获取hive数据库的信息");
        CookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
        HttpGet httpGet = new HttpGet(DATABASE_URL);
        BasicClientCookie cookie = new BasicClientCookie(CommonConfig.TICKET_ID_STRING().getValue(), ticketId);
        cookie.setVersion(0);
        cookie.setDomain(getIpFromUrl(GATEWAY_URL));
        cookie.setPath("/");
        cookie.setExpiryDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 30L));
        cookieStore.addCookie(cookie);
        String hiveDBJson = null;
        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
            hiveDBJson = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (IOException e) {
            logger.error("通过HTTP方式获取Hive数据库信息失败, reason:", e);
        }
        return hiveDBJson;
    }

    public static String getTables(String ticketId, String hiveDBName) {
        logger.info("开始获取hive数据库{} 相关的表以及字段信息", hiveDBName);
        String tableJson = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(TABLE_URL);
            uriBuilder.addParameter("database", hiveDBName);
            CookieStore cookieStore = new BasicCookieStore();
            CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            BasicClientCookie cookie = new BasicClientCookie(CommonConfig.TICKET_ID_STRING().getValue(), ticketId);
            cookie.setVersion(0);
            cookie.setDomain(getIpFromUrl(GATEWAY_URL));
            cookie.setPath("/");
            cookieStore.addCookie(cookie);
            CloseableHttpResponse response = httpClient.execute(httpGet);
            tableJson = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (URISyntaxException e) {
            logger.error("{} url 有问题", TABLE_URL, e);
        } catch (IOException e) {
            logger.error("获取hive数据库 {} 下面的表失败了", hiveDBName, e);
        }
        return tableJson;
    }

    public static String getColumns(String dbName, String tableName, String ticketId) {
        logger.info("开始获取hive数据库表 {}.{} 的字段信息", dbName, tableName);
        String columnJson = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(COLUMN_URL);
            uriBuilder.addParameter("database", dbName);
            uriBuilder.addParameter("table", tableName);
            CookieStore cookieStore = new BasicCookieStore();
            CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            BasicClientCookie cookie = new BasicClientCookie(CommonConfig.TICKET_ID_STRING().getValue(), ticketId);
            cookie.setVersion(0);
            cookie.setDomain(getIpFromUrl(GATEWAY_URL));
            cookie.setPath("/");
            cookieStore.addCookie(cookie);
            CloseableHttpResponse response = httpClient.execute(httpGet);
            columnJson = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (final URISyntaxException e) {
            logger.error("{} url 有问题", COLUMN_URL, e);
        } catch (final IOException e) {
            logger.error("获取hive数据库 {}.{} 字段信息失败 ", dbName, tableName, e);
        }
        return columnJson;
    }

    public static String getUserTicketId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String ticketId = null;
        for (Cookie cookie : cookies) {
            if (CommonConfig.TICKET_ID_STRING().getValue().equalsIgnoreCase(cookie.getName())) {
                ticketId = cookie.getValue();
                break;
            }
        }
        return ticketId;
    }

    // http://127.0.0.1:9001
    private static String getIpFromUrl(String url) {
        URI uri = URI.create(url);
        String ip = "";
        int port = 0;
        if (null != null) {
            ip = uri.getHost();
            port = uri.getPort();
        }
        return ip;
    }

}



