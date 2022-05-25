package com.webank.wedatasphere.dss.visualis.utils.export;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.linkis.adapt.LinkisUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.Random;

import static edp.davinci.common.utils.ScriptUtils.getExecuptParamScriptEngine;
import static edp.davinci.common.utils.ScriptUtils.getViewExecuteParam;

public class WidgetMigration {

    public static String migrate(String oldConfig, Long viewId) throws Exception {
        JsonObject jsonObject = LinkisUtils.gson().fromJson(oldConfig, JsonObject.class);

        JsonObject columnsWidth = jsonObject.getAsJsonObject("columnsWidth");
        JsonArray orders = jsonObject.getAsJsonArray("orders");
        JsonObject columnsShowAsPercent = jsonObject.getAsJsonObject("columnsShowAsPercent");

        // model
        JsonObject model = jsonObject.getAsJsonObject("model");
        for (Map.Entry<String, JsonElement> modelItem : model.entrySet()) {
            JsonObject newItem = modelItem.getValue().getAsJsonObject().deepCopy();
            if (modelItem.getValue().getAsJsonObject().get("sqlType") == null) {
                newItem.addProperty("sqlType", "STRING");
            } else {
                newItem.addProperty("sqlType", modelItem.getValue().getAsJsonObject().get("sqlType").getAsString().toUpperCase());
            }
            model.add(modelItem.getKey(), newItem);
        }
        jsonObject.add("model", model);

        //cols
        JsonArray oldCols = jsonObject.getAsJsonArray("cols");
        JsonArray newCols = new JsonArray();
        for (JsonElement col : oldCols) {
            String colName = col.getAsString();
            JsonObject newCol = new JsonObject();
            newCol.addProperty("name", colName);
            newCol.addProperty("type", model.getAsJsonObject(colName).getAsJsonPrimitive("modelType").getAsString());
            newCol.addProperty("visualType", model.getAsJsonObject(colName).getAsJsonPrimitive("visualType").getAsString());

            JsonObject field = new JsonObject();
            field.addProperty("alias", "");
            field.addProperty("desc", "");
            field.addProperty("useExpression", false);
            newCol.add("field", field);

            JsonObject format = new JsonObject();
            format.addProperty("formatType", "default");
            newCol.add("format", format);

            if (orders != null && orders.size() > 0) {
                for (JsonElement order : orders) {
                    if (colName.equals(order.getAsJsonObject().getAsJsonPrimitive("column").getAsString())) {
                        JsonObject sort = new JsonObject();
                        sort.add("sortType", order.getAsJsonObject().getAsJsonPrimitive("direction"));
                        newCol.add("sort", sort);
                        break;
                    }
                }
            }

            if (columnsWidth != null && columnsWidth.has(colName)) {
                newCol.add("width", columnsWidth.get(colName));
                newCol.addProperty("widthChanged", true);
                newCol.addProperty("alreadySetWidth", true);
                //"oldColumnCounts": 5
            } else {
                newCol.addProperty("width", 270);
                newCol.addProperty("widthChanged", false);
                newCol.addProperty("alreadySetWidth", true);
            }
            newCol.addProperty("from", "cols");
            newCols.add(newCol);
        }
        jsonObject.add("cols", newCols);

        //rows
        JsonArray oldRows = jsonObject.getAsJsonArray("rows");
        JsonArray newRows = new JsonArray();
        for (JsonElement row : oldRows) {
            String rowName = row.getAsString();
            JsonObject newRow = new JsonObject();
            newRow.addProperty("name", rowName);
            newRow.addProperty("type", model.getAsJsonObject(rowName).getAsJsonPrimitive("modelType").getAsString());
            newRow.addProperty("visualType", model.getAsJsonObject(rowName).getAsJsonPrimitive("visualType").getAsString());

            JsonObject field = new JsonObject();
            field.addProperty("alias", "");
            field.addProperty("desc", "");
            field.addProperty("useExpression", false);
            newRow.add("field", field);

            JsonObject format = new JsonObject();
            format.addProperty("formatType", "default");
            newRow.add("format", format);

            if (orders != null && orders.size() > 0) {
                for (JsonElement order : orders) {
                    if (rowName.equals(order.getAsJsonObject().getAsJsonPrimitive("column").getAsString())) {
                        JsonObject sort = new JsonObject();
                        sort.add("sortType", order.getAsJsonObject().getAsJsonPrimitive("direction"));
                        newRow.add("sort", sort);
                        break;
                    }
                }
            }

            if (columnsWidth != null && columnsWidth.has(rowName)) {
                newRow.add("width", columnsWidth.get(rowName));
                newRow.addProperty("widthChanged", true);
                newRow.addProperty("alreadySetWidth", true);
                //"oldColumnCounts": 5
            } else {
                newRow.addProperty("width", 270);
                newRow.addProperty("widthChanged", false);
                newRow.addProperty("alreadySetWidth", true);
            }
            newRow.addProperty("from", "rows");
            newRows.add(newRow);
        }
        jsonObject.add("rows", newRows);


        //metrics
        JsonArray oldMetrics = jsonObject.getAsJsonArray("metrics");
        int oldColumnCounts = oldCols.size() + oldMetrics.size();
        JsonArray newMetrics = new JsonArray();
        for (JsonElement metric : oldMetrics) {
            JsonObject newMetric = metric.deepCopy().getAsJsonObject();
            newMetric.addProperty("name", StringUtils.replace(metric.getAsJsonObject().get("name").getAsString(), "davinci", "Visualis"));
            JsonObject oldChart = metric.getAsJsonObject().getAsJsonObject("chart");
            JsonObject newChart = oldChart.deepCopy();
            JsonArray rules = new JsonArray();
            JsonObject rule = new JsonObject();
            rule.add("dimension", oldChart.get("requireDimetions"));
            rule.add("metric", oldChart.get("requireMetrics"));
            rules.add(rule);
            newChart.remove("requireDimetions");
            newChart.remove("requireMetrics");
            newChart.add("rules", rules);

            //for table
            if (jsonObject.getAsJsonObject("chartStyles").has("table")) {
                newChart.addProperty("name", "table");
                newChart.addProperty("title", "表格");
                newChart.addProperty("icon", "icon-table");
                newChart.addProperty("icon", "icon-table");
                newChart.addProperty("coordinate", "other");
            }

            JsonObject data = new JsonObject();
            JsonObject tmp = new JsonObject();
            tmp.addProperty("title", "列");
            tmp.addProperty("type", "category");
            data.add("cols", tmp);
            JsonObject tmp1 = new JsonObject();
            tmp1.addProperty("title", "行");
            tmp1.addProperty("type", "category");
            data.add("rows", tmp1);
            JsonObject tmp2 = new JsonObject();
            tmp2.addProperty("title", "指标");
            tmp2.addProperty("type", "value");
            data.add("metrics", tmp2);
            JsonObject tmp3 = new JsonObject();
            tmp3.addProperty("title", "筛选");
            tmp3.addProperty("type", "all");
            data.add("filters", tmp3);
            JsonObject tmp4 = new JsonObject();
            tmp4.addProperty("title", "颜色");
            tmp4.addProperty("type", "category");
            data.add("color", tmp4);
            newChart.add("data", data);
            JsonObject style = newChart.getAsJsonObject("style").deepCopy();
            //for table
            if (jsonObject.getAsJsonObject("chartStyles").has("table") && style.has("pivot")) {
                JsonObject table = new JsonObject();
                for (Map.Entry<String, JsonElement> entry : style.getAsJsonObject("pivot").entrySet()) {
                    table.add(entry.getKey(), entry.getValue());
                }
                table.add("headerConfig", new JsonArray());
                table.add("columnsConfig", new JsonArray());
                table.add("leftFixedColumns", new JsonArray());
                table.add("rightFixedColumns", new JsonArray());
                table.addProperty("headerFixed", true);
                table.addProperty("autoMergeCell", false);
                table.addProperty("bordered", true);
                table.addProperty("size", "default");
                table.addProperty("withPaging", true);
                table.addProperty("pageSize", "20");
                table.addProperty("withNoAggregators", false);
                style.add("table", table);
                style.remove("pivot");
                newMetric.addProperty("oldColumnCounts", oldColumnCounts);
            } else {
                if (style.getAsJsonObject("table") != null) {
                    JsonObject table = style.getAsJsonObject("table").deepCopy();
                    table.add("headerConfig", new JsonArray());
                    table.add("columnsConfig", new JsonArray());
                    table.add("leftFixedColumns", new JsonArray());
                    table.add("rightFixedColumns", new JsonArray());
                    table.addProperty("headerFixed", true);
                    table.addProperty("autoMergeCell", false);
                    table.addProperty("bordered", true);
                    table.addProperty("size", "default");
                    table.addProperty("withPaging", true);
                    table.addProperty("pageSize", "20");
                    table.addProperty("withNoAggregators", false);
                    style.add("table", table);
                }
            }

            style.add("spec", new JsonObject());
            //style.add("spec", new JsonObject());
            newChart.add("style", style);
            newMetric.add("chart", newChart);


            JsonObject field = new JsonObject();
            field.addProperty("alias", "");
            field.addProperty("desc", "");
            field.addProperty("useExpression", false);
            newMetric.add("field", field);

            JsonObject format = new JsonObject();
            if (columnsShowAsPercent != null &&
                    columnsShowAsPercent.has(metric.getAsJsonObject().get("name").getAsString()) &&
                    columnsShowAsPercent.get(metric.getAsJsonObject().get("name").getAsString()).getAsBoolean()) {
                format.addProperty("formatType", "percentage");
                JsonObject percentage = new JsonObject();
                percentage.addProperty("decimalPlaces", 2);
                newMetric.add("percentage", percentage);
                newMetric.add("format", format);
            } else {
                format.addProperty("formatType", "default");
                newMetric.add("format", format);
            }


            if (metric.getAsJsonObject().has("displayName")) {
                String displayName = metric.getAsJsonObject().get("displayName").getAsString();
                if (columnsWidth.has(displayName.toLowerCase())) {
                    newMetric.add("width", columnsWidth.get(displayName.toLowerCase()));
                    newMetric.addProperty("widthChanged", true);
                    newMetric.addProperty("alreadySetWidth", true);
                } else if (columnsWidth.has(displayName.toUpperCase())) {
                    newMetric.add("width", columnsWidth.get(displayName.toUpperCase()));
                    newMetric.addProperty("widthChanged", true);
                    newMetric.addProperty("alreadySetWidth", true);
                } else {
                    if (metric.getAsJsonObject().has("width")) {
                        newMetric.add("width", metric.getAsJsonObject().get("width"));
                        newMetric.addProperty("widthChanged", true);
                        newMetric.addProperty("alreadySetWidth", true);
                    } else {
                        newMetric.addProperty("width", 270);
                        newMetric.addProperty("widthChanged", false);
                        newMetric.addProperty("alreadySetWidth", true);
                    }
                }
            } else {
                if (metric.getAsJsonObject().has("width")) {
                    newMetric.add("width", metric.getAsJsonObject().get("width"));
                    newMetric.addProperty("widthChanged", true);
                    newMetric.addProperty("alreadySetWidth", true);
                } else {
                    newMetric.addProperty("width", 270);
                    newMetric.addProperty("widthChanged", false);
                    newMetric.addProperty("alreadySetWidth", true);
                }
            }

            //"oldColumnCounts": 5,
            if (metric.getAsJsonObject().has("sort")) {
                JsonObject sort = new JsonObject();
                sort.add("sortType", metric.getAsJsonObject().get("sort"));
                newMetric.add("sort", sort);
            }
            newMetric.addProperty("from", "metrics");
            newMetrics.add(newMetric);
        }
        jsonObject.add("metrics", newMetrics);

        //filters
        JsonArray oldFilters = jsonObject.getAsJsonArray("filters");
        JsonArray newFilters = new JsonArray();
        for (JsonElement filter : oldFilters) {
            JsonObject newFilter = filter.getAsJsonObject().deepCopy();
            JsonObject config = newFilter.getAsJsonObject("config");
            if (config.get("filterSource").isJsonArray()) {
                //value
                JsonArray sqlModel = new JsonArray();
                JsonObject sqlModelValue = new JsonObject();
                sqlModelValue.addProperty("name", newFilter.get("name").getAsString());
                sqlModelValue.addProperty("type", "filter");
                sqlModelValue.addProperty("operator", "in");
                if (newFilter.get("visualType").isJsonPrimitive()) {
                    sqlModelValue.addProperty("sqlType", newFilter.get("visualType").getAsString().toUpperCase());
                } else {
                    sqlModelValue.addProperty("sqlType", newFilter.get("visualType").getAsJsonObject().get("visualType").getAsString().toUpperCase());
                }

                JsonArray value = new JsonArray();
                for (JsonElement filterValue : config.get("filterSource").getAsJsonArray()) {
                    //TODO for number type
                    value.add("'" + filterValue.getAsString() + "'");
                }
                sqlModelValue.add("value", value);
                sqlModel.add(sqlModelValue);
                config.add("sqlModel", sqlModel);

            } else {
                //relation
                JsonArray sqlModel = convertSourceToModel(config.get("filterSource").getAsJsonObject(), newFilter);
                config.add("sqlModel", sqlModel);
            }
            config.remove("sql");
            newFilter.remove("visualType");
            newFilter.add("config", config);
            newFilters.add(newFilter);
        }


        jsonObject.add("filters", newFilters);

        // chartStyles
        JsonObject chartStyles = jsonObject.getAsJsonObject("chartStyles");
        if (chartStyles.getAsJsonObject("table") != null) {
            JsonObject table = chartStyles.getAsJsonObject("table").deepCopy();
            JsonArray headerConfig = new JsonArray();
            JsonArray columnConfig = new JsonArray();
            for (JsonElement newCol : newCols) {
                JsonObject config = new JsonObject();
                config.addProperty("key", Integer.toString(new Random().nextInt(100000)));
                config.addProperty("headerName", newCol.getAsJsonObject().get("name").getAsString());
                config.addProperty("alias", newCol.getAsJsonObject().get("name").getAsString());
                config.addProperty("visualType", newCol.getAsJsonObject().get("visualType").getAsString());
                config.addProperty("isGroup", false);
                config.add("children", null);
                JsonObject style = new JsonObject();
                style.add("fontSize", table.get("fontSize"));
                style.add("fontFamily", table.get("fontFamily"));
                if (table.get("isHeaderBold") != null && table.get("isHeaderBold").getAsBoolean()) {
                    style.addProperty("fontWeight", "bold");
                } else {
                    style.addProperty("fontWeight", "normal");
                }
                style.add("fontColor", table.get("color"));
                style.add("backgroundColor", table.get("headerBackgroundColor"));
                style.addProperty("justifyContent", "flex-start");
                config.add("style", style);
                headerConfig.add(config);

                JsonObject cConfig = new JsonObject();
                cConfig.addProperty("columnName", newCol.getAsJsonObject().get("name").getAsString());
                cConfig.addProperty("alias", newCol.getAsJsonObject().get("name").getAsString());
                cConfig.addProperty("visualType", newCol.getAsJsonObject().get("visualType").getAsString());
                cConfig.addProperty("styleType", 0);

                JsonObject cStyle = new JsonObject();
                cStyle.add("fontSize", table.get("fontSize"));
                cStyle.add("fontFamily", table.get("fontFamily"));
                if (table.get("isBodyBold") != null && table.get("isBodyBold").getAsBoolean()) {
                    cStyle.addProperty("fontWeight", "bold");
                } else {
                    cStyle.addProperty("fontWeight", "normal");
                }
                cStyle.add("fontColor", table.get("color"));
                cStyle.addProperty("backgroundColor", "transparent");
                cStyle.addProperty("justifyContent", "flex-start");
                cConfig.add("style", cStyle);
                cConfig.add("conditionStyles", new JsonArray());
                cConfig.add("width", columnsWidth.get(newCol.getAsJsonObject().get("name").getAsString()));
                cConfig.addProperty("alreadySetWidth", true);
                cConfig.addProperty("oldColumnCounts", oldColumnCounts);
                cConfig.addProperty("widthChanged", false);
                columnConfig.add(cConfig);
            }
            for (JsonElement newMetric : newMetrics) {
                JsonObject config = new JsonObject();
                config.addProperty("key", Integer.toString(new Random().nextInt(100000)));
                config.addProperty("headerName", newMetric.getAsJsonObject().get("name").getAsString());
                config.addProperty("alias", newMetric.getAsJsonObject().get("name").getAsString());
                config.addProperty("visualType", newMetric.getAsJsonObject().get("visualType").getAsString());
                config.addProperty("isGroup", false);
                config.add("children", null);
                JsonObject style = new JsonObject();
                style.add("fontSize", table.get("fontSize"));
                style.add("fontFamily", table.get("fontFamily"));
                if (table.get("isHeaderBold") != null && table.get("isHeaderBold").getAsBoolean()) {
                    style.addProperty("fontWeight", "bold");
                } else {
                    style.addProperty("fontWeight", "normal");
                }
                style.add("fontColor", table.get("color"));
                style.add("backgroundColor", table.get("headerBackgroundColor"));
                style.addProperty("justifyContent", "flex-start");
                config.add("style", style);
                headerConfig.add(config);

                JsonObject cConfig = new JsonObject();
                cConfig.addProperty("columnName", newMetric.getAsJsonObject().get("name").getAsString());
                cConfig.addProperty("alias", newMetric.getAsJsonObject().get("name").getAsString());
                cConfig.addProperty("visualType", newMetric.getAsJsonObject().get("visualType").getAsString());
                cConfig.addProperty("styleType", 0);

                JsonObject cStyle = new JsonObject();
                cStyle.add("fontSize", table.get("fontSize"));
                cStyle.add("fontFamily", table.get("fontFamily"));
                if (table.get("isBodyBold") != null && table.get("isBodyBold").getAsBoolean()) {
                    cStyle.addProperty("fontWeight", "bold");
                } else {
                    cStyle.addProperty("fontWeight", "normal");
                }
                cStyle.add("fontColor", table.get("color"));
                cStyle.addProperty("backgroundColor", "transparent");
                cStyle.addProperty("justifyContent", "flex-start");
                cConfig.add("style", cStyle);
                cConfig.add("conditionStyles", new JsonArray());
                cConfig.add("width", columnsWidth.get(newMetric.getAsJsonObject().get("name").getAsString()));
                cConfig.addProperty("alreadySetWidth", true);
                cConfig.addProperty("oldColumnCounts", oldColumnCounts);
                cConfig.addProperty("widthChanged", false);
                columnConfig.add(cConfig);
            }
            table.add("headerConfig", headerConfig);
            table.add("columnsConfig", columnConfig);
            table.add("leftFixedColumns", new JsonArray());
            table.add("rightFixedColumns", new JsonArray());
            table.addProperty("headerFixed", true);
            table.addProperty("autoMergeCell", false);
            table.addProperty("bordered", true);
            table.addProperty("size", "default");
            table.addProperty("withPaging", true);
            table.addProperty("pageSize", "20");
            table.addProperty("withNoAggregators", false);
            chartStyles.add("table", table);
            chartStyles.add("spec", new JsonObject());
            jsonObject.add("chartStyles", chartStyles);
        }


        // pagination
        JsonObject pagination = new JsonObject();
        pagination.addProperty("pageNo", 1);
        pagination.addProperty("pageSize", 20);
        pagination.addProperty("withPaging", true);
        pagination.addProperty("totalCount", 0);
        jsonObject.add("pagination", pagination);

        jsonObject.add("controls", new JsonArray());
        jsonObject.add("computed", new JsonArray());
        jsonObject.addProperty("cache", false);
        jsonObject.addProperty("nativeQuery", false);
        jsonObject.addProperty("expired", 300);
        jsonObject.addProperty("autoLoadData", true);

        jsonObject.addProperty("view", viewId);
        jsonObject.addProperty("contextId", "");
        jsonObject.addProperty("nodeName", "");

        jsonObject.addProperty("renderType", "clear");


        jsonObject.add("query", LinkisUtils.gson().toJsonTree(getViewExecuteParam(getExecuptParamScriptEngine(), null, LinkisUtils.gson().toJson(jsonObject), null)));

        return LinkisUtils.gson().toJson(jsonObject);
    }

    public static JsonArray convertSourceToModel(JsonObject filterSource, JsonObject filter) {
        JsonArray sqlModel = new JsonArray();
        sqlModel.add(sourceToModelRec(filterSource, filter));
        return sqlModel;
    }

    public static JsonObject sourceToModelRec(JsonObject parentSource, JsonObject filter) {
        JsonObject parentModel = new JsonObject();
        if (parentSource.get("type").equals("link")) {
            parentModel.addProperty("type", "relation");
            parentModel.addProperty("value", parentSource.get("rel").getAsString());
            JsonArray children = new JsonArray();
            for (JsonElement childSource : parentSource.getAsJsonArray("children")) {
                children.add(sourceToModelRec(childSource.getAsJsonObject(), filter));
            }
            return parentModel;
        } else {
            parentModel.addProperty("type", "filter");
            parentModel.addProperty("name", filter.get("name").getAsString());
            parentModel.add("value", parentSource.get("filterValue"));
            parentModel.add("operator", parentSource.get("filterOperator"));
            parentModel.addProperty("sqlType", filter.get("visualType").getAsString().toUpperCase());
            return parentModel;
        }
    }

}
