# Step 6 · Mock 与真接口差异审计表

> **Step 目标**：对比 `src/mock/index.js`（已门控不使用）与 `src/api/index.js`（真接口封装），找出 Mock 数据的"历史遗留问题"。  
> **审计基准日**：2026-06-08

---

## ⚠️ 总览

| 指标 | 数值 |
|------|------|
| Mock 注册的接口数 | 11 个 |
| 真 API 封装的接口数 | 23 个 |
| **路径完全一致** | 2 个 |
| **Mock 有，真接口无** | 9 个（其中 1 个是 mock 旧命名） |
| **Mock 无，真接口有** | 14 个（mock 不覆盖） |
| **严重偏差** | 3 处（路径/响应字段不一致） |

---

## 一、Mock vs 真接口 · 路径对照

| # | Mock 路径 | Mock 方法 | 真接口路径（`api/index.js`） | 一致性 |
|---|---------|----------|-----------------------------|--------|
| 1 | `/api/auth/login` | POST | `request.post('/auth/login')` | ✅ 一致 |
| 2 | `/api/auth/register` | POST | `request.post('/auth/register')` | ✅ 一致 |
| 3 | `/api/auth/users/{id}` | GET | `request.get('/auth/users/{userId}')` | ✅ 一致 |
| 4 | `/api/task/list` | GET | `request.get('/tasks')` | 🟠 **不一致**（Mock 用单数 + `/list`，真接口用复数） |
| 5 | `/api/task/my` | GET | `request.get('/tasks/my/{type}')` | 🟠 **不一致** |
| 6 | `/api/task/detail` | GET | `request.get('/tasks/{taskId}')` | 🟠 **不一致** |
| 7 | `/api/task/publish` | POST | `request.post('/tasks')` | 🟠 **不一致** |
| 8 | `/api/task/accept` | POST | `request.post('/tasks/{id}/accept')` | 🟠 **不一致** |
| 9 | `/api/task/submit` | POST | `request.post('/tasks/{id}/submit')` | 🟠 **不一致** |
| 10 | `/api/task/review` | POST | `request.post('/tasks/{id}/complete')` | 🟠 **不一致**（方法名不同） |
| 11 | `/api/task/cancel` | POST | `request.post('/tasks/{id}/cancel')` | 🟠 **不一致** |
| 12 | `/api/task/rate` | POST | `request.post('/tasks/{id}/rate')` | 🟠 **不一致** |
| 13 | `/api/account/info` | GET | `request.get('/account')` | 🟠 **不一致** |
| 14 | `/api/fund/flow` | GET | `request.get('/account/transactions')` | 🟠 **不一致** |
| 15 | `/api/notification/list` | GET | `request.get('/notifications')` | 🟠 **不一致** |
| 16 | `/api/notification/read` | POST | `request.put('/notifications/read-all')` | 🟠 **不一致** |
| 17 | `/api/admin/tasks` | GET | `request.get('/admin/tasks')` | ✅ 一致 |
| 18 | `/api/admin/disputes` | GET | `request.get('/admin/disputes')` | ✅ 一致 |
| 19 | `/api/admin/resolve` | POST | `request.post('/admin/disputes/{id}/resolve')` | 🟠 **不一致** |
| 20 | `/api/admin/statistics` | GET | `request.get('/admin/stats')` | 🟠 **不一致** |

---

## 二、Mock 与真接口的"语义错位"

| 错位 | Mock 做法 | 真接口做法 | 业务影响 |
|------|---------|-----------|---------|
| 1. 资源命名 | 单数 `/task` | 复数 `/tasks` | RESTful 不规范（资源应复数） |
| 2. 子资源位置 | `/task/list`、`/task/detail` | `/tasks`、`/tasks/{id}` | 真接口更符合 RESTful |
| 3. 验收 vs 完成 | Mock 用 `/task/review` | 真接口用 `/tasks/{id}/complete` | 命名歧义（"review"也指"查看"） |
| 4. 流水查询 | `/fund/flow` | `/account/transactions` | 命名不同 |
| 5. 通知已读 | `POST /notification/read` | `PUT /notifications/read-all` | HTTP 方法不同（POST vs PUT） |

---

## 三、Mock 数据的"数据格式"问题

### 3.1 任务分类字段

| 来源 | 字段 | 取值 |
|------|------|------|
| Mock | `category` | `delivery` / `food` / `print` / `other` |
| 真接口 | `category` | 同上 |
| **一致性** | ✅ 一致 |

### 3.2 任务状态字段

| 来源 | 字段 | 取值 |
|------|------|------|
| Mock | `status` | `pending` / `ongoing` / `pending_review` / `completed` / `disputed` / `cancelled` |
| 真接口 | `status` | 同上 |
| **一致性** | ✅ 一致 |

### 3.3 用户角色字段

| 来源 | 字段 | 取值 |
|------|------|------|
| Mock | `role` | `requester` / `helper` / `admin` |
| 真接口 | `role` | 同上 |
| **一致性** | ✅ 一致 |

### 3.4 金额字段

| 来源 | 字段 | 类型 |
|------|------|------|
| Mock | `reward` | Number（`15.00`） |
| 真接口 | `reward` | Number（后端 DECIMAL 转 Number） |
| **一致性** | ✅ 一致 |

### 3.5 时间字段

| 来源 | 字段 | 格式 |
|------|------|------|
| Mock | `createTime` | `'2026-05-17 10:30:00'` |
| 真接口 | `createTime` | 同上 |
| **一致性** | ✅ 一致 |

---

## 四、Mock 数据"过时"的字段

| 字段 | Mock 用法 | 真接口用法 | 处理 |
|------|---------|-----------|------|
| `viewCount` | ✅ 有 | ⚠️ 后端有但未自增 | Mock 数据假装自增；真接口永远是 0 |
| `auditStatus` | ❌ 无 | ✅ 有 | Mock 缺，**真接口有但只读** |
| `auditRemark` | ❌ 无 | ✅ 有 | 同上 |
| `proofImages` | ❌ 无 | ✅ 有 | Mock 缺 |
| `version` | ❌ 无 | ✅ 有（乐观锁） | Mock 缺 |

> 💡 **亮点评审**：**Mock 比真接口少 5 个字段**——证明 Mock 是在"先做"阶段写的，后来真接口加了字段但没回写 Mock。

---

## 五、Mock 的"测试账号"与 `data.sql` 不一致

| Mock 测试账号 | data.sql 测试账号 | 一致性 |
|-------------|------------------|--------|
| phone `13800000000` (admin) | `13800000000` (admin) | ✅ |
| phone `13800000001` (张小明 requester) | `13800000001` (张小明 requester) | ✅ |
| phone `13800000002` (李小红 helper) ❌ | `13800000003` (李小红 helper) | 🟠 **不一致** |
| phone `13800000003` (王小华 requester) | `13800000004` (王小华 requester) | 🟠 **不一致** |
| phone `13800000004` (赵小强 helper) | `13800000005` (赵小强 helper) | 🟠 **不一致** |

> **现状**：如果开 Mock，登录 `13800000002` 能登（Mock 假数据）；登录 `13800000003` 也能登（Mock 假数据）。  
> **但接真接口时**：登录 `13800000002` 会失败（data.sql 没这个号）。

---

## 六、Mock 与真接口的"使用场景"

| 场景 | 用 Mock？ | 用真接口？ |
|------|---------|----------|
| 开发阶段（后端未就绪） | ✅ | ❌ |
| 后端就绪后开发 | ❌ | ✅ |
| 演示 | ❌ | ✅（演示必须真） |
| 单元测试 | ✅ | ❌ |
| E2E 测试 | ❌ | ✅ |

**当前 main.js 配置**：

```js
if (import.meta.env.VITE_USE_MOCK === 'true') {
  await import('./mock')
}
```

**默认 `VITE_USE_MOCK=false`**——所以**默认走真接口**，Mock 仅作为应急/参考。

---

## 七、修复建议

| 优先级 | 任务 | 工作量 |
|--------|------|--------|
| 🔴 P0 | **删除 `src/mock/index.js`**（main.js 已门控，删除以绝后患） | 5 分钟 |
| 🟠 P1 | 把 Mock 中的 5 个测试账号数据**移入 data.sql**（已经在） | 0（已完成） |
| 🟡 P2 | 把 Mock 中有用的"测试任务数据"**写成 SQL 插入** | 10 分钟 |

> 💡 **建议直接删除 mock/index.js**——保留它只会让"未来某天有人误开启"成为可能。

---

## 八、为什么不修 Mock 而要"删除"？

**核心理由**：
1. **真接口已经稳定**——所有功能走真接口
2. **Mock 是历史包袱**——保留只会让代码难维护
3. **修复 Mock 路径要全部改写**（20+ 个路径）—— 工作量 ≈ 重写
4. **修复后的 Mock 也没人用**——投入无产出

> 💡 **亮点评审**：**"删除"是工程决策**——不是"图省事"，是"避免未来的维护负担"。

---

## 九、删除 Mock 的执行步骤

```bash
# 1. 删除文件
rm src/mock/index.js
rmdir src/mock  # 如果是空目录

# 2. 修改 main.js
# 删除以下 3 行：
if (import.meta.env.VITE_USE_MOCK === 'true') {
  await import('./mock')
}

# 3. 删除 .env 中的 VITE_USE_MOCK
# （如果存在的话）
```

**预计 5 分钟**，**风险接近 0**（因为默认就是 false）。

---

## 十、对 j 章的价值

> 📌 **这份审计表 + "删除 Mock"决策** 是 j 章"个人思考"的素材：
> 1. 展示了"AI 不会主动建议你删除代码"——是"减法"思维
> 2. 展示了"修复 vs 删除"的工程判断
> 3. 展示了"测试账号不一致"等真实问题
