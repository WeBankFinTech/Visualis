> Visualis AppConn安装

## 1. AppConn安装
&nbsp;&nbsp;&nbsp;&nbsp;DSS在编译打包时，会自动编译打包好Visualis AppConn物料包，Visualis AppConn的安装，只涉及到AppConn相关库表的配置。  
&nbsp;&nbsp;&nbsp;&nbsp;如果是使用[DSS一键安装全家桶](https://osp-1257653870.cos.ap-guangzhou.myqcloud.com/WeDatasphere/DataSphereStudio/1.0.1/DSS-Linkis%E5%85%A8%E5%AE%B6%E6%A1%B620220223.zip)来部署的服务，可以直接使用其软件包中提供的脚本工具。在一键全家桶部署完成后，可以在dss的安装目录下找到脚本工具，其目录结构和使用说明如下。
```shell
# 进入到dss安装的bin目录下
>> cd dss/bin

# 其中appconn-install.sh就是AppConn安装脚本工具
>> ls
>> appconn-install.sh  checkEnv.sh  excecuteSQL.sh  install.sh
```
&nbsp;&nbsp;&nbsp;&nbsp;为了能够安装顺利，首先需要部署完成Visualis服务，确保服务正常启动。Visualis的安装部署可以参考[Visualis安装部署文档](./Visualis_deploy_doc_cn.md)，使用AppConn安装脚本工具时步骤如下：
```shell
cd {DSS_INSTALL_HOME}/dss/bin

>> sh appconn-install.sh

# 输入Visualis名称
>> visualis

# 输入Visualis前端IP地址
>> 127.0.0.1

# 输入Visualis服务的前端端口后
>> 8989

# 在执行AppConn安装脚本工具后，会插入相关第三方AppConn的配置信息
```
&nbsp;&nbsp;&nbsp;&nbsp;修改完成后需要重启DSS服务。
