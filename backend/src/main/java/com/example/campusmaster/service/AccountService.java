package com.example.campusmaster.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusmaster.entity.Account;
import com.example.campusmaster.entity.Transaction;

import java.math.BigDecimal;

/**
 * 账户服务接口
 */
public interface AccountService {

    Account getAccount(Long userId);

    void freezeAmount(Long userId, BigDecimal amount, Long taskId);

    void unfreezeAmount(Long userId, BigDecimal amount, Long taskId);

    void transfer(Long fromUserId, Long toUserId, BigDecimal amount, Long taskId);

    void recharge(Long userId, BigDecimal amount);

    IPage<Transaction> getTransactions(Page<Transaction> page, Long userId);
}
