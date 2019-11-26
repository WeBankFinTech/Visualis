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
 * created by cooperyang on 2019/1/24
 * Description:
 */
public class HiveDBModel extends HiveModel{
    public static class HiveDB{
        private String dbName;

        public String getDbName() {
            return dbName;
        }

        public void setDbName(String dbName) {
            this.dbName = dbName;
        }
    }

    public static class Data{
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

