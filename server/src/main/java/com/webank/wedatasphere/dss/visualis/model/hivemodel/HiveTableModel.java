package com.webank.wedatasphere.dss.visualis.model.hivemodel;

import java.util.List;

public class HiveTableModel extends HiveModel {


    public static class HiveTable {
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

    public static class TableData {
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