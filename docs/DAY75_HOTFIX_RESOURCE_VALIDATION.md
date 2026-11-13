# Day75 · 热修复与资源验证

入口：首页 → Day75 · 热修复与资源。
源码：[HotfixResourceDemo.kt](../app/src/main/java/com/example/myapplication/day75/HotfixResourceDemo.kt)。

## 先区分几个概念

- Application 初始化：进程创建时执行，适合进程级依赖和日志。
- 资源合并：Gradle 构建 APK 时完成，决定当前变体包含哪些资源。
- BuildConfig：每个变体编译时生成的字段，不能在运行时切换 Flavor。
- 热部署/Apply Changes：IDE 尝试把支持的改动应用到正在运行的进程，不等于完整 APK 已安装。
- 热修复：产品级动态更新方案，涉及签名、版本、兼容、回滚和安全，本课不实现。

页面读取 BuildConfig 和 PackageManager 的当前包信息，帮助发现“源码配置”和“实际安装产物”是否一致。点击按钮只是记录验证策略，不下载、执行或加载外部代码。

## 启动与版本链

```text
构建变体配置
 → Gradle 生成 BuildConfig、资源表和 APK
 → 安装指定 APK
 → 进程创建 Application.onCreate
 → Activity / Compose 读取已安装包信息
 → 运行时只能观察当前产物，不能把 demo 变成 prod
```

`BuildConfig.FLAVOR`、`BUILD_TYPE`、`APPLICATION_ID` 和版本字段来自当前编译变体。`PackageManager` 读取的是已安装包。若两者看起来不一致，先检查安装包、进程包名、旧 APK 覆盖和构建目录，不要先推断热修复失败。

## 完整 APK 与热部署对照

固定源码、设备、数据、操作和变体，只改变部署方式：

```powershell
$env:JAVA_HOME = 'D:/android/Android Studio/jbr'
./gradlew.bat :app:assembleDemoDebug --no-configuration-cache --no-daemon
adb -s SERIAL shell am force-stop com.example.myapplication.demo
adb -s SERIAL install -r app/build/outputs/apk/demo/debug/app-demo-debug.apk
adb -s SERIAL shell am start -n com.example.myapplication.demo/com.example.myapplication.MainActivity
```

完整 APK 适合验证资源、Manifest、Native 库、Application 初始化和最终包内容。`install -r` 保留应用数据，不等于卸载重装；若要验证全新状态，应单独记录清除数据/卸载行为。

Apply Changes 由 IDE 根据改动类型决定实际动作，可能应用、重启 Activity、重启进程或回退为安装。资源、Manifest、Native 库、构建配置和 Application 初始化改动不能假设热部署覆盖；以 IDE 实际提示和设备包内容为准。

热部署成功而完整安装失败，或反过来，只能说明部署路径不同，仍需对比最终 APK、版本、签名、资源和 Logcat。不要把编辑器里看到新文案当成设备已运行新资源。

## 如果真的设计热修复，需要哪些边界

本课没有动态代码加载。生产热修复至少要明确：补丁格式、签名验证、版本/ABI/API 兼容、资源引用、原子替换、失败回滚、灰度、审计、隐私和强制升级策略。不要从网络下载未验证的 dex、so 或资源直接执行。

补丁检查也不能放在 Application 中无期限阻塞首屏。应定义超时、取消、离线行为和缓存版本；关键安全校验完成前不启用补丁。版本比较应使用结构化 versionCode/协议版本，不能只比较显示字符串。

## 资源验证顺序

1. 确定当前运行包名和变体。
2. 确定 APK 路径与构建时间，确认任务成功。
3. 检查资源来源、Flavor/BuildType 合并结果。
4. 完整安装或确认 IDE 实际部署动作。
5. 冷启动，读取 Application、Activity、BuildConfig 和资源日志。
6. 在设备上执行相同操作并截图/记录 Logcat。
7. 对比旧包、数据状态、签名和版本，再判断问题是否来自部署。

源码改动、构建成功、APK 安装成功、页面运行正确和已发布上线是五个不同证据层级。Day75 只提供本地诊断和验证模板。

## 验收

1. 分别构建 demoDebug/prodDebug，核对包名、Flavor、BuildType 和 APK 路径。
2. 进入页面，比较 BuildConfig 与 PackageManager 版本。
3. 选择完整 APK、热部署两个按钮，说明它们只是记录验证路线。
4. 改资源或 Manifest 后，使用完整 APK验证；记录 IDE 是否实际回退安装。
5. 卸载/清除数据实验单独记录，避免与热部署结果混淆。
6. 能解释为什么 Application 初始化不能被页面重组替代，为什么热部署不等于发布。
