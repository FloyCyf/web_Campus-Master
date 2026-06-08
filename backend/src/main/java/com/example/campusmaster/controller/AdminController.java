package com.example.campusmaster.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusmaster.annotation.RequiresRoles;
import com.example.campusmaster.common.Result;
import com.example.campusmaster.entity.Dispute;
import com.example.campusmaster.entity.Task;
import com.example.campusmaster.entity.User;
import com.example.campusmaster.service.AdminService;
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

    @GetMapping("/stats")
    @RequiresRoles({"admin"})
    public Result<Map<String, Object>> getStats() {
        return Result.success(adminService.getStats());
    }

    @GetMapping("/tasks")
    @RequiresRoles({"admin"})
    public Result<IPage<Task>> listAllTasks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String auditStatus) {
        Page<Task> page = new Page<>(pageNum, pageSize);
        IPage<Task> result = adminService.listTasks(page, status, keyword, auditStatus);
        return Result.success(result);
    }

    @GetMapping("/disputes")
    @RequiresRoles({"admin"})
    public Result<IPage<Dispute>> listDisputes(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String status) {
        Page<Dispute> page = new Page<>(pageNum, pageSize);
        IPage<Dispute> result = adminService.listDisputes(page, status);
        return Result.success(result);
    }

    @GetMapping("/users")
    @RequiresRoles({"admin"})
    public Result<IPage<User>> listUsers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        IPage<User> result = adminService.listUsers(page, role, keyword);
        result.getRecords().forEach(user -> user.setPassword(null));
        return Result.success(result);
    }

    @PostMapping("/disputes/{id}/resolve")
    @RequiresRoles({"admin"})
    public Result<Void> resolveDispute(@PathVariable Long id, @RequestBody @Valid ResolveDisputeRequest request) {
        adminService.resolveDispute(id, id, request.getResult(), request.getRemark());
        return Result.success("申诉处理完成", null);
    }

    @PostMapping("/tasks/{id}/approve")
    @RequiresRoles({"admin"})
    public Result<Void> approveTask(@PathVariable Long id, @RequestBody ApproveTaskRequest request) {
        adminService.approveTask(id, id, request.getRemark());
        return Result.success("任务审核通过", null);
    }

    @PostMapping("/tasks/{id}/take-down")
    @RequiresRoles({"admin"})
    public Result<Void> takeDownTask(@PathVariable Long id, @RequestBody @Valid TakeDownRequest request) {
        adminService.takeDownTask(id, id, request.getReason());
        return Result.success("任务已下架", null);
    }

    @PostMapping("/users/{id}/freeze")
    @RequiresRoles({"admin"})
    public Result<Void> freezeUser(@PathVariable Long id) {
        adminService.freezeUser(id, id);
        return Result.success("用户已冻结", null);
    }

    public static class ResolveDisputeRequest {
        @NotBlank(message = "处理结果不能为空")
        private String result;
        private String remark;

        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }

    public static class TakeDownRequest {
        @NotBlank(message = "下架原因不能为空")
        private String reason;

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class ApproveTaskRequest {
        private String remark;

        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }
}
