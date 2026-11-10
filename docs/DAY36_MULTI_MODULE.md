# Day36 · 多模块工程结构

## 前端类比

多模块类似 Monorepo 中的 package：`app` 是最终运行的应用包，`course-core` 是可复用的内部 package。`settings.gradle.kts` 类似 workspace 的 package 清单，`implementation(project(":course-core"))` 类似声明 workspace 依赖。

## 当前工程

```text
MyApplication/
├─ settings.gradle.kts       # 声明 :app 和 :course-core
├─ app/                      # Application 模块，生成 APK
└─ course-core/              # Library 模块，生成 AAR/供其他模块使用
```

入口：首页 → Day36。示例源码：[MultiModuleDemo.kt](../app/src/main/java/com/example/myapplication/day36/MultiModuleDemo.kt)。

## 逐步理解依赖链

### 1. `settings.gradle.kts`

```kotlin
include(":app")
include(":course-core")
```

Gradle 只会把被 settings 声明的模块纳入工程。模块名以冒号开头，模块目录默认对应项目根目录下的文件夹。

### 2. Library 模块

`course-core/build.gradle.kts` 使用 `com.android.library`，没有 launcher Activity，不生成可独立安装的 APK；它提供可被其他模块使用的代码和资源。

### 3. Application 模块依赖 Library

```kotlin
implementation(project(":course-core"))
```

`app` 编译时可以导入 `com.example.coursecore.CourseModuleInfo`。构建 APK 时，Gradle 会把 Library 的编译结果及其资源合并进 app。

### 4. 依赖方向

```text
app → course-core
```

`course-core` 不应该导入 `com.example.myapplication` 的 Activity 或 UI。这样 Library 才能复用、测试，并能在未来被其他 Application 模块使用。

## 与单模块的区别

| 结构 | 优点 | 代价 |
|---|---|---|
| 全部放 app | 入口简单 | 业务、UI、数据容易混在一起 |
| 拆成 Library | 边界清晰、复用、并行开发 | Gradle 配置和依赖关系更多 |

不是文件夹多就叫多模块；必须在 `settings.gradle.kts` 注册，并通过 Gradle 依赖连接。

## 验证

```powershell
$env:JAVA_HOME='D:/android/Android Studio/jbr'
.\gradlew.bat :app:assembleDebug --no-configuration-cache --no-daemon
```

看到 `:course-core:compile...` 和 `:app:assembleDebug` 成功，才能确认模块被纳入构建。运行时首页 Day36 显示 `course-core`，说明 app 确实调用了 Library 的公共 API。
