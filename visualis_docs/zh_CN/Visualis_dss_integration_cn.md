> Visualis接入DSS/Linkis注意点

## 1. 如何使用Linkis连接Hive数据源

&nbsp;&nbsp;&nbsp;&nbsp;在开始使用Visualis前，执行以下SQL插入数据，即可自动通过Linkis适配Hive数据源：

````sql
INSERT INTO `source` (id,name,description,config,type,project_id,create_by,create_time,update_by,update_time,parent_id,full_parent_id,is_folder,index) VALUES (1,'hiveDataSource','','{"parameters":"","password":"","url":"","username":"hiveDataSource-token"}','hive',-1,null,null,null,null,null,null,null,null);

````
注意：如果要在View编辑界面使用Hive的元数据浏览功能，需要依赖Linkis的metadata模块。

## 2. 如何使用Davinci项目原生的完整功能

&nbsp;&nbsp;&nbsp;&nbsp;Visualis基于开源项目Davinci实现，但在嵌入DSS的场景下，为了保证兼容性，对Davinci的原生功能做了取舍。
&nbsp;&nbsp;&nbsp;&nbsp;在将Visualis作为BI系统单独使用时，也可以通过添加URL参数的方式访问Davinci的完整功能：
````url
http://ip:port/dws/visualis/#/projects?withHeader=true
````

## 3. 如何使用自定义变量功能

&nbsp;&nbsp;&nbsp;&nbsp;支持以Davinci的方式在界面上定义变量。
&nbsp;&nbsp;&nbsp;&nbsp;支持在DSS的控制台中定义全局变量。
&nbsp;&nbsp;&nbsp;&nbsp;引用时，使用${变量名}的格式，如：
````sql
select * from students where class = ${className}
````

## 4. 如何使用邮件发送图表功能

&nbsp;&nbsp;&nbsp;&nbsp;DSS中的Display/Dashboard节点，在作为邮件发送内容时，会从Visualis系统获取图表对应的截图，为了确保截图功能正常，需检查以下几点：
1. 确认linkis-appjoint-entrance中已经添加sendmail与visualis的appjoint。
1. 根据在application.yml中的配置的default_browser，确认已经将对应的selenium driver放在部署的服务器的目录下
1. 确认selenium driver已配置在application.yml的phantomjs_path或chromedriver_path中(默认为安装路径的bin目录下)
1. 确认启动Visualis的用户对selenium driver文件具有执行权限









