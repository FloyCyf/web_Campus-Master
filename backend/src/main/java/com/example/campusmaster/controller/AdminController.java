package com.example.campusmaster.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusmaster.annotation.RequiresRoles;
import com.example.campusmaster.common.Result;
import com.example.campusmaster.entity.*;
import com.example.campusmaster.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @GetMapping("/stats")
    @RequiresRoles({"admin"})
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = adminService.getStats();
        return Result.success(stats);
    }

    @GetMapping("/tasks")
    @RequiresRoles({"admin"})
    public Result<IPage<Task>> listAllTasks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        Page<Task> page = new Page<>(pageNum, pageSize);
        IPage<Task> result = adminService.listTasks(page, status, keyword);
        return Result.success(result);
    }

    @GetMapping("/disputes")
    @RequiresRoles({"admin"})
    public Result<IPage<Dispute>> listDisputes(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        Page<Dispute> page = new Page<>(pageNum, pageSize);
        IPage<Dispute> result = adminService.listDisputes(page, status);
        return Result.success(result);
    }

    @GetMapping("/users")
    @RequiresRoles({"admin"})
    public Result<IPage<User>> listUsers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        IPage<User> result = adminService.listUsers(page, role, keyword);
        return Result.success(result);
    }

    @PostMapping("/disputes/{id}/resolve")
    @RequiresRoles({"admin"})
    public Result<Void> resolveDispute(@PathVariable Long id, @RequestBody @Valid ResolveDisputeRequest request) {
        adminService.resolveDispute(id, request.getDisputeId(), request.getResult(), request.getRemark());
        return Result.success("争议处理完成", null);
    }

    @PostMapping("/tasks/{id}/take-down")
    @RequiresRoles({"admin"})
    public Result<Void> takeDownTask(@PathVariable Long id, @RequestBody @Valid TakeDownRequest request) {
        adminService.takeDownTask(id, request.getTaskId(), request.getReason());
        return Result.success("任务已下架", null);
    }

    @PostMapping("/users/{id}/freeze")
    @RequiresRoles({"admin"})
    public Result<Void> freezeUser(@PathVariable Long id, @RequestBody @Valid FreezeUserRequest request) {
        adminService.freezeUser(id, request.getUserId());
        return Result.success("用户已冻结", null);
    }

    public static class ResolveDisputeRequest {
        @NotNull(message = "争议ID不能为空")
        private Long disputeId;
        @NotBlank(message = "处理结果不能为空")
        private String result;
        private String remark;

        public Long getDisputeId() { return disputeId; }
        public void setDisputeId(Long disputeId) { this.disputeId = disputeId; }
        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }

    public static class TakeDownRequest {
        @NotNull(message = "任务ID不能为空")
        private Long taskId;
        @NotBlank(message = "下架原因不能为空")
        private String reason;

        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class FreezeUserRequest {
        @NotNull(message = "用户ID不能为空")
        private Long userId;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }
}
