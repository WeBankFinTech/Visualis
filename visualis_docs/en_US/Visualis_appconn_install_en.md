> Visualis AppConn Installation

## 1. AppConn installation
&nbsp;&nbsp;&nbsp;&nbsp;The third-party component AppConn of DSS1.1.0 is maintained by the third-party component itself, so in order to successfully install visualis and support the DSS workflow, you need to pull the visualis1.0.0 code, compile and package the AppConn code.
```shell
# Enter the visualis source code project
cd visualis

# Enter the visualis-appconn module
cd visualis-appconn

mvn clean package -DskipTests=ture
```
&nbsp;&nbsp;&nbsp;&nbsp;The visualis.zip package as shown below is the package of visualis-appconn.  
![](./../images/visualis_appconn.jpg)
&nbsp;&nbsp;&nbsp;&nbsp;If you use [DSS one-click installation of the whole family bucket](https://github.com/WeBankFinTech/DataSphereStudio-Doc/blob/1.1.0/zh_CN/%E5%AE%89%E8%A3%85%E9%83%A8%E7%BD%B2/DSS%26Linkis%E4%B8%80%E9%94%AE%E9%83%A8%E7%BD%B2%E6%96%87%E6%A1%A3%E5%8D%95%E6%9C%BA%E7%89%88.md) to deploy the service, you can directly use the script tool provided in its software package. After the one-click family bucket deployment is complete, you can find the script tool in the dss installation directory. Its directory structure and usage instructions are as follows.
```shell
# Go to the bin directory of the dss installation
> cd dss/bin

# Where appconn-install.sh is the AppConn installation script tool
>> ls
>> appconn-install.sh appconn-refresh.sh checkEnv.sh executeSQL.sh install.sh start-default-appconn.sh
````
&nbsp;&nbsp;&nbsp;&nbsp;In order to install smoothly, the Visualis service needs to be deployed first, and then the zip package of visualis appconn needs to be placed in the specified appconn directory and decompressed. For the installation and deployment of Visualis, please refer to [Visualis Installation and Deployment Documentation](./Visualis_deploy_doc_cn.md). The steps for placing the visualis appconn zip package and the AppConn installation script tool are as follows:
```shell
# Put visualis appconn in the dss-appconns directory
rz -ybe ${DSS_INSTALL_HOME}/dss/dss-appconns

# Unzip the Visualis AppConn package
unzip visualis.zip

cd {DSS_INSTALL_HOME}/dss/bin

> sh appconn-install.sh

# Enter the Visualis name
>> visualis

# Enter the Visualis frontend IP address
>> 127.0.0.1

# After entering the front-end port of the Visualis service
>> 8088

# After executing the AppConn installation script tool, the configuration information of the relevant third-party AppConn will be inserted
````
&nbsp;&nbsp;&nbsp;&nbsp;DSS service needs to be restarted after modification.
&nbsp;&nbsp;&nbsp;&nbsp;If you use the domain name to access the DSS service, you need to refer to Section 5 of the [visualis installation and deployment document](./Visualis_deploy_doc_cn.md).