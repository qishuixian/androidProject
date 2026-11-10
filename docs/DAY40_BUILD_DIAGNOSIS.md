# Day40 · Gradle、JDK、SDK 与构建失败排查

## 前端类比：先分清工具角色

| Android | 前端近似角色 | 当前项目证据 |
|---|---|---|
| Gradle Wrapper | 项目固定版本的构建工具入口 | gradle-wrapper.properties：9.6.0 |
| AGP | 构建工具的 Android 专用插件 | libs.versions.toml：9.4.1 |
| JDK | 运行构建工具的 Java 环境，类似 Node 之于前端工具 | Daemon JVM criteria 要求 17 |
| Android SDK | Android 平台 API 与打包工具集合 | app compileSdk/targetSdk 37，minSdk 24 |
| adb | 设备通信、安装与日志工具 | 属于 SDK platform-tools |

这些版本不是同一个编号体系，不需要“全部改成 37”。安装目录也不是同一目录。sourceCompatibility/targetCompatibility=11 是本项目 Java 源码/字节码兼容目标，不等于 Gradle 进程一定使用 JDK 11。Kotlin 编译插件、Java 工具链也可能有各自配置。

入口：首页 → Day40。源码：[BuildDiagnosisDemo.kt](../app/src/main/java/com/example/myapplication/day40/BuildDiagnosisDemo.kt)。页面是固定案例练习，不会检查电脑上的真实 JVM，也不执行构建命令。

## 整体执行链

```text
选择 demoDebug
 → gradlew 选择 Gradle 版本
 → 启动/复用 Gradle Daemon JVM
 → 配置项目、解析依赖、定位 SDK
 → 合并资源、Kotlin/Java 编译、DEX、打包签名
 → 得到 APK
 → adb 安装
 → 启动 Activity
 → 查看应用运行日志
```

任务可以并行，以上是理解阶段的简化链路。失败时先找 `What went wrong`、失败任务与异常原因，`Configuring projects` 是状态，不是独立错误。Warning 不等于 Build Failed，`UP-TO-DATE` 也不等于该任务没有被检查。

## 之前 jlink 报错到底查什么？

```text
jlink executable .../.vscode/extensions/redhat.java.../bin/jlink.exe does not exist
```

证据指向缺失的 JDK 工具路径。先检查实际 Gradle JVM，而不是修改页面代码。

当前项目有 `gradle/gradle-daemon-jvm.properties`，其中 toolchainVersion=17。Daemon JVM criteria 会优先于 JAVA_HOME 和 org.gradle.java.home 影响 Daemon 选择；所以只改终端 JAVA_HOME 不能保证覆盖它，也不代表 IDE 与终端完全使用同一环境。

```powershell
# 查看启动 JVM 和 Daemon JVM 信息（不是只看 PATH 中的 java）。
.\gradlew.bat --version
# PATH 上的 java 仅作对照，可能不是 Gradle 实际使用的 JVM。
Get-Command java
java -version
# 验证某个候选 JDK 是否真的提供 jlink。
Test-Path 'D:/android/Android Studio/jbr/bin/jlink.exe'
```

Android Studio 的 Gradle 设置可能显示 Gradle JDK，也可能显示 Gradle JVM criteria；按实际界面检查 Version，而不是强找旧版下拉框。候选 JBR 路径只是本机示例，应核对其实际 Java 版本和项目要求，不要把不存在的路径写进仓库。

必要时在修改有效 JVM 配置后停止旧 Daemon：

```powershell
.\gradlew.bat --stop
.\gradlew.bat :app:assembleDemoDebug --no-configuration-cache --no-daemon --stacktrace
```

`--no-daemon` 仍可能启动单次构建 Daemon，日志对此有说明，不代表开关无效。更换配置后以新一次完整结果为准。

## 按阶段定位，避免把安装失败叫编译失败

| 现象 | 首先检查 | 不应直接推断 |
|---|---|---|
| SDK location not found | local.properties 的 sdk.dir、实际目录 | Kotlin 语法错误 |
| Could not resolve dependency | 坐标、仓库、网络、解析原因 | 所有缓存已坏 |
| Unresolved reference | 失败模块/变体、源码、依赖类路径 | 重装 SDK 必能解决 |
| Duplicate resources | 同优先级的重复资源声明 | Kotlin 类名错误 |
| INSTALL_FAILED_UPDATE_INCOMPATIBLE | 安装 id 与签名 | APK 编译失败 |
| FATAL EXCEPTION | 应用进程异常堆栈 | Qualcomm 系统日志就是根因 |

安装验证示例（已连接并选择正确设备时）：

```powershell
adb devices
adb install -r app/build/outputs/apk/demo/debug/app-demo-debug.apk
adb shell am start -W -n com.example.myapplication.demo/com.example.myapplication.MainActivity
adb logcat -d -b crash
```

demo 的 applicationId 带 .demo，Activity 类仍在原 namespace 包中，因此上面组件名两边不同。若有多个设备使用 adb -s 设备序列号。签名不一致时不要未经考虑就卸载，卸载可能丢失课程数据库等数据。

## 缓存和 clean 用在什么时候？

- 模块 build/：生成代码、中间文件、APK/AAR；不要直接改。
- 根目录 .gradle/：项目构建状态；Gradle user home 还有分发和依赖缓存。
- clean 清理构建输出，不会自动修复错误依赖、缺失 JDK 或业务代码。
- 先记录原始失败，再按证据处理；重装、删除全部缓存会增加成本且可能掩盖线索。

## 产物与成功边界

demoDebug：`app/build/outputs/apk/demo/debug/app-demo-debug.apk`。
prodDebug：`app/build/outputs/apk/prod/debug/app-prod-debug.apk`。

构建成功 → 有有效产物；安装成功 → 设备接收包；启动成功 → Activity 能启动；页面按钮正常 → 需要实际交互验证。四者不能互相代替，更不等于已发布。

## 验收

1. 能区分 Gradle、AGP、JDK、SDK、adb 的角色。
2. 能从失败任务判断是配置、编译、安装还是运行问题。
3. 能解释为何只设置 JAVA_HOME 可能不改变本项目 Daemon JVM。
4. 能读 jlink、缺 SDK、缺符号、签名不一致、运行崩溃五类日志。
5. 能定位新 APK，说明何时应该 clean，何时应修配置或源码。
