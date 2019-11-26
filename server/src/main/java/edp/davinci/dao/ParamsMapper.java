package edp.davinci.dao;

import edp.davinci.model.Params;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

@Component
public interface ParamsMapper {

    @Select({"select * from bdp_vg_params where uuid = #{uuid}"})
    Params getByUuid(@Param("uuid") String uuid);

    void insert(Params params);
}
