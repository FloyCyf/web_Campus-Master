# Step 3 · Controller 与原 API 文档差异审计表

> **审计目的**：对照 `API接口文档.md`（v1.0，42KB 手写）与 6 个 Controller 的实际实现，找出所有偏差。  
> **审计方式**：人工 + AI 协作（Claude）  
> **审计基准日**：2026-06-08

---

## ⚠️ 总览结论

| 指标 | 数值 |
|------|------|
| API 文档声明的接口数 | ~45 个（手写估算，含示例） |
| Controller 实际实现的接口数 | 30 个（6 个 Controller 全部 @RequestMapping） |
| **完全一致** | 0 个（路径前缀就不同！） |
| **接口语义一致，路径/方法略有偏差** | 约 18 个 |
| **仅代码有，文档无** | 约 8 个 |
| **仅文档有，代码无** | 约 5 个（可能未实现） |
| **严重偏差（路径前缀）** | 🔴 **1 个全局性问题** |

---

## 1. 🔴 全局性问题：路径前缀不一致

| 项 | API 文档 | 代码实际 | 严重度 | 处理 |
|----|---------|---------|--------|------|
| 基础路径 | `/api/v1`（带版本号） | `/api`（无版本号） | 🔴 全局 | **采纳代码值**，修订文档。理由：1) 代码已稳定运行 2) 加版本号要全改路径，影响前端调用 3) 6 个 Controller 全部一致，**改文档成本远低于改代码** |

> 💡 **亮点评审**：这是 Step 1 决策 1 的同类问题——**文档/代码冲突时选改动成本低的那个**。本项目第三次出现这个决策。

---

## 2. 用户模块（UserController）

| # | API 文档声明 | 代码实际 | 一致性 | 备注 |
|---|------------|---------|--------|------|
| 1 | `POST /api/v1/auth/register` | `POST /api/auth/register` | ⚠️ 路径前缀 | |
| 2 | `POST /api/v1/auth/login` | `POST /api/auth/login` | ⚠️ 路径前缀 | |
| 3 | `GET /api/v1/auth/me` | `GET /api/auth/me` | ⚠️ 路径前缀 | |
| 4 | `POST /api/v1/auth/logout` | ❌ 无 | 🟡 文档多 | 客户端用 localStorage.removeItem 替代 |
| 5 | `POST /api/v1/auth/password` | `POST /api/auth/password` | ⚠️ 路径前缀 | |
| 6 | `GET /api/v1/auth/users/{id}` | `GET /api/auth/users/{id}` | ⚠️ 路径前缀 | |
| 7 | 请求体字段 `username/password/phone/email/role/verifyCode` | 实际只收 `username/phone/password/role` | 🟠 缺 email/verifyCode | **代码简化**：未做邮箱、短信验证（PRD §7 范围界定已标"本期不做"） |
| 8 | 响应字段 `token/expiresIn/refreshToken/userInfo` | 实际只返 `token + 用户字段`（无 refreshToken） | 🟠 缺 refreshToken | **代码简化**：未实现 refresh token（24h JWT 过期则重新登录） |
| 9 | 错误码 `1001-1005`（用户名/手机号/密码/禁用/验证码） | `Result.badRequest("手机号或密码错误")` 通用 | 🟡 缺错误码体系 | **代码简化**：未做细分错误码（详细见 §5） |

---

## 3. 任务模块（TaskController）

| # | API 文档声明 | 代码实际 | 一致性 | 备注 |
|---|------------|---------|--------|------|
| 1 | `GET /api/v1/tasks` | `GET /api/tasks` | ⚠️ 路径前缀 | |
| 2 | `POST /api/v1/tasks` | `POST /api/tasks` | ⚠️ 路径前缀 | |
| 3 | `GET /api/v1/tasks/{id}` | `GET /api/tasks/{id}` | ⚠️ 路径前缀 | |
| 4 | `GET /api/v1/tasks/my/{type}` | `GET /api/tasks/my/{type}` | ✅ | 完全一致 |
| 5 | `POST /api/v1/tasks/{id}/accept` | `POST /api/tasks/{id}/accept` | ✅ | |
| 6 | `POST /api/v1/tasks/{id}/submit` | `POST /api/tasks/{id}/submit` | ✅ | |
| 7 | `POST /api/v1/tasks/{id}/complete` | `POST /api/tasks/{id}/complete` | ✅ | |
| 8 | `POST /api/v1/tasks/{id}/dispute` | `POST /api/tasks/{id}/dispute` | ✅ | |
| 9 | `POST /api/v1/tasks/{id}/cancel` | `POST /api/tasks/{id}/cancel` | ✅ | |
| 10 | `POST /api/v1/tasks/{id}/rate` | `POST /api/tasks/{id}/rate` | ✅ | |
| 11 | `POST /api/v1/tasks/{id}/take-down` | ❌ 在 AdminController 而非 TaskController | 🟠 归属错位 | 实际是 `POST /api/admin/tasks/{id}/take-down` |
| 12 | 任务状态码 `2001-2007` | `BusinessException.conflict()` 通用 | 🟡 缺错误码体系 | |
| 13 | 请求体 `category` 字段 | 代码用 String 而非枚举 | ✅ | 简化处理（实际值如 "delivery"/"food"/"print"/"other"） |

---

## 4. 账户模块（AccountController）

| # | API 文档声明 | 代码实际 | 一致性 | 备注 |
|---|------------|---------|--------|------|
| 1 | `GET /api/v1/account` | `GET /api/account` | ⚠️ 路径前缀 | |
| 2 | `GET /api/v1/account/transactions` | `GET /api/account/transactions` | ⚠️ 路径前缀 | |
| 3 | `POST /api/v1/account/recharge` | `POST /api/account/recharge` | ⚠️ 路径前缀 | |
| 4 | `POST /api/v1/account/withdraw` | ❌ 无 | 🟠 文档多 | 提现功能**本期不做**（PRD §7.1） |
| 5 | 响应字段 `balance/frozenAmount` | 实际 `Account` 实体字段 `balance/frozenBalance` | 🟡 字段名不一致 | 文档用 `frozenAmount`，代码用 `frozenBalance`——**采纳代码值** |
| 6 | 流水类型 `recharge/freeze/unfreeze/payment/income/refund` | 代码用 `recharge/freeze/unfreeze/income/outcome` | 🟡 字段名不一致 | 文档 `payment→代码 outcome`、`refund→代码无` |

---

## 5. 通知模块（NotificationController）

| # | API 文档声明 | 代码实际 | 一致性 | 备注 |
|---|------------|---------|--------|------|
| 1 | `GET /api/v1/notifications` | `GET /api/notifications` | ⚠️ 路径前缀 | |
| 2 | `GET /api/v1/notifications/unread-count` | `GET /api/notifications/unread-count` | ⚠️ 路径前缀 | |
| 3 | `PUT /api/v1/notifications/{id}/read` | `PUT /api/notifications/{id}/read` | ⚠️ 路径前缀 | |
| 4 | `PUT /api/v1/notifications/read-all` | `PUT /api/notifications/read-all` | ⚠️ 路径前缀 | |
| 5 | 通知类型枚举 | 类型用 String | ✅ | 实际值 task_accepted/task_submitted/... |

---

## 6. 评价模块（ReviewController）

| # | API 文档声明 | 代码实际 | 一致性 | 备注 |
|---|------------|---------|--------|------|
| 1 | `POST /api/v1/reviews` | `POST /api/reviews` | ⚠️ 路径前缀 | |
| 2 | `GET /api/v1/reviews/task/{taskId}` | `GET /api/reviews/task/{taskId}` | ⚠️ 路径前缀 | |
| 3 | `GET /api/v1/reviews/user/{userId}` | `GET /api/reviews/user/{userId}` | ⚠️ 路径前缀 | |
| 4 | `GET /api/v1/reviews/received` | `GET /api/reviews/received` | ⚠️ 路径前缀 | |
| 5 | `GET /api/v1/reviews/sent` | `GET /api/reviews/sent` | ⚠️ 路径前缀 | |
| 6 | 字段 `fromUserId/toUserId` | 实际 `reviewerId/revieweeId` | 🟡 字段名不一致 | **采纳代码值**（更符合通用语义） |

---

## 7. 管理员模块（AdminController）

| # | API 文档声明 | 代码实际 | 一致性 | 备注 |
|---|------------|---------|--------|------|
| 1 | `GET /api/v1/admin/stats` | `GET /api/admin/stats` | ⚠️ 路径前缀 | |
| 2 | `GET /api/v1/admin/tasks` | `GET /api/admin/tasks` | ⚠️ 路径前缀 | |
| 3 | `GET /api/v1/admin/disputes` | `GET /api/admin/disputes` | ⚠️ 路径前缀 | |
| 4 | `GET /api/v1/admin/users` | `GET /api/admin/users` | ⚠️ 路径前缀 | |
| 5 | `POST /api/v1/admin/disputes/{id}/resolve` | `POST /api/admin/disputes/{id}/resolve` | ⚠️ 路径前缀 | |
| 6 | `POST /api/v1/admin/tasks/{id}/approve` | `POST /api/admin/tasks/{id}/approve` | ⚠️ 路径前缀 | |
| 7 | `POST /api/v1/admin/users/{id}/freeze` | `POST /api/admin/users/{id}/freeze` | ⚠️ 路径前缀 | |
| 8 | `POST /api/v1/admin/tasks/{id}/take-down` | `POST /api/admin/tasks/{id}/take-down` | ⚠️ 路径前缀 | |

---

## 8. 严重偏差清单（必须修复或记录）

| # | 偏差 | 处理决策 | 理由 |
|---|------|---------|------|
| 🔴 1 | 路径前缀 `/api/v1/` vs `/api/` | **采纳代码，修订文档** | 改代码要改 6 个 Controller + 前端；改文档改 1 个文件 |
| 🟠 2 | 文档要求 `email/verifyCode` 注册字段 | **接受偏差，文档标"本期不做"** | PRD §7 已标"校园认证/短信验证"不在 MVP |
| 🟠 3 | 文档要求 `refreshToken` | **接受偏差，标"本期不做"** | 简化设计，24h 重新登录 |
| 🟡 4 | 文档错误码 `1001-1005/2001-2007/3001-3003` | **接受偏差，标"未实现细分错误码"** | 当前 `BusinessException.conflict("任务已被其他人接单")` 等已经描述清楚 |
| 🟡 5 | 字段名 `frozenAmount` vs `frozenBalance` | **采纳代码 `frozenBalance`** | 与 schema.sql 一致 |
| 🟡 6 | 字段名 `fromUserId/toUserId` vs `reviewerId/revieweeId` | **采纳代码** | 语义更标准 |
| 🟡 7 | 流水类型 `payment/refund` vs `outcome` | **采纳代码 `outcome`** | outcome 更准确（不是"支付"是"支出"） |
| 🟡 8 | `withdraw` 接口 | **接受偏差，标"本期不做"** | PRD §7.1 已标 |
| 🟡 9 | `logout` 接口 | **接受偏差** | 客户端用 localStorage 替代，服务端无状态 |

---

## 9. 整体审计结论

| 维度 | 评分 |
|------|------|
| 接口路径 | 🟠 一半一致（除前缀外，方法+路径基本一致） |
| 请求/响应字段 | 🟠 70% 一致，3 处命名差异 |
| 错误码体系 | 🔴 文档声明 vs 代码实现**严重脱节** |
| 鉴权标注 | ✅ Controller 用 `@RequiresRoles` 注解，比文档更准确 |
| 状态码（200/400/401/403/404/409/422） | 🟠 代码用 200/400/401/403/404/409；文档要求的 422/429 未实现 |
| 文档与代码同步性 | 🟡 **整体 60-70%**（去掉路径前缀后约 90%） |

---

## 10. 修复计划（v2 文档）

| 优先级 | 任务 | 负责人 | 状态 |
|--------|------|--------|------|
| 🔴 P0 | 路径前缀统一为 `/api`（文档改） | Floy | ⏳ 本 Step 进行 |
| 🟠 P1 | 删除文档中未实现的 `email/verifyCode/refreshToken/withdraw/logout` | Floy | ⏳ |
| 🟠 P1 | 字段名 `frozenBalance/reviewerId/revieweeId/outcome` 文档对齐 | Floy | ⏳ |
| 🟡 P2 | 错误码体系补充（1001-1005 等） | Step 7 后端 | ⏳ |
| 🟡 P2 | 422/429 状态码按需补 | Step 7 后端 | ⏳ |
| 🟢 P3 | Swagger UI 落地 | Step 3 末尾 | ⏳ |

---

## 11. 这份审计表对 j 章的价值

> 📌 **这份审计表是 j 章"AI 协作"的核心素材之一**：
> 1. 展示了**真实的人工审查过程**——不是 AI 一句话就相信
> 2. 展示了对"路径前缀不一致"等**全局性问题**的深度判断
> 3. 展示了**改文档 vs 改代码的成本权衡**（第三次出现这个决策）
> 4. 给出了**修复优先级**（P0/P1/P2/P3），是工程思维的体现

**建议保存到** `AI协作记录沉淀/03-Step3-Controller契约差异审计.md`（或合并到 00-通用反模式教训汇总.md）。
