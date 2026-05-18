
package com.example.campusmaster.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campusmaster.entity.TaskLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 任务日志Mapper
 */
@Mapper
public interface TaskLogMapper extends BaseMapper<TaskLog> {

    /**
     * 根据任务ID查询日志列表
     */
    List<TaskLog> selectByTaskId(Long taskId);
}
