
package com.example.campusmaster.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campusmaster.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评价Mapper
 */
@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    /**
     * 根据任务ID查询评价
     */
    @Select("SELECT * FROM review WHERE task_id = #{taskId} AND deleted = 0 LIMIT 1")
    Review selectByTaskId(Long taskId);

    /**
     * 根据被评价者ID查询评价列表
     */
    @Select("SELECT * FROM review WHERE reviewee_id = #{revieweeId} AND deleted = 0")
    List<Review> selectByRevieweeId(Long revieweeId);
}
