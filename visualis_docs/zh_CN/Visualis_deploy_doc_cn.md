> Visualis的单独安装

## 1. 获取安装包并安装

&nbsp;&nbsp;&nbsp;&nbsp;通过在我们的release安装包里拿到对应模块的安装包：

````bash
 ## 1. 解压安装包
unzip visualis-server.zip
cd visualis-server
````

## 2. 修改配置

&nbsp;&nbsp;&nbsp;&nbsp;包准备好了后，就是修改配置，配置主要修改application.yml和linkis.properties，配置都在conf目录下面

### 2.1 修改application.yml

```yaml
server:
  protocol: http
  address: #该服务所在的机器IP
  port:  #对应的服务端口
  url: #访问Visualis首页的完整http路径
  access: 
    address: #前端部署机器的IP或域名
    port: #前端部署的端口

eureka:
  client:
    serviceUrl:
      defaultZone: $EUREKA_URL #对应的 EUREKA地址

spring:
  application:
    name: visualis  #模块名，用于做高可用（必须）
  ## davinci datasouce config
  datasource:
    url: #应用数据库的JDBC URL
    username: #数据库用户名
    password: #数据库密码

screenshot:
  default_browser: PHANTOMJS    # PHANTOMJS or CHROME
  timeout_second: 1800
  phantomjs_path: ${DAVINCI3_HOME}/bin/phantomjs #selenium phantomjs Linux driver的路径（仅在default_browser选择PHANTOMJS的时候需要填写）
  chromedriver_path: $your_chromedriver_path$   #selenium chrome Linux driver的路径（仅在default_browser选择CHROME的时候需要填写）   
```

### 2.2 修改linkis.properties


```properties
    wds.dss.visualis.gateway.ip=   #Linkis gateway的ip
	wds.dss.visualis.gateway.port= #Linkis gateway的端口
```

## 3. 初始化数据库
&nbsp;&nbsp;&nbsp;&nbsp;在配置对应的数据库中执行安装包内的davinci.sql文件。

## 4. 启动应用

&nbsp;&nbsp;&nbsp;&nbsp;修改完配置后，进入bin目录，进行应用的启动。

### 4.1 执行启动脚本

&nbsp;&nbsp;&nbsp;&nbsp;进入bin目录，执行
```
   ./start-server.sh
```
### 4.1 确认应用启动成功

&nbsp;&nbsp;&nbsp;&nbsp;打开Eureka页面，在注册的服务列表中，找到Visualis，即可认为服务启动成功。如果3分钟内没有找到，可以到logs目录下的visualis.out中寻找错误信息。

## 5. 部署前端页面

&nbsp;&nbsp;&nbsp;&nbsp;Visualis当前使用前后端分离的部署方案，需要下载前端的安装包后，解压到DSS的Nginx配置中/dws/visualis这个URL路径对应的服务器目录下。







