# Day49 · 协议号、JSON、RxBus

## 用前端串起流程

像宿主 SDK 收到一条 JSON，先验证 action 和 payload，再交给事件处理器。本课把 Day47/48 中的消息处理拆成独立的解码函数和显式路由器，便于看清职责；没有接入真实 Laya、RxJava 或 RxBus。

入口：首页 → Day49。源码：[ProtocolDemo.kt](../app/src/main/java/com/example/myapplication/day49/ProtocolDemo.kt)。

```text
按钮模拟 Bridge 收到字符串
 → Default 线程 decodeCompletion
 → JSON 语法与字段校验
 → CourseCompleted 类型事件
 → 回到 Main 线程
 → CompletionRouter.dispatch
 → 按 requestId 去重
 → onComplete 回调
 → 业务次数加一 → Compose 显示
```

## 协议中的四个身份

```json
{"version":1,"protocol":1001,"requestId":"req-49","payload":{"courseId":"kotlin-49"}}
```

| 字段 | 负责什么 |
|---|---|
| version | 协议格式版本 |
| protocol | 动作类型，如 1001 表示课程完成 |
| requestId | 一次请求的身份，用于去重和关联响应 |
| courseId | 业务对象身份，不是消息身份 |

同一课程可以有多个不同业务请求，因此不能把 courseId 当作所有消息的去重键。同一请求重试应保留 requestId；新的请求才生成新 id。

JSON 只是传输格式，不是事件总线，也不是权限证明。先检查长度、版本、协议号、字段类型和允许值，再产生强类型事件。示例按精确类型校验整数与字符串，避免 optInt 等宽松转换掩盖错误类型。

## 路由器与 RxBus 的关系

本课 CompletionRouter 是一个显式单处理器回调路由器，不是 RxBus。类似前端把某种 action 交给指定 handler。RxBus 通常是项目基于 RxJava Subject 等能力封装的事件总线，并不是 Android 自带的固定 API，也没有所有项目通用的实现。

概念对照：

| 前端 | RxBus 常见写法 | 本例 |
|---|---|---|
| emit(event) | post/onNext | dispatch(event) |
| on(type, handler) | ofType + subscribe | 构造时传 onComplete |
| 切到 UI 调度 | observeOn(mainThread) | withContext 后回 Main |
| off/unsubscribe | Disposable.dispose | 页面协程取消、路由器随组合释放 |

真实源码里还要检查 Subject 是否序列化、是否缓存/重放、订阅线程和生命周期。不能看到 subscribe 就认为自动切到主线程；发送所在的线程与观察所在的线程由具体实现决定。

Java 概念对照：类型事件可写成普通 class，handler 可写成接口 `void onComplete(CourseCompleted event)`；Java/Kotlin 不改变 JSON、线程或去重规则。真实 RxBus 的 `subscribe/observeOn/dispose` 名称要以项目依赖为准。

## 重复消息为什么会出现？

网页重复点击、重试、重复注册监听、旋转后残留订阅都可能导致重复处理。单纯禁用按钮只能减少本页面并发点击，不能提供全链路幂等。

本例在当前页面内按 requestId 去重，最多保存 100 个，退出页面即重置；淘汰旧 id 后再次收到仍可处理。它不是跨进程、跨设备或服务器级 exactly-once 保证。实际持久化动作需要服务端/数据库唯一约束、幂等键或事务设计，且应在业务成功后确认处理状态。

## 线程与生命周期

解码放 Default 线程，路由和 Compose 状态更新回到 Main。rememberCoroutineScope 在组件离开时取消任务，CancellationException 必须继续抛出。不要用全局单例总线永久保存 Activity 回调；RxBus 订阅应在相应生命周期结束时解除，避免重复消息与泄漏。

## 验收

1. 第一次发送 req-49：业务次数为 1。
2. 再次发送：显示忽略重复，次数仍为 1。
3. 未知协议或损坏 JSON：拒绝消息，次数不增加。
4. 返回首页再进入：去重与计数重置，解释它仅属于页面会话。
5. 能区分协议号、请求 id、课程 id，以及解析、分发、业务处理三层。
6. 能说明本例与真实 RxBus 的共同职责和实现差异。

构建：`:app:assembleDemoDebug`、`:app:assembleProdDebug`。本课是本地确定性协议实验，不声称验证真实 Laya 引擎或 RxBus。
