# Step 4 · 技术决策记录 (ADR - Architecture Decision Records)

> **Step 目标**：用 ADR 格式记录项目中的**关键架构决策**，每条决策包含：背景、选项、决策、后果。  
> **意义**：ADR 是工程界标准实践，让评审看到"我们想清楚了再动手"。

---

## ADR-001 · 后端选用 Spring Boot 3.2 + Java 17

### 背景
- 课程主线是 Java EE / Spring 全家桶
- 项目核心难点是**资金事务一致性**（6 个资金动作）
- 团队对 Python/Node.js 熟悉度一般

### 选项
- **A. Spring Boot 3.2 + Java 17**（已选）
- B. NestJS (Node.js + TypeScript)
- C. FastAPI (Python)

### 决策
采纳 **A**。

### 理由
1. 课程主线（教学一致性）
2. `@Transactional` 是 Java 生态最强的事务抽象
3. MyBatis-Plus 的 `@Version` 乐观锁注解开箱即用
4. 团队 Java 基础扎实

### 后果
- ✅ 资金一致性方案成熟稳定
- ✅ 答辩时不会被问"为什么不学 Java"
- ⚠️ 学习曲线略陡（IoC/AOP），但**对大三下学生不是问题**
- ❌ 失去"用 Python 做 AI 集成"的优势（本项目不需要 AI 集成）

---

## ADR-002 · ORM 选用 MyBatis-Plus 而非 JPA

### 背景
- 项目有 8 张表，含**乐观锁**、**复杂查询**（如资金流水按类型筛选）
- 团队对 SQL 熟练

### 选项
- **A. MyBatis-Plus 3.5.5**（已选）
- B. Spring Data JPA + Hibernate
- C. 原生 MyBatis

### 决策
采纳 **A**。

### 理由
1. **乐观锁是本项目必修**（PRD §6.6）—— `@Version` 注解 + LambdaQueryWrapper
2. **Lambda QueryWrapper** 让代码可读性高（不写 SQL 字符串拼接）
3. **看得见 SQL**——便于审查资金一致性

### 后果
- ✅ 并发安全可控
- ⚠️ 复杂查询仍需手写 XML（少数场景）
- ❌ 不能像 JPA 那样"全自动"——但**本项目不追求全自动**

---

## ADR-003 · 前端状态管理选用 Pinia 而非 Vuex

### 背景
- Vue 3 时代，Vuex 4 已被标记"维护模式"
- 项目需要用户态、账户余额、未读通知数等全局状态

### 选项
- **A. Pinia 2.1**（已选）
- B. Vuex 4
- C. 不使用库（provide/inject + reactive）

### 决策
采纳 **A**。

### 理由
1. **Vue 官方推荐**
2. **TypeScript 友好**（虽然本项目用 JS，但未来易升级）
3. **API 与 Vuex 4 相似**，但更轻量

### 后果
- ✅ 与 Vue 3 组合式 API 完美集成
- ✅ `userStore.loadFromStorage()` 简化登录态持久化
- ⚠️ 旧 Vuex 资料可能误导新人——但 2023 年后已不存此问题

---

## ADR-004 · 样式方案选用 Tailwind 而非组件库

### 背景
- 校园平台需要"年轻/活泼"视觉
- Element Plus / Ant Design 偏企业风
- 团队希望"差异化设计"加评分

### 选项
- **A. Tailwind CSS 3.4**（已选）
- B. Element Plus
- C. Ant Design Vue
- D. 自研 CSS

### 决策
采纳 **A**。

### 理由
1. **校园场景** vs 组件库的"中规中矩"——自由发挥空间大
2. **包体积小**（PurgeCSS 剔除未用类）
3. **AI 生成代码时类名稳定**——Tailwind 类名规范，不会因版本升级失效
4. **设计一致性靠 Figma / 自定义 design token**——`tailwind.config.js` 中的 `accent-mauve` 系列

### 后果
- ✅ 视觉差异化（答辩加分点）
- ✅ 包体积小
- ⚠️ 开发时需要写更多类名（IDE 自动补全可缓解）
- ❌ 新人需要 1-2 天熟悉 Tailwind 类名

---

## ADR-005 · 认证方案选用 JWT 而非 Session

### 背景
- 前后端分离架构，前端 Vue SPA 在 3000 端口，后端 Spring Boot 在 8080
- 跨域不可避免

### 选项
- **A. JWT (Bearer Token)**（已选）
- B. Session + Cookie
- C. OAuth 2.0

### 决策
采纳 **A**。

### 理由
1. **前后端分离下 JWT 是事实标准**
2. **无状态**——后端不需要存 session，**便于水平扩展**（虽然本项目单实例）
3. **移动端友好**——Token 可放在任意 header

### 后果
- ✅ 跨域简单（CORS 配 `*`）
- ✅ 移动端可直接用
- ⚠️ **撤销困难**——Token 一旦签发，24h 内有效——本项目用户量小可接受
- 💡 **改进方向**：v2 可加 refresh token + 黑名单

---

## ADR-006 · 实时通信选用 WebSocket 而非轮询

### 背景
- 任务状态变更需要实时通知需求方/接单方
- 通知频率不高（每任务 4-5 次状态变更）

### 选项
- **A. WebSocket**（已选）
- B. Server-Sent Events (SSE)
- C. Long Polling
- D. 定时轮询

### 决策
采纳 **A**。

### 理由
1. **双向通信**——后端可主动推送，客户端也可发心跳
2. **Spring Boot 原生支持**（spring-boot-starter-websocket）
3. **比 SSE 更通用**（SSE 只能服务端推）

### 后果
- ✅ 实时性好
- ⚠️ **断线重连未实现**（PRD §6.7 已记录为缺口）
- ⚠️ **推送失败不重试**——DB 已落库，前端可主动拉取
- 💡 **改进方向**：加自动重连 + 心跳

---

## ADR-007 · 状态机实现选用"if 状态判断"而非状态机框架

### 背景
- 任务有 6 个状态（pending/ongoing/pending_review/completed/disputed/cancelled）
- 需要在 Service 层做合法转换校验

### 选项
- **A. if 状态判断 + @Transactional**（已选）
- B. Spring State Machine
- C. 第三方状态机库（如 Squirrel）

### 决策
采纳 **A**。

### 理由
1. **6 个状态不算多**——if 判断已足够清晰
2. **状态机框架引入新概念**——增加学习成本
3. **TaskServiceImpl 中 5 个状态判断** 集中在一处，**可读性不差**
4. **业务重点在"做什么"（freeze/transfer）**，不在"状态怎么转"

### 后果
- ✅ 实现简单
- ⚠️ 状态多时易漏掉某个转换——但**6 个状态可控**
- 💡 **改进方向**：可提取一个 `TaskStatusTransition` 工具类集中管理

---

## ADR-008 · 资金事务选用 `@Transactional` 而非手动管理

### 背景
- 6 个资金动作（freeze/unfreeze/transfer/recharge/dispute-resolve）
- 任何一步失败需要全部回滚

### 选项
- **A. Spring `@Transactional` 注解**（已选）
- B. 手动管理（手动开启/提交/回滚）
- C. 分布式事务（Seata 等）

### 决策
采纳 **A**。

### 理由
1. **Spring 生态最成熟**
2. **注解式简单**——加一个 `@Transactional` 即可
3. **自动传播**——`Propagation.REQUIRED` 默认行为符合需求
4. **回滚自动**——任何 RuntimeException 触发回滚

### 后果
- ✅ 实现简单
- ✅ 与业务代码解耦
- ⚠️ **事务边界要清晰**——Service 方法不能跨多个远程调用（避免大事务）
- 💡 **改进方向**：复杂场景可改用 `Propagation.REQUIRES_NEW`

---

## ADR-009 · 密码加密选用 BCrypt

### 背景
- 用户表存密码
- 不能用明文/MD5/SHA-1（已不安全）

### 选项
- **A. BCrypt（强度 10）**（已选）
- B. Argon2
- C. SCrypt

### 决策
采纳 **A**。

### 理由
1. **Spring Security 内置** —— `new BCryptPasswordEncoder()`
2. **加盐自动**——每次 hash 结果不同
3. **强度可调**——未来要升级时改 `strength=12`

### 后果
- ✅ 安全性足够
- ✅ 实现 1 行
- ⚠️ BCrypt 计算慢（~100ms/次）—— 登录场景可接受

---

## ADR-010 · 不引入 Redis / 消息队列

### 背景
- 单机部署，单数据库实例
- 任务量小（PRD 预估日 200 单）

### 选项
- **A. 不引入**（已选）
- B. Redis 做缓存
- C. RabbitMQ 做异步通知

### 决策
采纳 **A**。

### 理由
1. **课程时间紧**——引入 Redis 需要额外学习 + 部署
2. **单实例 MySQL 性能足够**——并发 100+ 用户无压力
3. **通知已经用 WebSocket**——不需要 MQ
4. **不增加运维复杂度**——单机演示场景下 Redis 是负担

### 后果
- ✅ 部署简单
- ✅ 答辩不增加"你 Redis 怎么配的"问题
- ⚠️ 任务量上量后（>1000 并发）需要重新评估
- 💡 **改进方向**：v2 可加 Redis 缓存任务列表 + 用 Caffeine 做本地缓存

---

## ADR 总览表

| 编号 | 决策 | 采纳 | 关键理由 |
|------|------|------|---------|
| 001 | 后端框架 | Spring Boot 3.2 | 课程主线 + 事务强 |
| 002 | ORM | MyBatis-Plus | 乐观锁 + 看得见 SQL |
| 003 | 前端状态 | Pinia | Vue 官方推荐 |
| 004 | 样式 | Tailwind | 差异化 + 包体积小 |
| 005 | 认证 | JWT | 前后端分离标配 |
| 006 | 实时通信 | WebSocket | 双向 + Spring 原生 |
| 007 | 状态机 | if 判断 | 6 状态可控 |
| 008 | 事务 | @Transactional | 生态成熟 |
| 009 | 密码 | BCrypt | Spring Security 内置 |
| 010 | 中间件 | 不引入 Redis/MQ | 时间紧 + 简化部署 |
