# Day50 · Bridge 日志和崩溃定位

## 用前端理解完整链路

类似给一次跨 iframe/宿主 SDK 调用增加 traceId：不能只记录“按钮点了”，而要看消息走到了哪里。requestId 关联一次调用，protocol 标记动作，stage 标记阶段。

```text
JS SEND → Bridge RECEIVE → VALIDATE → DISPATCH → HANDLE → REPLY → JS ACK
                           ↘ REJECT / ERROR
```

入口：首页 → Day50。源码：[BridgeDiagnosisDemo.kt](../app/src/main/java/com/example/myapplication/day50/BridgeDiagnosisDemo.kt)。本例是固定日志案例选择器，不触发真实崩溃，不运行真实 Laya 引擎，也没有采集真实 tombstone。

## 怎样写能用的 Bridge 日志？

```text
requestId=r50 protocol=1001 pageId=scene-2 stage=RECEIVE thread=bridge
requestId=r50 protocol=1001 pageId=scene-2 stage=HANDLE thread=main
requestId=r50 protocol=1001 pageId=scene-2 stage=REPLY elapsedMs=18 ok=true
```

上面是日志格式示意，不是本例真实耗时测量。时间差应使用单调时钟，比如 SystemClock.elapsedRealtime，而不是只减可能被校时影响的墙上时间。pageId/实例 id 帮助区分重建前后页面，requestId 不是 courseId，重复请求和业务对象也不是同一个身份。

必要字段：方向、请求 id、协议号/版本、阶段、线程、页面实例、结果码与耗时。不要默认打印完整 payload、Token、Cookie 或用户隐私；保留字段存在与类型等诊断信息即可。

## 按错误层次查

| 证据 | 优先检查 | 不能直接推断 |
|---|---|---|
| REJECT / JSON 解析异常 | 字段、类型、协议版本 | 引擎必然崩溃 |
| JS ReferenceError/TypeError | Console 堆栈、函数就绪、脚本加载 | Android 进程已崩溃 |
| FATAL EXCEPTION | Java/Kotlin 异常链、首个应用栈帧 | 必须改 HTML |
| 资源文件加载失败 | 路径、大小写、包版本、网络 | Native 方法一定未注册 |
| Fatal signal / lib*.so backtrace | tombstone、ABI、Build ID、符号 | 就是 Kotlin 空指针 |
| RECEIVE 后没有 REPLY | 最后成功阶段、处理器、取消/超时日志 | 自动认定网络慢或线程死锁 |

这里 C/C++ Native crash 与“原生 Android 页面”中的 Native 一词含义不同。Java/Kotlin 层也是原生 App 业务代码，但它的异常诊断方式与 C/C++ 信号崩溃不同。

## 实际排查步骤

1. 记录变体、安装 id、版本、设备、系统 WebView 版本、复现操作与时间。
2. 按正确进程筛选，先保留日志，再重复实验；不要只贴全设备系统噪声。
3. 搜同一个 requestId，找最后一个明确成功阶段和第一个明确错误。
4. Java/Kotlin 读取异常类型、Caused by 与应用源码栈帧；优化后的堆栈需要对应构建的 mapping。
5. JS 错误用 Debug WebView 的 Chrome Console；压缩脚本使用匹配版本的 source map。
6. C/C++ 崩溃保留匹配的未剥离符号与 Build ID，使用 ndk-stack/符号工具；错误版本符号不能可靠定位。
7. 修复后重放相同路径，分别报告构建、安装、启动与交互结果。

```powershell
adb devices
# 选择正确设备和包，demo 安装 id 带 .demo。
adb shell pidof com.example.myapplication.demo
adb logcat -d -b crash
# 当前进程专用日志：将实际 PID 填入；重启后 PID 会改变。
adb logcat --pid=实际PID
```

crash buffer 不是全部业务日志。设备权限可能限制 tombstone 访问，可结合 bugreport、系统崩溃报告或线上平台，不应声称 adb 在所有设备上都能直接读取完整 tombstone。

## 线程、重复和生命周期

Day43/47 的 JavascriptInterface 接收通常在 WebView 私有后台线程，更新 UI 前切 Main。收到消息不等于处理完；分发也不等于业务成功。Day49 的去重只是页面会话内示例，不是跨进程 exactly-once。

页面关闭后应移除监听/桥接、取消任务，并忽略迟到结果。日志记录页面实例和 disposed/cancelled 能帮助解释“收到了但不再回传”；取消不是普通业务错误，不应吞掉后继续使用已销毁 WebView。

Java 日志 `Log.d("Bridge", message)` 与 Kotlin 用同一套 Android Log；语言差异不改变线程与生命周期规则。真实 RxBus/Laya 调用链仍需从注册点追到处理器，不能用相似方法名代替实际证据。

## 验收

1. 点击各案例，指出属于协议、网页、托管代码、资源还是 C/C++ 层。
2. 能按 requestId 描述完整发送到应答过程。
3. 解释缺失完成日志为何还不能确定根因。
4. 区分 source map、mapping 和 Native symbols 各用于哪一层。
5. 能列出提交故障报告所需的版本、设备、复现步骤和关键日志。

构建任务仍为 `:app:assembleDemoDebug`、`:app:assembleProdDebug`。此课结束 Day46～Day50 阶段，重点是证据和定位流程，不是故意让应用闪退。
