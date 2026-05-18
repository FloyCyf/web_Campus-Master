package com.example.campusmaster.service.impl;

import com.example.campusmaster.common.BusinessException;
import com.example.campusmaster.entity.Account;
import com.example.campusmaster.entity.Transaction;
import com.example.campusmaster.mapper.AccountMapper;
import com.example.campusmaster.mapper.TransactionMapper;
import com.example.campusmaster.service.AccountService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountServiceImpl implements AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    @Override
    public Account getAccount(Long userId) {
        Account account = accountMapper.selectByUserId(userId);
        if (account == null) {
            account = new Account(userId, new BigDecimal("0"), new BigDecimal("0"));
            accountMapper.insert(account);
        }
        return account;
    }

    @Override
    @Transactional
    public void freezeAmount(Long userId, BigDecimal amount, Long taskId) {
        Account account = getAccount(userId);

        if (account.getBalance().compareTo(amount) < 0) {
            throw BusinessException.badRequest("余额不足");
        }

        account.setBalance(account.getBalance().subtract(amount));
        account.setFrozenBalance(account.getFrozenBalance().add(amount));
        accountMapper.updateById(account);

        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setTaskId(taskId);
        transaction.setType("freeze");
        transaction.setAmount(amount);
        transaction.setDescription("冻结金额用于发布任务");
        transaction.setBeforeBalance(account.getBalance().add(amount));
        transaction.setAfterBalance(account.getBalance());
        transactionMapper.insert(transaction);

        log.info("冻结金额成功: userId={}, amount={}, taskId={}", userId, amount, taskId);
    }

    @Override
    @Transactional
    public void unfreezeAmount(Long userId, BigDecimal amount, Long taskId) {
        Account account = getAccount(userId);

        if (account.getFrozenBalance().compareTo(amount) < 0) {
            throw BusinessException.badRequest("冻结金额不足");
        }

        BigDecimal beforeBalance = account.getBalance();
        BigDecimal beforeFrozenBalance = account.getFrozenBalance();
        account.setFrozenBalance(beforeFrozenBalance.subtract(amount));
        account.setBalance(beforeBalance.add(amount));
        accountMapper.updateById(account);

        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setTaskId(taskId);
        transaction.setType("unfreeze");
        transaction.setAmount(amount);
        transaction.setDescription("任务取消，冻结金额解冻");
        transaction.setBeforeBalance(beforeBalance);
        transaction.setAfterBalance(account.getBalance());
        transactionMapper.insert(transaction);

        log.info("解冻金额成功: userId={}, amount={}, taskId={}", userId, amount, taskId);
    }

    @Override
    @Transactional
    public void transfer(Long fromUserId, Long toUserId, BigDecimal amount, Long taskId) {
        Account fromAccount = getAccount(fromUserId);

        if (fromAccount.getFrozenBalance().compareTo(amount) < 0) {
            throw BusinessException.badRequest("冻结金额不足");
        }

        Account toAccount = getAccount(toUserId);

        BigDecimal serviceFee = amount.multiply(new BigDecimal("0.05"));
        BigDecimal actualAmount = amount.subtract(serviceFee);

        fromAccount.setFrozenBalance(fromAccount.getFrozenBalance().subtract(amount));

        BigDecimal toBeforeBalance = toAccount.getBalance();
        toAccount.setBalance(toAccount.getBalance().add(actualAmount));

        accountMapper.updateById(fromAccount);
        accountMapper.updateById(toAccount);

        Transaction fromTransaction = new Transaction();
        fromTransaction.setUserId(fromUserId);
        fromTransaction.setTaskId(taskId);
        fromTransaction.setType("outcome");
        fromTransaction.setAmount(amount);
        fromTransaction.setDescription("任务完成支付");
        fromTransaction.setBeforeBalance(fromAccount.getFrozenBalance().add(amount));
        fromTransaction.setAfterBalance(fromAccount.getFrozenBalance());
        transactionMapper.insert(fromTransaction);

        Transaction toTransaction = new Transaction();
        toTransaction.setUserId(toUserId);
        toTransaction.setTaskId(taskId);
        toTransaction.setType("income");
        toTransaction.setAmount(actualAmount);
        toTransaction.setDescription("任务完成收入（已扣除5%服务费）");
        toTransaction.setBeforeBalance(toBeforeBalance);
        toTransaction.setAfterBalance(toAccount.getBalance());
        transactionMapper.insert(toTransaction);

        log.info("转账成功: fromUserId={}, toUserId={}, amount={}, actualAmount={}",
                fromUserId, toUserId, amount, actualAmount);
    }

    @Override
    @Transactional
    public void recharge(Long userId, BigDecimal amount) {
        Account account = getAccount(userId);

        BigDecimal beforeBalance = account.getBalance();
        account.setBalance(account.getBalance().add(amount));
        accountMapper.updateById(account);

        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setType("recharge");
        transaction.setAmount(amount);
        transaction.setDescription("账户充值");
        transaction.setBeforeBalance(beforeBalance);
        transaction.setAfterBalance(account.getBalance());
        transactionMapper.insert(transaction);

        log.info("充值成功: userId={}, amount={}", userId, amount);
    }

    @Override
    public IPage<Transaction> getTransactions(Page<Transaction> page, Long userId) {
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getUserId, userId)
                .orderByDesc(Transaction::getCreateTime);
        return transactionMapper.selectPage(page, wrapper);
    }
}
