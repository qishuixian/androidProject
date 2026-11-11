# Android 入门学习项目

面向 Android 初学者，尤其是有前端开发经验的学习者。学习范围包括 Java、Kotlin、Android 基础、布局与 Koin 依赖注入。目前已实现六种 Android View 布局的学习页面，其他主题按下面的路线逐步学习和扩展。

完整教程见：[Android Java + Kotlin 混合学习教程](docs/ANDROID_JAVA_KOTLIN_TUTORIAL.md)；当前课程见：[Day 1：Java 基础与 Android 代码阅读](docs/DAY1_JAVA_BASICS.md)。 下一课：[Day 2：Kotlin 基础与 Java 对照](docs/DAY2_KOTLIN_BASICS.md)。 当前课程：[Day 3：Android 页面基础与运行链路](docs/DAY3_ANDROID_PAGE_BASICS.md)。 下一课：[Day 4：Android UI 细节与交互](docs/DAY4_ANDROID_UI_DETAILS.md)。 当前课程：[Day 5：页面状态、返回与 ViewModel](docs/DAY5_STATE_AND_VIEWMODEL.md)。 下一课：[Day 6：依赖注入与 Koin](docs/DAY6_DI_KOIN.md)。 [Day 7：协程、异步加载与重试](docs/DAY7_COROUTINES_AND_LOADING.md)。

## 技术栈与学习方向

| 技术 | 负责什么 | 当前项目状态 |
| --- | --- | --- |
| Java | 基础语法、面向对象、阅读传统 Android 项目，与 Kotlin 互操作 | 计划补充学习示例，当前应用源码使用 Kotlin |
| Kotlin | Android 开发语言；空安全、数据类、扩展函数、Lambda | 已使用 |
| Android SDK | Activity、生命周期、资源、事件、系统能力 | 已使用 Activity 与资源等基础能力 |
| XML / View | 传统界面与布局，理解控件尺寸和位置 | 已实现六种布局示例 |
| Jetpack Compose | 用声明式代码构建界面，状态变化驱动 UI 更新 | 已用于首页、列表和详情页 |
| Material 3 | 主题、卡片、文字和顶部栏等 UI 组件 | 已使用 |
| ViewModel / StateFlow | 管理页面状态，让 UI 与业务逻辑分离 | 尚未实现，当前页面使用 Compose 本地状态 |
| Koin | 管理对象的创建、依赖关系和生命周期 | 尚未接入依赖，作为后续学习主题 |
| Gradle | 依赖管理、编译、测试和打包 | 已使用 Kotlin DSL 与版本目录 |

Java、Kotlin 是编程语言；Koin 是依赖注入框架；Compose 是 UI 工具包。它们解决的问题不同，可以在 Android 项目中配合使用。

## Android 入门学习路线

### 1. Java 基础

- 学习变量、基本类型、条件、循环、数组和方法。
- 理解类与对象、构造方法、封装、继承和接口。
- 学习集合（`List`、`Map`）、泛型、异常处理与 Lambda。
- 练习：用 Java 定义一个课程类，创建课程列表并按名称筛选。

前端类比：Java 的类和接口可类比 TypeScript 的 `class` 与 `interface`，但 Java 在运行时有自己的类型系统；Java 和 JavaScript 是不同语言。

### 2. Kotlin 基础

- 对照 Java 学习 `val` / `var`、函数、类和接口。
- 重点掌握可空类型、`?.`、`?:`、`data class` 和 Lambda。
- 理解 Kotlin 调用 Java 的方式，再学习协程与 Flow。
- 练习：阅读 `LayoutLessons.kt`，理解 `LayoutLesson` 数据类和 `listOf`；为一个布局修改说明。

### 3. Android 基础与布局

- 认识工程目录、Manifest、Activity、`onCreate()` 和生命周期。
- 理解 `res` 资源、`R` 引用、`dp` 与 `sp`。
- 学习 View、ViewGroup、六种布局、点击事件和页面导航。
- 区分 `gravity`（内容对齐）与 `layout_gravity`（父布局支持时，控制子控件的位置）。
- 练习：按下方布局章节修改 XML，运行后观察变化；使用 Logcat 查看日志。

### 4. Compose 与页面状态

- 学习 `@Composable`、`Modifier`、`Row`、`Column`、`Box` 和 `LazyColumn`。
- 理解重组、`remember`、`rememberSaveable` 和状态提升。
- 练习：追踪首页点击后 `page` 如何变化，以及列表如何切换为详情。

前端类比：可以把 Composable 理解为描述 UI 的组件函数，状态更新后界面重新计算；具体生命周期和执行规则与 React、Vue 并不完全相同。

### 5. ViewModel 与分层

- 将界面显示、页面状态、数据获取分别放在 UI、ViewModel、Repository 中。
- 先通过构造方法手动传入依赖，理解对象之间的关系。
- 练习目标：让 Repository 提供课程列表，ViewModel 暴露页面状态，UI 负责显示。当前项目尚未实现这一分层。

### 6. Koin 依赖注入

Koin 用来统一声明和提供对象。例如 ViewModel 需要 Repository 时，可以由容器提供 Repository，而不是在每个页面里重复创建。

前端类比：与 Vue 的 `provide/inject` 都有“声明依赖、按需获取”的思路，但 Koin 还负责依赖对象的创建与作用域管理。

建议依次学习：

1. **构造方法注入**：明确一个类需要哪些依赖。
2. **`module`**：集中声明对象如何创建。
3. **`single`**：在容器内复用同一个实例；不要让长生命周期对象持有 Activity。
4. **`factory`**：每次请求时创建新实例。
5. **`get()`**：在依赖定义中获取其他已注册对象。
6. **`startKoin`**：在 Application 中初始化容器，Android 集成可通过 `androidContext` 提供应用上下文。
7. **ViewModel 集成与作用域**：使用匹配 Koin 版本的 Android / Compose 集成 API，并了解实例何时创建和释放。

后续练习目标：`布局列表 UI → ViewModel → LessonRepository`，由 Koin 提供依赖，并在测试中替换 Repository。

**当前没有添加 Koin 依赖或初始化代码。** 接入时需要选择与项目 Kotlin、Compose 等版本兼容的 Koin 版本，再添加依赖、模块声明和 Application 初始化；仅在 README 中列出框架不会使它生效。

### 学习资料

- [Java 入门资料](https://dev.java/learn/)
- [Kotlin 中文文档](https://book.kotlincn.net/text/d-language-reference.html)
- [Kotlin 官方文档](https://kotlinlang.org/docs/home.html)
- [Android 开发基础课程](https://developer.android.com/courses)
- [Compose 官方文档](https://developer.android.com/develop/ui/compose/documentation)
- [Koin 官方文档](https://insert-koin.io/docs/quickstart/android/)
- [Android 开发博客专栏](https://blog.csdn.net/qishuixian/category_13219784.html?spm=1001.2014.3001.5482)

## 功能入口

启动 App → 点击 **布局** → 选择一种布局 → 查看学习详情。

每个详情页包含：布局介绍、前端类比、常用属性、实际布局效果、关键 XML 和练习。支持顶部返回和系统返回，页面选择通过 `rememberSaveable` 保存。

页面外壳使用 Kotlin、Jetpack Compose 和 Material 3；演示区域通过 `AndroidView` 与 `LayoutInflater` 加载真实 XML。六种布局属于传统 View 系统，和 Compose 的 `Row`、`Column` 等组件不同。

## 六种布局

| 布局 | 用途与关键属性 | 示例源码 |
| --- | --- | --- |
| LinearLayout（线性布局） | 水平或垂直排列；`orientation`、`layout_weight` | [demo_linear.xml](app/src/main/res/layout/demo_linear.xml) |
| RelativeLayout（相对布局） | 相对父容器或其他控件定位；`layout_below`、`layout_toEndOf` | [demo_relative.xml](app/src/main/res/layout/demo_relative.xml) |
| TableLayout（表格布局） | 使用 `TableRow` 组织行与列；`stretchColumns` | [demo_table.xml](app/src/main/res/layout/demo_table.xml) |
| FrameLayout（帧布局） | 层叠内容、角标；`layout_gravity` | [demo_frame.xml](app/src/main/res/layout/demo_frame.xml) |
| AbsoluteLayout（绝对布局） | 固定坐标定位；`layout_x`、`layout_y` | [demo_absolute.xml](app/src/main/res/layout/demo_absolute.xml) |
| GridLayout（网格布局） | 行列网格；`columnCount`、`layout_columnWeight` | [demo_grid.xml](app/src/main/res/layout/demo_grid.xml) |

> `AbsoluteLayout` 自 API 3 起已废弃。这里仅用于学习和理解旧代码，不建议在新页面中使用。

## 开发环境

以下版本以当前仓库配置为准：

- Android Studio：使用支持项目 Android Gradle Plugin 的版本。
- Android Gradle Plugin：`9.4.1`；Gradle Wrapper：`9.6.0`。
- Gradle Daemon JVM：项目的 `gradle/gradle-daemon-jvm.properties` 指定 Java `25`。
- Android SDK：`compileSdk = 37`、`targetSdk = 37`。
- 运行设备：Android 7.0（API 24）及以上的手机或模拟器。

Gradle 运行用的 JDK 与应用源码的编译目标是不同设置；本项目 Java 源码兼容目标为 Java 11。首次同步需要联网下载依赖及可能缺失的工具链。

## 启动项目

1. 克隆仓库：

   ```bash
   git clone https://github.com/qishuixian/androidProject.git
   ```

2. 在 Android Studio 中打开仓库根目录，等待 Gradle Sync 完成。
3. 在 SDK Manager 中安装项目所需的 SDK。`local.properties` 中的 `sdk.dir` 应指向自己的 Android SDK，该文件不提交到仓库。
4. 准备运行设备：
   - 真机：开启开发者选项与 USB 调试，连接电脑并允许调试授权。
   - 模拟器：在 **Tools → Device Manager** 中创建并启动设备。
5. 顶部选择 **app** 和目标设备，点击 **Run ▶**，或按 **Shift + F10**。
6. 进入首页，点击 **布局**，选择需要学习的布局。

如果顶部仍显示 `Add Configuration` 且正在同步，先等待同步完成；同步失败时查看 Build / Sync 的具体错误。

### 命令行构建（Windows PowerShell）

在仓库根目录执行，先确保 Java、Android SDK 和依赖下载环境可用：

```powershell
# 编译 Debug APK
.\gradlew.bat :app:assembleDebug

# 安装到已连接的设备（不等同于启动）
.\gradlew.bat :app:installDebug

# 使用 SDK platform-tools 中的 adb 启动应用
adb shell am start -n com.example.myapplication/.MainActivity
```

Debug APK 默认位于 `app/build/outputs/apk/debug/app-debug.apk`。若 `adb` 未加入 PATH，可使用 Android SDK 下 `platform-tools/adb.exe` 的完整路径。

## 源码阅读路线

课程路线：Day1～Day8 的已完成文档见 `docs/`，Day9～Day79 的规划见 [DAY9_TO_DAY79_ROADMAP.md](docs/DAY9_TO_DAY79_ROADMAP.md)。

近期学习计划：Day31～Day55 见 [DAY31_TO_DAY55_LEARNING_PLAN.md](docs/DAY31_TO_DAY55_LEARNING_PLAN.md)。

Day33：[Fragment 通信与返回栈](docs/DAY33_FRAGMENT_COMMUNICATION_BACK_STACK.md)。

Day34：[RecyclerView、Adapter、ViewHolder](docs/DAY34_RECYCLERVIEW.md)。

Day35：[Dialog、BottomSheet、PopupWindow](docs/DAY35_OVERLAYS_AND_WINDOWS.md)。

Day36：[多模块工程结构](docs/DAY36_MULTI_MODULE.md)。

Day37：[api、implementation 与依赖管理](docs/DAY37_DEPENDENCY_MANAGEMENT.md)。

Day38：[Build Variant、Flavor、Debug/Release](docs/DAY38_BUILD_VARIANTS.md)。引入 Flavor 后请使用 `:app:assembleDemoDebug` 或 `:app:assembleProdDebug`，APK 路径见该文档。

Day39：[资源合并与 source set](docs/DAY39_RESOURCE_MERGING.md)。

Day40：[Gradle、JDK、SDK 与构建失败排查](docs/DAY40_BUILD_DIAGNOSIS.md)。

Day41：[WebView 加载与生命周期](docs/DAY41_WEBVIEW_LIFECYCLE.md)。

Day42：[Native 调 JavaScript](docs/DAY42_NATIVE_CALL_JAVASCRIPT.md)。

Day43：[JavaScript 调 Native](docs/DAY43_JAVASCRIPT_CALL_NATIVE.md)。

Day44：[Cookie、Token、登录态](docs/DAY44_WEBVIEW_SESSION.md)。

Day45：[WebView 白屏、调试和安全](docs/DAY45_WEBVIEW_DEBUG_SECURITY.md)。

Day46：[Native、H5、Laya 页面识别](docs/DAY46_PAGE_TYPE_IDENTIFICATION.md)。当前示例使用本地模拟资源，不代表引入真实 Laya SDK。

Day47：[Laya 调 Native](docs/DAY47_LAYA_CALL_NATIVE.md)。当前使用本地模拟 Laya 资源，未引入真实引擎。

Day48：[Native 调 Laya](docs/DAY48_NATIVE_CALL_LAYA.md)。当前使用本地模拟 Laya 资源，未引入真实引擎。

Day49：[协议号、JSON、RxBus 与事件分发](docs/DAY49_PROTOCOL_JSON_EVENT_BUS.md)。

Day50：[Bridge 日志和崩溃定位](docs/DAY50_BRIDGE_LOGS_CRASH_DIAGNOSIS.md)。

Day51：[统一请求封装、Token、业务错误码](docs/DAY51_UNIFIED_REQUEST_TOKEN_ERRORS.md)。

Day52：[文件上传和 OSS](docs/DAY52_FILE_UPLOAD_OSS.md)。

Day53：[WebSocket 连接与重连](docs/DAY53_WEBSOCKET_RECONNECT.md)。

Day54：[RTM 聊天消息](docs/DAY54_RTM_CHAT_MESSAGES.md)。

Day55：[断网、重复消息、取消和调试](docs/DAY55_OFFLINE_DUPLICATE_CANCEL_DEBUG.md)。

Day56：[音视频 SDK 生命周期](docs/DAY56_VIDEO_SDK_LIFECYCLE.md)。当前使用本地状态机，未接入真实 SDK。

状态流专项：[MutableStateFlow 详细学习](docs/MUTABLE_STATE_FLOW_DETAILED.md)。

- [MainActivity.kt](app/src/main/java/com/example/myapplication/MainActivity.kt)：应用入口、Compose 页面、点击导航和返回处理。
- [LayoutLessons.kt](app/src/main/java/com/example/myapplication/LayoutLessons.kt)：六种布局的说明、属性、代码片段、练习与资源映射。
- [res/layout](app/src/main/res/layout)：六种布局的完整 XML 示例。
- [ui/theme](app/src/main/java/com/example/myapplication/ui/theme)：Compose 主题、颜色和字体。
- [AndroidManifest.xml](app/src/main/AndroidManifest.xml)：启动 Activity 等应用配置。
- [app/build.gradle.kts](app/build.gradle.kts)：应用 SDK、构建选项与依赖声明。

运行链路：`MainActivity.onCreate()` → `setContent` → `LayoutLearningApp()` → 点击卡片更新 `page` → 根据 `layoutLessons` 显示详情 → `AndroidView` 加载对应 XML。

这里的“跳转”是在一个 Activity 内切换 Compose 内容，没有为每一种布局单独创建 Activity。

## 建议练习

1. **LinearLayout**：将第一个控件的 `layout_weight` 改为 `2`，观察宽度比例。
2. **RelativeLayout**：给 B 增加 `layout_toEndOf="@id/anchor"`，观察相对位置。
3. **TableLayout**：新增一个含两个单元格的 `TableRow`。
4. **FrameLayout**：把角标的 `layout_gravity` 改为 `top|end`。
5. **AbsoluteLayout**：修改 `layout_x`，理解固定坐标在不同屏幕上的限制。
6. **GridLayout**：将 `columnCount` 从 `2` 改为 `3`，观察排列变化。

修改 XML 后重新运行 App。页面展示的“关键 XML”是单独保存在 `LayoutLessons.kt` 中的教学片段，不会自动随 XML 文件更新；需要同步说明时请同时修改该文件。

## 检查与测试

```powershell
# 本地单元测试
.\gradlew.bat :app:testDebugUnitTest

# Android 静态检查
.\gradlew.bat :app:lintDebug

# 设备测试，需要连接手机或启动模拟器
.\gradlew.bat :app:connectedDebugAndroidTest
```

当前测试文件来自项目模板，尚未覆盖六种布局页面的导航与展示。建议手动检查：首页入口、六个详情页、上下滚动、顶部返回、系统返回，以及旋转屏幕后是否保留当前页面。

截至首次功能交付：六份 XML 已通过解析检查，但构建验证未完成（联网构建等待依赖读取，离线构建缺少 `foundation.aar` 缓存），尚未完成真机页面验证。以上命令是验证方法，不代表测试已通过。

遇到依赖下载失败时，先检查仓库网络访问和 Gradle Sync 错误；缓存未完整下载前，切换离线模式无法解决缺失依赖。
