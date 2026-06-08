package com.example.campusmaster.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campusmaster.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 消息Mapper
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    @Select("SELECT * FROM message WHERE task_id = #{taskId} AND deleted = 0 ORDER BY create_time ASC")
    List<Message> selectByTaskId(@Param("taskId") Long taskId);

    @Select("SELECT COUNT(*) FROM message WHERE receiver_id = #{userId} AND is_read = 0 AND deleted = 0")
    Integer selectUnreadCount(@Param("userId") Long userId);

    @Update("UPDATE message SET is_read = 1 WHERE task_id = #{taskId} AND receiver_id = #{userId} AND is_read = 0 AND deleted = 0")
    void markReadByTaskId(@Param("taskId") Long taskId, @Param("userId") Long userId);

    @Select("SELECT * FROM message WHERE (sender_id = #{userId} OR receiver_id = #{userId}) AND deleted = 0 ORDER BY create_time DESC")
    List<Message> selectConversations(@Param("userId") Long userId);
}
