package edp.davinci.model;

import edp.davinci.dto.viewDto.Param;
import lombok.Data;

import java.util.List;

@Data
public class ParamsDetail {
    private Long dashboardId;
    private Long displayId;
    private Long widgetId;
    private Long viewId;

    private List<Param> variables;
}
