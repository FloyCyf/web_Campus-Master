
package com.example.campusmaster.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusmaster.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 通知Mapper
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 分页查询用户通知
     */
    IPage<Notification> selectByUserId(Page<Notification> page, @Param("userId") Long userId);

    /**
     * 查询用户未读通知数量
     */
    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0 AND deleted = 0")
    Integer selectUnreadCount(Long userId);

    /**
     * 标记所有通知为已读
     */
    @Update("UPDATE notification SET is_read = 1 WHERE user_id = #{userId} AND deleted = 0")
    void updateAllRead(Long userId);
}
