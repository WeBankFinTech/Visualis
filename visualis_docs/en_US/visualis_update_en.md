Visualis 1.0.0-rc1 upgrading to 1.0.0 using documentation

---



## 1. The upgrade steps are mainly divided into:

- Service stop
- Execute database upgrade script
- Replace the visualis deployment directory with a new version package
- Add and modify configuration files
- Service startup

#### 1. Service stop

Enter the deployment directory of Visualis, and execute the command under the directory to stop the services of Visualis:
```shell
cd ${VISUALIS_INSTALL_PATH}
sh bin/stop-visualis-server.sh
```

#### 2. Execute database upgrade SQL script

After linking the visualis database, execute the following SQL:
```sql
alter table linkis_user rename to visualis_user;
```

#### 3. Replace the visualis deployment directory with a new version package

- Back up the deployment directory of the old version of visualis. Take this directory as an example: 
```shell
mv /appcom/Install/VisualisInstall/lib /appcom/Install/VisualisInstall/lib-bak
```
- Refer to [visualis installation and deployment document](./visualis_deploy_doc_cn.md). After compiling and packaging, replace lib.



#### 4. Modify configuration

- Visualis1.0.0-rc1 version is compatible with cookies in order to be compatible with dss1.0.1 and linkis1.1.1. You need to delete the following parameters and use the linkers configured by default in the code: linkis_user_session_ticket_id_v1 value.

```properties
#Delete the following configuration
wds. linkis. session. ticket. key=bdp-user-ticket-id
wds. dss. visualis. ticketid=bdp-user-ticket-id

```
- After the configuration modification is completed, you need to reinstall visualis appconn on the DSS side. To install visualis1.0.0 appconn, refer to [visualis appconn installation](./visualis_appconn_install_cn.md).




#### 5. Service startup
&nbsp;&nbsp;&nbsp;&nbsp;Now you can start the new version of Visualis services. Execute the command to start the services:
```shell
sh bin/start-visualis-server. sh
```