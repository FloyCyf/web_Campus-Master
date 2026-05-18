
package com.example.campusmaster.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusmaster.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 任务Mapper
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {

    /**
     * 分页查询任务列表（可筛选）
     */
    IPage<Task> selectTaskList(Page<Task> page, 
                               @Param("status") String status, 
                               @Param("category") String category,
                               @Param("keyword") String keyword);

    /**
     * 查询用户发布的任务
     */
    IPage<Task> selectRequesterTasks(Page<Task> page, @Param("requesterId") Long requesterId);

    /**
     * 查询用户接受的任务
     */
    IPage<Task> selectHelperTasks(Page<Task> page, @Param("helperId") Long helperId);
}
