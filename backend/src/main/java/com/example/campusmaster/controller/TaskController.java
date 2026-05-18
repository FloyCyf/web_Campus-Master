package com.example.campusmaster.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusmaster.annotation.RequiresRoles;
import com.example.campusmaster.common.JwtUtil;
import com.example.campusmaster.common.Result;
import com.example.campusmaster.entity.Task;
import com.example.campusmaster.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    @RequiresRoles({"requester", "admin"})
    public Result<Long> createTask(@Valid @RequestBody CreateTaskRequest request, HttpServletRequest httpRequest) {
        Long userId = jwtUtil.getUserId(httpRequest.getHeader("token"));
        Long taskId = taskService.createTask(request, userId);
        return Result.success("任务发布成功", taskId);
    }

    @GetMapping("/{id}")
    public Result<Task> getTask(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);
        return Result.success(task);
    }

    @GetMapping
    public Result<IPage<Task>> listTasks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {
        Page<Task> page = new Page<>(pageNum, pageSize);
        IPage<Task> result = taskService.listTasks(page, status, category);
        return Result.success(result);
    }

    @GetMapping("/my/{type}")
    @RequiresRoles({"requester", "helper", "admin"})
    public Result<IPage<Task>> myTasks(
            @PathVariable String type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest httpRequest) {
        Long userId = jwtUtil.getUserId(httpRequest.getHeader("token"));
        Page<Task> page = new Page<>(pageNum, pageSize);
        IPage<Task> result = null;
        if ("published".equals(type)) {
            result = taskService.getTasksByRequester(page, userId);
        } else if ("accepted".equals(type)) {
            result = taskService.getTasksByHelper(page, userId);
        }
        return Result.success(result);
    }

    @PostMapping("/{id}/accept")
    @RequiresRoles({"helper", "admin"})
    public Result<Void> acceptTask(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long helperId = jwtUtil.getUserId(httpRequest.getHeader("token"));
        taskService.acceptTask(id, helperId);
        return Result.success("接单成功", null);
    }

    @PostMapping("/{id}/submit")
    @RequiresRoles({"helper", "admin"})
    public Result<Void> submitTask(@PathVariable Long id, @RequestBody SubmitTaskRequest request, HttpServletRequest httpRequest) {
        Long helperId = jwtUtil.getUserId(httpRequest.getHeader("token"));
        taskService.submitTask(id, request.getProofImages(), helperId);
        return Result.success("提交成功，等待验收", null);
    }

    @PostMapping("/{id}/complete")
    @RequiresRoles({"requester", "admin"})
    public Result<Void> completeTask(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long requesterId = jwtUtil.getUserId(httpRequest.getHeader("token"));
        taskService.completeTask(id, requesterId);
        return Result.success("验收完成", null);
    }

    @PostMapping("/{id}/dispute")
    @RequiresRoles({"requester", "helper", "admin"})
    public Result<Void> disputeTask(@PathVariable Long id, @RequestBody DisputeRequest request, HttpServletRequest httpRequest) {
        Long userId = jwtUtil.getUserId(httpRequest.getHeader("token"));
        taskService.disputeTask(id, userId, request.getReason());
        return Result.success("已发起争议", null);
    }

    @PostMapping("/{id}/cancel")
    @RequiresRoles({"requester", "admin"})
    public Result<Void> cancelTask(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = jwtUtil.getUserId(httpRequest.getHeader("token"));
        taskService.cancelTask(id, userId);
        return Result.success("任务已取消", null);
    }

    @PostMapping("/{id}/rate")
    @RequiresRoles({"requester", "helper", "admin"})
    public Result<Void> rateTask(@PathVariable Long id, @RequestBody RateTaskRequest request, HttpServletRequest httpRequest) {
        Long userId = jwtUtil.getUserId(httpRequest.getHeader("token"));
        taskService.rateTask(id, userId, request.getRating(), request.getComment());
        return Result.success("评价成功", null);
    }

    public static class CreateTaskRequest {
        @NotBlank(message = "任务标题不能为空")
        private String title;
        private String description;
        @NotBlank(message = "任务分类不能为空")
        private String category;
        @NotNull(message = "奖励金额不能为空")
        private BigDecimal reward;
        @NotNull(message = "截止时间不能为空")
        private LocalDateTime deadline;
        private String location;
        private String contactInfo;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public BigDecimal getReward() { return reward; }
        public void setReward(BigDecimal reward) { this.reward = reward; }
        public LocalDateTime getDeadline() { return deadline; }
        public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getContactInfo() { return contactInfo; }
        public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
    }

    public static class SubmitTaskRequest {
        private String proofImages;

        public String getProofImages() { return proofImages; }
        public void setProofImages(String proofImages) { this.proofImages = proofImages; }
    }

    public static class DisputeRequest {
        @NotBlank(message = "争议原因不能为空")
        private String reason;

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class RateTaskRequest {
        private Integer rating;
        private String comment;

        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }
}