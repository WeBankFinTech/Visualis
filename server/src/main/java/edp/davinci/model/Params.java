package edp.davinci.model;

import lombok.Data;

import java.util.List;

@Data
public class Params {
    private Long id;
    private String uuid;
    private String params;
    private List<ParamsDetail> paramDetails;
}
