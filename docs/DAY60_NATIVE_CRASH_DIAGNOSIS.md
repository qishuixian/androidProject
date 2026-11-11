# Day60 · Native 崩溃排查

入口：首页 → Day60 · Native 崩溃排查。
源码：[NativeCrashDemo.kt](../app/src/main/java/com/example/myapplication/day60/NativeCrashDemo.kt)。

## 从前端经验理解 Native 崩溃

网页 JavaScript 抛异常，与浏览器底层渲染进程崩溃，是不同层次的问题。类似地，Android Kotlin/Java 代码运行在 ART 托管环境中，音视频 SDK 的一部分可能运行在 C/C++ 本地库中。

这里 Native 特指 C/C++ 层，不是泛指所有 Android 原生页面。调用 SDK 的 Kotlin 方法可能通过 JNI 进入 `.so` 动态库，再在 SDK 的工作线程中执行。崩溃发生在 C/C++ 时，Kotlin 调用栈不一定就是现场。

本课不引入 NDK、JNI、真实音视频 SDK，也不故意制造崩溃。页面上设备型号、Android 版本、支持 ABI 和 APK 配置是真实读取；四份日志、库名、地址、Build ID 和行号均是教学样例。

## 完整执行链

```text
点击案例按钮
 → selected 更新，revealed 重置
 → Compose 重组
 → 根据 selected 取得对应的固定日志
 → 学员判断层次与证据
 → 点击“查看参考分析”
 → revealed=true
 → 展示证据允许的结论与下一步
```

`selected`、`revealed` 用 rememberSaveable 保存，旋转可以恢复练习进度。`current` 是列表按编号取出的派生结果，不需要另建 State。点击只是切换教材，不触发真实异常；Logcat 的 Day60 日志明确带 simulation=true。

## 四类证据不要混用

| 证据 | 当前能说明什么 | 继续检查 |
|---|---|---|
| Fatal signal 11 (SIGSEGV) + backtrace | 本地层非法内存访问 | 完整 tombstone、线程、符号、生命周期 |
| FATAL EXCEPTION + Java/Kotlin 异常 | 托管层未捕获异常 | Caused by、应用栈帧、触发操作 |
| UnsatisfiedLinkError / dlopen failed | 动态库加载或链接失败 | APK 内容、依赖库、ABI、位数、名称 |
| 只有系统进程 E 日志 | 单独不足以解释应用故障 | 应用 PID、时间、退出/ANR 证据 |

SIGSEGV 常见于空指针、越界、释放后使用等，但信号本身不能区分全部原因。fault addr=0x0 是线索，不等于已经定位到 Kotlin 的某个空变量。崩溃落在某个 SDK 库中，也不自动证明一定是 SDK 缺陷；调用方违反资源或线程契约也可能导致它。

## 为什么 try/catch 通常救不了 SIGSEGV

Kotlin 和 Java 的 try/catch 处理 Throwable 异常，不是 C/C++ 操作系统信号的通用恢复器。在线程里访问已释放的内存，不会自动转换成 Kotlin Exception。协程的 catch 或 CoroutineExceptionHandler 也不能把致命信号安全地恢复成页面提示。

相反，System.loadLibrary 失败产生的 UnsatisfiedLinkError 属于 Error（也是 Throwable），与 SIGSEGV 不同。不要为了隐藏初始化失败而直接吞掉所有 Throwable；应明确失败策略，停止使用未初始化的 SDK。

## 接着 Day59：资源时间线如何分析

```text
进入页面 → 创建 Surface → 绑定 SDK 渲染目标
 → 切后台或退出 → 停止提交/解绑 → Surface 销毁
 → 检查旧线程或回调是否仍使用旧目标
```

这是一条排查假设，不能仅看到 SIGSEGV 就判定是 Surface。需要把 SDK 初始化、加入、绑定、解绑、Surface 回调、释放、用户操作和崩溃线程的时间对齐。真实 SDK 要按契约处理线程同步、停止渲染和等待任务结束；仅把 Kotlin holder 设为 null，并不能停止 C/C++ 中已经运行的任务。

Day59 中的 PreviewBinding 只输出日志，不是真实渲染器；其幂等解绑帮助理解结构，不证明某厂商 SDK 的释放已经完成。

## 先保留什么证据

记录包名、versionCode、变体、提交版本、安装来源、设备型号、系统版本、当前进程 ABI、SDK 版本、触发步骤、发生时间与复现次数。保留完整信号头、进程和线程、backtrace、Build ID，不能只截取最后一行库名。

页面显示的 Build.SUPPORTED_ABIS 是设备支持列表，不是当前进程实际位数，也不能证明 APK 包含了对应库。结合 tombstone 的 ABI、APK lib/<abi>/ 内容和真实构建配置判断。模拟器可能是 x86/x86_64，也可能使用 ARM 环境或转译；不得按“模拟器”三个字直接推定 ABI。

PowerShell 取证示例（把 SERIAL 替换成 adb devices 中的实际设备编号）：

```powershell
# 枚举设备，连接多台时必须指定目标。
adb devices -l
# 持续保存日志，复现后 Ctrl+C 结束；不要只按应用 PID 过滤而漏掉 DEBUG/crash_dump。
adb -s SERIAL logcat -b all -v threadtime > day60-device-log.txt
# 保存系统诊断包，按设备授权提示操作。
adb -s SERIAL bugreport day60-bugreport.zip
```

生产设备可能无权直接读取 /data/tombstones；不要把 Permission denied 当作应用根因。可从系统诊断、崩溃平台或 SDK 厂商支持获取可用资料，注意日志和诊断包中的个人信息。

## 符号化是在做什么

`#00 pc ... liblesson_rtc.so` 给的是机器地址附近的位置；符号化把它映射到函数、源文件和行号，前提是有匹配符号。

必须对应同一次构建、同一 ABI 和匹配 Build ID 的未剥离库/符号，不能拿“同名库”或新版本符号解释旧版本地址。Java 混淆的 mapping.txt 不能替代 C/C++ 符号。

如果以后安装了对应 NDK，可使用它提供的 ndk-stack，将 tombstone 与未剥离库目录关联；或按工具要求用 llvm-addr2line 等处理地址。符号缺失时，先收集准确库版本、Build ID 和完整堆栈交给维护方，不编造源码行号。本课未运行符号化，因为没有真实崩溃或匹配 Native 库。

## 完整 APK 安装与热部署怎么对照

完整 APK 安装会安装完整构建产物；`adb install -r` 覆盖更新并通常保留应用数据，不等于卸载后重装，也不等于清除数据。Android Studio 的 Apply Changes 尝试应用受支持的改动，不等于完整安装；某些资源、Manifest、Native 库或构建配置改动需要重启或重新安装，具体以当前 IDE 的提示为准。

先固定源码、变体、设备、应用数据和触发步骤，只改变部署方式：

```powershell
# 使用项目现有的 Java 环境构建完整 demoDebug 包。
$env:JAVA_HOME = 'D:/android/Android Studio/jbr'
./gradlew.bat :app:assembleDemoDebug --no-configuration-cache --no-daemon
# 停止旧进程，再覆盖安装，保留数据。
adb -s SERIAL shell am force-stop com.example.myapplication.demo
adb -s SERIAL install -r app/build/outputs/apk/demo/debug/app-demo-debug.apk
# 以相同入口启动并执行记录好的操作序列。
adb -s SERIAL shell am start -n com.example.myapplication.demo/com.example.myapplication.MainActivity
```

prodDebug 包路径为 app/build/outputs/apk/prod/debug/app-prod-debug.apk，包名 com.example.myapplication。不要混装、混看另一个包的日志。

然后在同一基线做 IDE 支持的改动，用 Apply Changes 复现同一步骤并记录 IDE 实际采用的部署动作。若 IDE 自动回退到重新安装，这一轮不能记成成功热部署。

| 实验 | 固定条件 | 变化项 | 记录 |
|---|---|---|---|
| A | 同设备/源码/数据/操作 | 完整 APK 覆盖安装 | 次数、退出表现、信号、栈、Build ID |
| B | 与 A 对齐 | 支持的 IDE Apply Changes 流程 | 实际部署动作、是否重启、同样的崩溃证据 |
| C | 同源码/变体/操作；对齐可控数据 | 另一台真机或模拟器 | 型号、系统、GPU、实际 ABI、SDK 支持范围 |

热部署失败而完整安装成功，只能说明现象与部署方式相关，仍需检查实际安装内容和资源状态。真机失败、模拟器成功也不能直接认定“手机坏了”或“SDK 有 bug”。设备、ABI、GPU 驱动、时序和硬件能力都可能不同，一次只改变可控变量。

卸载和清除数据会改变本地数据库、权限和会话条件，不应暗中夹进 A/B 实验。确需测试干净数据时，单独记录为另一轮实验。

## 动手与验收

1. 依次选择四个案例，先说出证据所属层，再查看分析。
2. 能解释为什么系统 E 日志不等于应用崩溃原因。
3. 能解释 SIGSEGV、Java 空指针与 UnsatisfiedLinkError 的区别。
4. 对照 Day59 写出资源销毁后仍使用旧目标的假设，并列出验证它所需日志。
5. 说出符号化所需的构建版本、ABI、Build ID 和符号，不能只说“有一个 .so”。
6. 能区分完整安装、覆盖安装、卸载重装、热部署和清除数据。
7. 用实验表记录真实结果；本课案例不是实际 Native 崩溃复现结果。
