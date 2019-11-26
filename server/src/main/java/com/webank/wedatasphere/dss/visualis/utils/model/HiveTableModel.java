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
package com.webank.wedatasphere.dss.visualis.utils.model;

import java.util.List;

/**
 * created by cooperyang on 2019/1/25
 * Description:
 */
public class HiveTableModel extends HiveModel{


    public static class HiveTable{
        private String tableName;
        private boolean isView;
        private String databaseName;
        private String createdBy;
        private Long createAt;
        private Long lastAccessAt;

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public boolean isView() {
            return isView;
        }

        public void setView(boolean view) {
            isView = view;
        }

        public String getDatabaseName() {
            return databaseName;
        }

        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }

        public Long getCreateAt() {
            return createAt;
        }

        public void setCreateAt(Long createAt) {
            this.createAt = createAt;
        }

        public Long getLastAccessAt() {
            return lastAccessAt;
        }

        public void setLastAccessAt(Long lastAccessAt) {
            this.lastAccessAt = lastAccessAt;
        }
    }

    public static class TableData{
        private List<HiveTable> tables;

        public List<HiveTable> getTables() {
            return tables;
        }

        public void setTables(List<HiveTable> tables) {
            this.tables = tables;
        }
    }

    private TableData data;

    public TableData getData() {
        return data;
    }

    public void setData(TableData data) {
        this.data = data;
    }
}

