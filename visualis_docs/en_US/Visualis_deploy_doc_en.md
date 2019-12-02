> How to deploy Visualis

## 1. Get installation package and deploy

&nbsp;&nbsp;&nbsp;&nbsp;Get the latest installation package from our Github releases, then：

````bash
 ## 1. Unzip the installation package
unzip visualis-assembly-0.5.0-dist-beta.7.zip
cd visualis-assembly-0.5.0-dist-beta.7
````

## 2. Modify configurations

&nbsp;&nbsp;&nbsp;&nbsp; After installation directories ready, follow below steps to modify configurations. (Basically application.yml and linkis.properties under conf directory)

### 2.1 Modify application.yml

```yaml
server:
  protocol: http
  address: #The IP address of the deployment machine
  port:  #The port of this service
  url: #The full path to vist Visualis index page
  access: 
    address: #The IP or host name of frontend address
    port: #The port of frontend address

eureka:
  client:
    serviceUrl:
      defaultZone: $EUREKA_URL #The eureka address

spring:
  application:
    name: visualis  #Service name
  ## davinci datasouce config
  datasource:
    url: #The JDBC url of the application database
    username: #The user name of the application database
    password: #The password of the application database

screenshot:
  default_browser: PHANTOMJS    # PHANTOMJS or CHROME
  timeout_second: 1800
  phantomjs_path: ${DAVINCI3_HOME}/bin/phantomjs #selenium phantomjs Linux driver path（only need to be filled if PHANTOMJS is chosen for default_browser）
  chromedriver_path: $your_chromedriver_path$   #selenium chrome Linux driver path（only need to be filled if CHROME is chosen for default_browser）   
```

### 2.2 Modify linkis.properties


```properties
    wds.dss.visualis.gateway.ip=   #Linkis gateway ip 
	  wds.dss.visualis.gateway.port= #Linkis gateway port
```

## 3. Initialize database
&nbsp;&nbsp;&nbsp;&nbsp; Execute the statements of davinci.sql in the application database. This file can be got from both the release package or source code.

## 4. Start the application

&nbsp;&nbsp;&nbsp;&nbsp; After modifying configurations, enter the bin directory and start the application.

### 4.1 Execute the start script

&nbsp;&nbsp;&nbsp;&nbsp;Enter bin deirectory, execute 
```
   ./start-server.sh
```
### 4.1 Confirm that the application is successfully started

&nbsp;&nbsp;&nbsp;&nbsp;Open Eureka web page, if Visualis was found on the registered server list, it can be concluded that the application has been started successfully. If Visualis was not found after 3 minutes, please got to logs directory and open visualis.out to find error messages.

## 5. Deploy frontend pages

&nbsp;&nbsp;&nbsp;&nbsp;Visualis frontend pages should be deployed separeted with backend services. Download the installation package and unzip to /dss/visualis directory under the path configured in nginx.







