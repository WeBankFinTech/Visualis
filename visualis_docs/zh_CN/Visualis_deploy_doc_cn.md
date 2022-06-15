Visualis编译部署文档
------

# 1. 环境准备及编译

## 1.1. 依赖环境准备
| 依赖的组件 | 是否必装 | 安装直通车 |
| -------------- | ------ | --------------- |
| MySQL (5.5+) | 必装  | [如何安装mysql](https://www.runoob.com/mysql/mysql-install.html) |
| JDK (1.8.0_141) | 必装 | [如何安装JDK](https://www.runoob.com/java/java-environment-setup.html) |
| Hadoop(2.7.2，Hadoop 其他版本需自行编译 Linkis) | 必装 | [Hadoop单机部署](https://linkis.apache.org/zh-CN/docs/latest/deployment/quick_deploy) ；[Hadoop分布式部署](https://linkis.apache.org/zh-CN/docs/latest/deployment/quick_deploy) |
| Spark(2.4.3，Spark 其他版本需自行编译 Linkis) | 必装 | [Spark快速安装](https://linkis.apache.org/zh-CN/docs/latest/deployment/quick_deploy) |
| DSS1.0.1 | 必装 | [如何安装DSS](https://github.com/WeBankFinTech/DataSphereStudio-Doc/blob/main/zh_CN/%E5%AE%89%E8%A3%85%E9%83%A8%E7%BD%B2/DSS%E5%8D%95%E6%9C%BA%E9%83%A8%E7%BD%B2%E6%96%87%E6%A1%A3.md) |
| Linkis1.1.1 | 必装 | [如何安装Linkis](https://linkis.apache.org/zh-CN/docs/latest/deployment/quick_deploy) |
| Nginx | 必装 | [如何安装 Nginx](http://nginx.org/en/linux_packages.html) |

## 1.2. 创建 Linux 用户

&nbsp;&nbsp;&nbsp;&nbsp;请保持Visualis的部署用户与Linkis的部署用户一致，采用hadoop用户部署。

## 1.3. 底层依赖组件检查

&nbsp;&nbsp;&nbsp;&nbsp;**请确保 DSS1.0.1 与 Linkis1.1.1 基本可用，可在 DSS 前端界面执行 SparkQL 脚本，可正常创建并执行 DSS 工作流。**

## 1.4. 下载源码包及编译后端
&nbsp;&nbsp;&nbsp;&nbsp;Visualis源码安装时，需要下载对应的源码包进行编译，目前Visualis在依赖的DSS 1.0.1版本和Linkis1.1.1版本已经上传到Maven中央仓库，只需Maven配置正常即可拉取相关依赖。
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

## 1.5. 编译前端
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

## 2. 安装部署
## 2.1. 安装后端
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

## 2.2. 初始化数据库
&nbsp;&nbsp;&nbsp;&nbsp;在初始化数据库前，需要注意，由于历史原因Visualis复用了DSS的用户权限体系，及使用了DSS的linkis_user表，所以在部署时，Visualis需要配置和DSS一样的数据库，如果分库实现，在使用时需要定时同步DSS用户到Visualis库的linkis_user表中），建好Visualis所依赖的表，进入到源码的跟目录，找到db文件夹，在链接到对应的数据库后，需要执行以下SQL文件，建立Visualis使用时需用到的表。
```shell
# 在源码包中找到对应的sql文件

# 链接visualis数据库（和DSS使用同一个库）
mysql -h 127.0.0.1 -u hadoop -d visualis -P3306 -p

source ${visualis_home}/davinci.sql
source ${visualis_home}/ddl.sql

# 其中davinci.sql是visualis需要使用到的davinci的表
# ddl.sql是visualis额外依赖的表
```


## 2.3. 字体库安装
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

## 2.4 安装前端
&nbsp;&nbsp;&nbsp;&nbsp;Visualis当前使用前后端分离的部署方案，完成前端编译后，把前端包放置在nginx前端包安装路径的dss/visualis路径对应的服务器目录下。

```shell

# 配置静态资源根路径（没有则需要创建）
cd /data/dss/web

# 在上一步/data/dss/web目录下，配置前端访问url路径地址（没有则需要创建）
cd dss/visualis

unzip build.zip # 解压前端包

cd build # 进入到解压路径

mv * ./../ # 把静态资源文件移动visualis路径下
```

&nbsp;&nbsp;&nbsp;&nbsp;根据上一步前端部署的内容，Visualis的nginx的前端配置可以参考如下：
```shell
# 在nginx配置参考
# 补充linkis gateway

# 换个端口
server {
    listen       8989; # 访问端口
    server_name  localhost;
    client_max_body_size 100M;

    # ...
    location /dss/visualis { # 前端访问路径，需要手动创建
    root   /data/dss/web; # Visualis前端静态资源文件目录，可自由指定
    autoindex off;
  }

  location /ws {
    proxy_pass http://127.0.0.1:9001; # Linkis gateway地址
    # ...
  }

  location /api {
    proxy_pass http://127.0.0.1:9001; # Linkis gateway地址
    # ...
  }
}
```

## 2.5. 修改配置

### 2.5.1. 修改application.yml
&nbsp;&nbsp;&nbsp;&nbsp;在配置application.yml文件中，必须要配置的有1、2、3配置项，其它配置可采用默认值，其中第1项中，需要配置一些部署IP和端口信息，第2项需要配置eureka的信息，第3项中只需要配置数据库的链接信息即可（其它参数可以保持默认值）。
&nbsp;&nbsp;&nbsp;&nbsp;**需要注意，由于历史原因Visualis复用了DSS的用户权限体系，及使用了DSS的linkis_user表，所以在部署时，Visualis需要配置和DSS一样的数据库，如果分库实现，在使用时需要定时同步DSS用户到Visualis库的linkis_user表中。**
```yaml
# ##################################
# 1. Visualis Service configuration
# ##################################
server:
  protocol: http
  address: 127.0.0.1 # server ip address（服务部署的机器IP）
  port:  9008 # server port（服务部署的端口）
  url: http://127.0.0.1:8989/dss/visualis # frontend index page full path（前端访问路径）
  access:
    address: 127.0.0.1 # frontend address（前端部署IP）
    port: 8989 # frontend port（前端部署端口）


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
  datasource: # 需要配置和DSS一个数据库
    url: jdbc:mysql://127.0.0.1:3306/dss?characterEncoding=UTF-8&allowMultiQueries=true # Configuration required
    username: hadoop
    password: hadoop

# 其它参数保持默认，如果不需要定制化修改，采用默认参数即可
```

### 2.2.2 修改linkis.properties
```properties
# ##################################
# 1. need configuration
#    需要配置
# ##################################
wds.linkis.gateway.url=http://127.0.0.1:9001/

# 其它参数使用配置文件中的默认配置即可
# 配置省略
```

## 3. 启动应用

&nbsp;&nbsp;&nbsp;&nbsp;在配置和前端包编译完成后，可以尝试启动服务。Visualis目前和DSS集成，使用了DSS的登录及权限体系，使用前需部署完成DSS1.0.1版本，可以参考DSS1.0.1一键安装部署。（**由于此次visualis-1.0.0-rc1版本属于内测版，如需正常使用，请编译最新的DSS master分支代码**）

### 3.1 执行启动脚本

&nbsp;&nbsp;&nbsp;&nbsp;进入Visualis的安装目录，找到bin文件夹，在此文件夹下执行一下命令。
```
sh ./start-server.sh
```
备注：**如果启动服务时，报启动脚本的换行符无法识别，需要在服务器上对脚本进行编码转换使用：dos2unix xxx.sh 命令进行转换**

### 3.2 确认应用启动成功

&nbsp;&nbsp;&nbsp;&nbsp;打开Eureka页面，在注册的服务列表中，找到visualis服务的实例，即可认为服务启动成功。同时也可以查看visualis的服务启动日志，如果没有报错，即服务顺利启动。
```
# 查看服务启动日志
less logs/linkis.out
```

## 4. AppConn安装
&nbsp;&nbsp;&nbsp;&nbsp;Visualis服务部署后，需要和DSS应用商店和工作流打通，需要在DSS侧安装对应的AppConn，可参考[VisualisAppConn安装](./Visualis_appconn_install_cn.md)。

## 5. 日志配置（可选）
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





