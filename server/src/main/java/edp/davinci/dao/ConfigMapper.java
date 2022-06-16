package edp.davinci.dao;

import edp.davinci.model.Config;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

@Component
public interface ConfigMapper {

    @Select({"select * from visualis_config where `key` = #{key} and `scope` = #{scope} and `username` = #{username}"})
    Config getConfig(@Param("key") String key, @Param("scope") String scope, @Param("username") String username);

}
