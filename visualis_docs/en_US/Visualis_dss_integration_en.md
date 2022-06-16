> Visualis access to DSS/Linkis attention points

## 1. How to use Linkis to connect to Hive data source

&nbsp;&nbsp;&nbsp;&nbsp;Before you start using Visualis, execute the following SQL to insert data to automatically adapt the Hive data source through Linkis:

````sql
INSERT INTO `source` (id,name,description,config,type,project_id,create_by,create_time,update_by,update_time,parent_id,full_parent_id,is_folder,`index`) VALUES(1,'hiveDataSource','','{" parameters":"","password":"","url":"test","username":"hiveDataSource-token"}','hive',-1,null,null,null,null,null, null,null,null);

````
Note: If you want to use the metadata browsing function of Hive in the View editing interface, you need to rely on the metadata module of Linkis.

## 2. How to use the full functionality native to the Davinci project

&nbsp;&nbsp;&nbsp;&nbsp;Visualis is implemented based on the open source project Davinci, but in the scenario of embedding DSS, in order to ensure compatibility, the native function of Davinci has been chosen.
&nbsp;&nbsp;&nbsp;&nbsp;When using Visualis as a BI system alone, you can also access the full functionality of Davinci by adding URL parameters:
````url
http://ip:port/dws/visualis/#/projects?withHeader=true
````

## 3. How to use the custom variable function

&nbsp;&nbsp;&nbsp;&nbsp;Support to define variables on the interface in Davinci way.
&nbsp;&nbsp;&nbsp;&nbsp;Define global variables in the DSS console.
&nbsp;&nbsp;&nbsp;&nbsp;When quoting, use the format of ${variable name}, such as:
````sql
select * from students where class = ${className}
````

## 4. How to send charts by email

&nbsp;&nbsp;&nbsp;&nbsp;The Display/Dashboard node in DSS will obtain the screenshot corresponding to the chart from the Visualis system when sending the content as an email. To ensure that the screenshot function is normal, you need to check the following points:
1. Make sure the DSS installation directory has the dss-appconn Sendemail AppConn directory.
1. According to the default_browser configured in application.yml, confirm that the corresponding selenium driver has been placed in the directory of the deployed server.
1. Confirm that the selenium driver has been configured in the phantomjs_path or chromedriver_path of application.yml (the default is the bin directory of the installation path).
1. Confirm that the user who started Visualis has execute permission on the selenium driver file.