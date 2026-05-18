
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `phone` VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `role` VARCHAR(20) NOT NULL COMMENT '角色',
    `credit_score` INT DEFAULT 100 COMMENT '信用分',
    `status` INT DEFAULT 1 COMMENT '状态',
    `version` INT DEFAULT 0 COMMENT '版本号',
    `deleted` INT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_phone` (`phone`),
    INDEX `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `task` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    `title` VARCHAR(100) NOT NULL COMMENT '任务标题',
    `description` TEXT COMMENT '任务描述',
    `category` VARCHAR(20) NOT NULL COMMENT '任务分类',
    `reward` DECIMAL(10,2) NOT NULL COMMENT '奖励金额',
    `service_fee` DECIMAL(10,2) NOT NULL COMMENT '服务费',
    `status` VARCHAR(20) NOT NULL COMMENT '任务状态',
    `deadline` DATETIME NOT NULL COMMENT '截止时间',
    `location` VARCHAR(100) COMMENT '任务地点',
    `contact_info` VARCHAR(50) COMMENT '联系方式',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `requester_id` BIGINT NOT NULL COMMENT '需求方ID',
    `helper_id` BIGINT COMMENT '接单方ID',
    `proof_images` TEXT COMMENT '凭证图片',
    `version` INT DEFAULT 0 COMMENT '版本号',
    `deleted` INT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_requester_id` (`requester_id`),
    INDEX `idx_helper_id` (`helper_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

CREATE TABLE IF NOT EXISTS `task_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
    `action` VARCHAR(20) NOT NULL COMMENT '操作类型',
    `description` VARCHAR(200) COMMENT '操作描述',
    `before_status` VARCHAR(20) COMMENT '操作前状态',
    `after_status` VARCHAR(20) COMMENT '操作后状态',
    `deleted` INT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_task_id` (`task_id`),
    INDEX `idx_operator_id` (`operator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务日志表';

CREATE TABLE IF NOT EXISTS `account` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '账户ID',
    `user_id` BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    `balance` DECIMAL(12,2) DEFAULT 0 COMMENT '余额',
    `frozen_balance` DECIMAL(12,2) DEFAULT 0 COMMENT '冻结金额',
    `version` INT DEFAULT 0 COMMENT '版本号',
    `deleted` INT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户表';

CREATE TABLE IF NOT EXISTS `transaction` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '流水ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `task_id` BIGINT COMMENT '任务ID',
    `type` VARCHAR(20) NOT NULL COMMENT '交易类型',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '金额',
    `description` VARCHAR(200) COMMENT '交易描述',
    `before_balance` DECIMAL(12,2) NOT NULL COMMENT '交易前余额',
    `after_balance` DECIMAL(12,2) NOT NULL COMMENT '交易后余额',
    `deleted` INT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资金流水表';

CREATE TABLE IF NOT EXISTS `notification` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '通知ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `task_id` BIGINT COMMENT '任务ID',
    `type` VARCHAR(20) NOT NULL COMMENT '通知类型',
    `title` VARCHAR(100) NOT NULL COMMENT '通知标题',
    `content` VARCHAR(500) COMMENT '通知内容',
    `is_read` INT DEFAULT 0 COMMENT '是否已读',
    `deleted` INT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

CREATE TABLE IF NOT EXISTS `review` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评价ID',
    `task_id` BIGINT NOT NULL UNIQUE COMMENT '任务ID',
    `reviewer_id` BIGINT NOT NULL COMMENT '评价者ID',
    `reviewee_id` BIGINT NOT NULL COMMENT '被评价者ID',
    `rating` INT NOT NULL COMMENT '评分',
    `content` VARCHAR(500) COMMENT '评价内容',
    `deleted` INT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_task_id` (`task_id`),
    INDEX `idx_reviewee_id` (`reviewee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

CREATE TABLE IF NOT EXISTS `dispute` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '争议ID',
    `task_id` BIGINT NOT NULL UNIQUE COMMENT '任务ID',
    `initiator_id` BIGINT NOT NULL COMMENT '发起者ID',
    `reason` VARCHAR(100) NOT NULL COMMENT '争议原因',
    `description` TEXT COMMENT '争议描述',
    `evidence_images` TEXT COMMENT '证据图片',
    `status` VARCHAR(20) NOT NULL COMMENT '处理状态',
    `result` VARCHAR(20) COMMENT '处理结果',
    `remark` VARCHAR(500) COMMENT '处理备注',
    `deleted` INT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_task_id` (`task_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='争议表';
