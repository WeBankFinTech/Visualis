> Visualis Access Linkis Datasource Design Manual

## 1. Original intention
&nbsp;&nbsp;&nbsp;&nbsp;The original Visualis must rely on a data source to develop View and Wideget. The data source needs to configure the relevant link information, and Visualis can query the corresponding information through the configured link information, provide View development, but the traditional Visualis does not support big data scenarios, or the supported big data scenarios are relatively simple (you can link Hive ThriftServer through JDBC), and within the WeBank enterprise, it provides computing middleware Linkis links to support a variety of big data data sources , and it provides a variety of enterprise-level features. In order to better support big data scenarios, Visualis is compatible with the original JDBC Source and provides Linkis Datasource to link related data sources. Currently, the most commonly used support is Hive Datasource, whose name is HiveDatasource, When creating a new View, it is directly bound to the data source by default. In use, the sidebar will display the library table information that it has permission to, just like a file tree, its library table can be double-clicked to expand. Not limited to Hive Datasource, Visualis supports data source extension at the code level. Among them, the new Presto data source, more access and extension usage methods, need users to discover and explore by themselves.

## 2. Design Ideas
&nbsp;&nbsp;&nbsp;&nbsp;HiveDatasource has certain specifications in Visualis. In order to enable each user to log in and use, it can provide a standard Hive data source with a default configuration. When creating a database, it needs to be in the Source in advance Insert a template. In the Davinci.sql file, the following SQL exists:
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
&nbsp;&nbsp;&nbsp;&nbsp;The default insertion primary key is id 1, in order to specify the index of the template in the database, so that the template location can be found when using it next. If there are other situations, the index of the template data source in the database changes, you need to modify the relevant configuration and restart the service. For the relevant configuration of the data source, you can refer to the configuration in the com.webank.wedatasphere.dss.visualis.utils.VisualisUtils class. When using it, you only need to configure the corresponding key-value pair in the linkis.properties file. The configuration related to the data source can be referred to as follows:
```scala
  // hive datasource token value
  val HIVE_DATA_SOURCE_TOKEN = CommonVars("wds.dss.visualis.hive.datasource.token","hiveDataSource-token")
  // hive datasource primary key id
  val HIVE_DATA_SOURCE_ID = CommonVars("wds.dss.visualis.hive.datasource.id",1)
  // presto data source token
  val PRESTO_DATA_SOURCE_TOKEN = CommonVars("wds.dss.visualis.presto.datasource.token","prestoDataSource-token")
  // presto data source token
  val PRESTO_DATA_SOURCE_ID = CommonVars("wds.dss.visualis.presto.datasource.id",210)
```
&nbsp;&nbsp;&nbsp;&nbsp;When the data source is created, it occurs when the data source information is obtained. When logging in to Visualis and switching to the Source's Tab, the front-end interface will trigger the acquisition of the Source's list interface. Its Restful interface is in the SourceController class, and the code is as follows.
```java
    // original Davinci interface
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
&nbsp;&nbsp;&nbsp;&nbsp;In the SourceController class, we have not modified other Davinci related implementations. In order to be compatible with the data source reuse logic, the interfaces in SourceServive have been modified. In the Service, there are three-step logic, Respectively, the corresponding Source list under the project is obtained through the project id, and the Source list is traversed to determine whether there is a Hive data source or a Presto data source. To the totalSource in the final list that needs to be returned, the code is as follows:
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

        // 1. Get the relevant data source through the project id
        List<Source> sources = sourceMapper.getByProject(projectId);
        List<Source> totalSources = Lists.newArrayList();
        totalSources.addAll(hiveDBHelper.sourcesToHiveSources(sources));
        if (!CollectionUtils.isEmpty(totalSources)) {
            ProjectPermission projectPermission = projectService.getProjectPermission(projectDetail, user);
            if (projectPermission.getSourcePermission() == UserPermissionEnum.HIDDEN.getPermission()) {
                sources = null;
            }
        }

        // 2. Identify the type and existence of the data source
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
&nbsp;&nbsp;&nbsp;&nbsp;When the data source is used, it relies on the Linkis service. Linkis provides a data source acquisition interface, which shields the difficulty of third-party components from obtaining Hive Metasource-related information. Linkis provides a data source interface and returns its Standardized library table information format. Here, Visualis only needs to define the interface request and parsing format, and can quickly integrate the usage scenarios of big data. When requesting Linkis data source, GateWay needs to forward it and set the corresponding cookie value, namely linkis ticket id. The interface returned by the request is in JSON format. When using it, the JSON string needs to be parsed. The core of the relevant code is as follows:
```java
public class HttpUtils {

    // linkis gateway related interface
    private static final String GATEWAY_URL = CommonConfig.GATEWAY_PROTOCOL().getValue() +
            CommonConfig.GATEWAY_IP().getValue() + ":" + CommonConfig.GATEWAY_PORT().getValue();
    
    // request db information interface
    private static final String DATABASE_URL = GATEWAY_URL + CommonConfig.DB_URL_SUFFIX().getValue();

    // Request table information interface
    private static final String TABLE_URL = GATEWAY_URL + CommonConfig.TABLE_URL_SUFFIX().getValue();
    
    // request column information interface
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
            logger.error("Failed to obtain Hive database information through HTTP, reason:", e);
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
            logger.error("{} url is wrong", TABLE_URL, e);
        } catch (IOException e) {
            logger.error("Failed to get the table below hive database {}", hiveDBName, e);
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
            logger.error("{} url is wrong", COLUMN_URL, e);
        } catch (final IOException e) {
            logger.error("Failed to get hive database {}.{} field information", dbName, tableName, e);
        }
        return columnJson;
    }
```
&nbsp;&nbsp;&nbsp;&nbsp;The Hive Datasorce usage scenario for configuration, the data source does not provide real execution logic, the binding logic of Visualis is, Widget needs to bind a View, View will bind a Source, Widget During execution, the executed library table information will not be obtained from Souce. In the non-traditional Davinci logic, there will be a View query record SQL. During actual execution, the logic of Widget rendering is submitted by submitting the SQL code. Therefore, the Linkis data source only provides a visual editing time.
&nbsp;&nbsp;&nbsp;&nbsp;The core fields in the View are as follows:
```json
// view bound sql
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
## 3. Others
&nbsp;&nbsp;&nbsp;&nbsp;At present, if Visualis is used by itself, Visualis supports Hive Datasource to provide tool components for View query. If it is developed through DSS workflow, when the Widget is bound to the upstream table, the data of its Widget It is obtained from the CS service and does not involve specific data sources. Currently, the Visualis code level also integrates the Presto data source to support faster query analysis. If you need to provide support for more data sources, you can refer to Presto and Hive The relevant implementation of the data source.