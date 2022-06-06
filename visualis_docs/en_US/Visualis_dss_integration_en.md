> Considerations for visualis accessing dss/linkis

## 1. How to use linkis to connect hive data sources

&nbsp;&nbsp;&nbsp;&nbsp;Before starting to use visualis, execute the following SQL insert data to automatically adapt hive data sources through linkis:

````sql
INSERT INTO `source` (id,name,description,config,type,project_id,create_by,create_time,update_by,update_time,parent_id,full_parent_id,is_folder,`index`) VALUES (1,'hiveDataSource','','{"parameters":"","password":"","url":"test","username":"hiveDataSource-token"}','hive',-1,null,null,null,null,null,null,null,null);

````
Note: if you want to use hive's metadata browsing function in the view editing interface, you need to rely on the metadata module of linkis.

## 2. How to use the complete functionality native to the DaVinci project

&nbsp;&nbsp;&nbsp;&nbsp;Visualis is implemented based on the open source project DaVinci. However, in the scenario of embedded DSS, in order to ensure compatibility, the native functions of DaVinci are selected.
&nbsp;&nbsp;&nbsp;&nbsp;When visualis is used separately as a BI system, you can also access the complete functions of DaVinci by adding URL parameters:
````url
http://ip:port/dws/visualis/#/projects?withHeader=true
````

## 3. How to use the custom variable function

&nbsp;&nbsp;&nbsp;&nbsp;Variables can be defined on the interface in DaVinci mode.
&nbsp;&nbsp;&nbsp;&nbsp;Global variables can be defined in the console of DSS.
&nbsp;&nbsp;&nbsp;&nbsp;When referencing, use the format of ${variable name}, such as:
````sql
select * from students where class = ${className}
````

## 4. How to use the e-mail chart function

&nbsp;&nbsp;&nbsp;&nbsp;The display/dashboard node in the DSS will obtain the screenshot corresponding to the chart from the visualis system when sending content as an email. To ensure that the screenshot function is normal, check the following points:
1. Confirm that the appjoints of sendmail and visualis have been added to the linkis appjoint entry.
1. According to the application Default of configuration in YML_ Browser, and confirm that the corresponding selenium driver has been placed in the directory of the deployed server
1. Confirm that selenium driver is configured in application Phantomjs of YML_ Path or chromedriver_ Path (the default is the bin directory of the installation path)
1. Verify that the user who started visualis has execute permission on the selenium driver file