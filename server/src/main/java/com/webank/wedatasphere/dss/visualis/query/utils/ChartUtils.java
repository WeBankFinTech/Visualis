package com.webank.wedatasphere.dss.visualis.query.utils;

import com.google.common.collect.Iterables;
import edp.core.utils.CollectionUtils;
import edp.davinci.dto.viewDto.ViewExecuteParam;

import java.util.Iterator;

public class ChartUtils {

    public static String RELATION_GRAPH = "relation_graph";

    public static void processViewExecuteParam(ViewExecuteParam viewExecuteParam){
        if(RELATION_GRAPH.equalsIgnoreCase(viewExecuteParam.getChartType())){
            String firstGroup = Iterables.getFirst(viewExecuteParam.getGroups(), null);
            if(firstGroup != null && !CollectionUtils.isEmpty(viewExecuteParam.getFilters())){
                Iterator<String> iterator = viewExecuteParam.getFilters().iterator();
                while (iterator.hasNext()){
                    if(iterator.next().contains(firstGroup)){
                        iterator.remove();
                        break;
                    }
                }
            }
        }
    }

}
