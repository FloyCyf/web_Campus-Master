package com.example.campusmaster.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusmaster.annotation.RequiresRoles;
import com.example.campusmaster.common.JwtUtil;
import com.example.campusmaster.common.Result;
import com.example.campusmaster.entity.Account;
import com.example.campusmaster.entity.Transaction;
import com.example.campusmaster.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    @RequiresRoles({"requester", "helper", "admin"})
    public Result<Account> getAccount(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Account account = accountService.getAccount(userId);
        return Result.success(account);
    }

    @GetMapping("/transactions")
    @RequiresRoles({"requester", "helper", "admin"})
    public Result<IPage<Transaction>> getTransactions(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Page<Transaction> page = new Page<>(pageNum, pageSize);
        IPage<Transaction> result = accountService.getTransactions(page, userId);
        return Result.success(result);
    }

    @PostMapping("/recharge")
    @RequiresRoles({"requester", "helper", "admin"})
    public Result<Account> recharge(@RequestBody @Valid RechargeRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        accountService.recharge(userId, request.getAmount());
        return Result.success("充值成功", accountService.getAccount(userId));
    }

    public static class RechargeRequest {
        @NotNull(message = "充值金额不能为空")
        private BigDecimal amount;

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }
}
