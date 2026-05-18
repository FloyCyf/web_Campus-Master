package com.example.campusmaster.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusmaster.entity.Dispute;
import com.example.campusmaster.entity.Task;
import com.example.campusmaster.entity.User;

import java.util.Map;

/**
 * 管理员服务接口
 */
public interface AdminService {

    Map<String, Object> getStats();

    IPage<Task> listTasks(Page<Task> page, String status, String keyword);

    IPage<Dispute> listDisputes(Page<Dispute> page, String status);

    IPage<User> listUsers(Page<User> page, String role, String keyword);

    boolean resolveDispute(Long id, Long disputeId, String result, String remark);

    boolean takeDownTask(Long id, Long taskId, String reason);

    boolean freezeUser(Long id, Long userId);
}
