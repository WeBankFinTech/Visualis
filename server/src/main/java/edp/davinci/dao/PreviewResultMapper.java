package edp.davinci.dao;

import edp.davinci.model.PreviewResult;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Date;

@Mapper
public interface PreviewResultMapper {

    void insert(PreviewResult previewResult);

    PreviewResult selectByIdAndKeyWord(@Param("id") Long id, @Param("execId") String execId);

    String checkStatus(@Param("execId") String execId);

    @Update("update visualis_preview_result set `status` = #{status} where id = #{id}")
    void updateResultStatusById(@Param("id") Long id, @Param("status") String status);

    @Update("update visualis_preview_result set `result` = #{result} where id = #{id} and `execId` = #{execId}")
    void setResult(@Param("id") Long id, @Param("execId") String execId, @Param("result") byte[] result);

    @Delete("delete from visualis_preview_result where id = #{id} and `execId` = #{execId}")
    void deleteResult(@Param("id") Long id, @Param("execId") String execId);

    @Delete("delete from visualis_preview_result where create_time < #{cleanTime}")
    void deleteResult(@Param("cleanTime") Date cleanTime);
}
