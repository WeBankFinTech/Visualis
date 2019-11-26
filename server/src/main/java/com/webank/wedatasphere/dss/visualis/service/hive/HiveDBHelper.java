/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.webank.wedatasphere.dss.visualis.service.hive;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.webank.wedatasphere.dss.visualis.model.HiveSource;
import com.webank.wedatasphere.dss.visualis.utils.HttpUtils;
import com.webank.wedatasphere.dss.visualis.utils.model.HiveColumnModel;
import com.webank.wedatasphere.dss.visualis.utils.model.HiveDBModel;
import com.webank.wedatasphere.dss.visualis.utils.model.HiveSchemaModel;
import com.webank.wedatasphere.dss.visualis.utils.model.HiveTableModel;

import com.webank.wedatasphere.linkis.adapt.LinkisUtils;
import edp.core.model.QueryColumn;
import edp.core.model.TableInfo;
import edp.core.utils.TokenUtils;
import edp.davinci.core.common.ResultMap;

import edp.davinci.model.Source;
import edp.davinci.model.User;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * created by cooperyang on 2019/1/23
 * Description:
 */

@Component
public class HiveDBHelper{

    private static final Logger logger = LoggerFactory.getLogger(HiveDBHelper.class);

    public static final String HIVE_PREFIX = "hive_";

    @Autowired
    private TokenUtils tokenUtils;

    public List<String> getHiveDBNames(String ticketId){
        if (ticketId == null){
            logger.error("cookie 中没有ticketID， 不能进行对hive数据库的操作");
            return null;
        }
        String hiveDBJson = HttpUtils.getDbs(ticketId);
        if (StringUtils.isEmpty(hiveDBJson)){
            logger.info("从database这个服务获取的内容为空，不能进行解析数据库名的操作，将返回null");
            return null;
        }
        HiveDBModel hiveDBModel = new Gson().fromJson(hiveDBJson, HiveDBModel.class);
        List<String> dbNames = new ArrayList<>();
        for (HiveDBModel.HiveDB db : hiveDBModel.getData().getDbs()){
            dbNames.add(db.getDbName());
        }
        return dbNames;
    }

    public List<QueryColumn> getHiveTables(String dbName, String ticketId){
        if (ticketId == null){
            logger.error("cookie 中没有ticketID， 不能进行对hive数据库的操作");
            return null;
        }
        String dbTableJson = HttpUtils.getTables(ticketId, dbName);
        if (StringUtils.isEmpty(dbTableJson)){
            logger.info("从database这个服务获取的内容为空，不能进行解析table名的操作，将返回null");
            return null;
        }
        List<QueryColumn> queryColumns = Lists.newArrayList();
        HiveTableModel hiveTableModel = new Gson().fromJson(dbTableJson, HiveTableModel.class);
        for(HiveTableModel.HiveTable hiveTable : hiveTableModel.getData().getTables()){
            String type = hiveTable.isView() ? "VIEW" : "TABLE";
            QueryColumn queryColumn = new QueryColumn(hiveTable.getTableName(), type);
            queryColumns.add(queryColumn);
        }
        return queryColumns;
    }

    public TableInfo getHiveTableInfo(String dbName, String tableName, String ticketId){
        if (ticketId == null){
            logger.error("cookie 中没有ticketID， 不能进行对hive数据库的操作");
            return null;
        }
        String columnJson = HttpUtils.getColumns(dbName, tableName, ticketId);
        if (StringUtils.isEmpty(columnJson)){
            logger.info("从database这个服务获取的内容为空，不能进行解析columns的操作，将返回null");
            return null;
        }

        List<QueryColumn> queryColumns = Lists.newArrayList();
        HiveColumnModel hiveColumnModel = new Gson().fromJson(columnJson, HiveColumnModel.class);
        for(HiveColumnModel.Column column : hiveColumnModel.getData().getColumns()){
            QueryColumn queryColumn = new QueryColumn(column.getColumnName(), column.getColumnType());
            queryColumns.add(queryColumn);
        }
        TableInfo tableInfo = new TableInfo(tableName, Lists.newArrayList(), queryColumns);
        return  tableInfo;
    }


    public List<HiveSource> sourcesToHiveSources(List<Source> sources){
        List<HiveSource> retList = new ArrayList<>();
        if (sources != null && sources.size() > 0){
            for(Source source : sources){
                retList.add(sourceToHiveSource(source));
            }
        }
        return retList;
    }

    public HiveSource sourceToHiveSource(Source source){
        HiveSource hiveSource = new HiveSource();
        hiveSource.setId(source.getId());
        hiveSource.setName(source.getName());
        hiveSource.setProjectId(source.getProjectId());
        hiveSource.setDescription(source.getDescription());
        hiveSource.setConfig(source.getConfig());
        hiveSource.setType(source.getType());
        return hiveSource;
    }

}
