package com.webank.wedatasphere.dss.visualis.model.hivemodel;

import java.util.List;

public class HiveSchemaModel {
    public static class FrontColumn {
        private String name;
        private String type;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    List<String> primaryKeys;

    private String tableName;

    private List<FrontColumn> frontColumns;

    public List<String> getPrimaryKeys() {
        return primaryKeys;
    }

    public void setPrimaryKeys(List<String> primaryKeys) {
        this.primaryKeys = primaryKeys;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<FrontColumn> getColumns() {
        return frontColumns;
    }

    public void setColumns(List<FrontColumn> frontColumns) {
        this.frontColumns = frontColumns;
    }
}

