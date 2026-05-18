# 校园万事达互助众包任务平台

## 项目启动步骤

### 1. 安装依赖

```bash
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

启动成功后，在浏览器中打开 http://localhost:5173

### 3. 构建生产版本

```bash
npm run build
```

### 4. 预览生产版本

```bash
npm run preview
```

---

## 技术栈

- Vue 3 + Vite
- Tailwind CSS
- Pinia (状态管理)
- Vue Router (路由)

---

## 项目结构

```
src/
├── components/     # 公共组件
│   ├── AppLayout.vue
│   ├── BaseButton.vue
│   ├── Navbar.vue
│   └── StatusTag.vue
├── views/          # 页面组件
│   ├── Home.vue        # 任务大厅
│   ├── Login.vue       # 登录
│   ├── Register.vue    # 注册
│   ├── Publish.vue     # 发布任务
│   ├── MyTasks.vue     # 我的任务
│   ├── TaskDetail.vue  # 任务详情
│   ├── SubmitTask.vue  # 提交完成
│   ├── ReviewTask.vue  # 验收任务
│   ├── RateTask.vue    # 评价任务
│   ├── Profile.vue     # 个人中心
│   └── Admin.vue       # 管理后台
├── stores/         # Pinia 状态管理
│   └── user.js
├── router/         # 路由配置
│   └── index.js
├── App.vue
├── main.js
└── style.css       # 全局样式
```

---

## 测试账号

### 管理员账号
- **手机号**: 13800000000
- **密码**: Admin123
- **角色**: 管理员

### 需求方账号
- **手机号**: 13800000001
- **密码**: 123456
- **角色**: 需求方（发布任务）

### 接单方账号
- **手机号**: 13800000003
- **密码**: 123456
- **角色**: 接单方（接受任务）

### 账号权限说明
- **管理员**: 可访问管理后台，查看数据统计、处理争议
- **需求方**: 可发布任务、验收任务、评价接单方
- **接单方**: 可接受任务、完成任务、提交凭证

登录后系统会自动创建模拟数据，可直接体验所有功能。

---

## 常见问题

### PowerShell 脚本执行策略错误

如果遇到 `无法加载文件 npm.ps1，因为在此系统上禁止运行脚本` 错误，请以管理员身份运行 PowerShell 并执行：

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

然后重新尝试运行 `npm run dev`。
