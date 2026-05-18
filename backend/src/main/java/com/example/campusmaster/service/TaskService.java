package com.example.campusmaster.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusmaster.entity.Task;
import com.example.campusmaster.controller.TaskController.CreateTaskRequest;

/**
 * 任务服务接口
 */
public interface TaskService {

    Long createTask(CreateTaskRequest request, Long requesterId);

    Task getTaskById(Long taskId);

    IPage<Task> listTasks(Page<Task> page, String status, String category);

    IPage<Task> getTasksByRequester(Page<Task> page, Long requesterId);

    IPage<Task> getTasksByHelper(Page<Task> page, Long helperId);

    boolean acceptTask(Long taskId, Long helperId);

    boolean submitTask(Long taskId, String proofImages, Long helperId);

    boolean completeTask(Long taskId, Long requesterId);

    boolean disputeTask(Long taskId, Long userId, String reason);

    boolean cancelTask(Long taskId, Long userId);

    boolean rateTask(Long taskId, Long userId, Integer rating, String comment);
}