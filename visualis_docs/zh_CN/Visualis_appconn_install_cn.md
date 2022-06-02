> Visualis AppConn安装

## 1. 简介
&nbsp;&nbsp;&nbsp;&nbsp;Visualis是基于开源项目Davinci二次开发的一个数据可视化系统，为了实现与DSS打通，Visualis实现了DSS AppConn接入的相关规范，对于App Conn接入规范可以参考[DSS AppConn接入规范]()，Visualis作为数据可视化节点接入DSS，其需要满足三级规范，即：  
* 为了实现与DSS免密互通，实现了SSO规范
* 为了实现与DSS工程打通，实现了组织结构规范
* 为了实现与DSS工作流开发互通，实现了应用开发规范  

&nbsp;&nbsp;&nbsp;&nbsp;AppConn是运行在其DSS和Linkis内部的一个JVM进程，对于每一个接入进来的第三方应用都存在一个其独有的AppConn实例，其作用类似于一个第三方应用在DSS中的一个代理客户端，如果DSS需要使用与第三方系统的交互，可调用相关的AppConn，在AppConn中去请求第三方应用，来实现交互，可以通过下图可以直接的了解AppConn的作用。  
![AppConn](../images/appconn.png)

&nbsp;&nbsp;&nbsp;&nbsp;为了在DSS工作流中使用Visualis的相关功能，所以需要安装相应的AppConn，以便在DSS工作流中开发Visualis可视化报表。

## 2. 一键安装包安装
&nbsp;&nbsp;&nbsp;&nbsp;如果是使用[DSS一键安装全家桶]()来部署的服务，可以直接使用其软件包中提供的脚本工具。在一键全家桶部署完成后，可以在dss的安装目录下找到脚本工具，其目录结构和使用说明如下。
```shell
# 进入到dss安装的bin目录下
>> cd dss/bin

# 其中appconn-install.sh就是AppConn安装脚本工具
>> ls
>> appconn-install.sh  checkEnv.sh  excecuteSQL.sh  install.sh
```
&nbsp;&nbsp;&nbsp;&nbsp;为了能够安装顺利，首先我们需要部署和启动Visualis服务。Visualis的安装部署可以参考[Visualis安装部署文档]()，使用AppConn安装脚本工具时步骤如下：
```shell
>> sh appconn-install.sh

# 选择安装Visualis AppConn
>> 1

# 输入Visualis部署的服务器IP
>> 127.0.0.1

# 输入Visualis服务的端口号
>> 8008

# 在执行AppConn安装脚本工具后，会插入相关第三方AppConn的配置信息
```

## 3. 独立安装
&nbsp;&nbsp;&nbsp;&nbsp;如果是独立部署的DSS和Linkis服务，需要额外的新增Visualis来使用，可以选择独立安装AppConn。下载DSS一键安装全家桶，解压后，在dss/bin中找到appconn-install.sh脚本，把该脚本放置在安装完成的DSS路径下的bin目录下(需要确保源码编译安装的DSS其AppConn的包也是完好的)。
```shell
# 复制解压后的一键安装全家桶appconn安装脚本工具到安装的DSS bin目录下
>> cp appconn-install.sh /appconn/Install/DSSInstall/bin/

# 执行该脚本
>> sh appconn-install.sh

# 类似于第2步中的步骤
...

```