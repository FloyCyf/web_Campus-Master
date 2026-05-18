package com.example.campusmaster.service.impl;

import com.example.campusmaster.common.BusinessException;
import com.example.campusmaster.entity.User;
import com.example.campusmaster.mapper.UserMapper;
import com.example.campusmaster.service.AccountService;
import com.example.campusmaster.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountService accountService;

    @Override
    public User login(String phone, String password) {
        User user = userMapper.selectByPhone(phone);

        if (user == null) {
            throw BusinessException.badRequest("手机号或密码错误");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw BusinessException.badRequest("手机号或密码错误");
        }

        if (user.getStatus() == 0) {
            throw BusinessException.forbidden("账号已被禁用");
        }

        return user;
    }

    @Override
    @Transactional
    public User register(String username, String phone, String password, String role) {
        User existingUser = userMapper.selectByPhone(phone);
        if (existingUser != null) {
            throw BusinessException.badRequest("该手机号已注册");
        }

        if (!"requester".equals(role) && !"helper".equals(role)) {
            throw BusinessException.badRequest("无效的角色");
        }

        User user = new User();
        user.setUsername(username);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setCreditScore(100);
        user.setStatus(1);

        userMapper.insert(user);

        accountService.getAccount(user.getId());

        log.info("用户注册成功: userId={}, phone={}", user.getId(), phone);
        return user;
    }

    @Override
    public User getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        return user;
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw BusinessException.badRequest("旧密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        log.info("用户修改密码成功: userId={}", userId);
    }

    @Override
    @Transactional
    public void updateCreditScore(Long userId, Integer changeScore) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        int newScore = user.getCreditScore() + changeScore;
        newScore = Math.max(0, Math.min(150, newScore));

        user.setCreditScore(newScore);
        userMapper.updateById(user);

        log.info("用户信用分更新: userId={}, score={}", userId, newScore);
    }
}
