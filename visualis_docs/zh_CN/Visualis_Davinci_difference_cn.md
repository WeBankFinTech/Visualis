> Visualis与Davinci的功能区别

## 1. 自定义变量格式

Davinci的自定义变量格式默认为$variableName$的方式，并支持在配置中对默认格式进行修改，而Visualis中，变量一律为${variableName}格式，且无法修改，此格式与Linkis的自定义变量一致。如：

````sql
select * from students where class = ${className}
````

## 2. 组织与权限功能

&nbsp;&nbsp;&nbsp;&nbsp;在将Visualis作为DSS的内嵌模块使用时，组织和权限功能被移除了。如果需要单独使用与Davinci一致的组织和权限功能，可以通过以下url参数的形式，在单独的页面访问Visualis。
````url
http://ip:port/dws/visualis/#/projects?withHeader=true
````

## 3. 邮件定时发送功能

&nbsp;&nbsp;&nbsp;&nbsp;DataSphere Studio的工作流中，提供了SendMail节点，支持将Visualis中的Dashboard和Display作为邮件发送内容。
&nbsp;&nbsp;&nbsp;&nbsp;Davinci的原有的邮件定时任务功能，在Visualis中保持不变。


## 4. Dashboard与Display的预览功能

&nbsp;&nbsp;&nbsp;&nbsp;出于用户需要对邮件实际发送的图片进行验证的需求，Visualis将Dashboard/Display编辑界面上的预览按钮跳转的页面，变为了显示该Dashboard/Display的实际截图。


## 5. 用户管理与登录

&nbsp;&nbsp;&nbsp;&nbsp;不再支持Davinci原生的登录和用户管理方式。Visualis与DataSphere Studio共享用户session，从DSS的登录页面登录后，即可无缝跳转到Visualis。
&nbsp;&nbsp;&nbsp;&nbsp;在数据库层面，Visualis的用户改为从linkis_user表中读取。

## 6. 项目

&nbsp;&nbsp;&nbsp;&nbsp;与Davinci不同，Visualis的项目可以没有所属组织，允许只属于个人的项目存在。
&nbsp;&nbsp;&nbsp;&nbsp;Visualis的项目与DSS的项目保持完全同步，在数据库层面，从visualis_project表中读取。

## 7. SQL分割提交

&nbsp;&nbsp;&nbsp;&nbsp;Davinci中通过JDBC执行SQL时，如果一个View中包含多个SQL语句，这些语句将被按顺序分隔，每次仅提交执行一条语句。
&nbsp;&nbsp;&nbsp;&nbsp;Visualis中，通过JDBC执行的逻辑保持不变。但通过Linkis提交Spark-SQL对Hive数据源进行查询时，为了保证同一个View的SQL被提交到同一个引擎执行，在Visualis中不再对SQL语句进行分隔，即每个View中的语句将被一起提交给Linkis，在分配给具体的引擎进行执行后，由引擎按顺序分割执行。

## 8. 与DSS工作流打通
&nbsp;&nbsp;&nbsp;&nbsp;Davinci不支持工作流调度。
&nbsp;&nbsp;&nbsp;&nbsp;DSS支持拖拽式开发visualis可视化报表，支持与DSS数据开发节点协调widget, display, dashboard节点开发。并且可以一键发布执行调度，并发送邮件。


