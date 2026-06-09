package com.example.campusmaster.service.impl;

import com.example.campusmaster.entity.Message;
import com.example.campusmaster.entity.Task;
import com.example.campusmaster.mapper.MessageMapper;
import com.example.campusmaster.mapper.TaskMapper;
import com.example.campusmaster.service.MessageService;
import com.example.campusmaster.websocket.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Override
    @Transactional
    public Message sendMessage(Long taskId, Long senderId, Long receiverId, String content) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new com.example.campusmaster.common.BusinessException("任务不存在");
        }

        if (!task.getRequesterId().equals(senderId) && !task.getHelperId().equals(senderId)) {
            throw new com.example.campusmaster.common.BusinessException("无权在此任务中发送消息");
        }

        if (!task.getRequesterId().equals(receiverId) && !task.getHelperId().equals(receiverId)) {
            throw new com.example.campusmaster.common.BusinessException("消息接收方无效");
        }

        Message message = new Message();
        message.setTaskId(taskId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setIsRead(0);

        messageMapper.insert(message);

        WebSocketServer.sendToUser(receiverId.toString(), message);

        log.info("发送消息: taskId={}, senderId={}, receiverId={}", taskId, senderId, receiverId);
        return message;
    }

    @Override
    public List<Message> getTaskMessages(Long taskId, Long userId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new com.example.campusmaster.common.BusinessException("任务不存在");
        }

        if (!task.getRequesterId().equals(userId) && !task.getHelperId().equals(userId)) {
            throw new com.example.campusmaster.common.BusinessException("无权查看此任务的聊天记录");
        }

        return messageMapper.selectByTaskId(taskId);
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        return messageMapper.selectUnreadCount(userId);
    }

    @Override
    public void markRead(Long taskId, Long userId) {
        messageMapper.markReadByTaskId(taskId, userId);
    }

    @Override
    public List<Message> getConversations(Long userId) {
        return messageMapper.selectConversations(userId);
    }
}
