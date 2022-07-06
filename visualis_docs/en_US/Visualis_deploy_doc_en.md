Visualis compile and deploy documentation
------

# 1. Environment preparation and compilation

## 1.1. Dependency environment preparation
| Dependent components | Whether it must be installed | Install through train |
| -------------- | ------ | --------------- |
| MySQL (5.5+) | 必装  | [how to install mysql](https://www.runoob.com/mysql/mysql-install.html) |
| JDK (1.8.0_141) | 必装 | [how to install mysql JDK](https://www.runoob.com/java/java-environment-setup.html) |
| Hadoop(2.7.2，Hadoop 其他版本需自行编译 Linkis) | 必装 | [how to install mysql Hadoop](https://linkis.apache.org/zh-CN/docs/latest/deployment/quick_deploy) ；[how to install mysql Hadoop](https://linkis.apache.org/zh-CN/docs/latest/deployment/quick_deploy) |
| Spark(2.4.3，Spark 其他版本需自行编译 Linkis) | 必装 | [how to install mysql Spark](https://linkis.apache.org/zh-CN/docs/latest/deployment/quick_deploy) |
| DSS1.1.0 | 必装 | [how to install mysql DSS](https://github.com/WeBankFinTech/DataSphereStudio-Doc/blob/1.1.0/zh_CN/%E5%AE%89%E8%A3%85%E9%83%A8%E7%BD%B2/DSS%26Linkis%E4%B8%80%E9%94%AE%E9%83%A8%E7%BD%B2%E6%96%87%E6%A1%A3%E5%8D%95%E6%9C%BA%E7%89%88.md) |
| Linkis1.1.1（大于等于该版本） | 必装 | [how to install mysql Linkis](https://linkis.apache.org/zh-CN/docs/latest/deployment/quick_deploy) |
| Nginx | 必装 | [how to install mysql Nginx](http://nginx.org/en/linux_packages.html) |

## 1.2. Create a Linux user

&nbsp;&nbsp;&nbsp;&nbsp;Please keep the deployment user of Visualis consistent with the deployment user of Linkis, and use hadoop user deployment.  

## 1.3. Low-level dependency component checking

&nbsp;&nbsp;&nbsp;&nbsp;**After installing linkis, please ensure that DSS1.1.0 and Linkis1.1.1 are basically available, you can execute SparkQL scripts on the DSS front-end interface, and you can create and execute DSS workflows normally.**

## 1.4. Download the source package and compile the backend

&nbsp;&nbsp;&nbsp;&nbsp;When installing the Visualis source code, you need to download the corresponding source code package for compilation. At present, the Linkis1.1.1 version that Visualis depends on has been uploaded to the Maven central warehouse. As long as the Maven configuration is normal, the relevant dependencies can be pulled. **DSS 1.1.0 version is being released and has not been uploaded to the Maven central repository. You need to pull 1.1.0 of the DSS repository for compilation, and install the dependencies locally.**

```shell
# 1. Download the source code
git clone https://github.com/WeBankFinTech/Visualis.git

# 2. Switch to the 1.0.0 branch
git checkout 1.0.0

# 3. Execute compilation and packaging
cd Visualis
mvn -N install
mvn clean package -DskipTests=true
````

## 1.5. Compile the frontend
&nbsp;&nbsp;&nbsp;&nbsp;Visualis is a front-end and back-end separation project. Front-end files can be compiled and packaged separately. You need to install npm tools on your computer. You can view [npm installation](https://nodejs.org/en/download/ ), on the windows machine, you can open the Terminal interface of the Idea tool, or use Git bash to complete the front-end compilation.
```shell
# Check if npm is installed
npm -v
>> 8.1.0

cd webapp # Enter the front-end file path
npm i # download front-end dependencies
npm run build # Compile front-end packages

# A build file directory will be generated in the webapp directory, which is the compiled front-end package file

# In the windows environment, compress the build directory into a zip file
````

## 2. Install and deploy
## 2.1. Install the backend
&nbsp;&nbsp;&nbsp;&nbsp;Visualis uses assembly as a packaging plug-in. After compiling, go to the Visualis/assembly/target directory to find the compiled visualis-server.zip package.
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
&nbsp;&nbsp;&nbsp;&nbsp;On the server to be deployed (or the server deployed by DSS), upload the visualis-server.zip package, and decompress it on the path to be deployed to complete the Visualis installation.

## 2.2. Initialize the database
&nbsp;&nbsp;&nbsp;&nbsp;The compilation package of Visualis is installed by decompression, and the related SQL files are not executed. Therefore, in the normal installation steps, you need to create a visualis database and execute the visualis related table building statement.
&nbsp;&nbsp;&nbsp;&nbsp;Relevant table building statements can be found in the source code, enter the root directory of the source code, find the db folder, connect to the corresponding database, execute the following SQL file, and create the required use of Visualis surface.
```shell
# Find the corresponding sql file in the source package db directory

# Connect to the visualis database
mysql -h 127.0.0.1 -u hadoop -d visualis -P3306 -p

source ${visualis_home}/davinci.sql
source ${visualis_home}/ddl.sql

# Where davinci.sql is the davinci table that visualis needs to use
# ddl.sql is a table that visualis additionally depends on
````


## 2.3. Font library installation
&nbsp;&nbsp;&nbsp;&nbsp;For mail reports, Chinese fonts need to be rendered, and the Visualis screenshot function depends on Chinese fonts, which are located in the /usr/share/fonts directory on the deployed machine. Create a new visualis folder, upload **pf.ttf in the ext directory of the Visualis source package to the visualis folder**, and execute the fc-cache –fv command to refresh the font cache.
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
&nbsp;&nbsp;&nbsp;&nbsp;When using visualis, when calling the preview function or executing Display and Dashboard in the workflow, if an error is reported: **error while loading shared libraries: libfontconfig.so.1: cannot open shared object file : No such file or directory**, an error is reported due to the lack of dependencies on the machine where visualis is deployed. Execute **sudo yum -y install fontconfig-devel** to install the dependencies.


## 2.4 Install the front end

&nbsp;&nbsp;&nbsp;&nbsp;In order to better explain the front-end configuration, first give the configuration of nginx, the front-end configuration and description of nginx of visualis:
```shell
server {
    
    listen 8088;# a. access port
    server_name localhost;

  location /dss/linkis { # b. static file directory of the linkis console
    root /data/dss_linkis/web;
    autoindex on;
  }
  
  location /dss/visualis { # c. Front-end access path, which needs to be created manually
    root /data/dss_linkis/web; # d. Visualis front-end static resource file directory, which can be freely specified
    autoindex off;
  }

  location / { # e.dss static file directory
    root /data/dss_linkis/web/dist;
    index index.html index.html;
  }

  location /ws {
    proxy_pass http://127.0.0.1:9001; # f. linkis gateway address
    # ...
  }

  location /api {
    proxy_pass http://127.0.0.1:9001; # g. linkis gateway address
    # ...
  }
}
```
**The above configuration c and d small items.**
```shell
# Configure the root path of static resources (used to configure the root parameter of nginx, that is, the d item)
cd /data/dss_linkis/web

# In the previous step /data/dss_linkis/web directory, configure the front-end access url path address (ie the c small item, if not, you need to create it)
cd dss/visualis

# Upload Visualis front-end package
rz -ybe build.zip

unzip build.zip # Unzip the front-end package

cd build # Enter to the decompression path

mv * ./../ # Move the static resource files to the c-item dss/visualis path
````
&nbsp;&nbsp;&nbsp;&nbsp;After the front-end deployment configuration, you can restart nginx or refresh the nginx configuration to make the above configuration take effect**sudo nginx -s reload.**


## 2.5. Modify configuration

### 2.5.1. Modify application.yml
&nbsp;&nbsp;&nbsp;&nbsp;In the configuration application.yml file, configuration items 1, 2, and 3 must be configured, and other configurations can use the default values. In item 1, you need to configure some deployment IP and port information , the second item needs to configure the information of eureka, and the third item only needs to configure the link information of the database.**(The library of visualis can be the same as the library of dss, or it can be different, the deployment user needs to choose by himself)**.
````yaml
# ####################################
# 1. Visualis Service configuration
# ####################################
server:
  protocol: http
  address: 127.0.0.1 # server ip address (the IP of the machine where the service is deployed)
  port: 8008 # server port (visualis service process port)
  url: http://127.0.0.1:8088/dss/visualis # frontend index page full path (the full path of the frontend to access visualis)
  access:
    address: 127.0.0.1 # frontend address (front-end deployment IP)
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
    url: jdbc:mysql://127.0.0.1:3306/visualis?characterEncoding=UTF-8&allowMultiQueries=true # Configuration required
    username: hadoop
    password: hadoop

# Keep other parameters as default, if you don't need customized modification, just use the default parameters
````

### 2.5.2. Modify linkis.properties
````properties
# ####################################
# 1. need configuration
# need to configure
# ####################################
wds.linkis.gateway.url=http://127.0.0.1:9001

# Others can use default parameters
# Omit configuration
````
&nbsp;&nbsp;&nbsp;&nbsp;**If the deployed hadoop cluster has Kerberos enabled, you need to enable Kerberos in the visualis configuration file linkis.properties file, and add the configuration items:**
````properties
wds.linkis.keytab.enable=true
````

## 3. Start the application

&nbsp;&nbsp;&nbsp;&nbsp;After configuring and compiling the frontend package, you can try to start the service. Visualis is currently integrated with DSS and uses the DSS login and permission system. Before use, the DSS1.1.0 version needs to be deployed. You can refer to DSS1.1.0 one-click installation and deployment.

### 3.1. Execute the startup script

&nbsp;&nbsp;&nbsp;&nbsp;Enter the Visualis installation directory, find the bin folder, and execute the following command in this folder.
````
sh ./start-server.sh
````
Note: **If the newline character of the startup script cannot be recognized when the service is started, you need to convert the script on the server and use: dos2unix xxx.sh command to convert**

### 3.2. Confirm that the application starts successfully

&nbsp;&nbsp;&nbsp;&nbsp;Open the Eureka page, find the instance of the visualis service in the list of registered services, and then consider the service to start successfully. At the same time, you can also view the service startup log of visualis. If no error is reported, the service starts successfully.
````
# View service startup log
less logs/linkis.out
````
&nbsp;&nbsp;&nbsp;&nbsp;Check the Eureka page to see if the service is successfully registered.
![](./../images/visualis_eureka.png)

## 4. AppConn installation
&nbsp;&nbsp;&nbsp;&nbsp;After the Visualis service is deployed, it needs to be connected with the DSS application store and workflow, and the corresponding AppConn needs to be installed on the DSS side. Please refer to [VisualisAppConn Installation](./Visualis_appconn_install_cn.md).

## 5. Visualis configuration instructions for domain name access to DSS (optional)
&nbsp;&nbsp;&nbsp;&nbsp;In actual production, access to DSS generally uses a domain name for access. When readers read the visualis installation and deployment documents and appconn's deployment documents, they will find that there are several front-end configurations in the visualis configuration. These front-end configurations affect Preview function and mail report function.
&nbsp;&nbsp;&nbsp;&nbsp;If you use a domain name, you need to pay attention to the following configuration:
1. When installing AppConn, when specifying the access ip and port of visualis appconn, you can write an analog value first. After the installation is complete, modify the url field of the dss_appconn_instance table to the domain name value, similar to: http://dss.bdp.com/ (note the trailing slash /, which cannot be omitted when configuring).
2. In the configuration file application.yml of the Visualis service, the specified front-end ip and port need to be specified as the ip of the front-end nginx server and the visualis port configured by nginx.



## 6. Log configuration (optional)
&nbsp;&nbsp;&nbsp;&nbsp;In the actual usage scenario, depending on the linkis.out log output scenario is not in compliance with the specification, the log file is not rolled back, and long-term operation is likely to cause the production server disk capacity alarm, which will bring production problems , at present, we can optimize the log printing by modifying the log configuration. The log configuration can be modified as follows:
````properties
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
````