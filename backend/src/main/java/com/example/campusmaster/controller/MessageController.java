package com.example.campusmaster.controller;

import com.example.campusmaster.common.JwtUtil;
import com.example.campusmaster.common.Result;
import com.example.campusmaster.entity.Message;
import com.example.campusmaster.entity.Task;
import com.example.campusmaster.service.MessageService;
import com.example.campusmaster.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/task/{taskId}")
    public Result<Message> sendMessage(@PathVariable Long taskId,
                                       @RequestBody SendMessageRequest request,
                                       HttpServletRequest httpRequest) {
        Long senderId = jwtUtil.getUserId(httpRequest.getHeader("token"));
        Task task = taskService.getTaskById(taskId);

        Long receiverId = task.getRequesterId().equals(senderId) ? task.getHelperId() : task.getRequesterId();
        if (receiverId == null) {
            return Result.badRequest("对方尚未接单，无法发送消息");
        }

        Message message = messageService.sendMessage(taskId, senderId, receiverId, request.getContent());
        return Result.success("发送成功", message);
    }

    @GetMapping("/task/{taskId}")
    public Result<List<Message>> getMessages(@PathVariable Long taskId, HttpServletRequest httpRequest) {
        Long userId = jwtUtil.getUserId(httpRequest.getHeader("token"));
        List<Message> messages = messageService.getTaskMessages(taskId, userId);
        return Result.success(messages);
    }

    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(HttpServletRequest httpRequest) {
        Long userId = jwtUtil.getUserId(httpRequest.getHeader("token"));
        Integer count = messageService.getUnreadCount(userId);
        return Result.success(count);
    }

    @PostMapping("/task/{taskId}/read")
    public Result<Void> markRead(@PathVariable Long taskId, HttpServletRequest httpRequest) {
        Long userId = jwtUtil.getUserId(httpRequest.getHeader("token"));
        messageService.markRead(taskId, userId);
        return Result.success("已标记为已读", null);
    }

    @GetMapping("/conversations")
    public Result<List<Message>> getConversations(HttpServletRequest httpRequest) {
        Long userId = jwtUtil.getUserId(httpRequest.getHeader("token"));
        List<Message> conversations = messageService.getConversations(userId);
        return Result.success(conversations);
    }

    public static class SendMessageRequest {
        @NotBlank(message = "消息内容不能为空")
        private String content;

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
