package com.example.campusmaster.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusmaster.entity.Notification;

public interface NotificationService {

    void sendNotification(Long userId, Long taskId, String type, String title, String content);

    IPage<Notification> getNotifications(Page<Notification> page, Long userId);

    Integer getUnreadCount(Long userId);

    void markAsRead(Long notificationId, Long userId);

    void markAllRead(Long userId);

    void pushNotification(Long userId, Notification notification);
}
