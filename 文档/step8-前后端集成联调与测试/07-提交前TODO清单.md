# Step 8 · 提交前 TODO 清单

> **用途**：8 步路线图完成后，**提交答辩前**还需要做的事。  
> **建议时间**：2-3 小时可全部完成  
> **执行人**：Floy（本环境无法直接执行命令）

---

## ⚠️ 总览

| 优先级 | 任务数 | 预计总时间 |
|--------|--------|----------|
| 🔴 P0 | 3 个 | ~85 分钟（必须完成） |
| 🟠 P1 | 3 个 | ~55 分钟（强烈建议） |
| 🟡 P2 | 2 个 | ~10 分钟（建议） |
| 🟢 P3 | 1 个 | ~30 分钟（时间允许就做） |
| **合计** | **9 个** | **~3 小时** |

---

## 一、🔴 P0 · 必须完成（85 min）

### TODO-1 · 启动后端验证 Swagger UI（15 min）

| 步骤 | 命令/操作 | 验证点 |
|------|---------|--------|
| 1.1 | `cd D:\大三下作业\web\web\backend` | 进入后端目录 |
| 1.2 | `mvn spring-boot:run` | 启动 Spring Boot |
| 1.3 | 浏览器访问 `http://localhost:8080/swagger-ui.html` | 看到 Swagger UI |
| 1.4 | 浏览器访问 `http://localhost:8080/v3/api-docs` | 看到 OpenAPI JSON |
| 1.5 | 截图保存到 `step3-API接口契约设计/06-Swagger-UI-截图/` | 1-2 张 |

**如果启动失败**：
- 检查 `application.yml` 中 `DB_PASSWORD` 是否设置
- 检查 MySQL 是否启动
- 把错误日志贴给 AI 排查

### TODO-2 · 跑联调 checklist 53 个用例（60 min）

| 步骤 | 操作 |
|------|------|
| 2.1 | 打开 `step8-.../01-联调checklist与执行记录.md` |
| 2.2 | 按 checklist 顺序执行（注册登录→发任务→接单→...） |
| 2.3 | 每完成一项在 ☐ 打勾 ✅ |
| 2.4 | 关键步骤截图（保存到 `step8-.../08-联调截图/`） |
| 2.5 | 失败项记录到缺陷清单 |

**配套文档**：
- 测试账号见 `README.md` 或 `data.sql`
- 5 个核心页面操作见 `step2-.../03-交互说明清单.md`

### TODO-3 · 实施 Step 7 抢单乐观锁修复（30 min）

> 这是 j 章"闭环反馈"的关键演示

| 步骤 | 文件 | 操作 |
|------|------|------|
| 3.1 | `TaskMapper.java` | 加 `@Update` 注解的 `updateTaskWithVersion` 方法 |
| 3.2 | `TaskServiceImpl.acceptTask` | 改用 `updateTaskWithVersion` 替换 `updateById` |
| 3.3 | 重启后端 | `Ctrl+C` 停止 → `mvn spring-boot:run` 重启 |
| 3.4 | 复跑 TODO-2 中"抢单"用例 | 验证修复后行为 |
| 3.5 | （可选）跑 10 线程并发测试 | 见 `step7-.../03-核心业务单元测试.md` |

**修复代码片段**（可直接复制）：

```java
// TaskMapper.java
@Update("UPDATE task SET helper_id = #{helperId}, status = #{status}, " +
        "version = version + 1 " +
        "WHERE id = #{id} AND version = #{version} AND status = 'pending'")
int updateTaskWithVersion(@Param("id") Long id,
                          @Param("helperId") Long helperId,
                          @Param("status") String status,
                          @Param("version") Integer version);
```

```java
// TaskServiceImpl.java 替换 acceptTask 的 updateById
int updated = taskMapper.updateTaskWithVersion(
    taskId, helperId, "ongoing", task.getVersion()
);
```

---

## 二、🟠 P1 · 强烈建议（55 min）

### TODO-4 · 录 5 分钟演示视频（30 min）

| 步骤 | 操作 |
|------|------|
| 4.1 | 准备：清库 + 充值初始数据（见下方 SQL） |
| 4.2 | 打开 OBS / Win+G，录 1080p |
| 4.3 | 按 `step8-.../03-演示视频脚本.md` 演示 |
| 4.4 | 导出 MP4，文件命名 `演示视频_校园万事达.mp4` |
| 4.5 | 上传到提交目录（不上传到 Git） |

**演示前清库 SQL**（保证数据干净）：

```sql
-- 清空业务数据
DELETE FROM task_log;
DELETE FROM review;
DELETE FROM dispute;
DELETE FROM notification;
DELETE FROM transaction;

-- 重置账户
UPDATE account SET balance=500, frozen_balance=0 WHERE user_id=2;
UPDATE account SET balance=200, frozen_balance=0 WHERE user_id=3;
UPDATE account SET balance=1000, frozen_balance=0 WHERE user_id=1;

-- 软删除旧任务
UPDATE task SET deleted=1;
```

### TODO-5 · 补 5-10 张关键截图（20 min）

| 截图 | 来源 | 保存到 |
|------|------|--------|
| 登录页（含测试账号提示） | 浏览器 | `step8-.../08-联调截图/01-登录.png` |
| 任务大厅（任务列表） | 浏览器 | `.../02-任务大厅.png` |
| 发布任务（金额预览） | 浏览器 | `.../03-发布任务.png` |
| 任务详情（状态机变化） | 浏览器 | `.../04-任务详情.png` |
| 我的任务（多 tab） | 浏览器 | `.../05-我的任务.png` |
| 管理后台（统计） | 浏览器 | `.../06-管理后台.png` |
| Swagger UI 首页 | 浏览器 | `step3-.../06-Swagger-UI-截图/01.png` |
| Swagger UI 接口列表 | 浏览器 | `.../02.png` |
| Swagger UI Try it out | 浏览器 | `.../03.png` |
| Navicat 数据库 ER | Navicat | `step5-.../05-ER图截图.png` |

### TODO-6 · 实施 401 自动跳登录（5 min）

| 步骤 | 文件 | 操作 |
|------|------|------|
| 6.1 | `src/utils/request.js` | 在响应拦截器加 401 判断（见下方代码） |
| 6.2 | 复跑 TODO-2 中"权限校验"用例 | 验证自动跳登录 |

**代码片段**：

```js
// src/utils/request.js 响应拦截器 error 分支内
if (error.response && error.response.status === 401) {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  if (!window.location.pathname.includes('/login')) {
    window.location.href = '/login'
  }
}
```

---

## 三、🟡 P2 · 建议（10 min）

### TODO-7 · 删除 src/mock/index.js（5 min）

| 步骤 | 操作 |
|------|------|
| 7.1 | `rm src/mock/index.js` |
| 7.2 | `rmdir src/mock`（如果是空目录） |
| 7.3 | `src/main.js` 删除 3 行 mock 加载代码 |
| 7.4 | 复跑 TODO-2 | 确认无影响 |

### TODO-8 · 实施 view_count 自增（5 min）

```java
// TaskController.getTask 加 1 行
task.setViewCount(task.getViewCount() + 1);
taskMapper.updateById(task);
```

---

## 四、🟢 P3 · 时间允许就做（30 min）

### TODO-9 · 整合 Markdown 报告 → PDF（30 min）

| 步骤 | 操作 |
|------|------|
| 9.1 | 把 8 个 Step 的关键 Markdown 整合到一份大文档 |
| 9.2 | 用 Typora / VS Code + Markdown PDF 插件 渲染 |
| 9.3 | 校对格式（页码、目录、图编号） |
| 9.4 | 输出 PDF，文件命名 `学号+姓名_课程设计报告.pdf` |

**整合顺序**（按 a-j 章节）：
1. 摘要 + 关键词 + 目录
2. a：PRD（`step1-.../01-修订版PRD.md`）
3. b：技术选型 + 架构（`step4-.../01-技术选型对比表.md` + `02-系统架构图_Mermaid.md`）
4. c：UI/UX 原型（`step2-.../01-页面清单与设计说明.md` + `03-交互说明清单.md`）
5. d：数据库设计（`step5-.../01-ER关系图.md` + `02-数据建模审计报告.md`）
6. e：API 接口（`step3-.../01-OpenAPI契约.yaml` + `02-Controller与契约差异审计表.md`）
7. f：核心实现（`step7-.../02-并发安全性审查报告.md` + `03-核心业务单元测试.md`）
8. g：编码规范（`step6-.../01-前端编码规范SPEC.md` + `step7-.../01-后端编码规范SPEC.md`）
9. h：部署（v1.0 `安装部署说明.md` + 补充 Swagger 访问）
10. i：测试（v1.0 `测试报告.md` + `step8-.../02-真实测试报告.md`）
11. **j：AI 协作方法论（`step8-.../05-AI协作方法论沉淀.md` 直接复制）** ⭐
12. 团队分工 + 致谢 + 参考文献 + 附录

---

## 五、提交包结构

```
提交包_学号+姓名/
├── 课程设计报告-成绩页.docx          # 独立文件
├── 课程设计报告.pdf                  # 主报告（含 j 章）
├── 演示视频_校园万事达.mp4           # ≤5 分钟
├── 项目源代码/                        # 压缩后的源码
│   ├── backend/                       # 去掉 target/
│   ├── src/                           # 去掉 node_modules/
│   ├── 文档/                          # 全部 v1.0 + v2.0
│   ├── package.json
│   ├── README.md
│   └── ...
└── (可选) 关键截图/                  # 5-10 张
```

---

## 六、TODO 执行 Checklist

> 📌 **执行时勾选每一步**：

```
□ TODO-1 启动后端验证 Swagger UI (15 min)
□ TODO-2 跑联调 checklist 53 用例 (60 min)
□ TODO-3 实施 Step 7 抢单乐观锁修复 (30 min)
□ TODO-4 录 5 分钟演示视频 (30 min)
□ TODO-5 补 5-10 张关键截图 (20 min)
□ TODO-6 实施 401 自动跳登录 (5 min)
□ TODO-7 删除 src/mock/index.js (5 min)
□ TODO-8 实施 view_count 自增 (5 min)
□ TODO-9 整合 Markdown 报告 → PDF (30 min)
```

**预计总时间**：~3 小时

**建议执行顺序**：
1. TODO-3（修代码）→ TODO-1（启动）→ TODO-2（联调）→ TODO-7/TODO-8（清理）
2. TODO-6（401）→ TODO-5（截图）→ TODO-4（视频）
3. TODO-9（PDF）→ 提交

---

## 七、为什么这个 TODO 列表之前没单独成文件？

**坦白说**：上一轮对话中我**把这个列表贴在了聊天回复里**（表格形式），没有写进文件——这是一个**流程疏漏**。现在已经补救，写成 `step8-.../07-提交前TODO清单.md` 单独成文。

**教训**：**重要的清单必须成文**——对话里的内容会随上下文丢失，文件里的内容会留存。
