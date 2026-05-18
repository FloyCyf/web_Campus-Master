
package com.example.campusmaster.service;

import com.example.campusmaster.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户登录
     * @param phone 手机号
     * @param password 密码
     * @return 用户信息
     */
    User login(String phone, String password);

    /**
     * 用户注册
     * @param username 用户名
     * @param phone 手机号
     * @param password 密码
     * @param role 角色
     * @return 用户信息
     */
    User register(String username, String phone, String password, String role);

    /**
     * 根据ID查询用户
     * @param userId 用户ID
     * @return 用户信息
     */
    User getUserById(Long userId);

    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 更新信用分
     * @param userId 用户ID
     * @param changeScore 分数变化（可正可负）
     */
    void updateCreditScore(Long userId, Integer changeScore);
}
