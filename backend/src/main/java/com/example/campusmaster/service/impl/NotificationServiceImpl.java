package com.example.campusmaster.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusmaster.entity.Notification;
import com.example.campusmaster.mapper.NotificationMapper;
import com.example.campusmaster.service.NotificationService;
import com.example.campusmaster.websocket.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public void sendNotification(Long userId, Long taskId, String type, String title, String content) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTaskId(taskId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead(0);

        notificationMapper.insert(notification);

        pushNotification(userId, notification);

        log.info("发送通知: userId={}, type={}, title={}", userId, type, title);
    }

    @Override
    public IPage<Notification> getNotifications(Page<Notification> page, Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId).orderByDesc(Notification::getCreateTime);
        return notificationMapper.selectPage(page, wrapper);
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId).eq(Notification::getIsRead, 0);
        return notificationMapper.selectCount(wrapper).intValue();
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification != null && notification.getUserId().equals(userId)) {
            notification.setIsRead(1);
            notificationMapper.updateById(notification);
        }
    }

    @Override
    public void markAllRead(Long userId) {
        Notification notification = new Notification();
        notification.setIsRead(1);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId).eq(Notification::getIsRead, 0);
        notificationMapper.update(notification, wrapper);
    }

    @Override
    public void pushNotification(Long userId, Notification notification) {
        WebSocketServer.sendToUser(userId.toString(), notification);
    }
}
