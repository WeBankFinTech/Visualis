Visualis编译部署文档
------

## 1. 下载源码包及编译打包
&nbsp;&nbsp;&nbsp;&nbsp;Visualis源码安装时，需要下载对应的源码包进行编译，目前Visualis在依赖的DSS 1.0.1版本和Linkis1.0.6版本已经上传到Maven中央仓库。
```shell
## 1. 下载源码
git clone https://github.com/WeDataSphere/Visualis.git

## 2. 切换到1.0.0-rc1分支
git checkout 1.0.0-rc1

## 3. 执行编译打包
mvn -N install
mvn clean package -DskipTests=true
```

## 2. 安装Visualis包
&nbsp;&nbsp;&nbsp;&nbsp;在编译完成后，进入到Visualis/assembly/target目录下，可以找到编译完成后的Visualis-server.zip包。
````bash
 ## 1. 解压安装包
unzip visualis-server.zip
cd visualis-server
````
&nbsp;&nbsp;&nbsp;&nbsp;解压完成visualis包后，进入目录可以看到以下文件目录。
```
visualis-server
    --- bin   #服务启停脚本
    --- conf  #服务配置目录
    --- davinvi-ui    #前端模板，有无并不影响使用
    --- lib   #服务jar包存放位置
    --- logs  #日志目录
```


## 2. 修改配置

&nbsp;&nbsp;&nbsp;&nbsp;安装完成后，在使用前需要修改配置，配置主要修改conf目录下的application.yml和linkis.properties两个文件。

### 2.1 修改application.yml

```yaml
server:
  protocol: http
  address: 127.0.0.1 # Visualis部署的服务器IP
  port:  9008 # Visualis服务端口
  url: http://127.0.0.1:8088/dss/visualis #Visualis前端访问路径
  access:
    address: 127.0.0.1 # 前端部署的服务器IP
    port: 8088 # 前端端口

eureka:
  client:
    serviceUrl:
      defaultZone: http://127.0.0.1:20303/eureka/ # eureka注册地址
  instance:
    metadata-map:
      test: wedatasphere
management:
  endpoints:
    web:
      exposure:
        include: refresh,info

logging:
  config: classpath:log4j2.xml

file:
  userfiles-path: ${DAVINCI3_HOME}/userfiles
  web_resources: ${DAVINCI3_HOME}/davinci-ui/
  phantomJs-path: ${DAVINCI3_HOME}/bin/phantom.js
  base-path: ${DAVINCI3_HOME}

spring:
  main:
    allow-bean-definition-overriding: true
  application:
    name: visualis #服务模块名，用于做高可用（必须）
  ## visualis mysql数据库连接配置
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/visualis?characterEncoding=UTF-8&allowMultiQueries=true
    username: hadoop
    password: hadoop
    driver-class-name: com.mysql.jdbc.Driver
    initial-size: 2
    min-idle: 1
    max-wait: 60000
    max-active: 10
    type: com.alibaba.druid.pool.DruidDataSource
    time-between-eviction-runs-millis: 30000
    min-evictable-idle-time-millis: 300000
    test-while-idle: true
    test-on-borrow: false
    test-on-return: false
    filters: stat
    break-after-acquire-failure: true
    connection-error-retry-attempts: 3
    validation-query: SELECT 1
  servlet:
    multipart:
      max-request-size: 1024MB
      max-file-size: 1024MB
      enabled: true
      location: /

  config:
    location: classpath:/
    additional-location: file:${DAVINCI3_HOME}/conf/
    name: application

  resources:
    static-locations: classpath:/META-INF/resources/, classpath:/resources/, classpath:/static/, file:${file.userfiles-path}, file:${file.web_resources}

  mvc:
    static-path-pattern: /**


  thymeleaf:
    mode: HTML5
    cache: true
    prefix: classpath:/templates/
    encoding: UTF-8
    suffix: .html
    check-template-location: true
    template-resolver-order: 1

  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8

  cache:
    caffeine:
      type: caffeine
  mail:
    host: 127.0.0.1
    port: 8000
    username: wedatasphere
    password: wedatasphere
    nickname: wedatasphere

    properties:
      smtp:
        starttls:
          enable: true
          required: true
        auth: true
      mail:
        smtp:
          ssl:
            enable: false

springfox:
  documentation:
    swagger:
      v2:
        path: /api-doc


pagehelper:
  supportMethodsArguments: true
  reasonable: true
  returnPageInfo: check
  helperDialect: mysql
  params: count=countSql

mybatis:
  mapper-locations: classpath:mybatis/mapper/*Mapper.xml
  config-locations: classpath:mybatis/mybatis-config.xml
  type-aliases-package: edp.davinci.model
  configuration:
    map-underscore-to-camel-case: true
    use-generated-keys: true

mapper:
  identity: MYSQL
  not-empty: false
  mappers: edp.davinci.dao

sql_template_delimiter: $

custom-datasource-driver-path: ${DAVINCI3_HOME}/conf/datasource_driver.yml

phantomjs_home: ${DAVINCI3_HOME}/bin/phantomjs

email:
  suffix: ""
screenshot:
  default_browser: PHANTOMJS                    # 选择PHANTOMJS or CHROME作为报表发送工具，Visualis该版本使用PhantomJS
  timeout_second: 1800
  phantomjs_path: ${DAVINCI3_HOME}/bin/phantomjs
  chromedriver_path: $your_chromedriver_path$

```

### 2.2 修改linkis.properties


```properties
#是否启动测试默认
wds.linkis.rpc.eureka.client.refresh.wait.time.max=60s
wds.linkis.test.mode=false
wds.linkis.test.user=test
#Restful扫描的package
wds.linkis.server.restful.scan.packages=com.webank.wedatasphere.linkis.entrance.restful,com.webank.wedatasphere.dss.visualis.restful
wds.linkis.engine.application.name=sparkEngine
wds.linkis.enginemanager.application.name=sparkEngineManager

wds.linkis.query.application.name=linkis-ps-jobhistory

wds.linkis.console.config.application.name=linkis-ps-publicservice
wds.linkis.engine.creation.wait.time.max=20m
wds.linkis.server.socket.mode=false

wds.linkis.server.distinct.mode=true
wds.linkis.filesystem.root.path=file:///mnt/bdap/
wds.linkis.filesystem.hdfs.root.path=hdfs:///tmp/linkis

wds.dss.visualis.project.name=default

wds.linkis.server.version=v1

wds.dss.visualis.gateway.ip=127.0.0.1
wds.dss.visualis.gateway.port=9001
wds.dss.visualis.query.timeout=1200000

wds.linkis.gateway.url=http://127.0.0.1:9001/
wds.linkis.gateway.ip=127.0.0.1
wds.linkis.gateway.port=9001

wds.dss.engine.allowed.creators=Visualis,nodeexecution,IDE
wds.linkis.max.ask.executor.time=45m
wds.linkis.server.component.exclude.classes=com.webank.wedatasphere.linkis.entrance.parser.SparkJobParser

wds.dss.visualis.enable.password.encrypt=false
wds.dss.visualis.creator=nodeexecution
```

## 3. 初始化数据库
&nbsp;&nbsp;&nbsp;&nbsp;在使用前，需要创建好Visualis数据库，建好Visualis所依赖的表，进入到源码的跟目录，找到db文件夹。
```
davinci.sql # visualis需要使用到的davinci的表
ddl.sql # visualis额外依赖的表
```

## 4. 编译前端文件
&nbsp;&nbsp;&nbsp;&nbsp;Visualis是一个前后端分离项目，前端文件可以单独编译打包，因为存在一些依赖国外源的文件，理想情况下，需要在电脑上部署好vpn打包。
```shell
cd webapp # 进入前端文件路径
npm i # 下载前端依赖
npm run build # 编译前端包

# 在webapp目录下会生成一个build文件目录，该目录及编译完成的前端包文件
```

## 4. 启动应用

&nbsp;&nbsp;&nbsp;&nbsp;在配置和前端包编译完成后，可以尝试启动服务。

### 4.1 执行启动脚本

&nbsp;&nbsp;&nbsp;&nbsp;进入bin目录，执行
```
   ./start-server.sh
```
### 4.1 确认应用启动成功

&nbsp;&nbsp;&nbsp;&nbsp;打开Eureka页面，在注册的服务列表中，找到visualis服务的实例，即可认为服务启动成功。同时也可以查看visualis的服务启动日志，如果没有报错，及服务顺利启动。

## 5. 部署前端页面
&nbsp;&nbsp;&nbsp;&nbsp;Visualis当前使用前后端分离的部署方案，完成第4步的编译后，把前端包放置在dss/visualis这个URL路径对应的服务器目录下，启动nginx即可。
```shell
mkdir visualis # 建立好文件目录后，长传build.zip包到该文件夹下
unzip build.zip # 解压前端包
cd build
mv * ./../ # 把前端文件移动到上一个目录
sudo nginx
```







