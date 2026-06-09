-- 清理旧数据
DELETE FROM `user` WHERE 1=1;
DELETE FROM `account` WHERE 1=1;

-- 密码使用 BCrypt 加密：123456
INSERT INTO `user` (`username`, `phone`, `password`, `role`, `credit_score`, `status`) VALUES
('需求方', '13800000001', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', 'requester', 100, 1),
('接单方', '13800000002', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', 'helper', 100, 1),
('管理员', '13800000000', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', 'admin', 100, 1);

-- 创建账户
INSERT INTO `account` (`user_id`, `balance`, `frozen_balance`) VALUES
(1, 100.00, 0.00),
(2, 100.00, 0.00),
(3, 0.00, 0.00);
