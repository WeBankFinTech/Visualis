package com.webank.wedatasphere.dss.visualis.model.hivemodel;

import java.util.List;

public class HiveDBModel extends HiveModel {
    public static class HiveDB {
        private String dbName;

        public String getDbName() {
            return dbName;
        }

        public void setDbName(String dbName) {
            this.dbName = dbName;
        }
    }

    public static class Data {
        private List<HiveDB> dbs;

        public List<HiveDB> getDbs() {
            return dbs;
        }

        public void setDbs(List<HiveDB> dbs) {
            this.dbs = dbs;
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

