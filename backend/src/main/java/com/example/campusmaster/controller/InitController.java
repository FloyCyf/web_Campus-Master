package com.example.campusmaster.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.campusmaster.common.Result;
import com.example.campusmaster.entity.Account;
import com.example.campusmaster.entity.User;
import com.example.campusmaster.mapper.AccountMapper;
import com.example.campusmaster.mapper.UserMapper;
import com.example.campusmaster.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/init")
public class InitController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountService accountService;

    @PostMapping("/users")
    public Result<String> initUsers() {
        userMapper.deleteAll();
        accountMapper.delete(new LambdaQueryWrapper<>());

        createUser("需求方", "13800000001", "123456", "requester");
        createUser("接单方", "13800000002", "123456", "helper");
        createUser("管理员", "13800000000", "123456", "admin");

        return Result.success("初始化成功", "已创建3个测试账号，各充值1000元");
    }

    private void createUser(String username, String phone, String password, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setCreditScore(100);
        user.setStatus(1);
        userMapper.insert(user);
        
        Account account = accountService.getAccount(user.getId());
        account.setBalance(new BigDecimal("1000.00"));
        accountMapper.updateById(account);
    }
}
