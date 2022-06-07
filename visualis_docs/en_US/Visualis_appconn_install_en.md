> Visualis AppConn Install

## 1. 简介
&nbsp;&nbsp;&nbsp;&nbsp;Visualis is a data visualization system based on the secondary development of the open source project DaVinci. In order to connect with DSS, visualis has implemented the relevant specifications for DSS appconn access. For the app conn access specification, please refer to [dss appconn Access Specification] (). Visualis is connected to DSS as a data visualization node, which needs to meet the three-level specifications, namely:
* In order to realize secret free interworking with DSS, SSO specification is implemented
* In order to connect with DSS project, the organization structure specification is realized
* In order to realize the interworking with DSS workflow development, the application development specification is implemented

&nbsp;&nbsp;&nbsp;&nbsp;Appconn is a JVM process running inside its DSS and linkis. For each third-party application accessed, there is a unique appconn instance, which is similar to a proxy client of a third-party application in the DSS. If the DSS needs to use interaction with the third-party system, it can call the relevant appconn to request the third-party application in the appconn to realize the interaction, You can directly understand the role of appconn through the following figure.  
![AppConn](../images/appconn.png)

&nbsp;&nbsp;&nbsp;&nbsp;In order to use the relevant functions of visualis in DSS workflow, you need to install the corresponding appconn to develop visualis visual reports in DSS workflow.

## 2. One click installation package installation
&nbsp;&nbsp;&nbsp;&nbsp;If the service is deployed using [dss one click Install bucket] (), you can directly use the scripting tool provided in the software package. After one click deployment, you can find the script tool in the installation directory of DSS. Its directory structure and instructions are as follows.
```shell
# Enter the bin directory of DSS installation
>> cd dss/bin

# Where appconn-install.sh is the appconn installation script tool
>> ls
>> appconn-install.sh  checkEnv.sh  excecuteSQL.sh  install.sh
```
&nbsp;&nbsp;&nbsp;&nbsp;In order to install smoothly, we first need to deploy and start the visualis service. For the installation and deployment of visualis, please refer to [visualis installation and deployment document] (). When using appconn to install the script tool, the steps are as follows:
```shell
>> sh appconn-install.sh

# Choose to install visualis appconn
>> 1

# Enter the server IP for visualis deployment
>> 127.0.0.1

# Enter the port number for the visualis service
>> 8008

# After executing the appconn installation script tool, the configuration information of the relevant third-party appconn will be inserted
```

## 3. Independent installation
&nbsp;&nbsp;&nbsp;&nbsp;If the DSS and linkis services are deployed independently, you need to add additional visualis to use them. You can choose to install appconn independently. Download DSS and install the whole family bucket with one click. After decompression, find appconn install in dss/bin SH script. Place the script in the bin directory of the DSS path after installation (you need to ensure that the appconn package of the DSS compiled and installed by the source code is also intact).
```shell
# Copy the unzipped one click installation script tool for whole bucket appconn installation to the installed DSS bin directory
>> cp appconn-install.sh /appconn/Install/DSSInstall/bin/

# Execute the script
>> sh appconn-install.sh

# Similar to step 2
...

```
&nbsp;&nbsp;&nbsp;&nbsp;Of course, if you need a more flexible installation method, you can execute the relevant SQL of visualis appconn separately. The tables involved are as follows. Please refer to the following table. If you need to pay attention to the need during installation, due to the limited space, the relevant SQL of DSS insert table can be found in the one click bucket under the DSS appconn lib package SQL. The SQL script has some variables that need to be inserted after replacement.

|Table name|effect|remarks|
|-----|-----|-----|
|dss_application|DSS quick store configuration|The project front-end access address of visualis needs to be configured|
|dss_menu|DSS quick store|Configure the classification information of the app store|
|dss_onestop_menu_application|Applied menu table|Configure the menu to which the visualis application belongs|
|dss_appconn|DSS appconn configuration table|Related configuration information of appconn|
|dss_appconn_instance|Appconn instance information table|Configure information about each appconn instance|
|dss_workflow_node|Workflow node configuration table|Configure relevant node configurations to be added to DSS workflow|
|dss_workflow_node_to_ui|Configuration table required for each node of Workflow|Configure the configuration items required by each node in the DSS|