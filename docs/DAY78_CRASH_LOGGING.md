# Day78 · 崩溃与日志

入口：首页 → Day78 · 崩溃与日志。
源码：[CrashLogDemo.kt](../app/src/main/java/com/example/myapplication/day78/CrashLogDemo.kt)。

## 先区分三类现场

- Java/Kotlin 崩溃：通常看到 `FATAL EXCEPTION`、异常类型、线程和 Java/Kotlin 堆栈。
- Native 崩溃：常见 `SIGSEGV`、`SIGABRT`，要记录线程、信号、so 名称、地址和 tombstone。
- ANR：进程没有及时响应，不等同于闪退；要看 `ANR in`、主线程堆栈和 traces。

排查时先确认发生时间、设备、Android 版本、Flavor/BuildType、包名、复现步骤和最近改动，再从日志中找到第一个属于自己代码的栈帧。系统日志很多，不能只看最后一行。

## Logcat 阅读顺序

```text
确认时间和进程
 → 搜索 FATAL EXCEPTION / ANR / signal
 → 判断 Java、Native 还是无响应
 → 找线程名和首个业务栈帧
 → 对照版本、变体和最近改动
 → 做最小复现并记录修复前后结果
```

页面按钮会使用 `Log.e("Day78", ...)` 写入一条练习日志。可以在 Android Studio Logcat 中按 `Day78` 过滤；它不会主动抛异常，也不会模拟真实崩溃。

## 日志脱敏

Token、密码、Cookie、手机号、身份证号和完整用户资料不应直接写入日志。需要记录时保留必要的类型、长度、哈希或掩码，例如 `token=***REDACTED***`。脱敏应在日志产生处完成，不能依赖后来人工删除。

Bugly、Crashlytics 等平台可以集中收集崩溃，但接入 SDK 不会替代本地复现、版本确认和堆栈阅读；还要检查上传内容是否包含隐私数据。

## 现场信息模板

```text
发生时间：
设备/Android 版本：
包名、Flavor、BuildType：
versionCode/versionName：
复现步骤：
预期结果 / 实际结果：
关键日志与首个业务栈帧：
最近改动：
是否每次复现：
```

不要把“启动不了”“页面没反应”和“闪退”混为一个结论：分别检查进程是否存在、Activity 是否启动、主线程是否阻塞，以及是否产生异常或 tombstone。

## 验收清单

1. 进入 Day78，切换三个固定案例。
2. 点击“记录脱敏日志”，在 Logcat 搜索 `Day78`。
3. 能指出 Java/Kotlin 崩溃、Native 崩溃和 ANR 的关键词差异。
4. 能从堆栈找到第一个属于自己代码的调用位置。
5. 能写出包含设备、版本、变体和复现步骤的现场记录。
6. 说明为什么不应把 Token 和密码写入日志。

本课没有接入 Bugly/Crashlytics，也没有主动制造崩溃；真实设备崩溃、ANR 和 Native tombstone 需要单独采集验证。
