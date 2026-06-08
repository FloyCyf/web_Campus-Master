# Step 7 · Swagger 集成补充 + 401 自动跳转


> **Step 目标**：补齐 Step 3 提到的 Swagger 落地细节 + 前端 401 自动跳登录。

---

## 一、Swagger 集成补充（与 Step 3 配合）

### 1.1 给关键 Controller 加 @Tag 注解

```java
// UserController.java
@Tag(name = "用户模块", description = "用户注册、登录、个人信息管理")
@RestController
@RequestMapping("/api/auth")
public class UserController { ... }
```

```java
// TaskController.java
@Tag(name = "任务模块", description = "任务全生命周期管理")
@RestController
@RequestMapping("/api/tasks")
public class TaskController { ... }
```

```java
// 关键方法加 @Operation
@Operation(summary = "接单", description = "接单方锁定任务，系统进入担保状态")
@PostMapping("/{id}/accept")
@RequiresRoles({"helper", "admin"})
public Result<Void> acceptTask(...) { ... }
```

### 1.2 全局错误码在 Swagger 展示

`OpenApiConfig.java` 添加：

```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("校园万事达 API")
            .version("2.0")
            .description("""
                # 业务错误码约定
                - 200: 成功
                - 400: 参数错误
                - 401: 未登录或 Token 失效
                - 403: 权限不足
                - 404: 资源不存在
                - 409: 状态冲突（抢单冲突等）
                - 500: 系统异常
                """)
            .license(new License().name("课程设计")))
        ...
}
```

### 1.3 答辩时的 Swagger 展示

> 启动后端 → 访问 `http://localhost:8080/swagger-ui.html` → 看到 6 个 tag → 任选一个 → "Try it out" → 现场演示

---

## 二、前端 401 自动跳登录（Step 6 决定的补漏）

### 2.1 修改 `request.js` 响应拦截器

```js
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    } else {
      const error = new Error(res.message || '请求失败')
      error.response = response
      return Promise.reject(error)
    }
  },
  error => {
    // ✅ 新增：401 自动跳登录
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      // 避免在登录页本身死循环跳
      if (!window.location.pathname.includes('/login')) {
        window.location.href = '/login'
      }
    }
    if (error.response && error.response.data) {
      error.message = error.response.data.message || '请求异常'
    }
    return Promise.reject(error)
  }
)
```

### 2.2 业务代码可以简化

修改后，业务代码不需要再单独处理 401：

```vue
<!-- 之前：每个 API 调用都要 catch 401 -->
<script setup>
const loadTask = async () => {
  try {
    const data = await taskApi.getDetail(id)
    task.value = data
  } catch (error) {
    if (error.response?.status === 401) {
      router.push('/login')  // 重复处理
    } else {
      alert(error.message)
    }
  }
}
</script>

<!-- 之后：拦截器自动处理 401 -->
<script setup>
const loadTask = async () => {
  try {
    const data = await taskApi.getDetail(id)
    task.value = data
  } catch (error) {
    alert(error.message)  // 401 已被拦截器处理
  }
}
</script>
```

### 2.3 副作用记录

| 改动 | 影响范围 | 风险 |
|------|---------|------|
| 401 自动清 localStorage | 全局 | 低（已登录用户 401 = Token 过期） |
| 自动跳 /login | 全局 | 中（避免在 /login 页面死循环——已加判断） |
| 业务代码简化 | 11 个 view | 极低 |

---

## 三、其他"低垂果实"补漏

> 趁 Step 7 顺带把几处"明显不完美"补上：

### 3.1 TaskController.getTask 增加 view_count 自增

```java
@GetMapping("/{id}")
public Result<Task> getTask(@PathVariable Long id) {
    Task task = taskService.getTaskById(id);
    // ✅ 新增：浏览次数 +1
    task.setViewCount(task.getViewCount() + 1);
    taskMapper.updateById(task);
    return Result.success(task);
}
```

> **说明**：这是 Step 5 数据建模审计报告 §3.4 标注的"业务缺口"。

### 3.2 任务分类枚举校验

```java
// 在 CreateTaskRequest 内部
private static final List<String> VALID_CATEGORIES = 
    List.of("delivery", "food", "print", "other");

// Service 校验
if (!VALID_CATEGORIES.contains(request.getCategory())) {
    throw BusinessException.badRequest("无效的任务分类");
}
```

### 3.3 金额上限校验

```java
// Service 校验
if (reward.compareTo(new BigDecimal("10000")) > 0) {
    throw BusinessException.badRequest("单笔任务奖励不超过 10000 元");
}
```

---

## 四、Step 7 整体收尾

### 4.1 本 Step 的"代码改动优先级"

| 优先级 | 改动 | 工作量 | 必要性 |
|--------|------|--------|--------|
| 🔴 P0 | acceptTask 乐观锁修复 | 30min | **必须**（最严重 bug） |
| 🟠 P1 | completeTask/transfer 乐观锁 | 50min | 强烈建议 |
| 🟠 P1 | 401 自动跳登录 | 5min | 强烈建议（用户体验） |
| 🟡 P2 | view_count 自增 | 5min | 建议（业务缺口） |
| 🟡 P2 | 分类/金额校验 | 10min | 建议（数据质量） |
| 🟢 P3 | 单元测试编写 | 2-3h | 时间允许就做 |

### 4.2 时间分配建议

| 操作 | 建议时间 |
|------|---------|
| 必做的 🔴 P0 + 🟠 P1 | 1-2 小时 |
| 建议的 🟡 P2 | 30 分钟 |
| 单元测试 | 时间允许就做（不是必交） |

> 💡 **评估**：**核心修复 + 401 跳转 = 1.5 小时工作量**。如果只剩 30 分钟，**只做 P0 抢单修复**就够。

### 4.3 答辩时的"修复故事"

**问**："你们的抢单安全吗？"

**A**：
1. **早期代码有 bug**（`updateById` 不带 version 条件）—— Step 1 PRD §6.6 审计时发现
2. **Step 7 给出修复方案**（Mapper 自定义 SQL + Service 改用乐观锁）
3. **Step 7 给出 10 线程并发测试用例**（JUnit 5 + CountDownLatch）
4. **修复前**：10 线程可能多个都成功；**修复后**：只有 1 个成功
5. **闭环反馈**：从 PRD 警示 → 代码修复 → 测试验证，**三步完整故事**

---

## 五、对 j 章的价值

> 📌 **Step 7 的所有产出都是 j 章"AI 协作"的核心素材**：
> 1. 闭环反馈案例（Step 1 → Step 7）
> 2. 并发测试用例（AI 写，人工补）
> 3. 401 自动跳登录（5 分钟改动，大幅提升 UX）
> 4. 后端 SPEC（含已违反清单）

**建议保存到** `AI协作记录沉淀/07-Step7-后端开发闭环反馈.md`——**j 章最重的素材**。
