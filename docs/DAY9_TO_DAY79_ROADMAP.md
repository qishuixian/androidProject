# Day 9～Day 30：Java、Kotlin 与 Android 提升路线

这份路线承接 Day7 的协程与异步加载、Day8 的列表渲染与状态提升。你已有十年前端经验，因此不重复 HTML、CSS、JavaScript 基础，而是用前端已有经验类比 Android 概念，重点补齐 Java、Kotlin 和 Android 实战能力。

## 提升规则

每一天只增加一个核心概念，并完成一个可运行的小改动：

1. 先用已有前端经验理解概念。
2. 对照 Java 写法，再学习 Kotlin 写法。
3. 接入当前 Android App，保留可点击、可观察、可验证的结果。
4. 在前一天代码上增加能力，不推翻已有结构。

| 天数 | 核心提升 | Java / Kotlin | Android 实践 |
|---|---|---|---|
| Day 9 | 列表数据建模 | Java 类、Kotlin `data class` | 将课程字符串改为课程对象 |
| Day 10 | 稳定身份与列表 key | `equals`、`hashCode`、Kotlin key | 排序、删除后保持项身份 |
| Day 11 | 用户事件与回调 | 接口、函数类型 | 列表项点击、删除、重试 |
| Day 12 | 空值安全 | Java 判空、Kotlin `?.` | 可选课程详情与空状态 |
| Day 13 | 集合转换 | Java Stream、Kotlin `map/filter` | 搜索和筛选课程 |
| Day 14 | sealed 状态 | Java 枚举/类、Kotlin sealed class | Idle、Loading、Success、Error |
| Day 15 | ViewModel 状态 | Java 生命周期思路、Kotlin StateFlow | 列表状态从 UI 提升到 ViewModel |
| Day 16 | 单向数据流 | 接口与不可变数据 | State、Event、Effect 分离 |
| Day 17 | 页面生命周期 | Activity/Fragment Java API | 观察创建、销毁与重建 |
| Day 18 | 配置变化与状态恢复 | Bundle、保存状态 | 旋转屏幕保留列表和选中项 |
| Day 19 | Compose 重组 | Java 对比、Kotlin 稳定性 | 观察哪些组件重新组合 |
| Day 20 | UI 状态与业务状态 | DTO、Entity 基础 | 区分页面状态和课程数据 |
| Day 21 | Repository 分层 | Java 接口实现 | UI、ViewModel、Repository 分层 |
| Day 22 | 依赖注入深化 | 构造方法、接口 | Koin 注入真实 Repository |
| Day 23 | Retrofit 请求 | Java 回调对比、Kotlin suspend | 获取课程列表 |
| Day 24 | 网络错误处理 | 异常类型、Result | 网络失败、空数据、重试 |
| Day 25 | Room 本地数据库 | Java 注解模型、Kotlin Entity | 保存和读取课程 |
| Day 26 | 缓存与离线优先 | 数据层接口 | 本地数据优先、网络刷新 |
| Day 27 | 协程并发 | Thread、Executor、Coroutine | 并行加载课程与详情 |
| Day 28 | Paging 与大列表 | Java 分页模型、Kotlin Flow | 分页加载与加载更多 |
| Day 29 | 测试与调试 | JUnit、Mock 思路 | ViewModel、Repository、Compose 测试 |
| Day 30 | 综合项目 | Java/Kotlin 代码边界 | 完成课程列表、详情、刷新、缓存 |

## 与 Day7、Day8 的直接衔接

- **Day7 已学**：Repository、ViewModel、协程、Loading/Success/Error、StateFlow。
- **Day8 已学**：列表渲染、父组件持有选中状态、子组件通过回调上报事件。
- **Day9 开始**：把 Day8 的字符串列表提升为类型明确的课程对象。
- **后续提升**：把 UI 状态逐步提升到 ViewModel，再接入 Repository、网络和本地数据库。

不再安排 HTML、CSS、JavaScript 入门课程；前端经验只用于帮助理解状态、组件、事件和异步之间的对应关系。

## Day30 之后：面向 vipThinkStudent2 的补充路线

当前项目已经覆盖 Java/Kotlin 基础、Compose、ViewModel、StateFlow、Repository、Retrofit、Room、缓存、分页、协程和测试。对比 `E:/code/app/vipThinkStudent2` 后，还需要补充传统 Android、多模块、H5/Laya、实时通信和发布工程能力。

`vipThinkStudent2` 是 Java 与 Kotlin 混合的多模块项目，同时使用 XML、ViewBinding、Fragment、Compose、WebView、Laya、WebSocket、RTM、RTC、推送和多渠道构建。当前项目是单模块、单 Activity 的 Compose 教学项目，两者的工程复杂度不同。

### 第一阶段：传统 Android 页面

| 天数 | 主题 | 重点 |
|---|---|---|
| Day31 | XML 与 ViewBinding | XML、ViewBinding、Context、Activity 与 Application |
| Day32 | Fragment 生命周期 | Fragment、返回栈、Activity 与 Fragment 通信 |
| Day33 | RecyclerView | Adapter、ViewHolder、DiffUtil、稳定 id |
| Day34 | Dialog 与 PopupWindow | Dialog、BottomSheet、PopupWindow、页面通信 |
| Day35 | View 与 Compose 对照 | View 测量布局绘制、传统 View 和 Compose 状态模型 |

### 第二阶段：大型项目与 Gradle

| 天数 | 主题 | 重点 |
|---|---|---|
| Day36 | 多模块 Android 工程 | `settings.gradle`、Application 与 Library 模块 |
| Day37 | Gradle 依赖 | `api`、`implementation`、版本管理、依赖冲突 |
| Day38 | Build Variant | product flavor、debug/release、source set |
| Day39 | 多模块资源 | 公共资源、代码依赖、资源合并 |
| Day40 | 构建排错 | Gradle、JDK、SDK、缓存和 APK 产物诊断 |

### 第三阶段：WebView、H5 与 JavaScript Bridge

| 天数 | 主题 | 重点 |
|---|---|---|
| Day41 | WebView 基础 | `loadUrl`、生命周期、返回键、页面加载 |
| Day42 | H5 与 Native 通信 | Native 调 JavaScript，JavaScript 调 Native |
| Day43 | JavaScript Bridge | `addJavascriptInterface`、参数、回调和线程 |
| Day44 | WebView 登录态 | Cookie、Token、User-Agent 和页面会话 |
| Day45 | WebView 调试 | 白屏、页面错误、网络请求和安全边界 |

### 第四阶段：Laya 与 Native Bridge

| 天数 | 主题 | 重点 |
|---|---|---|
| Day46 | 页面类型判断 | Native、H5、WebView、Laya 的运行路径 |
| Day47 | Laya 双向通信 | Laya 与 Android 的调用链 |
| Day48 | 事件协议 | JSON、协议号、参数校验和 RxBus |
| Day49 | Laya 生命周期 | 引擎初始化、页面销毁、资源和回调 |
| Day50 | Bridge 排错 | Native 崩溃、资源加载和完整 APK 对比 |

### 第五阶段：网络与实时通信

| 天数 | 主题 | 重点 |
|---|---|---|
| Day51 | 公共请求封装 | Token、请求头、业务错误码和登录失效 |
| Day52 | 文件上传 | Multipart、OSS、进度和失败重试 |
| Day53 | WebSocket | 连接、断线、重连和消息分发 |
| Day54 | RTM 实时消息 | 聊天、事件监听、协议和消息状态 |
| Day55 | 实时通信测试 | 网络切换、重复消息、顺序和取消 |

### 第六阶段：音视频与复杂输入

| 天数 | 主题 | 重点 |
|---|---|---|
| Day56 | 音视频 SDK | 初始化、加入频道、退出和资源释放 |
| Day57 | 摄像头与麦克风 | 权限、音频状态、视频渲染和前后台切换 |
| Day58 | 远端用户回调 | SDK 回调线程、加入/离开、网络质量 |
| Day59 | 音视频页面状态 | Surface、生命周期、旋转和状态恢复 |
| Day60 | Native 崩溃 | `SIGSEGV`、设备差异、完整安装与热部署对比 |
| Day61 | EditText 与 TextField | 输入框、焦点、selection 和清空 |
| Day62 | 键盘与 IME | `InputMethodManager`、提交和组合文本 |
| Day63 | 复杂输入调试 | `InputConnection`、`ACTION_DOWN`、焦点链路 |
| Day64 | Native 与 Laya 输入 | 两套输入控件的焦点和键盘冲突 |

### 第七阶段：多语言、权限与发布

| 天数 | 主题 | 重点 |
|---|---|---|
| Day65 | Android 资源 | `strings.xml`、`@string` 和资源合并 |
| Day66 | 多语言 fallback | `values`、地区目录、匹配和回退 |
| Day67 | 业务语言边界 | App 语言、报告语言、Native 与 Laya 文案 |
| Day68 | 多语言测试 | 手机布局、文本溢出、日期和数字格式 |
| Day69 | 运行时权限 | 相机、麦克风、存储和拒绝处理 |
| Day70 | 通知与推送 | 通知渠道、推送初始化和点击跳转 |
| Day71 | 前台 Service | 后台限制、生命周期和通知绑定 |
| Day72 | 屏幕录制 | MediaProjection、授权、Service 和资源释放 |
| Day73 | 权限兼容性 | Android 版本差异、永久拒绝和设置页 |
| Day74 | Application 启动链 | Application、Splash、登录态和主页面 |
| Day75 | 热修复与资源 | 初始化时机、版本和完整 APK 验证 |
| Day76 | 渠道包 | flavor、渠道参数、Walle 和产物检查 |
| Day77 | Release 签名 | keystore、APK/AAB、版本和升级 |
| Day78 | 崩溃与日志 | Bugly、Logcat、脱敏和现场信息 |
| Day79 | 综合发布 | 自动检查、真机回归、发布记录和回滚 |

### 最优先补充的十个知识点

如果不希望一次学习全部内容，建议按以下顺序开始：

1. XML 与 ViewBinding
2. Fragment 生命周期
3. RecyclerView Adapter
4. WebView 基础
5. JavaScript Bridge
6. 多模块 Gradle
7. Laya 与 Native 通信
8. 运行时权限与 Activity Result
9. WebSocket 与实时消息
10. 音视频 SDK 生命周期

这十项最直接对应 `vipThinkStudent2` 的实际代码结构。后续学习应以真实调用链为主：从 Activity 或 Fragment 入口，跟到 ViewModel、Repository、WebView/Laya/SDK 回调，再回到 UI；同时区分源码检查、编译测试、设备验证和生产验证。

## 已锁定的学习优先级清单

近期执行计划见 [DAY31_TO_DAY55_LEARNING_PLAN.md](DAY31_TO_DAY55_LEARNING_PLAN.md)。

以下顺序是当前确定的优先级，后续安排课程时优先按此顺序推进：

1. **XML 与 ViewBinding**：掌握传统 Android 页面、ViewBinding 生成和 View 引用关系。
2. **Fragment 生命周期**：掌握 Fragment 创建、销毁、返回栈和与 Activity 的通信。
3. **RecyclerView Adapter**：掌握 Adapter、ViewHolder、DiffUtil、列表状态和稳定 id。
4. **WebView 基础**：掌握 `loadUrl`、页面生命周期、返回键、Cookie 和 WebView 调试。
5. **JavaScript Bridge**：掌握 H5 调 Native、Native 调 JavaScript、参数和回调线程。
6. **多模块 Gradle**：掌握 Application/Library 模块、依赖关系、source set 和构建变体。
7. **Laya 与 Native 通信**：掌握 Laya 页面识别、协议号、JSON、RxBus 和 Native 回调链。
8. **运行时权限与 Activity Result**：掌握相机、麦克风、通知、文件、权限拒绝和系统设置跳转。
9. **WebSocket 与实时消息**：掌握连接、断线重连、消息顺序、重复消息和取消。
10. **音视频 SDK 生命周期**：掌握 SDK 初始化、频道、远端用户、Surface、权限和资源释放。

后续每个主题都应完成四步：

1. 阅读真实项目中的入口和调用链。
2. 在当前教学项目中做一个最小可运行 Demo。
3. 用 Java 与 Kotlin 分别理解关键 API。
4. 区分源码分析、编译测试、设备验证和生产验证。
