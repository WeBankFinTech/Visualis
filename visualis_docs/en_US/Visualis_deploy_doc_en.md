Visualis compile and deploy documentation
------

## 1. Download the source package and compile and package
&nbsp;&nbsp;&nbsp;&nbsp;When installing the Visualis source code, you need to download the corresponding source code package for compilation. At present, the DSS 1.0.1 version and Linkis 1.0.3 version that Visualis depends on have been uploaded to the Maven central warehouse, as long as the Maven configuration is normal Relevant dependencies can be pulled.
```shell
# 1. Download the source code
git clone https://github.com/WeDataSphere/Visualis.git

# 2. Switch to the 1.0.0-rc1 branch
git checkout 1.0.0-rc1

# 3. Execute compilation and packaging
cd Visualis
mvn -N install
mvn clean package -DskipTests=true
```

## 2. Install the Visualis package
&nbsp;&nbsp;&nbsp;&nbsp;Visualis uses assembly as a packaging plug-in. After compiling, go to the Visualis/assembly/target directory to find the compiled Visualis-server.zip package.
````bash
 # 1. Unzip the installation package
unzip visualis-server.zip
cd visualis-server
````  

&nbsp;&nbsp;&nbsp;&nbsp;After decompressing the visualis compilation package, enter the directory and you can see the following file directory.
````
visualis-server
    --- bin # Service start and stop script
    --- conf # Service configuration directory
    --- davinvi-ui # Front-end template, presence or absence does not affect use
    --- lib # Service jar package storage location
    --- logs # log directory
````
## 3. Modify the configuration

&nbsp;&nbsp;&nbsp;&nbsp;After the decompression package is installed, the configuration needs to be modified before use. The configuration mainly modifies the two files, application.yml and linkis.properties in the conf directory. The application.yml file needs to conform to the configuration specification of yaml (Key-value pairs need to be separated by a space after the colon).

### 3.1 Modify application.yml
&nbsp;&nbsp;&nbsp;&nbsp;In the configuration application.yml file, configuration items 1, 2, and 3 must be configured. In the first item, you need to configure some deployment IP and port information, and the second item needs to configure eureka information, only the link information of the configuration database is required in item 3 (other parameters can be kept at their default values). **It should be noted that due to historical reasons, Visualis reuses the user authority system of DSS and uses the linkis_user table of DSS. Therefore, when deploying, Visualis needs to configure the same database as DSS. If it is implemented in separate databases, it needs to be used regularly. Synchronize DSS users to the linkis_user table in the Visualis library.**
```yaml
# ####################################
# 1. Visualis Service configuration
# ####################################
server:
  protocol: http
  address: 127.0.0.1 # server ip address
  port: 9008 # server port
  url: http://127.0.0.1:8088/dss/visualis # frontend index page full path
  access:
    address: 127.0.0.1 # frontend address
    port: 8088 # frontend port


# ####################################
# 2. eureka configuration
# ####################################
eureka:
  client:
    serviceUrl:
      defaultZone: http://127.0.0.1:20303/eureka/ # Configuration required
  instance:
    metadata-map:
      test: wedatasphere
management:
  endpoints:
    web:
      exposure:
        include: refresh,info


# ####################################
# 3. Spring configuration
# ####################################
spring:
  main:
    allow-bean-definition-overriding: true
  application:
    name: visualis-dev
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/dss?characterEncoding=UTF-8&allowMultiQueries=true # Configuration required
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
logging:
  config: classpath:log4j2.xml


# ####################################
# 4. static resource configuration
# ####################################
file:
  userfiles-path: ${DAVINCI3_HOME}/userfiles
  web_resources: ${DAVINCI3_HOME}/davinci-ui/
  base-path: ${DAVINCI3_HOME}

sql_template_delimiter: $
custom-datasource-driver-path: ${DAVINCI3_HOME}/conf/datasource_driver.yml


# ####################################
# 5. SQL configuration
# ####################################
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

# ####################################
# 6. Screenshot drive
# ####################################
email:
  suffix: ""
screenshot:
  default_browser: PHANTOMJS
  timeout_second: 1800
  phantomjs_path: ${DAVINCI3_HOME}/bin/phantomjs

```

### 3.2 Modify linkis.properties
```properties
# ####################################
# 1. need configuration
# need to configure
# ####################################
wds.dss.visualis.gateway.ip=127.0.0.1
wds.dss.visualis.gateway.port=9001
wds.dss.visualis.query.timeout=1200000

wds.linkis.gateway.url=http://127.0.0.1:9001/
wds.linkis.gateway.ip=127.0.0.1
wds.linkis.gateway.port=9001


# ####################################
# 2. can keep the default configuration
# can keep the default configuration
# ####################################
# Whether to start the test default
wds.linkis.rpc.eureka.client.refresh.wait.time.max=60s
wds.linkis.test.mode=false
wds.linkis.test.user=test

wds.linkis.server.restful.scan.packages=com.webank.wedatasphere.linkis.entrance.restful,com.webank.wedatasphere.dss.visualis.restful
wds.linkis.query.application.name=linkis-ps-jobhistory
wds.linkis.console.config.application.name=linkis-ps-publicservice
wds.linkis.engine.creation.wait.time.max=20m
wds.linkis.server.socket.mode=false

wds.linkis.server.distinct.mode=true
wds.linkis.filesystem.root.path=file:///mnt/bdap/
wds.linkis.filesystem.hdfs.root.path=hdfs:///tmp/linkis

wds.dss.visualis.project.name=default
wds.linkis.server.version=v1

wds.dss.engine.allowed.creators=Visualis,nodeexecution,IDE
wds.linkis.max.ask.executor.time=45m
wds.linkis.server.component.exclude.classes=com.webank.wedatasphere.linkis.entrance.parser.SparkJobParser
wds.dss.visualis.creator=Visualis

```
### 3.3 Other configuration file modifications
&nbsp;&nbsp;&nbsp;&nbsp;In the actual usage scenario, depending on the linkis.out log output scenario is not in compliance with the specification, the log file is not rolled back, and long-term operation is likely to cause the production server disk capacity alarm, which will bring production problems , at present, we can optimize the log printing by modifying the log configuration. The log configuration can be modified as follows:
```properties
<?xml version="1.0" encoding="UTF-8"?>
<configuration status="error" monitorInterval="30">
    <appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <ThresholdFilter level="trace" onMatch="ACCEPT" onMismatch="DENY"/>
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%t] %logger{36} %L %M - %msg%xEx%n"/>
        </Console>
        <RollingFile name="RollingFile" fileName="/data/logs/visualis/visualis.log"
                     filePattern="/data/logs/visualis/$${date:yyyy-MM}/visualis-log-%d{yyyy-MM-dd}-%i.log.gz">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%t] %logger{36} %L %M - %msg%xEx%n"/>
            <SizeBasedTriggeringPolicy size="100MB"/>
            <DefaultRolloverStrategy max="20"/>
        </RollingFile>
    </appenders>
    <loggers>
        <root level="INFO">
            <appender-ref ref="RollingFile"/>
            <appender-ref ref="Console"/> # Removing this configuration will cancel the linkis.out log output.
        </root>
    </loggers>
</configuration>
```

## 4. Initialize the database
&nbsp;&nbsp;&nbsp;&nbsp;Before use, you need to create a Visualis database (currently recommended, it should be noted that due to historical reasons Visualis reuses the DSS user authority system and uses the DSS linkis_user table, so when deploying, Visualis needs to configure the same database as DSS. If the sub-database is implemented, it needs to synchronize the DSS user to the linkis_user table of the Visualis library.), build the table that Visualis depends on, go to the source code directory, and find the db file Folder, after linking to the corresponding database, you need to execute the following SQL file to create the table() that Visualis needs to use.
```shell
# link visualis database
mysql -h 127.0.0.1 -u hadoop -d visualis -P3306 -p

source ${visualis_home}/davinci.sql
source ${visualis_home}/ddl.sql

# Where davinci.sql is the davinci table that visualis needs to use
# ddl.sql is a table that visualis additionally depends on
````

## 5. Compile frontend files
&nbsp;&nbsp;&nbsp;&nbsp;Visualis is a front-end and back-end separation project, the front-end files can be compiled and packaged separately, and the npm tool needs to be installed on the computer.
```shell
# Check if npm is installed
npm -v
>> 8.1.0

cd webapp # Enter the front-end file path
npm i # download front-end dependencies
npm run build # Compile front-end packages

# A build file directory will be generated in the webapp directory, which is the compiled front-end package file
````

## 6. Start the application

&nbsp;&nbsp;&nbsp;&nbsp;After configuring and compiling the frontend package, you can try to start the service. Visualis is currently integrated with DSS and uses the DSS login and permission system. Before use, the DSS1.0.1 version needs to be deployed. You can refer to DSS1.0.1 one-click installation and deployment. (Because this visualis-1.0.0-rc1 version is an internal beta version, if you want to use it normally, please compile the latest DSS master branch code)

### 6.1 Execute the startup script

&nbsp;&nbsp;&nbsp;&nbsp;Enter the Visualis installation directory, find the bin folder, and execute the following command in this folder.
````
sh ./start-server.sh
````
Note: **If the newline character of the startup script cannot be recognized when the service is started, you need to convert the script on the server and use: dos2unix xxx.sh command to convert**

### 6.2 Confirm that the application starts successfully

&nbsp;&nbsp;&nbsp;&nbsp;Open the Eureka page, find the instance of the visualis service in the list of registered services, and then consider the service to start successfully. At the same time, you can also view the service startup log of visualis. If no error is reported, the service starts successfully.
````
# View service startup log
less logs/linkis.out
````

## 7. Deploy the front-end page
&nbsp;&nbsp;&nbsp;&nbsp;Visualis currently uses the front-end and back-end separation deployment scheme. After completing the compilation in step 4, place the front-end package in the server directory corresponding to the dss/visualis path of the nginx front-end package installation path, and start nginx. Can.
&nbsp;&nbsp;&nbsp;&nbsp;Visualis' nginx front-end configuration can refer to the following:
```shell
# Configuration reference in nginx
server {
    listen 8088; # access port
    server_name localhost;
    client_max_body_size 100M;

    # ...

    location /dss/visualis { # url path
    root /data/dss/web; # Visualis front-end static resource file directory, which can be freely specified
    autoindex off;
  }
  
  # ...

}

````

&nbsp;&nbsp;&nbsp;&nbsp;After configuring the corresponding ngixn configuration, you can install the corresponding front-end files.
```shell
cd /data/dss/web # Enter the static resource installation path
mkdir -p dss/visualis # After the file directory is established, upload the build.zip package to the folder
unzip build.zip # Unzip the front-end package
cd build
mv * ./../ # move frontend files to the previous directory
sudo nginx # start nginx
````

## 8. Font library
&nbsp;&nbsp;&nbsp;&nbsp;For mail reports, Chinese fonts need to be rendered, and the Visualis screenshot function depends on Chinese fonts, which are located in the /usr/share/fonts directory on the deployed machine. Create a new visualis folder, upload the pf.ttf file in the ext directory of the Visualis source package to the visualis folder, and execute the fc-cache –fv command to refresh the font cache.
```shell
# Need to switch to root user
sudo su
cd /usr/share/fonts
mkdir visualis

# Upload pf.ttf Chinese font library
rz -ybe

# Refresh font library cache
fc-cache –fv
````