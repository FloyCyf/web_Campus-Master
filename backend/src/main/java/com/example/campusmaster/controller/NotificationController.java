package com.example.campusmaster.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusmaster.annotation.RequiresRoles;
import com.example.campusmaster.common.JwtUtil;
import com.example.campusmaster.common.Result;
import com.example.campusmaster.entity.Notification;
import com.example.campusmaster.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    @RequiresRoles({"requester", "helper", "admin"})
    public Result<IPage<Notification>> getNotifications(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Page<Notification> page = new Page<>(pageNum, pageSize);
        IPage<Notification> result = notificationService.getNotifications(page, userId);
        return Result.success(result);
    }

    @GetMapping("/unread-count")
    @RequiresRoles({"requester", "helper", "admin"})
    public Result<Integer> getUnreadCount(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Integer count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    @PutMapping("/{id}/read")
    @RequiresRoles({"requester", "helper", "admin"})
    public Result<Void> markAsRead(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        notificationService.markAsRead(id, userId);
        return Result.success("标记已读", null);
    }

    @PutMapping("/read-all")
    @RequiresRoles({"requester", "helper", "admin"})
    public Result<Void> markAllRead(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        notificationService.markAllRead(userId);
        return Result.success("标记成功", null);
    }
}
