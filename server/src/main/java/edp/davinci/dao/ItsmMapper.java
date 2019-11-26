package edp.davinci.dao;

import edp.davinci.model.Itsm;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ItsmMapper {

    @Select({"select * from bdp_vg_itsm where itsm_id = #{itsmId}"})
    Itsm getByItsmId(@Param("itsmId") String itsmId);

    @Select({"select * from bdp_vg_itsm"})
    List<Itsm> getAllItsms();

    void insert(Itsm itsm);

    @Update({
            "update bdp_vg_itsm",
            "set content = #{content,jdbcType=VARCHAR},",
            "status = #{status,jdbcType=VARCHAR}",
            "where id = #{id,jdbcType=INTEGER}"
    })
    void update(Itsm itsm);
}
