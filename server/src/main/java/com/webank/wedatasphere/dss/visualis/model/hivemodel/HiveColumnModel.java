package com.webank.wedatasphere.dss.visualis.model.hivemodel;

import java.util.List;

public class HiveColumnModel extends HiveModel {
    public static class Column {
        private String columnName;
        private String columnType;
        private String columnComment;
        private boolean partitioned;

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnType() {
            return columnType;
        }

        public void setColumnType(String columnType) {
            this.columnType = columnType;
        }

        public String getColumnComment() {
            return columnComment;
        }

        public void setColumnComment(String columnComment) {
            this.columnComment = columnComment;
        }

        public boolean isPartitioned() {
            return partitioned;
        }

        public void setPartitioned(boolean partitioned) {
            this.partitioned = partitioned;
        }
    }


    public static class Data {
        private List<Column> columns;

        public List<Column> getColumns() {
            return columns;
        }

        public void setColumns(List<Column> columns) {
            this.columns = columns;
        }
    }

    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}

