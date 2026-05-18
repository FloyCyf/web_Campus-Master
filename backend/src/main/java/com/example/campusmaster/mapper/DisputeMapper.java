
package com.example.campusmaster.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusmaster.entity.Dispute;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 争议Mapper
 */
@Mapper
public interface DisputeMapper extends BaseMapper<Dispute> {

    /**
     * 根据任务ID查询争议
     */
    @Select("SELECT * FROM dispute WHERE task_id = #{taskId} AND deleted = 0 LIMIT 1")
    Dispute selectByTaskId(Long taskId);

    /**
     * 分页查询争议列表
     */
    IPage<Dispute> selectDisputeList(Page<Dispute> page, @Param("status") String status);
}
