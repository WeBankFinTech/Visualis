package com.webank.wedatasphere.dss.visualis.content;

public class WidgetContant {

    public static String WIDGET_CONFIG_TEMPLATE = "{${context_info}\"data\":[],\"cols\":[],\"rows\":[]," +
            "\"metrics\":[],\"filters\":[],\"color\":{\"title\":\"颜色\",\"type\":\"category\"," +
            "\"value\":{\"all\":\"#509af2\"},\"items\":[]},\"chartStyles\":{\"pivot\":{\"fontFamily\":\"PingFang SC\"," +
            "\"fontSize\":\"12\",\"color\":\"#666\",\"lineStyle\":\"solid\",\"lineColor\":\"#D9D9D9\"," +
            "\"headerBackgroundColor\":\"#f7f7f7\"}},\"selectedChart\":1,\"pagination\":{\"pageNo\":0," +
            "\"pageSize\":0,\"withPaging\":false,\"totalCount\":0},\"renderType\":\"clear\",\"orders\":[]," +
            "\"mode\":\"pivot\",\"model\":${model_content},\"controls\":[],\"computed\":[],\"cache\":false,\"expired\":300,\"autoLoadData\":true}";

    public static String WIDGET_CHART_CONFIG_TEMPLE = "{${context_info}\"data\":[],\"pagination\":{\"pageNo\":0," +
            "\"pageSize\":0,\"totalCount\":0,\"withPaging\":false}," +
            "\"cols\":[],\"rows\":[],\"metrics\":[],\"secondaryMetrics\":[]," +
            "\"filters\":[],\"chartStyles\":{\"pivot\":{\"fontFamily\":\"PingFangSC\"," +
            "\"fontSize\":\"12\",\"color\":\"#666\",\"lineStyle\":\"solid\"," +
            "\"lineColor\":\"#D9D9D9\",\"headerBackgroundColor\":\"#f7f7f7\"}," +
            "\"table\":{\"fontFamily\":\"PingFangSC\",\"fontSize\":\"12\"," +
            "\"color\":\"#666\",\"lineStyle\":\"solid\",\"lineColor\":\"#D9D9D9\"," +
            "\"headerBackgroundColor\":\"#f7f7f7\",\"headerConfig\":[],\"columnsConfig\":[]," +
            "\"leftFixedColumns\":[],\"rightFixedColumns\":[],\"headerFixed\":true," +
            "\"autoMergeCell\":false,\"bordered\":true,\"size\":\"small\",\"withPaging\":true," +
            "\"pageSize\":\"5000\",\"withNoAggregators\":false}},\"selectedChart\":1," +
            "\"orders\":[],\"mode\":\"chart\",\"model\":${model_content},\"controls\":[],\"computed\":[]," +
            "\"cache\":false,\"expired\":300,\"autoLoadData\":true,\"query\":null}";

    public static String WIDGET_ID = "widgetId";

    public static String NAME = "name";

    public static String WIDGET = "widget";

    public static String WIDGETS = "widgets";


}
