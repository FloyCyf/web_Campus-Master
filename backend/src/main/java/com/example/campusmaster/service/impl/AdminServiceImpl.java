package com.example.campusmaster.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusmaster.common.BusinessException;
import com.example.campusmaster.entity.Dispute;
import com.example.campusmaster.entity.Task;
import com.example.campusmaster.entity.User;
import com.example.campusmaster.mapper.DisputeMapper;
import com.example.campusmaster.mapper.TaskMapper;
import com.example.campusmaster.mapper.UserMapper;
import com.example.campusmaster.service.AccountService;
import com.example.campusmaster.service.AdminService;
import com.example.campusmaster.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DisputeMapper disputeMapper;

    @Autowired
    private AccountService accountService;

    @Autowired
    private NotificationService notificationService;

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        Long totalUsers = userMapper.selectCount(null);
        stats.put("totalUsers", totalUsers);

        Long totalTasks = taskMapper.selectCount(null);
        stats.put("totalTasks", totalTasks);

        LambdaQueryWrapper<Task> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Task::getStatus, "pending");
        Long pendingTasks = taskMapper.selectCount(pendingWrapper);
        stats.put("pendingTasks", pendingTasks);

        LambdaQueryWrapper<Task> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.eq(Task::getStatus, "completed");
        Long completedTasks = taskMapper.selectCount(completedWrapper);
        stats.put("completedTasks", completedTasks);

        LambdaQueryWrapper<Task> disputedWrapper = new LambdaQueryWrapper<>();
        disputedWrapper.eq(Task::getStatus, "disputed");
        Long disputedTasks = taskMapper.selectCount(disputedWrapper);
        stats.put("disputedTasks", disputedTasks);

        stats.put("totalAmount", BigDecimal.ZERO);

        return stats;
    }

    @Override
    public IPage<Task> listTasks(Page<Task> page, String status, String keyword) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Task::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Task::getTitle, keyword);
        }
        wrapper.orderByDesc(Task::getCreateTime);
        return taskMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Dispute> listDisputes(Page<Dispute> page, String status) {
        LambdaQueryWrapper<Dispute> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Dispute::getStatus, status);
        }
        wrapper.orderByDesc(Dispute::getCreateTime);
        return disputeMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<User> listUsers(Page<User> page, String role, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (role != null && !role.isEmpty()) {
            wrapper.eq(User::getRole, role);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return userMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public boolean resolveDispute(Long id, Long disputeId, String result, String remark) {
        Dispute dispute = disputeMapper.selectById(disputeId);
        if (dispute == null) {
            throw BusinessException.notFound("争议不存在");
        }

        if (!"pending".equals(dispute.getStatus())) {
            throw BusinessException.conflict("争议已处理");
        }

        Task task = taskMapper.selectById(dispute.getTaskId());
        if (task == null) {
            throw BusinessException.notFound("任务不存在");
        }

        if ("reject".equals(result)) {
            accountService.unfreezeAmount(task.getRequesterId(), task.getReward(), task.getId());
        } else if ("approve".equals(result)) {
            accountService.transfer(task.getRequesterId(), task.getHelperId(), task.getReward(), task.getId());
        } else {
            throw BusinessException.badRequest("无效的处理结果");
        }

        dispute.setStatus("resolved");
        dispute.setResult(result);
        dispute.setRemark(remark);
        disputeMapper.updateById(dispute);

        task.setStatus("completed");
        taskMapper.updateById(task);

        notificationService.sendNotification(
                task.getRequesterId(), task.getId(), "system", "争议已处理",
                "任务「" + task.getTitle() + "」的争议已处理，结果：" + ("reject".equals(result) ? "支持需求方" : "支持接单方")
        );

        notificationService.sendNotification(
                task.getHelperId(), task.getId(), "system", "争议已处理",
                "任务「" + task.getTitle() + "」的争议已处理，结果：" + ("approve".equals(result) ? "支持接单方" : "支持需求方")
        );

        log.info("争议处理成功: disputeId={}, result={}", disputeId, result);
        return true;
    }

    @Override
    @Transactional
    public boolean takeDownTask(Long id, Long taskId, String reason) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw BusinessException.notFound("任务不存在");
        }

        if ("pending".equals(task.getStatus())) {
            accountService.unfreezeAmount(task.getRequesterId(), task.getReward(), taskId);
        }

        taskMapper.deleteById(taskId);

        log.info("任务下架成功: taskId={}, reason={}", taskId, reason);
        return true;
    }

    @Override
    @Transactional
    public boolean freezeUser(Long id, Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        user.setStatus(0);
        userMapper.updateById(user);

        log.info("用户冻结成功: userId={}", userId);
        return true;
    }
}
