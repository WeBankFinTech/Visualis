> Visualis Access Linkis Datasource Design Manual

## 1. Original intention
&nbsp;&nbsp;&nbsp;&nbsp;The original Visualis must rely on a data source to develop View and Wideget. The data source needs to be configured with relevant link information, and Visualis can query the corresponding information and provide View development through the configured link information, but traditional Visualis does not. Supports big data scenarios, or the supported big data scenarios are relatively simple (you can link Hive ThriftServer through JDBC), within WeBank enterprises, provide computing middleware Linkis links to support a variety of big data data sources, and at the same time it provides a variety of enterprise-level Features, in order to better support big data scenarios, Visualis is compatible with the original JDBC Source and provides Linkis Datasource to link related data sources. Currently, Hive Datasource is more commonly used, and its name is HiveDatasource. When creating a new View, the default value is HiveDatasource. It is directly bound to the data source. In use, the sidebar will display the information of the library table that it has permission to, just like the file tree, the library table can be double-clicked to expand. Not limited to Hive Datasource, Visualis supports data source extension at the code level. Among them, the new Presto data source, more access and extension usage methods, need users to discover and explore by themselves.

## 2. Design ideas
&nbsp;&nbsp;&nbsp;&nbsp;HiveDatasource has certain specifications in Visualis. In order to enable each user to log in and use, a standard Hive datasource with default configuration can be provided. When creating a database, a template needs to be inserted into Source in advance. In the Davinci.sql file, the following SQL exists:
```sql
DELETE FROM source;
INSERT INTO `source` (
    id,
    name,
    description,
    config,
    type,
    project_id,
    create_by,
    create_time,
    update_by,
    update_time,
    parent_id,
    full_parent_id,
    is_folder,
    `index`)
VALUES (
    1,
    'hiveDataSource',
    '',
    '{"parameters":"","password":"","url":"test","username":"hiveDataSource-token"}',
    'hive',
    -1,
    null,null,null,null,null,null,null,null);
```
&nbsp;&nbsp;&nbsp;&nbsp;The default insertion primary key is id 1, in order to specify the index of the template in the database, so that the template position can be found when using it next. If there are other situations, the index of the template data source in the database changes, you need to modify the relevant configuration and restart the service. For the relevant configuration of the data source, you can refer to the configuration in the com.webank.wedatasphere.dss.visualis.utils.VisualisUtils class. When using it, you only need to configure the corresponding key-value pair in the linkis.properties file. The configuration related to the data source can be referred to as follows:
```scala
  // hive datasource token
  val HIVE_DATA_SOURCE_TOKEN = CommonVars("wds.dss.visualis.hive.datasource.token","hiveDataSource-token")
  // hive datasource
  val HIVE_DATA_SOURCE_ID =  CommonVars("wds.dss.visualis.hive.datasource.id",1)
  // presto token
  val PRESTO_DATA_SOURCE_TOKEN = CommonVars("wds.dss.visualis.presto.datasource.token","prestoDataSource-token")
  // presto token
  val PRESTO_DATA_SOURCE_ID =  CommonVars("wds.dss.visualis.presto.datasource.id",210)
```
&nbsp;&nbsp;&nbsp;&nbsp;When the data source is created, it occurs when the data source information is obtained. When logging in to Visualis and switching to the Source's Tab, the front-end interface will trigger the acquisition of the Source's list interface. Its Restful interface is in the SourceController class, and the code is as follows.
```java
    // Original Davinci interface
    @MethodLog
    @GetMapping
    public ResponseEntity getSources(@RequestParam Long projectId,
                                     @CurrentUser User user,
                                     HttpServletRequest request) {
        if (invalidId(projectId)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid project id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }
        List<Source> sources = sourceService.getSources(projectId, user, HttpUtils.getUserTicketId(request));
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(sources));
    }
```
&nbsp;&nbsp;&nbsp;&nbsp;In the SourceController class, we have not modified other Davinci related implementations. In order to be compatible with the data source reuse logic, the interfaces in SourceServive have been modified. In the Service, there are three steps of logic, which are to obtain the project through the project id. Go to the corresponding Source list. The Source list is traversed to determine whether there is a Hive data source or a Presto data source. In the last step, if there is no relevant Hive Datasource or Presto data source, it will be inserted and added to the final list that needs to be returned. TotalSource , the code is as follows:
```java
    @Override
    public List<Source> getSources(Long projectId, User user, String ticketId) throws NotFoundException, UnAuthorizedExecption, ServerException {
        ProjectDetail projectDetail = null;
        try {
            projectDetail = projectService.getProjectDetail(projectId, user, false);
        } catch (NotFoundException e) {
            throw e;
        } catch (UnAuthorizedExecption e) {
            return null;
        }

        // 1. Obtain the relevant data source through the project id
        List<Source> sources = sourceMapper.getByProject(projectId);
        List<Source> totalSources = Lists.newArrayList();
        totalSources.addAll(hiveDBHelper.sourcesToHiveSources(sources));
        if (!CollectionUtils.isEmpty(totalSources)) {
            ProjectPermission projectPermission = projectService.getProjectPermission(projectDetail, user);
            if (projectPermission.getSourcePermission() == UserPermissionEnum.HIDDEN.getPermission()) {
                sources = null;
            }
        }

        // 2. Identify the type and existence of data sources
        if(sources.stream().noneMatch(s -> VisualisUtils.isLinkisDataSource(s))){
            
            // 3. Insert the data source
            Source hiveSource = sourceMapper.getById(VisualisUtils.getHiveDataSourceId());
            hiveSource.setId(null);
            hiveSource.setProjectId(projectId);
            sourceMapper.insert(hiveSource);
            totalSources.add(hiveDBHelper.sourceToHiveSource(hiveSource));
        }
        if(getAvailableEngineTypes(user.username).contains(VisualisUtils.PRESTO().getValue()) && sources.stream().noneMatch(
                s -> VisualisUtils.isPrestoDataSource(s))){
            
            // 3. Insert the data source
            Source prestoSource = sourceMapper.getById(VisualisUtils.getPrestoDataSourceId());
            prestoSource.setId(null);
            prestoSource.setProjectId(projectId);
            sourceMapper.insert(prestoSource);
            totalSources.add(hiveDBHelper.sourceToHiveSource(prestoSource));
        }
        return totalSources;
    }
```
&nbsp;&nbsp;&nbsp;&nbsp;When the data source is used, it relies on the Linkis service. Linkis provides a data source acquisition interface, which shields the difficulty of obtaining Hive Metasource-related information for third-party components. Linkis provides a data source interface and returns its standard library table information format. Here, Visualis only needs to define the interface request and parsing format, and can quickly integrate the usage scenarios of big data. When requesting Linkis data source, GateWay needs to forward it and set the corresponding cookie value, namely linkis ticket id. The interface returned by the request is in JSON format. When using it, the JSON string needs to be parsed. The core of the relevant code is as follows:
```java
public class HttpUtils {

    // linkis gateway related interface
    private static final String GATEWAY_URL = CommonConfig.GATEWAY_PROTOCOL().getValue() +
            CommonConfig.GATEWAY_IP().getValue() + ":" + CommonConfig.GATEWAY_PORT().getValue();
    
    // request db information interface
    private static final String DATABASE_URL = GATEWAY_URL + CommonConfig.DB_URL_SUFFIX().getValue();

    // Request table information interface
    private static final String TABLE_URL = GATEWAY_URL + CommonConfig.TABLE_URL_SUFFIX().getValue();
    
    // Request column information interface
    private static final String COLUMN_URL = GATEWAY_URL + CommonConfig.COLUMN_URL_SUFFIX().getValue();

    public static String getDbs(String ticketId) {
        // ...
        HttpGet httpGet = new HttpGet(DATABASE_URL);
        BasicClientCookie cookie = new BasicClientCookie(CommonConfig.TICKET_ID_STRING().getValue(), ticketId);
        cookie.setVersion(0);
        cookie.setDomain(CommonConfig.GATEWAY_IP().getValue());
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
        // ...
        String tableJson = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(TABLE_URL);
            uriBuilder.addParameter("database", hiveDBName);
            CookieStore cookieStore = new BasicCookieStore();
            CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            BasicClientCookie cookie = new BasicClientCookie(CommonConfig.TICKET_ID_STRING().getValue(), ticketId);
            cookie.setVersion(0);
            cookie.setDomain(CommonConfig.GATEWAY_IP().getValue());
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
        // ...
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
            cookie.setDomain(CommonConfig.GATEWAY_IP().getValue());
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
```
&nbsp;&nbsp;&nbsp;&nbsp;Regarding the configured Hive Datasource usage scenario, the data source does not provide real execution logic. The binding logic of Visualis is that the Widget needs to be bound to a View, and the View will be bound to a Source. Get the executed library table information in . In the non-traditional Davinci logic, there will be a View query record SQL. During actual execution, the logic of Widget rendering is submitted by submitting the SQL code. Therefore, the Linkis data source only provides a tool component for visual editing, not will affect the actual execution.
&nbsp;&nbsp;&nbsp;&nbsp;The core fields in View are as follows:
```json
// view-bound sql
select * from default.dwc_vsbi_students_demo 

// Its indicator dimension information
{
    "id":{"sqlType":"INT","visualType":"number","modelType":"value"},
    "name":{"sqlType":"STRING","visualType":"string","modelType":"category"},
    "sex":{"sqlType":"STRING","visualType":"string","modelType":"category"},
    "age":{"sqlType":"INT","visualType":"number","modelType":"value"},
    "class":{"sqlType":"STRING","visualType":"string","modelType":"category"},
    "lesson":{"sqlType":"STRING","visualType":"string","modelType":"category"},
    "city":{"sqlType":"STRING","visualType":"string","modelType":"category"},
    "teacher":{"sqlType":"STRING","visualType":"string","modelType":"category"},
    "score":{"sqlType":"DOUBLE","visualType":"number","modelType":"value"},
    "fee":{"sqlType":"DOUBLE","visualType":"number","modelType":"value"},
    "birthday":{"sqlType":"STRING","visualType":"string","modelType":"category"},
    "exam_date":{"sqlType":"STRING","visualType":"string","modelType":"category"}
}
```
## 3. Other
&nbsp;&nbsp;&nbsp;&nbsp;At present, if Visualis is used by itself, Visualis supports Hive Datasource to provide tool components for View query. If it is developed through the DSS workflow, when the Widget is bound to the upstream table, the data of the Widget is obtained from the CS service. It does not involve specific data sources. Currently, the Visualis code level also integrates Presto data sources to support faster query analysis. If you need to provide support for more data sources, you can refer to the implementation of Presto and Hive data sources.
