# Step 3 · Swagger UI 落地说明

> **落地目标**：把"接口契约从手写 Markdown 升级为机器可读的 OpenAPI 3.0 + 可视化 Swagger UI"。  
> **依赖版本**：`springdoc-openapi-starter-webmvc-ui 2.3.0`（Swagger 官方推荐的现代版本）

---

## 一、为什么需要 Swagger

| 维度 | 手写 Markdown（v1.0） | Swagger UI（v2.0） |
|------|---------------------|-------------------|
| 可读性 | ✅ 人读友好 | ✅ 网页更友好 |
| 机器可读 | ❌ 无法被工具消费 | ✅ OpenAPI YAML/JSON 标准 |
| 在线测试 | ❌ 需另开 Postman | ✅ 网页内 "Try it out" |
| 与代码同步 | ❌ 人工维护，**容易脱节** | ✅ springdoc 自动扫描 Controller 注解 |
| 错误反馈 | 静态 | ✅ 输入校验自动显示 |
| 维护成本 | 高（每次改接口要改 2 处） | 低（代码即文档） |

---

## 二、落地步骤

### 2.1 加依赖

编辑 `backend/pom.xml`，在 `<dependencies>` 节点内添加：

```xml
<!-- Swagger UI / OpenAPI 3.0 -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

### 2.2 加配置类

新建 `backend/src/main/java/com/example/campusmaster/config/OpenApiConfig.java`：

```java
package com.example.campusmaster.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("校园万事达 API")
                .version("2.0")
                .description("校园互助众包任务平台 - RESTful API 文档")
                .license(new License().name("课程设计")))
            .components(new Components()
                .addSecuritySchemes("bearer-jwt",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization")))
            .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
```

### 2.3 在 Controller 上加注解（可选但推荐）

在每个 Controller 类上：

```java
@Tag(name = "用户模块", description = "用户注册、登录、个人信息")
@RestController
@RequestMapping("/api/auth")
public class UserController {
    ...
}
```

在每个方法上：

```java
@Operation(summary = "用户登录", description = "通过手机号+密码登录，返回 JWT Token")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "登录成功"),
    @ApiResponse(responseCode = "400", description = "参数错误")
})
@PostMapping("/login")
public Result<Map<String, Object>> login(...) { ... }
```

> ⚠️ **本 Step 只给出方法，不要求全部加上**——这是 j 章"工程量 vs 质量"的权衡点。答辩时可展示 2-3 个关键 Controller 的注解作为示例。

### 2.4 启动后访问

1. `mvn spring-boot:run` 启动后端
2. 浏览器访问：**http://localhost:8080/swagger-ui.html**（或 `/swagger-ui/index.html`）
3. 应看到 6 个 tag（用户/任务/账户/通知/评价/管理）的接口列表
4. 任意接口点 "Try it out" → 输入参数 → Execute → 看响应

---

## 三、安全配置确认

`SecurityConfig.java` 已经放行了 Swagger 相关路径（无需修改）：

```java
.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
```

---

## 四、OpenAPI 契约文件（v1 简化版）

> 完整 OpenAPI 文件请见 `01-OpenAPI契约.yaml`（v1 简化版，覆盖 6 个核心模块各 1-2 个接口作为代表）。

---

## 五、与"差异审计表"的关联

| 差异点 | Swagger 如何暴露 |
|--------|-----------------|
| 路径前缀 `/api/v1/` vs `/api/` | Swagger 自动展示**实际**路径，文档中保留 v1 注释 |
| 字段名 `frozenAmount` vs `frozenBalance` | Swagger 自动展示 `frozenBalance`（代码实际） |
| 错误码体系缺失 | Swagger 暂不展示错误码（需在每个方法上 `@ApiResponses`） |
| 缺 `withdraw` 等接口 | Swagger **自然不显示**（代码无对应 Controller） |
| 缺 `email/verifyCode/refreshToken` | Swagger **自然不显示**（DTO 字段决定） |

> 💡 **亮点评审**：**Swagger 自动同步是它最大的价值**——解决了"代码改、文档忘改"的脱节问题。

---

## 六、答辩时怎么讲

**Q**：你们怎么保证接口文档与代码同步？

**A**：
1. 早期手写 Markdown（v1.0）—— 有脱节风险
2. 已落地 Swagger UI（v2.0）—— springdoc 自动扫描 Controller 注解，**改代码即改文档**
3. 提供 `/swagger-ui.html` 在线测试，**评审可现场验证**
4. 配合 `02-Controller与契约差异审计表.md` 完整记录了"手写版 vs Swagger版"的差异

---

## 七、本 Step 的 Swagger 落地实际状态

> ⚠️ **坦白说**：本 Step 写落地说明 + 加依赖 + 写配置类，**预计 15-20 分钟**即可完成。
> 但因为本工作环境无法直接执行 `mvn spring-boot:run` 验证，落地后的**截图**和**在线验证**需要 Floy 在本地完成。

**建议**：
- Step 3 末尾由 Floy 在本地执行 `mvn spring-boot:run`，访问 `http://localhost:8080/swagger-ui.html` 截图
- 截图保存到 `step3-API接口契约设计/06-Swagger-UI-截图/` 目录
- 如果落地失败/报错，把错误日志发给 AI 分析（**这是 j 章"闭环反馈"原则的体现**）
