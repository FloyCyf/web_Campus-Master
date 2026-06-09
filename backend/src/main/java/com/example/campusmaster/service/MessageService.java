package com.example.campusmaster.service;

import com.example.campusmaster.entity.Message;

import java.util.List;

/**
 * 消息服务接口
 */
public interface MessageService {

    Message sendMessage(Long taskId, Long senderId, Long receiverId, String content);

    List<Message> getTaskMessages(Long taskId, Long userId);

    Integer getUnreadCount(Long userId);

    void markRead(Long taskId, Long userId);

    List<Message> getConversations(Long userId);
}
