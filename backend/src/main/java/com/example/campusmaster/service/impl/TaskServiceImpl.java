package com.example.campusmaster.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusmaster.common.BusinessException;
import com.example.campusmaster.entity.Dispute;
import com.example.campusmaster.entity.Review;
import com.example.campusmaster.entity.Task;
import com.example.campusmaster.entity.TaskLog;
import com.example.campusmaster.mapper.DisputeMapper;
import com.example.campusmaster.mapper.ReviewMapper;
import com.example.campusmaster.mapper.TaskLogMapper;
import com.example.campusmaster.mapper.TaskMapper;
import com.example.campusmaster.service.AccountService;
import com.example.campusmaster.service.NotificationService;
import com.example.campusmaster.service.TaskService;
import com.example.campusmaster.service.UserService;
import com.example.campusmaster.controller.TaskController.CreateTaskRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskLogMapper taskLogMapper;

    @Autowired
    private DisputeMapper disputeMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private AccountService accountService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public Long createTask(CreateTaskRequest request, Long requesterId) {
        BigDecimal reward = request.getReward();
        if (reward.compareTo(BigDecimal.ZERO) <= 0) {
            throw BusinessException.badRequest("奖励金额必须大于0");
        }

        BigDecimal serviceFee = reward.multiply(new BigDecimal("0.05"));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCategory(request.getCategory());
        task.setReward(reward);
        task.setServiceFee(serviceFee);
        task.setStatus("pending");
        task.setDeadline(request.getDeadline());
        task.setLocation(request.getLocation());
        task.setContactInfo(request.getContactInfo());
        task.setViewCount(0);
        task.setRequesterId(requesterId);

        taskMapper.insert(task);
        accountService.freezeAmount(requesterId, reward, task.getId());

        saveTaskLog(task.getId(), requesterId, "create", "创建任务", null, "pending");

        log.info("任务发布成功: taskId={}, title={}", task.getId(), request.getTitle());
        return task.getId();
    }

    @Override
    public Task getTaskById(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw BusinessException.notFound("任务不存在");
        }
        return task;
    }

    @Override
    public IPage<Task> listTasks(Page<Task> page, String status, String category) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Task::getStatus, status);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Task::getCategory, category);
        }
        wrapper.orderByDesc(Task::getCreateTime);
        return taskMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Task> getTasksByRequester(Page<Task> page, Long requesterId) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getRequesterId, requesterId).orderByDesc(Task::getCreateTime);
        return taskMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Task> getTasksByHelper(Page<Task> page, Long helperId) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getHelperId, helperId).orderByDesc(Task::getCreateTime);
        return taskMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public boolean acceptTask(Long taskId, Long helperId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw BusinessException.notFound("任务不存在");
        }

        if (!"pending".equals(task.getStatus())) {
            throw BusinessException.conflict("任务状态不允许接单");
        }

        if (task.getRequesterId().equals(helperId)) {
            throw BusinessException.badRequest("不能接自己发布的任务");
        }

        task.setHelperId(helperId);
        task.setStatus("ongoing");

        int updated = taskMapper.updateById(task);
        if (updated == 0) {
            throw BusinessException.conflict("任务已被其他人接单");
        }

        saveTaskLog(taskId, helperId, "accept", "接单成功", "pending", "ongoing");

        notificationService.sendNotification(
                task.getRequesterId(), taskId, "task_accepted",
                "任务被接单", "您发布的任务「" + task.getTitle() + "」已被接单"
        );

        log.info("任务接单成功: taskId={}, helperId={}", taskId, helperId);
        return true;
    }

    @Override
    @Transactional
    public boolean submitTask(Long taskId, String proofImages, Long helperId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw BusinessException.notFound("任务不存在");
        }

        if (!"ongoing".equals(task.getStatus())) {
            throw BusinessException.conflict("任务状态不允许提交");
        }

        if (!task.getHelperId().equals(helperId)) {
            throw BusinessException.forbidden("无权操作此任务");
        }

        task.setStatus("pending_review");
        task.setProofImages(proofImages);

        int updated = taskMapper.updateById(task);
        if (updated == 0) {
            throw BusinessException.conflict("任务更新失败");
        }

        saveTaskLog(taskId, helperId, "submit", "提交完成凭证", "ongoing", "pending_review");

        notificationService.sendNotification(
                task.getRequesterId(), taskId, "task_submitted",
                "任务已提交", "您发布的任务「" + task.getTitle() + "」已完成并提交凭证"
        );

        log.info("任务提交成功: taskId={}, helperId={}", taskId, helperId);
        return true;
    }

    @Override
    @Transactional
    public boolean completeTask(Long taskId, Long requesterId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw BusinessException.notFound("任务不存在");
        }

        if (!"pending_review".equals(task.getStatus())) {
            throw BusinessException.conflict("任务状态不允许验收");
        }

        if (!task.getRequesterId().equals(requesterId)) {
            throw BusinessException.forbidden("无权操作此任务");
        }

        accountService.transfer(task.getRequesterId(), task.getHelperId(), task.getReward(), taskId);

        task.setStatus("completed");
        int updated = taskMapper.updateById(task);
        if (updated == 0) {
            throw BusinessException.conflict("任务更新失败");
        }

        saveTaskLog(taskId, requesterId, "complete", "确认完成", "pending_review", "completed");

        notificationService.sendNotification(
                task.getHelperId(), taskId, "task_completed",
                "任务已完成", "您接的任务「" + task.getTitle() + "」已完成，奖励已到账"
        );

        log.info("任务完成成功: taskId={}, requesterId={}", taskId, requesterId);
        return true;
    }

    @Override
    @Transactional
    public boolean disputeTask(Long taskId, Long userId, String reason) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw BusinessException.notFound("任务不存在");
        }

        Dispute existingDispute = disputeMapper.selectByTaskId(taskId);
        if (existingDispute != null) {
            throw BusinessException.conflict("该任务已存在争议");
        }

        if (!task.getRequesterId().equals(userId) && !task.getHelperId().equals(userId)) {
            throw BusinessException.forbidden("无权发起争议");
        }

        task.setStatus("disputed");
        taskMapper.updateById(task);

        Dispute dispute = new Dispute();
        dispute.setTaskId(taskId);
        dispute.setInitiatorId(userId);
        dispute.setReason(reason);
        dispute.setStatus("pending");

        disputeMapper.insert(dispute);

        saveTaskLog(taskId, userId, "dispute", "发起争议", task.getStatus(), "disputed");

        Long targetUserId = task.getRequesterId().equals(userId) ? task.getHelperId() : task.getRequesterId();
        notificationService.sendNotification(
                targetUserId, taskId, "dispute_created",
                "任务争议", "任务「" + task.getTitle() + "」已被发起争议，请关注处理结果"
        );

        log.info("争议创建成功: taskId={}, initiatorId={}", taskId, userId);
        return true;
    }

    @Override
    @Transactional
    public boolean cancelTask(Long taskId, Long userId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw BusinessException.notFound("任务不存在");
        }

        if (!"pending".equals(task.getStatus())) {
            throw BusinessException.conflict("任务状态不允许取消");
        }

        if (!task.getRequesterId().equals(userId)) {
            throw BusinessException.forbidden("无权取消此任务");
        }

        accountService.unfreezeAmount(userId, task.getReward(), taskId);

        task.setStatus("cancelled");
        taskMapper.updateById(task);

        saveTaskLog(taskId, userId, "cancel", "取消任务", "pending", "cancelled");

        log.info("任务取消成功: taskId={}, requesterId={}", taskId, userId);
        return true;
    }

    @Override
    @Transactional
    public boolean rateTask(Long taskId, Long userId, Integer rating, String comment) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw BusinessException.notFound("任务不存在");
        }

        if (!"completed".equals(task.getStatus())) {
            throw BusinessException.conflict("任务未完成，无法评价");
        }

        Long targetUserId = null;
        if (task.getRequesterId().equals(userId)) {
            targetUserId = task.getHelperId();
        } else if (task.getHelperId().equals(userId)) {
            targetUserId = task.getRequesterId();
        } else {
            throw BusinessException.forbidden("无权评价此任务");
        }

        Review review = new Review();
        review.setTaskId(taskId);
        review.setReviewerId(userId);
        review.setRevieweeId(targetUserId);
        review.setRating(rating);
        review.setContent(comment);
        reviewMapper.insert(review);

        userService.updateCreditScore(targetUserId, rating >= 4 ? 2 : (rating >= 3 ? 0 : -2));

        saveTaskLog(taskId, userId, "rate", "评价任务", "completed", "completed");

        log.info("任务评价成功: taskId={}, reviewerId={}, revieweeId={}, rating={}", taskId, userId, targetUserId, rating);
        return true;
    }

    private void saveTaskLog(Long taskId, Long operatorId, String action, String description,
                             String beforeStatus, String afterStatus) {
        TaskLog taskLog = new TaskLog();
        taskLog.setTaskId(taskId);
        taskLog.setOperatorId(operatorId);
        taskLog.setAction(action);
        taskLog.setDescription(description);
        taskLog.setBeforeStatus(beforeStatus);
        taskLog.setAfterStatus(afterStatus);
        taskLogMapper.insert(taskLog);
    }
}
