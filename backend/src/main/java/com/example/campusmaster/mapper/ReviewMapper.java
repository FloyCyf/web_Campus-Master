package com.example.campusmaster.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campusmaster.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    @Select("SELECT * FROM review WHERE task_id = #{taskId} AND deleted = 0 LIMIT 1")
    Review selectByTaskId(Long taskId);

    @Select("SELECT * FROM review WHERE task_id = #{taskId} AND reviewer_id = #{reviewerId} AND deleted = 0 LIMIT 1")
    Review selectByTaskIdAndReviewerId(@Param("taskId") Long taskId, @Param("reviewerId") Long reviewerId);

    @Select("SELECT * FROM review WHERE reviewee_id = #{revieweeId} AND deleted = 0 ORDER BY create_time DESC")
    List<Review> selectByRevieweeId(Long revieweeId);

    @Select("SELECT * FROM review WHERE reviewer_id = #{reviewerId} AND deleted = 0 ORDER BY create_time DESC")
    List<Review> selectByReviewerId(Long reviewerId);
}
