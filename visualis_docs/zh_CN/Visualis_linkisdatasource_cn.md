> Visualis 接入Linkis Datasource设计手册

## 1. 初衷
&nbsp;&nbsp;&nbsp;&nbsp;原始的Visualis，其必须要依赖一个数据源才能进行View和Wideget的开发，数据源需要配置相关的链接信息，Visualis才能通过配置的链接信息，查询相应的信息，提供View开发，但是传统的Visualis不支持大数据场景，或者支持的大数据场景较为简单（可以通过JDBC链接Hive ThriftServer），在微众银行企业内部，提供计算中间件Linkis链接支持多种大数据数据源，同时其提供多种企业级特性，为了更好的支持大数据场景，Visualis兼容原有的JDBC Source，并提供Linkis Datasource链接相关数据源，目前较为常用的是支持Hive Datasource，其名称为HiveDatasource，在建立新的View时，默认直接绑定为该数据源，在使用上，侧边栏会显示出其具有权限的库表信息，就像文件树类似，其库表可以鼠标双击展开。不局限于Hive Datasource，Visualis在代码层面支持数据源扩展，其中新增Presto数据源，更多的接入和扩展使用方法，需要用户自己去发现探索。

## 2. 设计思路
&nbsp;&nbsp;&nbsp;&nbsp;HiveDatasource在Visualis中存在一定的规范，为了使得每个用户登录使用时，能提供默认的配置的，一个标准的Hive数据源，在建立数据库时，需要提前在Source中插入一个模板。在Davinci.sql文件中，存在如下SQL：
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
&nbsp;&nbsp;&nbsp;&nbsp;其中默认插入主键为id为1，是为了规定好模板在数据库中的索引，使得接下来在使用时可以找到该模板位置。如果存在其它情况，该模板数据源在数据库中的索引变化，需要修改相关配置，并重启服务。有关数据源的相关配置，可以参考com.webank.wedatasphere.dss.visualis.utils.VisualisUtils类中的配置，使用时只需配置相应的键值对在linkis.properties文件中。和数据源有关的配置可以参考如下：
```scala
  // hive datasource token值
  val HIVE_DATA_SOURCE_TOKEN = CommonVars("wds.dss.visualis.hive.datasource.token","hiveDataSource-token")
  // hive datasource主键id
  val HIVE_DATA_SOURCE_ID =  CommonVars("wds.dss.visualis.hive.datasource.id",1)
  // presto数据源token
  val PRESTO_DATA_SOURCE_TOKEN = CommonVars("wds.dss.visualis.presto.datasource.token","prestoDataSource-token")
  // presto数据源token
  val PRESTO_DATA_SOURCE_ID =  CommonVars("wds.dss.visualis.presto.datasource.id",210)
```
&nbsp;&nbsp;&nbsp;&nbsp;数据源创建时刻时发生在获取数据源信息时，在登录到Visualis，切换到Source的Tab时，前端接口会触发获取Source的列表接口。其Restful接口在SourceController类中，代码如下。
```java
    // 原始的Davinci接口
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
&nbsp;&nbsp;&nbsp;&nbsp;在SourceController类中，我们暂未修改其它Davinci的相关实现，为了兼容数据源复用逻辑，对SourceServive其中的接口进行了相关改造，在Service中，存在三步逻辑，分别是通过工程id获取该工程下对应的Source列表，Source列表进行遍历判断是否含有Hive数据源或是Presto数据源，最后一步如果没有相关的HiveDatasource数据源或是Presto数据源会进行插入，并加入到最终需要返回的列表中totalSource，其代码如下：
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

        // 1.通过工程id获取相关的数据源
        List<Source> sources = sourceMapper.getByProject(projectId);
        List<Source> totalSources = Lists.newArrayList();
        totalSources.addAll(hiveDBHelper.sourcesToHiveSources(sources));
        if (!CollectionUtils.isEmpty(totalSources)) {
            ProjectPermission projectPermission = projectService.getProjectPermission(projectDetail, user);
            if (projectPermission.getSourcePermission() == UserPermissionEnum.HIDDEN.getPermission()) {
                sources = null;
            }
        }

        // 2. 对数据源的种类和存在性进行判别
        if(sources.stream().noneMatch(s -> VisualisUtils.isLinkisDataSource(s))){
            
            // 3. 对数据源进行插入
            Source hiveSource = sourceMapper.getById(VisualisUtils.getHiveDataSourceId());
            hiveSource.setId(null);
            hiveSource.setProjectId(projectId);
            sourceMapper.insert(hiveSource);
            totalSources.add(hiveDBHelper.sourceToHiveSource(hiveSource));
        }
        if(getAvailableEngineTypes(user.username).contains(VisualisUtils.PRESTO().getValue()) && sources.stream().noneMatch(
                s -> VisualisUtils.isPrestoDataSource(s))){
            
            // 3. 对数据源进行插入
            Source prestoSource = sourceMapper.getById(VisualisUtils.getPrestoDataSourceId());
            prestoSource.setId(null);
            prestoSource.setProjectId(projectId);
            sourceMapper.insert(prestoSource);
            totalSources.add(hiveDBHelper.sourceToHiveSource(prestoSource));
        }
        return totalSources;
    }
```
&nbsp;&nbsp;&nbsp;&nbsp;数据源在使用时，依赖了Linkis服务，Linkis提供了数据源获取接口，屏蔽掉了第三方组件获取Hive Metasource相关信息的难度，Linkis提供数据源接口，并返回其规范的库表信息格式，在这里，Visualis只需要定义好接口请求和解析格式即可，并能快速的集成大数据的使用场景。请求Linkis数据源时，需要有GateWay进行转发，并设置相应的cookie值，即linkis ticket id，请求返回的接口为JSON格式，在使用时，需要对JSON字符串进行解析，其相关代码核心如下：
```java
public class HttpUtils {

    // linkis gateway相关接口
    private static final String GATEWAY_URL = CommonConfig.GATEWAY_PROTOCOL().getValue() +
            CommonConfig.GATEWAY_IP().getValue() + ":" + CommonConfig.GATEWAY_PORT().getValue();
    
    // 请求db信息接口
    private static final String DATABASE_URL = GATEWAY_URL + CommonConfig.DB_URL_SUFFIX().getValue();

    // 请求table信息接口
    private static final String TABLE_URL = GATEWAY_URL + CommonConfig.TABLE_URL_SUFFIX().getValue();
    
    // 请求列信息接口
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
&nbsp;&nbsp;&nbsp;&nbsp;有关配置的Hive Datasorce使用场景，该数据源并不会提供真实的执行逻辑，Visualis的绑定逻辑为，Widget需要绑定一个View，View会绑定一个Source，Widget执行时，并不会从Souce中获取执行的库表信息。在非传统的Davinci逻辑里面，会存在一个View的查询记录SQL，在真实执行时，通过提交该SQL代码Widget渲染的逻辑，所以，Linkis数据源仅仅是提供一个可视化编辑时的工具组件，并不会影响真实的执行。
&nbsp;&nbsp;&nbsp;&nbsp;View中的核心字段如下：
```json
// view绑定的sql
select * from default.dwc_vsbi_students_demo 

// 其指标维度信息
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
## 3. 其它
&nbsp;&nbsp;&nbsp;&nbsp;目前如果在Visualis自身使用时，Visualis支持Hive Datasource，来提供View查询时的工具组件，如果是通过DSS工作流进行开发，Widget绑定上游表时，其Widget的数据是从CS服务中获取的，并不涉及到具体的数据源，目前Visualis代码层面还集成了Presto数据源，支持更加快速的查询分析，如果需要提供更多数据源的支持，可以参考Presto和Hive数据源的相关实现。
