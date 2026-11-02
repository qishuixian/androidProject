# Android 布局学习项目

面向 Android 初学者，尤其是有前端开发经验的学习者。通过中文说明、前端类比、真实 XML 效果和动手练习，了解六种 Android View 布局。

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
