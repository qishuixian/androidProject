# Day38 · Build Variant、Flavor、Debug/Release

## 用前端把流程串起来

类似执行 `vite build --mode demo`：构建前选择配置，构建工具产出对应文件，浏览器运行产物。Android 也一样：先选变体 → Gradle 合并源码与配置 → 生成 APK → 安装后显示配置。它不是页面中的运行时环境开关。

入口：首页 → Day38。源码：[BuildVariantDemo.kt](../app/src/main/java/com/example/myapplication/day38/BuildVariantDemo.kt)。配置：[app/build.gradle.kts](../app/build.gradle.kts)。

## 两个维度别混在一起

| 变体 | Flavor 环境 | BuildType 构建方式 | 安装身份 |
|---|---|---|---|
| demoDebug | demo | debug | com.example.myapplication.demo |
| prodDebug | prod | debug | com.example.myapplication |
| demoRelease | demo | release | com.example.myapplication.demo |
| prodRelease | prod | release | com.example.myapplication |

demo/prod 表示环境；debug/release 表示构建方式。prodDebug 完全成立：正式配置也可以配调试构建。debug 默认可调试并使用调试签名；release 默认不可调试，优化是否开启取决于配置，本项目保留既有 release 优化规则。

同一 Flavor 的 Debug/Release 使用同一安装 id，不可同时独立安装，签名不一致也可能无法覆盖。本例 demo 加 .demo 后可以与原包共存，拥有独立数据；prod 保留原包身份。namespace 保持 com.example.myapplication，故导入 BuildConfig 的路径不随安装 id 改变。

## 配置调用链

```text
app/build.gradle.kts
  → flavorDimensions("environment")
  → demo / prod 的 buildConfigField
  → 与 debug / release 组合
  → 自动生成 BuildConfig
  → Day38 读取并显示
```

`buildConfigField("String", "ENVIRONMENT", "\"本地演示\"")` 的第三个参数是生成代码中的表达式，因此 String 值需要带引号。最终类似生成 `public static final String ENVIRONMENT = "本地演示";`。Java/Kotlin 都可以读取这些生成字段，不要手写 BuildConfig。

BuildConfig 会被打包，不能用它保存密钥。环境名称只是配置，不能证明服务器地址、签名和发布渠道都正确。

## source set：本次构建选哪些文件？

```text
src/main/         所有变体共享
src/demo/         demo 变体专用
src/prod/         prod 变体专用
src/debug/        debug 构建专用（本例没有新建）
src/demoDebug/    单个变体专用（本例没有新建）
```

本例新增 src/demo/assets/day38_environment.txt 与 src/prod/assets/day38_environment.txt。相同路径的文件分别进入对应 Flavor 的 APK，下一课继续学习资源合并优先级。此处是构建选择，不是运行时 if 判断。

## 如何运行

先 Sync Gradle，再打开 Android Studio 的 Build Variants 工具窗口，将 app 的 Active Build Variant 选为 demoDebug 或 prodDebug，重新 Run。截图页面字段应随 APK 改变；已有安装不会因为 IDE 下拉框变化而自动更新。

```powershell
$env:JAVA_HOME='D:/android/Android Studio/jbr'
# 只构建指定变体，避免无意构建全部 Flavor。
.\gradlew.bat :app:assembleDemoDebug --no-configuration-cache --no-daemon
.\gradlew.bat :app:assembleProdDebug --no-configuration-cache --no-daemon
```

产物分别位于 `app/build/outputs/apk/demo/debug/app-demo-debug.apk` 和 `app/build/outputs/apk/prod/debug/app-prod-debug.apk`。引入 Flavor 后旧的 `app/build/outputs/apk/debug/app-debug.apk` 可能是历史产物，不应继续拿它安装。

`:app:assembleDebug` 现在聚合构建多个 debug Flavor；单元测试也按变体细分，比如 `:app:testDemoDebugUnitTest`。Library 没有新增 Flavor，仍提供其 debug/release 变体，app 的环境维度无需强制复制到所有库。

## Release 与发布边界

`assembleProdRelease` 是构建任务，不等于已签名发布。本项目没有新增正式签名配置，也没有执行发布；正式分发还需保护签名材料、核对应用 id/版本/权限及验证优化后的行为。AAB 使用 bundleProdRelease 生成，不能把 AAB 当作普通 APK 直接 adb install。

## 验收

1. demoDebug 页面显示 demo、debug、.demo 安装后缀；prodDebug 显示 prod、debug、原安装 id。
2. 能区分 Flavor、BuildType、Variant 三个概念。
3. 能指出 BuildConfig 从哪个 Gradle 配置生成，而不是把 import 当作配置来源。
4. 能定位两个新 APK，解释为何旧 APK 路径不再适用。
5. 能说明构建环境选择不同于运行时切换，Release 构建不同于已发布。
