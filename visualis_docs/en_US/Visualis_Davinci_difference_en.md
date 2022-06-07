> Functional differences between visualis and DaVinci

## 1. Custom variable format

DaVinci's custom variable format is $variablename$by default, and supports modifying the default format in the configuration. In visualis, variables are all in the ${variablename} format and cannot be modified. This format is consistent with the custom variables of linkis. For example:

````sql
select * from students where class = ${className}
````

## 2. Organization and permission functions

&nbsp;&nbsp;&nbsp;&nbsp;When visualis is used as an embedded module of DSS, the organization and permission functions are removed. If you need to use the organization and permission functions consistent with DaVinci separately, you can access visualis on a separate page in the form of the following URL parameters.
````url
http://ip:port/dws/visualis/#/projects?withHeader=true
````

## 3. Regular mail sending function

&nbsp;&nbsp;&nbsp;&nbsp;In the workflow of datasphere studio, the sendmail node is provided to support sending the dashboard and display in visualis as mail content.
&nbsp;&nbsp;&nbsp;&nbsp;DaVinci's original mail scheduled task function remains unchanged in visualis.


## 4.Preview function of dashboard and display

&nbsp;&nbsp;&nbsp;&nbsp;In order to verify the pictures actually sent by the mail, visualis changes the page to which the preview button on the dashboard/display editing interface jumps to display the actual screenshot of the dashboard/display.


## 5. User management and login

&nbsp;&nbsp;&nbsp;&nbsp;DaVinci's native login and user management methods are no longer supported. Visualis shares a user session with datasphere studio. After logging in from the DSS login page, you can seamlessly jump to visualis.
&nbsp;&nbsp;&nbsp;&nbsp;At the database level, the users of visualis are changed from linkis_ Read from the user table.

## 6. project

&nbsp;&nbsp;&nbsp;&nbsp;Unlike DaVinci, visualis projects can have no organization and allow projects that belong only to individuals to exist.
&nbsp;&nbsp;&nbsp;&nbsp;The visualis project is fully synchronized with the DSS project. At the database level,read from the Visualis_project table.

## 7. SQL split commit

&nbsp;&nbsp;&nbsp;&nbsp;When executing SQL through JDBC in DaVinci, if a view contains multiple SQL statements, these statements will be separated in order, and only one statement will be submitted for execution at a time.
&nbsp;&nbsp;&nbsp;&nbsp;In visualis, the logic executed through JDBC remains unchanged. However, when spark SQL is submitted through linkis to query hive data sources, in order to ensure that the SQL of the same view is submitted to the same engine for execution, the SQL statements will not be separated in visualis, that is, the statements in each view will be submitted to linkis together. After being allocated to a specific engine for execution, the engine will execute them separately in order.

## 8. Connect with DSS workflow
&nbsp;&nbsp;&nbsp;&nbsp;DaVinci does not support workflow scheduling.
&nbsp;&nbsp;&nbsp;&nbsp;DSS supports drag and drop development of visualisation visual reports, and supports coordination with DSS data development nodes for widget, display, and dashboard node development. And you can publish the execution schedule with one click and send mail.
