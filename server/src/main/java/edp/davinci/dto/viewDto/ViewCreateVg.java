package edp.davinci.dto.viewDto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Created by johnnwang on 2019/3/5.
 * 该类是为了兼容VG的hive的datasource id。因为vg的sourceId为字符串
 */
@Data
@NotNull(message = "view cannot be null")
public class ViewCreateVg {
    public ViewCreateVg(){}

    public ViewCreateVg(String name,String description,Long projectId,String sourceId,String sql,String model,String config){
        this.name = name;
        this.description = description;
        this.projectId = projectId;
        this.sourceId = sourceId;
        this.sql = sql;
        this.model = model;
        this.config = config;
    }

    @NotBlank(message = "view name cannot be empty")
    private String name;

    private String description;

    @Min(value = 1, message = "Invalid project Id")
    private Long projectId;

    private String sourceId;

    private String sql;

    private String model;

    private String config;

}
