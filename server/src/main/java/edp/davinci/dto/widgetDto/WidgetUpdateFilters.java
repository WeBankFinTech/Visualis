package edp.davinci.dto.widgetDto;


import lombok.Data;

@Data
public class WidgetUpdateFilters {

    private String name;

    private String type;

    private Object value;

    private String operator;

    private String sqlType;
}
