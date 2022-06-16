package edp.davinci.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class Config {
    private Long id;
    private String key;
    private String value;
    private String scope;
    private String username;
    @JSONField(serialize = false)
    private String params;
}
