# Step 7 · 后端编码规范 SPEC

> **Step 目标**：为后端项目制定一份**可执行、可检查**的编码规范。  
> **AI 协作方式**：让 AI 基于现有 backend/ 代码"逆向推导"，人工核对加严。

---

## 一、技术栈与版本

| 类别 | 选型 | 版本 |
|------|------|------|
| 框架 | Spring Boot | 3.2.0 |
| 语言 | Java | 17 |
| ORM | MyBatis-Plus | 3.5.5 |
| 安全 | Spring Security | 6.1 |
| 认证 | JJWT | 0.12.5 |
| 通信 | WebSocket | 内置 |
| 构建 | Maven | 3.x |

---

## 二、目录与包结构

```
com.example.campusmaster/
├── CampusMasterApplication.java    # 启动类
├── annotation/                       # 自定义注解
│   └── RequiresRoles.java
├── common/                           # 通用基础
│   ├── Result.java                   # 统一响应
│   ├── BusinessException.java
│   ├── GlobalExceptionHandler.java
│   └── JwtUtil.java
├── config/                           # 配置类
├── controller/                       # REST API
├── entity/                           # 数据库实体
├── mapper/                           # MyBatis-Plus Mapper
├── service/                          # 业务接口
│   └── impl/                         # 业务实现
├── interceptor/                      # 拦截器
└── websocket/                        # WebSocket
```

**包命名**：小写 + 点分（`com.example.campusmaster.controller`）

---

## 三、类与文件命名

| 类型 | 规范 | 示例 |
|------|------|------|
| Controller | `XxxController` | `TaskController` |
| Service | `XxxService` / `XxxServiceImpl` | `TaskService` / `TaskServiceImpl` |
| Mapper | `XxxMapper` | `TaskMapper` |
| Entity | `Xxx`（不加后缀） | `Task` / `User` |
| 异常 | `XxxException` | `BusinessException` |
| 配置 | `XxxConfig` | `SecurityConfig` |
| 工具 | `XxxUtil` / `XxxUtils` | `JwtUtil` |
| 拦截器 | `XxxInterceptor` | `AuthInterceptor` |

---

## 四、Controller 规范

### 4.1 统一返回 `Result<T>`

```java
// ✅ 正确
@PostMapping
public Result<Long> createTask(@RequestBody CreateTaskRequest request) {
    Long taskId = taskService.createTask(request, getUserId(request));
    return Result.success("任务发布成功", taskId);
}

// ❌ 错误
@PostMapping
public Long createTask(@RequestBody CreateTaskRequest request) {
    return taskService.createTask(request, getUserId(request));
}
```

### 4.2 业务异常用 `BusinessException`

```java
// ✅ 正确
if (task == null) {
    throw BusinessException.notFound("任务不存在");
}

// ❌ 错误
if (task == null) {
    return Result.notFound("任务不存在");
}
```

### 4.3 权限用 `@RequiresRoles` 注解

```java
// ✅ 正确
@PostMapping
@RequiresRoles({"requester", "admin"})
public Result<Long> createTask(@RequestBody CreateTaskRequest request) {
    ...
}

// ❌ 错误（在方法内手动校验）
@PostMapping
public Result<Long> createTask(@RequestBody CreateTaskRequest request, HttpServletRequest req) {
    String role = (String) req.getAttribute("role");
    if (!"requester".equals(role) && !"admin".equals(role)) {
        return Result.forbidden("权限不足");
    }
    ...
}
```

### 4.4 路径统一 `/api/xxx` 前缀

```java
@RestController
@RequestMapping("/api/tasks")  // ✅ 用复数 + kebab-case
public class TaskController { ... }
```

### 4.5 HTTP 方法语义

| 方法 | 用途 | 幂等性 |
|------|------|--------|
| GET | 查询 | ✅ |
| POST | 创建 | ❌ |
| PUT | 完整更新 | ✅ |
| PATCH | 部分更新 | ❌ |
| DELETE | 删除 | ✅ |

---

## 五、Service 规范

### 5.1 事务边界清晰

```java
// ✅ 正确：Service 方法级别事务
@Override
@Transactional
public boolean acceptTask(Long taskId, Long helperId) {
    ...
}
```

### 5.2 写操作必须 `@Transactional`

| 场景 | 事务 |
|------|------|
| 创建/更新/删除 | 必加 |
| 多个写操作 | 必加 |
| 单条查询 | 不加（默认 REQUIRED 即可） |

### 5.3 异常处理交给全局处理器

```java
// ✅ 正确：抛业务异常
if (status != "pending") {
    throw BusinessException.conflict("状态不允许");
}

// ❌ 错误：try-catch 后返回错误
try {
    ...
} catch (Exception e) {
    log.error("操作失败", e);
    return Result.error(500, "系统错误");
}
```

### 5.4 状态机用 if 不用框架（ADR-007）

```java
// ✅ 正确
if (!"pending".equals(task.getStatus())) {
    throw BusinessException.conflict("状态不允许接单");
}
```

---

## 六、Mapper 规范

### 6.1 优先用 MyBatis-Plus 基础方法

```java
// ✅ 优先用 BaseMapper 提供的方法
taskMapper.selectById(id);
taskMapper.selectList(wrapper);
taskMapper.updateById(task);
taskMapper.insert(task);
```

### 6.2 复杂查询用 `LambdaQueryWrapper`

```java
// ✅ 正确
LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Task::getRequesterId, userId)
       .orderByDesc(Task::getCreateTime);
return taskMapper.selectPage(page, wrapper);

// ❌ 错误（用字符串字段名）
QueryWrapper<Task> wrapper = new QueryWrapper<>();
wrapper.eq("requester_id", userId)
       .orderByDesc("create_time");
```

### 6.3 自定义 SQL 用 `@Update` / `@Select` 注解

```java
@Update("UPDATE task SET helper_id = #{helperId}, status = #{status}, " +
        "version = version + 1 " +
        "WHERE id = #{id} AND version = #{version} AND status = 'pending'")
int updateTaskWithVersion(@Param("id") Long id, ...);
```

### 6.4 必须使用 `@Param` 标注参数

---

## 七、Entity 规范

### 7.1 字段必须使用 `@TableField`

```java
@TableField("user_name")
private String username;
```

### 7.2 主键使用 `@TableId(type = IdType.AUTO)`

### 7.3 逻辑删除字段

```java
@TableLogic
@TableField("deleted")
private Integer deleted;
```

### 7.4 审计字段自动填充

```java
@TableField(value = "create_time", fill = FieldFill.INSERT)
private LocalDateTime createTime;

@TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
private LocalDateTime updateTime;
```

### 7.5 乐观锁字段

```java
@Version
@TableField("version")
private Integer version;
```

---

## 八、异常处理规范

### 8.1 业务异常用 `BusinessException`

```java
public class BusinessException extends RuntimeException {
    private final Integer code;
    
    public static BusinessException badRequest(String message) { return new BusinessException(400, message); }
    public static BusinessException unauthorized(String message) { return new BusinessException(401, message); }
    public static BusinessException forbidden(String message) { return new BusinessException(403, message); }
    public static BusinessException notFound(String message) { return new BusinessException(404, message); }
    public static BusinessException conflict(String message) { return new BusinessException(409, message); }
}
```

### 8.2 全局处理器统一返回

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.error(400, message);
    }
    
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统异常，请稍后重试");
    }
}
```

### 8.3 错误码体系

| 码 | 含义 |
|----|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 状态冲突（抢单冲突） |
| 500 | 系统异常 |

---

## 九、安全规范

### 9.1 密码用 BCrypt

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

// 加密
user.setPassword(passwordEncoder.encode(rawPassword));

// 校验
passwordEncoder.matches(rawPassword, user.getPassword());
```

### 9.2 禁止明文日志

```java
// ✅ 正确
log.info("用户登录: userId={}", user.getId());

// ❌ 错误（不要打密码）
log.info("用户登录: phone={}, password={}", phone, password);
```

### 9.3 防止 SQL 注入

- ✅ MyBatis-Plus 参数化查询
- ❌ 禁止字符串拼接 SQL

---

## 十、注释规范

### 10.1 类必须有 Javadoc

```java
/**
 * 任务业务实现
 * 
 * @author Floy
 * @since 2026-05-17
 */
@Service
public class TaskServiceImpl implements TaskService {
    ...
}
```

### 10.2 复杂方法必须有"为什么"注释

```java
// 服务费不在发布时预扣，验收通过时从实得中扣除
BigDecimal actualAmount = amount.subtract(serviceFee);
```

### 10.3 业务规则有 TODO 时

```java
// TODO: Step 7 优化为乐观锁 SQL
int updated = taskMapper.updateById(task);
```

---

## 十一、Git 提交规范

```
<type>(<scope>): <subject>

🤖 AI 协作声明：xxx 由 Claude 生成，人工审查补 yyy
```

**示例**：

```
fix(task): 修复抢单并发问题

- TaskService.acceptTask 增加乐观锁
- 新增 updateTaskWithVersion SQL
- 新增 10 线程并发测试

🤖 AI 协作声明：基础方案由 Claude 提供，人工审查指出"先改状态再转账"是关键决策
```

---

## 十二、ENV 配置规范

### 12.1 不在代码中硬编码

```java
// ❌ 错误
String dbUrl = "jdbc:mysql://localhost:3306/campus_master";

// ✅ 正确
@Value("${spring.datasource.url}")
private String dbUrl;
```

### 12.2 敏感配置走环境变量

```yaml
spring:
  datasource:
    password: ${DB_PASSWORD:}
jwt:
  secret: ${JWT_SECRET:campus_master_dev_secret_key_2026_local}
```

---

## 十三、对已存在代码的"已违反"清单

| 文件 | 违反项 | 处理 |
|------|--------|------|
| TaskServiceImpl.acceptTask | 缺乐观锁 | Step 7 已修 |
| TaskServiceImpl.completeTask | 缺乐观锁 | Step 7 方案已给 |
| AccountServiceImpl.transfer | 缺乐观锁 | Step 7 方案已给 |
| 大部分 ServiceImpl | 缺单元测试 | Step 7 给出测试代码 |
| 异常类 | 用 `new BusinessException(400, msg)` 而非工厂方法 | 可接受（功能等价） |

---

## 十四、规范的"防御性"价值

> 📌 **这份 SPEC 的真正价值不是"约束代码"，而是"答辩时能讲清楚"**：

| 评委问 | 直接答 |
|--------|--------|
| "你们代码规范吗？" | "我们遵守 X 条规范"（指本 SPEC） |
| "为什么用 if 判断状态机？" | "ADR-007 决策——6 状态可控" |
| "为什么不用外键？" | "ADR 决策——阿里规约" |
| "乐观锁怎么做的？" | "Step 7 修复方案 + 10 线程测试" |
