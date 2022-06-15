Visualis compile deployment document

------



# 1. Environment preparation and compilation
## 1.1. Dependent environment preparation

| Dependent components | Is it required | Install a through train |
| -------------- | ------ | --------------- |
| MySQL (5.5+) | required  | [how to install mysql](https://www.runoob.com/mysql/mysql-install.html) |
| JDK (1.8.0_141) | required | [how to install JDK](https://www.runoob.com/java/java-environment-setup.html) |
| Hadoop(2.7.2，Other versions of Hadoop need to compile Linkis themselves) | required | [how to install  Hadoop](https://linkis.apache.org/zh-CN/docs/latest/deployment/quick_deploy) [Hadoop cluster](https://linkis.apache.org/zh-CN/docs/latest/deployment/quick_deploy) |
| Spark(2.4.3，Other Spark versions need to compile Linkis themselves) | required | [how to install Spark](https://linkis.apache.org/zh-CN/docs/latest/deployment/quick_deploy) |
| DSS1.0.1 | required | [how to install DSS](https://github.com/WeBankFinTech/DataSphereStudio-Doc/blob/main/zh_CN/%E5%AE%89%E8%A3%85%E9%83%A8%E7%BD%B2/DSS%E5%8D%95%E6%9C%BA%E9%83%A8%E7%BD%B2%E6%96%87%E6%A1%A3.md) |
| Linkis1.1.1 | required | [how to install Linkis](https://linkis.apache.org/zh-CN/docs/latest/deployment/quick_deploy) |
| Nginx | required | [how to install Nginx](http://nginx.org/en/linux_packages.html) |




## 1.2. Creating Linux users

&nbsp;&nbsp;&nbsp;&nbsp;Please keep the deployment users of visualis consistent with those of linkis and deploy with Hadoop users.



## 1.3. Underlying dependent component check

&nbsp;&nbsp;&nbsp;&nbsp;**Please ensure that dss1.0.1 and linkis1.1.1 are basically available. You can execute sparkql scripts in the DSS front-end interface, and create and execute DSS workflows normally**



## 1.4. Download the source package and compile the backend

&nbsp;&nbsp;&nbsp;&nbsp;When installing the visualis source code, you need to download the corresponding source code package for compilation. Currently, the DSS version 1.0.1 and linkis1.1.1 that visualis relies on have been uploaded to the Maven central warehouse. As long as the Maven configuration is normal, you can pull the relevant dependencies.
```shell
# 1. Download source code
git clone https://github.com/WeDataSphere/Visualis.git

# 2. Switch to 1.0.0-rc1 branch
git checkout 1.0.0-rc1

# 3.Perform compilation and packaging
cd Visualis

mvn -N install

mvn clean package -DskipTests=true

```



## 1.5. Compile front end

 & nbsp;& nbsp;& nbsp; Visualis is a front-end and back-end separated project. The front-end files can be compiled and packaged separately. NPM tools need to be installed on the computer.

```shell

#Check whether NPM installation is completed

npm -v

>> 8.1.0


cd webapp #enter the front-end file path

npm i #download front end dependency

npm run build

#A build file directory will be generated under the webapp directory, which is the compiled front-end package file
```



## 2. Install deployment

## 2.1. Installing the rear end

&nbsp;&nbsp;&nbsp;&nbsp;Visualis uses assembly as a package plug-in. After compilation, go to the visualis/assembly/target directory to find the compiled visualis server Zip package.

````bash
# 1. Unzip the installation package
unzip visualis-server. zip

cd visualis-server
````

 & nbsp;& nbsp;& nbsp; After decompressing the visualis compilation package, you can enter the directory to see the following file directories.

```shell

visualis-server

---bin 

---conf # service configuration directory

---davinvi-ui

---lib # service jar package storage location

---logs # log directory

```



## 2.2. Initialize database

&nbsp;&nbsp;&nbsp;Before initializing the database, it should be noted that due to historical reasons, visualis reuses the user permission system of DSS and uses the links of DSS_ User table. Therefore, during deployment, visualis needs to configure the same database as DSS. If the sub database is implemented, it needs to regularly synchronize the links of DSS users to visualis library during use_ In the user table), create the tables that visualis depends on, enter the source directory, and find the DB folder. After linking to the corresponding database, you need to execute the following SQL files to create the tables that visualis needs to use.

```shell

#Find the corresponding SQL file in the source package



#Link visualis database (same library as DSS)

mysql -h 127.0.0.1 -u hadoop -d visualis -P3306 -p



source ${visualis_home}/davinci. sql

source ${visualis_home}/ddl. sql



#Davinci SQL is the DaVinci table that visualis needs to use

# ddl. SQL is an additional dependent table of visualis

```




## 2.3. Font library installation

 & nbsp;& nbsp;& nbsp; For Mail reports, you need to render Chinese fonts. The visualis screenshot function depends on Chinese fonts and is located in the /usr/share/fonts directory on the deployed machine. Create a visualis folder and upload pf Ttf file to visualis folder, execute FC cache – FV command to refresh font cache.

```shell
#Need to switch to root

sudo su

cd /usr/share/fonts

mkdir visualis

#Upload pf Ttf Chinese font library

rz -ybe

#Refresh font library cache

fc-cache –fv

```



##2.4 installing the front end

 & nbsp;& nbsp;& nbsp; Visualis currently uses the front-end and back-end deployment scheme. After the front-end compilation is completed, place the front-end package in the server directory corresponding to the dss/visualis path of the nginx front-end package installation path.



```shell
#Configure the static resource root path (if not, create it)
cd /data/dss/web


#In the previous step, under /data/dss/web directory, configure the front-end access URL path address (if not, you need to create it)

cd dss/visualis


unzip build. Zip #unzip the front-end package

cd build 

mv * ./../ # Move the static resource file to the visualisation path
```



&nbsp;&nbsp;&nbsp;&nbsp; According to the front-end deployment in the previous step, the front-end configuration of nginx of visualis can be referred to as follows:

```shell

#Configure reference in nginx

#Supplement the linkis gateway



#Change the port

server {

listen 8989; # Access port

server_ name localhost;

client_ max_ body_ size 100M;



#

Location /dss/visualis {\\

root /data/dss/web; # Visualis front-end static resource file directory, freely specified

autoindex off;

}



location /ws {

proxy_ pass http://127.0.0.1:9001 # Link gateway address

#

}



location /api {

proxy_ pass http://127.0.0.1:9001 # Link gateway address

#

}

}

```



## 2.5. Modify configuration



### 2.5.1. Modify application yml

 & nbsp;& nbsp;& nbsp; Configure application In the YML file, there are 1, 2 and 3 configuration items that must be configured. Other configurations can adopt default values. In the first item, some deployment IP and port information needs to be configured, in the second item, Eureka information needs to be configured, and in the third item, only the link information of the configuration database is required (other parameters can remain the default values).

 & nbsp;& nbsp;& nbsp;** It should be noted that due to historical reasons, visualis reuses the user permission system of DSS and uses the linkis of DSS_ User table. Therefore, during deployment, visualis needs to configure the same database as DSS. If the sub database is implemented, it needs to regularly synchronize the links of DSS users to visualis library during use_ User table**

```yaml

# ##################################

# 1. Visualis Service configuration

# ##################################

server:

protocol: http

Address: 127.0.0.1 \server IP address (machine IP of service deployment)

Port: 9008 \server port (service deployment port)

url: http://127.0.0.1:8989/dss/visualis #Frontend index page full path

access:

Address: 127.0.0.1 \frontend address (front-end deployment IP)

Port: 8989 \frontend port




# ##################################

# 2. eureka configuration

# ##################################

eureka:

client:

serviceUrl:

defaultZone: http://127.0.0.1:20303/eureka/ # Configuration required