# 校园万事达互助众包任务平台 - RESTful API 接口契约

## 一、通用规范

### 1.1 基础信息

| 项目 | 说明 |
|------|------|
| 基础路径 | `/api/v1` |
| 协议 | HTTPS |
| 数据格式 | JSON |
| 编码 | UTF-8 |
| 命名规范 | camelCase |

### 1.2 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1700000000000
}
```

### 1.3 分页请求参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码，从1开始 |
| pageSize | Integer | 否 | 10 | 每页条数，最大100 |

### 1.4 分页响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 10
  },
  "timestamp": 1700000000000
}
```

### 1.5 状态码规范

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或Token失效 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 409 | 资源冲突（如抢单冲突） |
| 422 | 业务逻辑错误 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |

### 1.6 业务错误码规范

| 错误码 | 说明 |
|--------|------|
| 1001 | 用户名已存在 |
| 1002 | 手机号已注册 |
| 1003 | 密码错误 |
| 1004 | 账号已被禁用 |
| 1005 | 验证码错误 |
| 2001 | 任务不存在 |
| 2002 | 任务状态不允许此操作 |
| 2003 | 任务已被接单 |
| 2004 | 非任务发布者无权操作 |
| 2005 | 非任务接单者无权操作 |
| 2006 | 余额不足 |
| 2007 | 任务已过期 |
| 3001 | 账户余额不足 |
| 3002 | 资金冻结中 |
| 3003 | 提现金额超限 |

### 1.7 请求头规范

| Header | 必填 | 说明 |
|--------|------|------|
| Content-Type | 是 | application/json |
| Authorization | 是(需登录) | Bearer {token} |
| X-Request-Id | 否 | 请求追踪ID |

---

## 二、用户模块

### 2.1 用户注册

| 项目 | 说明 |
|------|------|
| 接口名称 | 用户注册 |
| 请求路径 | `/api/v1/auth/register` |
| 请求方法 | POST |
| 是否需要Token | 否 |
| 权限说明 | 所有人可访问 |

**请求参数 (Body)**

```json
{
  "username": "zhangsan",
  "password": "Password123",
  "phone": "13800138000",
  "email": "zhangsan@example.com",
  "role": "helper",
  "verifyCode": "123456"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名，3-20字符 |
| password | String | 是 | 密码，6-20字符，需包含字母和数字 |
| phone | String | 是 | 手机号，11位 |
| email | String | 是 | 邮箱地址 |
| role | String | 是 | 角色：helper(接单方)、requester(需求方) |
| verifyCode | String | 是 | 短信验证码，6位 |

**成功响应**

```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 10001,
    "username": "zhangsan",
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "role": "helper",
    "creditScore": 100,
    "balance": 0,
    "createdAt": "2024-01-01T10:00:00Z"
  },
  "timestamp": 1700000000000
}
```

**失败响应**

```json
{
  "code": 400,
  "message": "用户名已存在",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 2.2 用户登录

| 项目 | 说明 |
|------|------|
| 接口名称 | 用户登录 |
| 请求路径 | `/api/v1/auth/login` |
| 请求方法 | POST |
| 是否需要Token | 否 |
| 权限说明 | 所有人可访问 |

**请求参数 (Body)**

```json
{
  "phone": "13800138000",
  "password": "Password123",
  "rememberMe": true
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone | String | 是 | 手机号 |
| password | String | 是 | 密码 |
| rememberMe | Boolean | 否 | 是否记住登录，默认false |

**成功响应**

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "refreshToken": "refresh_token_string",
    "userInfo": {
      "userId": 10001,
      "username": "zhangsan",
      "phone": "13800138000",
      "email": "zhangsan@example.com",
      "avatar": "https://example.com/avatar.jpg",
      "role": "helper",
      "creditScore": 100,
      "balance": 50.00
    }
  },
  "timestamp": 1700000000000
}
```

**失败响应**

```json
{
  "code": 401,
  "message": "密码错误",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 2.3 退出登录

| 项目 | 说明 |
|------|------|
| 接口名称 | 退出登录 |
| 请求路径 | `/api/v1/auth/logout` |
| 请求方法 | POST |
| 是否需要Token | 是 |
| 权限说明 | 已登录用户 |

**请求参数** 无

**成功响应**

```json
{
  "code": 200,
  "message": "退出成功",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 2.4 刷新Token

| 项目 | 说明 |
|------|------|
| 接口名称 | 刷新Token |
| 请求路径 | `/api/v1/auth/refresh` |
| 请求方法 | POST |
| 是否需要Token | 是(使用refreshToken) |
| 权限说明 | 已登录用户 |

**请求参数 (Body)**

```json
{
  "refreshToken": "refresh_token_string"
}
```

**成功响应**

```json
{
  "code": 200,
  "message": "刷新成功",
  "data": {
    "token": "new_access_token",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "refreshToken": "new_refresh_token"
  },
  "timestamp": 1700000000000
}
```

---

### 2.5 获取当前用户信息

| 项目 | 说明 |
|------|------|
| 接口名称 | 获取当前用户信息 |
| 请求路径 | `/api/v1/users/me` |
| 请求方法 | GET |
| 是否需要Token | 是 |
| 权限说明 | 已登录用户 |

**请求参数** 无

**成功响应**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 10001,
    "username": "zhangsan",
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "avatar": "https://example.com/avatar.jpg",
    "role": "helper",
    "creditScore": 100,
    "balance": 50.00,
    "frozenBalance": 15.00,
    "completedTasks": 12,
    "publishedTasks": 5,
    "createdAt": "2024-01-01T10:00:00Z"
  },
  "timestamp": 1700000000000
}
```

---

### 2.6 更新用户信息

| 项目 | 说明 |
|------|------|
| 接口名称 | 更新用户信息 |
| 请求路径 | `/api/v1/users/me` |
| 请求方法 | PUT |
| 是否需要Token | 是 |
| 权限说明 | 已登录用户 |

**请求参数 (Body)**

```json
{
  "username": "zhangsan_new",
  "email": "newemail@example.com",
  "avatar": "https://example.com/new_avatar.jpg"
}
```

**成功响应**

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "userId": 10001,
    "username": "zhangsan_new",
    "email": "newemail@example.com",
    "avatar": "https://example.com/new_avatar.jpg"
  },
  "timestamp": 1700000000000
}
```

---

### 2.7 修改密码

| 项目 | 说明 |
|------|------|
| 接口名称 | 修改密码 |
| 请求路径 | `/api/v1/users/me/password` |
| 请求方法 | PUT |
| 是否需要Token | 是 |
| 权限说明 | 已登录用户 |

**请求参数 (Body)**

```json
{
  "oldPassword": "Password123",
  "newPassword": "NewPassword456",
  "confirmPassword": "NewPassword456"
}
```

**成功响应**

```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 2.8 获取用户信用分记录

| 项目 | 说明 |
|------|------|
| 接口名称 | 获取用户信用分记录 |
| 请求路径 | `/api/v1/users/me/credit-history` |
| 请求方法 | GET |
| 是否需要Token | 是 |
| 权限说明 | 已登录用户 |

**请求参数 (Query)**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |

**成功响应**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "recordId": 1,
        "change": 5,
        "reason": "按时完成任务",
        "taskId": 100,
        "createdAt": "2024-01-15T10:00:00Z"
      },
      {
        "recordId": 2,
        "change": -10,
        "reason": "逾期完成",
        "taskId": 101,
        "createdAt": "2024-01-10T10:00:00Z"
      }
    ],
    "total": 20,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 2
  },
  "timestamp": 1700000000000
}
```

---

### 2.9 发送验证码

| 项目 | 说明 |
|------|------|
| 接口名称 | 发送验证码 |
| 请求路径 | `/api/v1/auth/send-code` |
| 请求方法 | POST |
| 是否需要Token | 否 |
| 权限说明 | 所有人可访问 |

**请求参数 (Body)**

```json
{
  "phone": "13800138000",
  "type": "register"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone | String | 是 | 手机号 |
| type | String | 是 | 类型：register(注册)、login(登录)、reset(重置密码) |

**成功响应**

```json
{
  "code": 200,
  "message": "验证码已发送",
  "data": {
    "expireIn": 300
  },
  "timestamp": 1700000000000
}
```

---

## 三、任务模块

### 3.1 任务状态流转说明

```
┌─────────────┐
│   待接单    │ (pending)
│  (pending)  │
└──────┬──────┘
       │ 接单
       ▼
┌─────────────┐
│   进行中    │ (ongoing)
│  (ongoing)  │
└──────┬──────┘
       │ 提交完成
       ▼
┌─────────────┐
│   待验收    │ (pending_review)
└──────┬──────┘
       │
   ┌───┴───┐
   │       │
   ▼       ▼
┌─────┐ ┌─────────────┐
│已完成│ │   争议中    │ (disputed)
│(completed)│ (disputed) │
└─────┘ └──────┬──────┘
                │
                ▼
          ┌──────────┐
          │ 已完成/已取消 │
          └──────────┘
```

### 3.2 发布任务

| 项目 | 说明 |
|------|------|
| 接口名称 | 发布任务 |
| 请求路径 | `/api/v1/tasks` |
| 请求方法 | POST |
| 是否需要Token | 是 |
| 权限说明 | 需求方、管理员 |

**请求参数 (Body)**

```json
{
  "title": "帮我取一下菜鸟驿站的快递",
  "description": "有2个包裹，都是衣服，大概5kg，在菜鸟驿站3号门",
  "category": "delivery",
  "reward": 15.00,
  "deadline": "2024-01-20T18:00:00Z",
  "location": "菜鸟驿站3号门",
  "contactInfo": "13800138000",
  "attachments": [
    "https://example.com/image1.jpg"
  ]
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| title | String | 是 | 任务标题，最长50字符 |
| description | String | 是 | 任务描述，最长500字符 |
| category | String | 是 | 分类：delivery(代取快递)、food(代买外卖)、print(文件打印)、other(其他) |
| reward | Decimal | 是 | 报酬金额，最小0.01 |
| deadline | DateTime | 是 | 截止时间，需大于当前时间 |
| location | String | 否 | 任务地点 |
| contactInfo | String | 否 | 联系方式 |
| attachments | Array | 否 | 附件图片URL列表，最多9张 |

**成功响应**

```json
{
  "code": 200,
  "message": "发布成功",
  "data": {
    "taskId": 10001,
    "title": "帮我取一下菜鸟驿站的快递",
    "status": "pending",
    "reward": 15.00,
    "serviceFee": 0.75,
    "totalFee": 15.75,
    "frozen": true,
    "createdAt": "2024-01-15T10:00:00Z"
  },
  "timestamp": 1700000000000
}
```

**失败响应**

```json
{
  "code": 422,
  "message": "余额不足，请先充值",
  "data": {
    "required": 15.75,
    "current": 10.00
  },
  "timestamp": 1700000000000
}
```

---

### 3.3 获取任务列表

| 项目 | 说明 |
|------|------|
| 接口名称 | 获取任务列表 |
| 请求路径 | `/api/v1/tasks` |
| 请求方法 | GET |
| 是否需要Token | 否 |
| 权限说明 | 所有人可访问 |

**请求参数 (Query)**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |
| category | String | 否 | 分类筛选 |
| status | String | 否 | 状态筛选 |
| keyword | String | 否 | 关键词搜索 |
| sortBy | String | 否 | 排序字段：createdAt、reward、deadline |
| sortOrder | String | 否 | 排序方向：asc、desc |

**成功响应**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "taskId": 10001,
        "title": "帮我取一下菜鸟驿站的快递",
        "description": "有2个包裹，都是衣服",
        "category": "delivery",
        "categoryName": "代取快递",
        "reward": 15.00,
        "status": "pending",
        "statusName": "待接单",
        "deadline": "2024-01-20T18:00:00Z",
        "location": "菜鸟驿站3号门",
        "viewCount": 23,
        "requester": {
          "userId": 10001,
          "username": "小明",
          "avatar": "https://example.com/avatar.jpg",
          "creditScore": 95
        },
        "createdAt": "2024-01-15T10:00:00Z"
      }
    ],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 10
  },
  "timestamp": 1700000000000
}
```

---

### 3.4 获取任务详情

| 项目 | 说明 |
|------|------|
| 接口名称 | 获取任务详情 |
| 请求路径 | `/api/v1/tasks/{taskId}` |
| 请求方法 | GET |
| 是否需要Token | 否 |
| 权限说明 | 所有人可访问 |

**请求参数 (Path)**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| taskId | Long | 是 | 任务ID |

**成功响应**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "taskId": 10001,
    "title": "帮我取一下菜鸟驿站的快递",
    "description": "有2个包裹，都是衣服，大概5kg，在菜鸟驿站3号门",
    "category": "delivery",
    "categoryName": "代取快递",
    "reward": 15.00,
    "serviceFee": 0.75,
    "status": "pending",
    "statusName": "待接单",
    "deadline": "2024-01-20T18:00:00Z",
    "location": "菜鸟驿站3号门",
    "contactInfo": "13800138000",
    "attachments": [
      "https://example.com/image1.jpg"
    ],
    "viewCount": 24,
    "requester": {
      "userId": 10001,
      "username": "小明",
      "avatar": "https://example.com/avatar.jpg",
      "creditScore": 95,
      "completedTasks": 12
    },
    "helper": null,
    "submission": null,
    "review": null,
    "createdAt": "2024-01-15T10:00:00Z",
    "updatedAt": "2024-01-15T10:00:00Z"
  },
  "timestamp": 1700000000000
}
```

---

### 3.5 抢单/接单（重点：高并发设计）

| 项目 | 说明 |
|------|------|
| 接口名称 | 抢单/接单 |
| 请求路径 | `/api/v1/tasks/{taskId}/accept` |
| 请求方法 | POST |
| 是否需要Token | 是 |
| 权限说明 | 接单方 |

**设计要点**
- 使用乐观锁防止并发冲突
- Redis 分布式锁保证原子性
- 返回特定冲突码供前端处理

**请求参数 (Path)**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| taskId | Long | 是 | 任务ID |

**请求参数 (Body)**

```json
{
  "expectedStatus": "pending",
  "version": 1
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| expectedStatus | String | 是 | 期望的当前状态，用于乐观锁校验 |
| version | Integer | 是 | 数据版本号，用于乐观锁 |

**成功响应**

```json
{
  "code": 200,
  "message": "接单成功",
  "data": {
    "taskId": 10001,
    "status": "ongoing",
    "statusName": "进行中",
    "helper": {
      "userId": 10002,
      "username": "张三"
    },
    "acceptedAt": "2024-01-15T12:00:00Z"
  },
  "timestamp": 1700000000000
}
```

**失败响应 - 状态冲突**

```json
{
  "code": 409,
  "message": "任务已被其他人接单",
  "data": {
    "currentStatus": "ongoing",
    "conflictType": "STATUS_CHANGED"
  },
  "timestamp": 1700000000000
}
```

**失败响应 - 版本冲突**

```json
{
  "code": 409,
  "message": "数据已变更，请刷新后重试",
  "data": {
    "currentVersion": 2,
    "yourVersion": 1,
    "conflictType": "VERSION_MISMATCH"
  },
  "timestamp": 1700000000000
}
```

---

### 3.6 提交任务完成凭证

| 项目 | 说明 |
|------|------|
| 接口名称 | 提交任务完成凭证 |
| 请求路径 | `/api/v1/tasks/{taskId}/submit` |
| 请求方法 | POST |
| 是否需要Token | 是 |
| 权限说明 | 任务接单者 |

**请求参数 (Path)**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| taskId | Long | 是 | 任务ID |

**请求参数 (Body)**

```json
{
  "description": "已完成取件，2个包裹都在小推车上，完好无损",
  "attachments": [
    "https://example.com/proof1.jpg",
    "https://example.com/proof2.jpg"
  ]
}
```

**成功响应**

```json
{
  "code": 200,
  "message": "提交成功，等待验收",
  "data": {
    "taskId": 10001,
    "status": "pending_review",
    "statusName": "待验收",
    "submission": {
      "description": "已完成取件，2个包裹都在小推车上，完好无损",
      "attachments": [
        "https://example.com/proof1.jpg",
        "https://example.com/proof2.jpg"
      ],
      "submittedAt": "2024-01-16T10:00:00Z"
    }
  },
  "timestamp": 1700000000000
}
```

**失败响应 - 状态错误**

```json
{
  "code": 422,
  "message": "任务状态不允许此操作",
  "data": {
    "currentStatus": "pending",
    "allowedStatus": ["ongoing"]
  },
  "timestamp": 1700000000000
}
```

---

### 3.7 验收通过

| 项目 | 说明 |
|------|------|
| 接口名称 | 验收通过 |
| 请求路径 | `/api/v1/tasks/{taskId}/approve` |
| 请求方法 | POST |
| 是否需要Token | 是 |
| 权限说明 | 任务发布者 |

**请求参数 (Path)**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| taskId | Long | 是 | 任务ID |

**请求参数 (Body)**

```json
{
  "rating": 5,
  "comment": "非常准时，态度很好"
}
```

**成功响应**

```json
{
  "code": 200,
  "message": "验收通过，资金已转给接单方",
  "data": {
    "taskId": 10001,
    "status": "completed",
    "statusName": "已完成",
    "settledAmount": 15.00,
    "settledAt": "2024-01-16T12:00:00Z"
  },
  "timestamp": 1700000000000
}
```

---

### 3.8 验收拒绝

| 项目 | 说明 |
|------|------|
| 接口名称 | 验收拒绝 |
| 请求路径 | `/api/v1/tasks/{taskId}/reject` |
| 请求方法 | POST |
| 是否需要Token | 是 |
| 权限说明 | 任务发布者 |

**请求参数 (Path)**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| taskId | Long | 是 | 任务ID |

**请求参数 (Body)**

```json
{
  "reason": "未按照要求完成，包裹有损坏"
}
```

**成功响应**

```json
{
  "code": 200,
  "message": "已拒绝，接单方需重新提交",
  "data": {
    "taskId": 10001,
    "status": "ongoing",
    "statusName": "进行中"
  },
  "timestamp": 1700000000000
}
```

---

### 3.9 发起争议

| 项目 | 说明 |
|------|------|
| 接口名称 | 发起争议 |
| 请求路径 | `/api/v1/tasks/{taskId}/dispute` |
| 请求方法 | POST |
| 是否需要Token | 是 |
| 权限说明 | 任务发布者或接单者 |

**请求参数 (Path)**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| taskId | Long | 是 | 任务ID |

**请求参数 (Body)**

```json
{
  "reason": "接单方未按时完成，且无法联系",
  "evidence": [
    "https://example.com/evidence1.jpg"
  ]
}
```

**成功响应**

```json
{
  "code": 200,
  "message": "争议已提交，等待管理员处理",
  "data": {
    "taskId": 10001,
    "status": "disputed",
    "statusName": "争议中",
    "disputeId": 5001
  },
  "timestamp": 1700000000000
}
```

---

### 3.10 取消任务

| 项目 | 说明 |
|------|------|
| 接口名称 | 取消任务 |
| 请求路径 | `/api/v1/tasks/{taskId}/cancel` |
| 请求方法 | POST |
| 是否需要Token | 是 |
| 权限说明 | 任务发布者（仅待接单状态） |

**请求参数 (Path)**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| taskId | Long | 是 | 任务ID |

**请求参数 (Body)**

```json
{
  "reason": "不需要了"
}
```

**成功响应**

```json
{
  "code": 200,
  "message": "任务已取消，冻结资金已退还",
  "data": {
    "taskId": 10001,
    "status": "cancelled",
    "statusName": "已取消",
    "refundedAmount": 15.75
  },
  "timestamp": 1700000000000
}
```

---

### 3.11 评价任务

| 项目 | 说明 |
|------|------|
| 接口名称 | 评价任务 |
| 请求路径 | `/api/v1/tasks/{taskId}/review` |
| 请求方法 | POST |
| 是否需要Token | 是 |
| 权限说明 | 任务发布者或接单者（已完成状态） |

**请求参数 (Path)**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| taskId | Long | 是 | 任务ID |

**请求参数 (Body)**

```json
{
  "toUserId": 10002,
  "rating": 5,
  "tags": ["timely", "quality", "attitude"],
  "comment": "非常满意，下次还会找他"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| toUserId | Long | 是 | 被评价用户ID |
| rating | Integer | 是 | 评分，1-5 |
| tags | Array | 否 | 标签：timely(准时)、quality(质量)、attitude(态度) |
| comment | String | 否 | 评价内容，最长200字符 |

**成功响应**

```json
{
  "code": 200,
  "message": "评价成功",
  "data": {
    "reviewId": 1,
    "rating": 5,
    "createdAt": "2024-01-16T15:00:00Z"
  },
  "timestamp": 1700000000000
}
```

---

### 3.12 获取我发布的任务

| 项目 | 说明 |
|------|------|
| 接口名称 | 获取我发布的任务 |
| 请求路径 | `/api/v1/tasks/my/published` |
| 请求方法 | GET |
| 是否需要Token | 是 |
| 权限说明 | 已登录用户 |

**请求参数 (Query)**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |
| status | String | 否 | 状态筛选 |

**成功响应**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "taskId": 10001,
        "title": "帮我取一下菜鸟驿站的快递",
        "reward": 15.00,
        "status": "ongoing",
        "statusName": "进行中",
        "helper": {
          "userId": 10002,
          "username": "张三",
          "avatar": "https://example.com/avatar.jpg"
        },
        "deadline": "2024-01-20T18:00:00Z",
        "createdAt": "2024-01-15T10:00:00Z"
      }
    ],
    "total": 5,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1
  },
  "timestamp": 1700000000000
}
```

---

### 3.13 获取我承接的任务

| 项目 | 说明 |
|------|------|
| 接口名称 | 获取我承接的任务 |
| 请求路径 | `/api/v1/tasks/my/accepted` |
| 请求方法 | GET |
| 是否需要Token | 是 |
| 权限说明 | 已登录用户 |

**请求参数 (Query)**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |
| status | String | 否 | 状态筛选 |

**成功响应**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "taskId": 10001,
        "title": "帮我取一下菜鸟驿站的快递",
        "reward": 15.00,
        "status": "ongoing",
        "statusName": "进行中",
        "requester": {
          "userId": 10001,
          "username": "小明",
          "avatar": "https://example.com/avatar.jpg"
        },
        "deadline": "2024-01-20T18:00:00Z",
        "acceptedAt": "2024-01-15T12:00:00Z"
      }
    ],
    "total": 12,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 2
  },
  "timestamp": 1700000000000
}
```

---

## 四、资金账户模块

### 4.1 获取账户余额

| 项目 | 说明 |
|------|------|
| 接口名称 | 获取账户余额 |
| 请求路径 | `/api/v1/accounts/balance` |
| 请求方法 | GET |
| 是否需要Token | 是 |
| 权限说明 | 已登录用户 |

**成功响应**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "balance": 50.00,
    "frozenBalance": 15.00,
    "availableBalance": 35.00,
    "totalWithdrawn": 100.00
  },
  "timestamp": 1700000000000
}
```

---

### 4.2 充值

| 项目 | 说明 |
|------|------|
| 接口名称 | 充值 |
| 请求路径 | `/api/v1/accounts/recharge` |
| 请求方法 | POST |
| 是否需要Token | 是 |
| 权限说明 | 已登录用户 |

**请求参数 (Body)**

```json
{
  "amount": 100.00,
  "payMethod": "alipay",
  "returnUrl": "https://example.com/recharge/result"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| amount | Decimal | 是 | 充值金额，最小1元 |
| payMethod | String | 是 | 支付方式：alipay、wechat |
| returnUrl | String | 否 | 支付完成跳转地址 |

**成功响应**

```json
{
  "code": 200,
  "message": "订单创建成功",
  "data": {
    "orderId": "R202401150001",
    "amount": 100.00,
    "payUrl": "https://pay.example.com/xxx",
    "expireIn": 1800
  },
  "timestamp": 1700000000000
}
```

---

### 4.3 提现

| 项目 | 说明 |
|------|------|
| 接口名称 | 提现 |
| 请求路径 | `/api/v1/accounts/withdraw` |
| 请求方法 | POST |
| 是否需要Token | 是 |
| 权限说明 | 已登录用户 |

**请求参数 (Body)**

```json
{
  "amount": 50.00,
  "bankCard": "6222021234567890123",
  "realName": "张三"
}
```

**成功响应**

```json
{
  "code": 200,
  "message": "提现申请已提交",
  "data": {
    "withdrawId": "W202401150001",
    "amount": 50.00,
    "fee": 0.00,
    "actualAmount": 50.00,
    "status": "processing",
    "estimatedArrival": "2024-01-17T10:00:00Z"
  },
  "timestamp": 1700000000000
}
```

---

### 4.4 获取资金流水

| 项目 | 说明 |
|------|------|
| 接口名称 | 获取资金流水 |
| 请求路径 | `/api/v1/accounts/transactions` |
| 请求方法 | GET |
| 是否需要Token | 是 |
| 权限说明 | 已登录用户 |

**请求参数 (Query)**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |
| type | String | 否 | 类型：income、expense、frozen、unfrozen |
| startDate | Date | 否 | 开始日期 |
| endDate | Date | 否 | 结束日期 |

**成功响应**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "transactionId": "T202401150001",
        "type": "income",
        "typeName": "任务收益",
        "amount": 15.00,
        "balance": 65.00,
        "taskId": 10001,
        "taskTitle": "帮我取快递",
        "createdAt": "2024-01-15T12:00:00Z"
      },
      {
        "transactionId": "T202401140001",
        "type": "frozen",
        "typeName": "任务冻结",
        "amount": -15.75,
        "balance": 50.00,
        "taskId": 10002,
        "taskTitle": "代买奶茶",
        "createdAt": "2024-01-14T10:00:00Z"
      }
    ],
    "total": 50,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 5
  },
  "timestamp": 1700000000000
}
```

---

### 4.5 资金冻结（内部接口）

| 项目 | 说明 |
|------|------|
| 接口名称 | 资金冻结 |
| 请求路径 | `/api/v1/accounts/freeze` |
| 请求方法 | POST |
| 是否需要Token | 是(内部服务调用) |
| 权限说明 | 系统内部 |

**请求参数 (Body)**

```json
{
  "userId": 10001,
  "amount": 15.75,
  "taskId": 10001,
  "reason": "发布任务冻结"
}
```

**成功响应**

```json
{
  "code": 200,
  "message": "冻结成功",
  "data": {
    "freezeId": "F202401150001",
    "userId": 10001,
    "amount": 15.75,
    "balance": 34.25,
    "frozenBalance": 15.75
  },
  "timestamp": 1700000000000
}
```

---

### 4.6 资金解冻（内部接口）

| 项目 | 说明 |
|------|------|
| 接口名称 | 资金解冻 |
| 请求路径 | `/api/v1/accounts/unfreeze` |
| 请求方法 | POST |
| 是否需要Token | 是(内部服务调用) |
| 权限说明 | 系统内部 |

**请求参数 (Body)**

```json
{
  "freezeId": "F202401150001",
  "reason": "任务取消，解冻退还"
}
```

**成功响应**

```json
{
  "code": 200,
  "message": "解冻成功",
  "data": {
    "freezeId": "F202401150001",
    "amount": 15.75,
    "balance": 50.00,
    "frozenBalance": 0
  },
  "timestamp": 1700000000000
}
```

---

### 4.7 资金结算（内部接口）

| 项目 | 说明 |
|------|------|
| 接口名称 | 资金结算 |
| 请求路径 | `/api/v1/accounts/settle` |
| 请求方法 | POST |
| 是否需要Token | 是(内部服务调用) |
| 权限说明 | 系统内部 |

**设计要点**
- 使用分布式事务保证一致性
- 幂等性设计，支持重试
- 记录完整的结算流水

**请求参数 (Body)**

```json
{
  "taskId": 10001,
  "fromUserId": 10001,
  "toUserId": 10002,
  "amount": 15.00,
  "serviceFee": 0.75,
  "settlementId": "S202401150001"
}
```

**成功响应**

```json
{
  "code": 200,
  "message": "结算成功",
  "data": {
    "settlementId": "S202401150001",
    "taskId": 10001,
    "amount": 15.00,
    "serviceFee": 0.75,
    "fromUserBalance": 34.25,
    "toUserBalance": 65.00,
    "settledAt": "2024-01-16T12:00:00Z"
  },
  "timestamp": 1700000000000
}
```

---

## 五、通知模块

### 5.1 获取通知列表

| 项目 | 说明 |
|------|------|
| 接口名称 | 获取通知列表 |
| 请求路径 | `/api/v1/notifications` |
| 请求方法 | GET |
| 是否需要Token | 是 |
| 权限说明 | 已登录用户 |

**请求参数 (Query)**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |
| type | String | 否 | 类型：system、task、account |
| isRead | Boolean | 否 | 是否已读 |

**成功响应**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "notificationId": 1,
        "type": "task",
        "title": "任务已被接单",
        "content": "您的任务「帮我取快递」已被张三接单",
        "taskId": 10001,
        "isRead": false,
        "createdAt": "2024-01-15T12:00:00Z"
      }
    ],
    "total": 20,
    "unreadCount": 5,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 2
  },
  "timestamp": 1700000000000
}
```

---

### 5.2 标记通知已读

| 项目 | 说明 |
|------|------|
| 接口名称 | 标记通知已读 |
| 请求路径 | `/api/v1/notifications/{notificationId}/read` |
| 请求方法 | PUT |
| 是否需要Token | 是 |
| 权限说明 | 已登录用户 |

**请求参数 (Path)**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| notificationId | Long | 是 | 通知ID |

**成功响应**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 5.3 标记全部已读

| 项目 | 说明 |
|------|------|
| 接口名称 | 标记全部已读 |
| 请求路径 | `/api/v1/notifications/read-all` |
| 请求方法 | PUT |
| 是否需要Token | 是 |
| 权限说明 | 已登录用户 |

**成功响应**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "affectedCount": 5
  },
  "timestamp": 1700000000000
}
```

---

### 5.4 WebSocket 连接（实时通知）

| 项目 | 说明 |
|------|------|
| 接口名称 | WebSocket 连接 |
| 连接路径 | `/ws/notifications` |
| 协议 | WebSocket |
| 认证 | URL参数传递token: `/ws/notifications?token=xxx` |

**消息格式**

```json
{
  "type": "TASK_ACCEPTED",
  "data": {
    "taskId": 10001,
    "title": "帮我取快递",
    "helperName": "张三"
  },
  "timestamp": 1700000000000
}
```

---

## 六、管理员模块

### 6.1 获取统计数据

| 项目 | 说明 |
|------|------|
| 接口名称 | 获取统计数据 |
| 请求路径 | `/api/v1/admin/statistics` |
| 请求方法 | GET |
| 是否需要Token | 是 |
| 权限说明 | 管理员 |

**成功响应**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userCount": 1256,
    "taskCount": 342,
    "pendingTaskCount": 3,
    "disputedTaskCount": 2,
    "totalTransactionAmount": 45680.00,
    "todayTransactionAmount": 3280.00,
    "taskTrend": [
      { "date": "2024-01-09", "count": 12 },
      { "date": "2024-01-10", "count": 18 },
      { "date": "2024-01-11", "count": 15 }
    ],
    "categoryDistribution": [
      { "category": "delivery", "name": "代取快递", "count": 120, "percent": 35 },
      { "category": "food", "name": "代买外卖", "count": 92, "percent": 27 }
    ]
  },
  "timestamp": 1700000000000
}
```

---

### 6.2 获取待审核任务列表

| 项目 | 说明 |
|------|------|
| 接口名称 | 获取待审核任务列表 |
| 请求路径 | `/api/v1/admin/tasks/pending` |
| 请求方法 | GET |
| 是否需要Token | 是 |
| 权限说明 | 管理员 |

**请求参数 (Query)**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |

**成功响应**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "taskId": 10001,
        "title": "代写期末论文",
        "category": "other",
        "reward": 200.00,
        "reportCount": 5,
        "reportReason": "内容涉嫌学术不端",
        "requester": {
          "userId": 10001,
          "username": "小明"
        },
        "createdAt": "2024-01-15T10:00:00Z"
      }
    ],
    "total": 3,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1
  },
  "timestamp": 1700000000000
}
```

---

### 6.3 审核任务通过

| 项目 | 说明 |
|------|------|
| 接口名称 | 审核任务通过 |
| 请求路径 | `/api/v1/admin/tasks/{taskId}/approve` |
| 请求方法 | POST |
| 是否需要Token | 是 |
| 权限说明 | 管理员 |

**请求参数 (Path)**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| taskId | Long | 是 | 任务ID |

**成功响应**

```json
{
  "code": 200,
  "message": "审核通过",
  "data": null,
  "timestamp": 1700000000000
}
```

---

### 6.4 审核任务拒绝

| 项目 | 说明 |
|------|------|
| 接口名称 | 审核任务拒绝 |
| 请求路径 | `/api/v1/admin/tasks/{taskId}/reject` |
| 请求方法 | POST |
| 是否需要Token | 是 |
| 权限说明 | 管理员 |

**请求参数 (Path)**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| taskId | Long | 是 | 任务ID |

**请求参数 (Body)**

```json
{
  "reason": "内容违规，涉及学术不端"
}
```

**成功响应**

```json
{
  "code": 200,
  "message": "已拒绝，冻结资金已退还",
  "data": {
    "refundedAmount": 210.00
  },
  "timestamp": 1700000000000
}
```

---

### 6.5 获取争议列表

| 项目 | 说明 |
|------|------|
| 接口名称 | 获取争议列表 |
| 请求路径 | `/api/v1/admin/disputes` |
| 请求方法 | GET |
| 是否需要Token | 是 |
| 权限说明 | 管理员 |

**请求参数 (Query)**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |
| status | String | 否 | 状态：pending、resolved |

**成功响应**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "disputeId": 5001,
        "taskId": 10001,
        "taskTitle": "帮我取外卖",
        "reward": 8.00,
        "requester": {
          "userId": 10001,
          "username": "小明"
        },
        "helper": {
          "userId": 10002,
          "username": "张三"
        },
        "reason": "接单方未按时完成",
        "evidence": [
          "https://example.com/evidence1.jpg"
        ],
        "status": "pending",
        "createdAt": "2024-01-15T10:00:00Z"
      }
    ],
    "total": 2,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1
  },
  "timestamp": 1700000000000
}
```

---

### 6.6 处理争议

| 项目 | 说明 |
|------|------|
| 接口名称 | 处理争议 |
| 请求路径 | `/api/v1/admin/disputes/{disputeId}/resolve` |
| 请求方法 | POST |
| 是否需要Token | 是 |
| 权限说明 | 管理员 |

**请求参数 (Path)**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| disputeId | Long | 是 | 争议ID |

**请求参数 (Body)**

```json
{
  "decision": "requester",
  "reason": "接单方确实未按时完成，支持需求方",
  "penalty": {
    "userId": 10002,
    "creditScoreChange": -10
  }
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| decision | String | 是 | 判决：requester(判给需求方)、helper(判给接单方)、split(平分) |
| reason | String | 是 | 判决理由 |
| penalty | Object | 否 | 处罚信息 |

**成功响应**

```json
{
  "code": 200,
  "message": "争议已处理",
  "data": {
    "disputeId": 5001,
    "decision": "requester",
    "settledAmount": 8.00,
    "toUserId": 10001,
    "penaltyApplied": true
  },
  "timestamp": 1700000000000
}
```

---

### 6.7 获取用户列表

| 项目 | 说明 |
|------|------|
| 接口名称 | 获取用户列表 |
| 请求路径 | `/api/v1/admin/users` |
| 请求方法 | GET |
| 是否需要Token | 是 |
| 权限说明 | 管理员 |

**请求参数 (Query)**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |
| keyword | String | 否 | 搜索关键词 |
| role | String | 否 | 角色筛选 |
| status | String | 否 | 状态：active、disabled |

**成功响应**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "userId": 10001,
        "username": "小明",
        "phone": "13800138000",
        "email": "xiaoming@example.com",
        "role": "requester",
        "creditScore": 95,
        "balance": 50.00,
        "completedTasks": 12,
        "status": "active",
        "createdAt": "2024-01-01T10:00:00Z"
      }
    ],
    "total": 1256,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 126
  },
  "timestamp": 1700000000000
}
```

---

### 6.8 禁用/启用用户

| 项目 | 说明 |
|------|------|
| 接口名称 | 禁用/启用用户 |
| 请求路径 | `/api/v1/admin/users/{userId}/status` |
| 请求方法 | PUT |
| 是否需要Token | 是 |
| 权限说明 | 管理员 |

**请求参数 (Path)**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**请求参数 (Body)**

```json
{
  "status": "disabled",
  "reason": "多次违规操作"
}
```

**成功响应**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 10001,
    "status": "disabled"
  },
  "timestamp": 1700000000000
}
```

---

## 七、文件上传模块

### 7.1 上传图片

| 项目 | 说明 |
|------|------|
| 接口名称 | 上传图片 |
| 请求路径 | `/api/v1/files/upload` |
| 请求方法 | POST |
| Content-Type | multipart/form-data |
| 是否需要Token | 是 |
| 权限说明 | 已登录用户 |

**请求参数 (Form)**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | 是 | 图片文件，支持jpg/png/gif，最大5MB |
| type | String | 否 | 用途：avatar、task、proof |

**成功响应**

```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "fileId": "f_202401150001",
    "url": "https://cdn.example.com/images/2024/01/15/xxx.jpg",
    "filename": "xxx.jpg",
    "size": 102400,
    "mimeType": "image/jpeg"
  },
  "timestamp": 1700000000000
}
```

---

## 八、权限控制说明

### 8.1 RBAC 角色权限矩阵

| 功能模块 | 需求方 | 接单方 | 管理员 |
|----------|--------|--------|--------|
| 发布任务 | ✓ | ✗ | ✓ |
| 接单/抢单 | ✗ | ✓ | ✗ |
| 验收任务 | ✓ | ✗ | ✗ |
| 提交完成 | ✗ | ✓ | ✗ |
| 发起争议 | ✓ | ✓ | ✗ |
| 处理争议 | ✗ | ✗ | ✓ |
| 审核任务 | ✗ | ✗ | ✓ |
| 用户管理 | ✗ | ✗ | ✓ |
| 数据统计 | ✗ | ✗ | ✓ |

### 8.2 权限拦截规则

```
1. Token 验证
   - 检查 Authorization header
   - 验证 JWT 签名和有效期
   - 解析用户角色信息

2. 角色验证
   - 根据接口定义的角色要求
   - 检查用户角色是否匹配
   - 不匹配返回 403

3. 资源归属验证
   - 检查用户是否为资源所有者
   - 任务操作需验证发布者/接单者身份
   - 不匹配返回 403
```

---

## 九、接口版本控制

### 9.1 版本策略

- URL 路径版本：`/api/v1/`、`/api/v2/`
- 向后兼容原则
- 废弃接口提前通知，保留3个月过渡期

### 9.2 版本响应头

```
X-API-Version: 1.0.0
X-API-Deprecated: false
X-API-Sunset: 2024-12-31 (仅废弃接口)
```

---

## 十、接口限流策略

### 10.1 限流规则

| 接口类型 | 限制 | 时间窗口 |
|----------|------|----------|
| 登录 | 5次 | 1分钟 |
| 发送验证码 | 1次 | 1分钟 |
| 发布任务 | 10次 | 1小时 |
| 接单/抢单 | 20次 | 1分钟 |
| 其他接口 | 100次 | 1分钟 |

### 10.2 限流响应

```json
{
  "code": 429,
  "message": "请求过于频繁，请稍后再试",
  "data": {
    "retryAfter": 60
  },
  "timestamp": 1700000000000
}
```

---

## 十一、错误码汇总

| 错误码 | HTTP状态码 | 说明 |
|--------|------------|------|
| 200 | 200 | 成功 |
| 400 | 400 | 请求参数错误 |
| 401 | 401 | 未登录或Token失效 |
| 403 | 403 | 无权限访问 |
| 404 | 404 | 资源不存在 |
| 409 | 409 | 资源冲突 |
| 422 | 422 | 业务逻辑错误 |
| 429 | 429 | 请求过于频繁 |
| 500 | 500 | 服务器内部错误 |
| 1001 | 400 | 用户名已存在 |
| 1002 | 400 | 手机号已注册 |
| 1003 | 401 | 密码错误 |
| 1004 | 403 | 账号已被禁用 |
| 1005 | 400 | 验证码错误 |
| 2001 | 404 | 任务不存在 |
| 2002 | 422 | 任务状态不允许此操作 |
| 2003 | 409 | 任务已被接单 |
| 2004 | 403 | 非任务发布者无权操作 |
| 2005 | 403 | 非任务接单者无权操作 |
| 2006 | 422 | 余额不足 |
| 2007 | 422 | 任务已过期 |
| 3001 | 422 | 账户余额不足 |
| 3002 | 422 | 资金冻结中 |
| 3003 | 422 | 提现金额超限 |
