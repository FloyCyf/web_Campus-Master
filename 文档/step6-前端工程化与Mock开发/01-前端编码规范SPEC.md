# Step 6 · 前端编码规范 SPEC

> **Step 目标**：为前端项目制定一份**可执行、可检查**的编码规范。  
> **AI 协作方式**：让 AI 基于现有 `src/` 代码"逆向推导"出规范，人工核对并加严。  
> **SPEC（Specification）原则**：每条规范都有"✅ 正确示例"和"❌ 错误示例"。

---

## 一、技术栈与版本

| 类别 | 选型 | 版本 |
|------|------|------|
| 框架 | Vue 3（Composition API + `<script setup>`） | 3.4.21 |
| 构建 | Vite | 5.2.0 |
| 状态 | Pinia | 2.1.7 |
| 路由 | Vue Router | 4.3.0 |
| 样式 | Tailwind CSS（原子化） | 3.4.1 |
| HTTP | Axios | 1.6.x |
| 图标 | 内联 SVG（不引图标库） | - |

---

## 二、目录与文件命名

| 类型 | 规范 | 示例 |
|------|------|------|
| 页面 | `PascalCase.vue` | `TaskDetail.vue` |
| 组件 | `PascalCase.vue` | `BaseButton.vue` |
| 工具 | `camelCase.js` | `request.js` |
| Store | `camelCase.js` | `user.js` |
| 常量 | `UPPER_SNAKE_CASE.js` 或 `camelCase.js` | `API_ENDPOINTS.js` |

> ✅ `views/Publish.vue` / `stores/user.js` / `utils/request.js`  
> ❌ `views/publish.vue` / `stores/UserStore.js`

---

## 三、Vue 组件规范

### 3.1 使用 `<script setup>` 语法糖

```vue
<!-- ✅ 正确 -->
<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'

const count = ref(0)
const userStore = useUserStore()
</script>
```

```vue
<!-- ❌ 错误（不要用 Options API） -->
<script>
export default {
  data() { return { count: 0 } }
}
</script>
```

### 3.2 Props 定义必须用 TypeScript 风格注释

```vue
<!-- ✅ 正确（即使不用 TS，也要写 prop 验证） -->
<script setup>
const props = defineProps({
  status: {
    type: String,
    required: true,
    validator: (v) => ['pending', 'ongoing', 'completed'].includes(v)
  },
  size: {
    type: String,
    default: 'md'
  }
})
</script>
```

### 3.3 组件命名（多单词）

```vue
<!-- ✅ 正确 -->
<TaskCard :task="task" />
<BaseButton variant="primary" />

<!-- ❌ 错误（不要单单词组件名） -->
<Card :task="task" />
<Button variant="primary" />
```

### 3.4 自闭合 vs 闭合标签

```vue
<!-- ✅ 单根属性标签用自闭合 -->
<TaskCard :task="task" />

<!-- ✅ 有 slot 时用闭合标签 -->
<Modal title="提示">
  <p>这是内容</p>
</Modal>
```

---

## 四、API 调用规范

### 4.1 所有 API 调用必须在 `src/api/index.js` 中定义

```js
// ✅ 正确
// src/api/index.js
export const taskApi = {
  getList: () => request.get('/tasks').then(normalizePage),
  getDetail: (taskId) => request.get(`/tasks/${taskId}`),
  accept: (taskId) => request.post(`/tasks/${taskId}/accept`)
}
```

```vue
<!-- ❌ 错误（不要在组件内直接 axios 调用） -->
<script setup>
import axios from 'axios'
const loadTask = () => axios.get('/api/tasks/1')  // 反模式
</script>
```

### 4.2 统一使用 `request` 封装，不要直接用 axios

```js
// ✅ 正确
import request from '@/utils/request'
request.get('/tasks')

// ❌ 错误
import axios from 'axios'
axios.get('/api/tasks')
```

### 4.3 错误处理统一在 `request.js` 的响应拦截器

```vue
<!-- ✅ 正确：业务代码只关心成功 -->
<script setup>
const loadTask = async () => {
  try {
    const data = await taskApi.getDetail(id)
    task.value = data
  } catch (error) {
    // error.message 已经是后端返回的友好提示
    alert(error.message)
  }
}
</script>
```

### 4.4 Loading 状态用 `ref(false)` + `try/finally`

```js
// ✅ 正确
const loading = ref(false)
const loadData = async () => {
  loading.value = true
  try {
    const data = await api.xxx()
    list.value = data
  } finally {
    loading.value = false  // 无论成功失败都关闭
  }
}
```

---

## 五、状态管理规范（Pinia）

### 5.1 用 Setup Store 风格（`defineStore('xxx', () => {...})`）

```js
// ✅ 正确
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref('')
  const isAuthenticated = computed(() => !!token.value)
  return { token, isAuthenticated }
})
```

```js
// ❌ 错误（不要用 Options Store 风格）
export const useUserStore = defineStore('user', {
  state: () => ({ token: '' }),
  getters: { isAuthenticated: (state) => !!state.token }
})
```

### 5.2 持久化用 `localStorage`，不要在 Store 内直接读

```js
// ✅ 正确
const login = (data) => {
  token.value = data.token
  localStorage.setItem('token', data.token)  // 显式持久化
}
const loadFromStorage = () => {
  const t = localStorage.getItem('token')
  if (t) token.value = t
}

// 在 main.js 中调用
const userStore = useUserStore()
userStore.loadFromStorage()
```

### 5.3 Store 之间互引用必须显式 import

```js
// ✅ 正确
import { useUserStore } from './user'  // 显式 import
const userStore = useUserStore()
```

---

## 六、路由与权限规范

### 6.1 路由 meta 必须包含 `requiresAuth` + 可选 `roles`

```js
// ✅ 正确
const routes = [
  { path: '/home', name: 'Home', component: () => import('@/views/Home.vue'),
    meta: { requiresAuth: true } },
  { path: '/admin', name: 'Admin', component: () => import('@/views/Admin.vue'),
    meta: { requiresAuth: true, roles: ['admin'] } }
]
```

### 6.2 路由守卫统一在 `router/index.js`

```js
// ✅ 正确（不要在组件内做权限判断）
router.beforeEach((to, from, next) => {
  const user = JSON.parse(localStorage.getItem('userInfo') || 'null')
  if (to.meta.requiresAuth && !user) return next('/login')
  if (to.meta.roles && !to.meta.roles.includes(user.role)) return next('/home')
  next()
})
```

---

## 七、样式规范（Tailwind）

### 7.1 使用预定义的语义化类名（CSS 变量）

```html
<!-- ✅ 正确：用 CSS 变量表达"主题色" -->
<button class="bg-[var(--role-accent)] text-white">发布</button>

<!-- ❌ 错误：硬编码具体色值 -->
<button class="bg-purple-500 text-white">发布</button>
```

### 7.2 避免深层 `<div>` 嵌套

```vue
<!-- ✅ 正确：扁平结构 -->
<template>
  <div class="card">
    <h3 class="title">{{ task.title }}</h3>
    <p class="desc">{{ task.description }}</p>
  </div>
</template>

<!-- ❌ 错误：嵌套地狱 -->
<template>
  <div class="outer">
    <div class="middle">
      <div class="inner">
        <div class="deepest">
          <span>{{ task.title }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
```

### 7.3 响应式断点统一

| 断点 | 用途 |
|------|------|
| `sm:` (640px) | 手机横屏 |
| `md:` (768px) | 平板 |
| `lg:` (1024px) | 笔记本 |
| `xl:` (1280px) | 桌面 |

---

## 八、注释规范

### 8.1 函数/方法必须有 JSDoc 风格注释

```js
// ✅ 正确
/**
 * 标准化分页响应，兼容后端 records/list 两种字段
 * @param {Object} data - 后端返回的 IPage 对象
 * @returns {Object} - { list, total, current, ... }
 */
const normalizePage = (data) => ({ ... })
```

### 8.2 复杂业务逻辑必须加"为什么"注释

```js
// ✅ 正确
// 服务费不在发布时预扣，验收通过时从实得中扣除
const actualAmount = amount.subtract(amount.multiply(0.05))

// ❌ 错误
// 计算实际金额
const actualAmount = amount * 0.95
```

### 8.3 TODO 标记

```js
// ✅ 正确
// TODO: Step 7 修复乐观锁 SQL
int updated = taskMapper.updateById(task)
```

---

## 九、Git 提交规范

### 9.1 Commit 消息格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

| type | 含义 |
|------|------|
| feat | 新功能 |
| fix | 修复 bug |
| docs | 仅文档变更 |
| style | 样式调整（不影响逻辑） |
| refactor | 重构（非新功能、非 bug 修复） |
| test | 测试相关 |
| chore | 构建/工具链变更 |

### 9.2 示例

```
feat(task): 添加任务评价功能

- 评价弹窗
- 1-5 星评分
- 评价后更新对方信用分

🤖 AI 协作声明：基础结构由 Claude 生成，Floy 审查并补 2 处业务校验
```

> 💡 **亮点评审**：**Commit 消息里写"AI 协作声明"是 j 章的特色**——把 AI 使用情况落到代码层面。

---

## 十、ENV 环境变量

### 10.1 必须用 `import.meta.env.VITE_*` 访问

```js
// ✅ 正确
if (import.meta.env.VITE_USE_MOCK === 'true') {
  await import('./mock')
}

// ❌ 错误
if (process.env.VITE_USE_MOCK) { ... }  // Vite 不支持 process.env
```

### 10.2 必须在 `.env` 中定义

```
# .env
VITE_USE_MOCK=false
VITE_API_BASE_URL=/api
```

---

## 十一、规范自检 Checklist

> 📌 **每次 PR 前对照这张表自查**：

- [ ] 组件名是 PascalCase 多单词
- [ ] `<script setup>` 语法糖
- [ ] API 调用全部走 `@/api/index.js`
- [ ] 错误用响应拦截器统一处理
- [ ] Loading 用 `try/finally`
- [ ] Pinia 用 Setup Store 风格
- [ ] 路由守卫不写在组件内
- [ ] 样式用 CSS 变量而非硬编码
- [ ] 复杂业务加"为什么"注释
- [ ] Commit 消息格式规范
- [ ] ENV 用 `import.meta.env`

---

## 十二、规范的"已违反"清单

> 📌 **现有代码中违反上述规范的地方**（用于 Step 6 后续修复或解释）：

| 文件 | 违反项 | 处理 |
|------|--------|------|
| `src/mock/index.js` | 路径与真接口不一致 | 见 `02-Mock与真接口差异审计表.md` |
| `src/main.js` | 顶部用 `await import` 引入 mock（条件加载 OK） | ✅ 符合 |
| `src/utils/request.js` | Token 同时放 `token` 和 `Authorization` 双头 | ⚠️ 见 §十三 解释 |
| 部分 views | 用 `alert()` 错误提示 | ⚠️ 见 §十三 解释 |

---

## 十三、对已存在不规范代码的辩护

> 📌 **不是所有"违反规范"的代码都该改**——记录决策原因：

### 13.1 Token 放在 `token` 和 `Authorization` 双头

- **现状**：`request.js` 同时设 `config.headers['token'] = token` 和 `Authorization: Bearer token`
- **原因**：后端 `AuthInterceptor` 优先读 `token` 头，兼容旧的 `Authorization` 头
- **决策**：保持现状，**v2 收敛到只用一个**（推荐用 `Authorization: Bearer`）

### 13.2 用 `alert()` 而非 toast/notification 组件

- **现状**：所有错误用 `alert(error.message)` 弹窗
- **原因**：避免引入额外的 UI 组件库（Element Plus/Ant Design）
- **决策**：接受现状，**v2 可加 toast 组件**

### 13.3 Mock 与真接口路径不一致

- **现状**：`mock/index.js` 用 `/api/auth/login`，真接口也用 `/api/auth/login`（一致）；但 `mock` 用 `/api/task/list` 而真接口用 `/api/tasks`
- **原因**：mock 早期按旧命名写
- **决策**：**彻底不用 mock**（main.js 中默认 false），保留作为"参考实现"

---

## 十四、SPEC 的可执行性

> 📌 **这份 SPEC 不是"参考"而是"约束"**——可通过以下方式强制：

| 约束方式 | 实施难度 | 建议 |
|---------|---------|------|
| ESLint + Vue 规则 | 中 | 引入 `eslint-plugin-vue`，配置 `vue/multi-word-component-names` |
| Prettier | 低 | 已隐式使用（IDE 自动格式化） |
| Code Review | 低 | PR 时人工检查 |
| Husky + lint-staged | 中 | pre-commit 自动跑 lint |

**本期不强制 ESLint**——团队 2-3 人 + 6-8 小时，时间成本不划算。**v2 可加**。
