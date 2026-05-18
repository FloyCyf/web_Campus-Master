
-- 插入管理员用户（密码：Admin123，BCrypt加密）
INSERT IGNORE INTO `user` (`id`, `username`, `phone`, `password`, `role`, `credit_score`, `status`)
VALUES (1, 'admin', '13800000000', '$2a$10$vBt6mouMl74cDwGW8tXP3.xgcaL7IfbaJ6KO0UsmNQmp/TPYg4bAu', 'admin', 100, 1);

-- 插入测试需求方用户（密码：123456）
INSERT IGNORE INTO `user` (`id`, `username`, `phone`, `password`, `role`, `credit_score`, `status`)
VALUES (2, '张小明', '13800000001', '$2a$10$009QoQNNI0He0n5C3BONcOdTY7ULATbnf0W2b6G8nE.XiPoxRhD9m', 'requester', 98, 1);

-- 插入测试接单方用户（密码：123456）
INSERT IGNORE INTO `user` (`id`, `username`, `phone`, `password`, `role`, `credit_score`, `status`)
VALUES (3, '李小红', '13800000003', '$2a$10$009QoQNNI0He0n5C3BONcOdTY7ULATbnf0W2b6G8nE.XiPoxRhD9m', 'helper', 95, 1);

-- 插入测试需求方用户2（密码：123456）
INSERT IGNORE INTO `user` (`id`, `username`, `phone`, `password`, `role`, `credit_score`, `status`)
VALUES (4, '王小华', '13800000004', '$2a$10$009QoQNNI0He0n5C3BONcOdTY7ULATbnf0W2b6G8nE.XiPoxRhD9m', 'requester', 100, 1);

-- 插入测试接单方用户2（密码：123456）
INSERT IGNORE INTO `user` (`id`, `username`, `phone`, `password`, `role`, `credit_score`, `status`)
VALUES (5, '赵小强', '13800000005', '$2a$10$009QoQNNI0He0n5C3BONcOdTY7ULATbnf0W2b6G8nE.XiPoxRhD9m', 'helper', 92, 1);

-- 为每个用户创建账户并初始充值
INSERT IGNORE INTO `account` (`user_id`, `balance`, `frozen_balance`) VALUES (1, 1000.00, 0.00);
INSERT IGNORE INTO `account` (`user_id`, `balance`, `frozen_balance`) VALUES (2, 500.00, 0.00);
INSERT IGNORE INTO `account` (`user_id`, `balance`, `frozen_balance`) VALUES (3, 200.00, 0.00);
INSERT IGNORE INTO `account` (`user_id`, `balance`, `frozen_balance`) VALUES (4, 800.00, 0.00);
INSERT IGNORE INTO `account` (`user_id`, `balance`, `frozen_balance`) VALUES (5, 100.00, 0.00);
