# Day 9～Day 30 学习路线

这份路线承接 Day8 的列表渲染与状态提升，逐步扩展到导航、网络、本地数据、系统能力、测试和发布。

| 天数 | 文档主题 | 主要内容 |
|---|---|---|
| Day 9 | `LazyColumn` 性能与稳定 key | 稳定 key、列表项复用、重组观察、大列表性能 |
| Day 10 | Compose 导航与多页面路由 | NavHost、路由、返回栈 |
| Day 11 | 页面参数传递与返回结果 | 路由参数、SavedStateHandle、页面结果 |
| Day 12 | 表单输入、校验与错误提示 | TextField、输入状态、表单校验 |
| Day 13 | Material 3 主题、颜色与字体 | Theme、ColorScheme、Typography |
| Day 14 | 深色模式与响应式布局 | 夜间主题、窗口尺寸、横竖屏适配 |
| Day 15 | `rememberSaveable` 与屏幕旋转 | 状态保存、配置变化、恢复流程 |
| Day 16 | ViewModel、StateFlow 与 UI 状态 | 状态建模、收集状态、事件处理 |
| Day 17 | Repository 分层与单向数据流 | UI、ViewModel、Repository 职责边界 |
| Day 18 | Retrofit 网络请求基础 | API 接口、挂起请求、响应模型 |
| Day 19 | 网络状态、错误处理与重试 | Loading、Success、Empty、Error |
| Day 20 | Room 数据库与本地缓存 | Entity、Dao、Database、查询流 |
| Day 21 | 离线优先与数据同步 | 本地优先、刷新策略、同步冲突 |
| Day 22 | Paging 分页列表 | PagingSource、Pager、分页加载状态 |
| Day 23 | 图片加载与缓存 | 图片请求、占位图、内存与磁盘缓存 |
| Day 24 | 权限申请与系统能力 | 运行时权限、拒绝处理、权限状态 |
| Day 25 | 文件选择、相册与相机 | Activity Result、Uri、文件访问 |
| Day 26 | 通知、后台任务与 WorkManager | 通知渠道、约束条件、周期任务 |
| Day 27 | 登录状态、Token 与安全存储 | 登录流程、Token 保存、退出登录 |
| Day 28 | 单元测试与 ViewModel 测试 | 测试状态转换、Repository、协程测试 |
| Day 29 | Compose UI 测试与调试 | 语义节点、点击断言、布局检查 |
| Day 30 | 项目整合、构建、签名与发布 | 构建变体、签名、APK、发布检查 |

## 建议的文档文件名

```text
DAY9_LAZY_COLUMN_PERFORMANCE.md
DAY10_COMPOSE_NAVIGATION.md
DAY11_NAVIGATION_ARGUMENTS.md
DAY12_FORM_INPUT_AND_VALIDATION.md
DAY13_MATERIAL3_THEME.md
DAY14_DARK_MODE_AND_RESPONSIVE_LAYOUT.md
DAY15_SAVESTATE_AND_ROTATION.md
DAY16_VIEWMODEL_STATEFLOW.md
DAY17_REPOSITORY_AND_UNIDIRECTIONAL_DATA_FLOW.md
DAY18_RETROFIT_NETWORK.md
DAY19_NETWORK_ERROR_HANDLING.md
DAY20_ROOM_DATABASE.md
DAY21_OFFLINE_FIRST_SYNC.md
DAY22_PAGING_LIST.md
DAY23_IMAGE_LOADING_AND_CACHE.md
DAY24_PERMISSIONS.md
DAY25_FILES_CAMERA_AND_GALLERY.md
DAY26_NOTIFICATIONS_AND_WORKMANAGER.md
DAY27_LOGIN_TOKEN_SECURITY.md
DAY28_UNIT_TESTING.md
DAY29_COMPOSE_UI_TESTING.md
DAY30_RELEASE_AND_DEPLOYMENT.md
```

Day9 建议先承接 Day8，重点练习稳定 `key`、数据类列表、列表项复用和大列表性能，再进入导航主题。
