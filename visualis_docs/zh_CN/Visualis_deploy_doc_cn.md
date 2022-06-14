Visualis编译部署文档
------

## 1. 下载源码包及编译打包
&nbsp;&nbsp;&nbsp;&nbsp;Visualis源码安装时，需要下载对应的源码包进行编译，目前Visualis在依赖的DSS 1.0.1版本和Linkis1.0.3版本已经上传到Maven中央仓库，只需Maven配置正常即可拉取相关依赖。
```shell

# 1. 下载源码
git clone https://github.com/WeDataSphere/Visualis.git

# 2. 切换到1.0.0-rc1分支
git checkout 1.0.0-rc1

# 3. 执行编译打包
cd Visualis
mvn -N install
mvn clean package -DskipTests=true
```

## 2. 安装Visualis包
&nbsp;&nbsp;&nbsp;&nbsp;Visualis使用assembly作为打包插件，在编译完成后，进入到Visualis/assembly/target目录下，可以找到编译完成后的Visualis-server.zip包。
````bash
 # 1. 解压安装包
unzip visualis-server.zip
cd visualis-server
````
&nbsp;&nbsp;&nbsp;&nbsp;解压完成visualis编译包后，进入目录可以看到以下文件目录。
```
visualis-server
    --- bin   # 服务启停脚本
    --- conf  # 服务配置目录
    --- davinvi-ui    # 前端模板，有无并不影响使用
    --- lib   # 服务jar包存放位置
    --- logs  # 日志目录
```

## 2. 修改配置

&nbsp;&nbsp;&nbsp;&nbsp;解压包安装完成后，在使用前需要修改配置，配置主要修改conf目录下的application.yml和linkis.properties两个文件，其中application.yml文件需要符合yaml的配置规范（键值对间冒号后需要空格隔开）。

### 2.1 修改application.yml
&nbsp;&nbsp;&nbsp;&nbsp;在配置application.yml文件中，必须要配置的有1、2、3配置项，其中第1项中，需要配置一些部署IP和端口信息，第2项需要配置eureka的信息，第3项中只需要配置数据库的链接信息即可（其它参数可以保持默认值）。**需要注意，由于历史原因Visualis复用了DSS的用户权限体系，及使用了DSS的linkis_user表，所以在部署时，Visualis需要配置和DSS一样的数据库，如果分库实现，在使用时需要定时同步DSS用户到Visualis库的linkis_user表中。**
```yaml
# ##################################
# 1. Visualis Service configuration
# ##################################
server:
  protocol: http
  address: 127.0.0.1 # server ip address
  port:  9008 # server port
  url: http://127.0.0.1:8088/dss/visualis # frontend index page full path
  access:
    address: 127.0.0.1 # frontend address
    port: 8088 # frontend port


# ##################################
# 2. eureka configuration
# ##################################
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


# ##################################
# 3. Spring configuration
# ##################################
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


# ##################################
# 4. static resource configuration
# ##################################
file:
  userfiles-path: ${DAVINCI3_HOME}/userfiles
  web_resources: ${DAVINCI3_HOME}/davinci-ui/
  base-path: ${DAVINCI3_HOME}

sql_template_delimiter: $
custom-datasource-driver-path: ${DAVINCI3_HOME}/conf/datasource_driver.yml


# ##################################
# 5. SQL configuration
# ##################################
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


# ##################################
# 6. Screenshot drive
# ##################################
email:
  suffix: ""
screenshot:
  default_browser: PHANTOMJS
  timeout_second: 1800
  phantomjs_path: ${DAVINCI3_HOME}/bin/phantomjs
  chromedriver_path: $your_chromedriver_path$
```

### 2.2 修改linkis.properties
```properties
# ##################################
# 1. need configuration
#    需要配置
# ##################################
wds.dss.visualis.gateway.ip=127.0.0.1
wds.dss.visualis.gateway.port=9001
wds.dss.visualis.query.timeout=1200000

wds.linkis.gateway.url=http://127.0.0.1:9001/
wds.linkis.gateway.ip=127.0.0.1
wds.linkis.gateway.port=9001


# ##################################
# 2. can keep the default configuration
#    可以保持默认配置
# ##################################
# 是否启动测试默认
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

### 2.3 其它配置文件修改
&nbsp;&nbsp;&nbsp;&nbsp;在实际的使用场景中，依赖于linkis.out日志输出场景比较不符合规范，日志文件不回滚，长时间运行容易造成生产服务器磁盘容量告警，从而带来生产问题，目前我们可以通过修改日志配置，来优化日志打印，日志配置可以参考如下修改：
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
            <appender-ref ref="Console"/> # 去掉该配置即会取消掉linkis.out日志输出。
        </root>
    </loggers>
</configuration>
```

## 3. 初始化数据库
&nbsp;&nbsp;&nbsp;&nbsp;在使用前，需要创建好Visualis数据库（目前建议，由于历史原因Visualis复用了DSS的用户权限体系，及使用了DSS的linkis_user表，所以在部署时，Visualis需要配置和DSS一样的数据库，如果分库实现，在使用时需要定时同步DSS用户到Visualis库的linkis_user表中。），建好Visualis所依赖的表，进入到源码的跟目录，找到db文件夹，在链接到对应的数据库后，需要执行以下SQL文件，建立Visualis使用时需用到的表()。
```shell
# 链接visualis数据库
mysql -h 127.0.0.1 -u hadoop -d visualis -P3306 -p

source ${visualis_home}/davinci.sql
source ${visualis_home}/ddl.sql

# 其中davinci.sql是visualis需要使用到的davinci的表
# ddl.sql是visualis额外依赖的表
```

## 4. 编译前端文件
&nbsp;&nbsp;&nbsp;&nbsp;Visualis是一个前后端分离项目，前端文件可以单独编译打包，在电脑上需要安装npm工具。
```shell
# 查看npm是否安装完成
npm -v
>> 8.1.0

cd webapp # 进入前端文件路径
npm i # 下载前端依赖
npm run build # 编译前端包

# 在webapp目录下会生成一个build文件目录，该目录即编译完成的前端包文件
```

## 5. 启动应用

&nbsp;&nbsp;&nbsp;&nbsp;在配置和前端包编译完成后，可以尝试启动服务。Visualis目前和DSS集成，使用了DSS的登录及权限体系，使用前需部署完成DSS1.0.1版本，可以参考DSS1.0.1一键安装部署。（由于此次visualis-1.0.0-rc1版本属于内测版，如需正常使用，请编译最新的DSS master分支代码）

### 5.1 执行启动脚本

&nbsp;&nbsp;&nbsp;&nbsp;进入Visualis的安装目录，找到bin文件夹，在此文件夹下执行一下命令。
```
sh ./start-server.sh
```
备注：**如果启动服务时，报启动脚本的换行符无法识别，需要在服务器上对脚本进行编码转换使用：dos2unix xxx.sh 命令进行转换**

### 5.1 确认应用启动成功

&nbsp;&nbsp;&nbsp;&nbsp;打开Eureka页面，在注册的服务列表中，找到visualis服务的实例，即可认为服务启动成功。同时也可以查看visualis的服务启动日志，如果没有报错，即服务顺利启动。
```
# 查看服务启动日志
less logs/linkis.out
```

## 6. 部署前端页面
&nbsp;&nbsp;&nbsp;&nbsp;Visualis当前使用前后端分离的部署方案，完成第4步的编译后，把前端包放置在nginx前端包安装路径的dss/visualis路径对应的服务器目录下，启动nginx即可。
&nbsp;&nbsp;&nbsp;&nbsp;Visualis的nginx的前端配置可以参考如下：
```shell
# 在nginx配置参考
server {
    listen       8088; # 访问端口
    server_name  localhost;
    client_max_body_size 100M;

    # ...

    location /dss/visualis { # url路径
    root   /data/dss/web; # Visualis前端静态资源文件目录，可自由指定
    autoindex off;
  }
  
  # ...

}

```

&nbsp;&nbsp;&nbsp;&nbsp;在配置好相应的ngixn配置后，即可安装相应的前端文件。
```shell
cd /data/dss/web # 进入静态资源安装路径
mkdir -p dss/visualis # 建立好文件目录后，上传build.zip包到该文件夹下
unzip build.zip # 解压前端包
cd build
mv * ./../ # 把前端文件移动到上一个目录
sudo nginx # 启动nginx
```

## 7. 字体库
&nbsp;&nbsp;&nbsp;&nbsp;对于邮件报表而言，需要渲染中文字体，其中Visualis截图功能依赖中文字体，在部署的机器上/usr/share/fonts目录下。新建一个visualis文件夹，上传Visualis源码包中ext目录下的pf.ttf文件到visualis文件夹下，执行fc-cache –fv命令刷新字体缓存即可。
```shell
# 需要切换到root用户
sudo su
cd /usr/share/fonts
mkdir visualis

# 上传pf.ttf中文字体库
rz -ybe

# 刷新字体库缓存
fc-cache –fv
```







