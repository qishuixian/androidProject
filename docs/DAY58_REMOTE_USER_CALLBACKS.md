# Day58 · 远端用户回调

## 前端流程类比

类似订阅聊天室的成员事件：join 更新成员列表，leave 移除成员，quality 更新某人的状态。SDK 通知不一定在主线程，也可能晚于退出频道，所以不能在任何回调里都无条件 push 用户。

入口：首页 → Day58。源码：[RemoteUserDemo.kt](../app/src/main/java/com/example/myapplication/day58/RemoteUserDemo.kt)。本例只用协程模拟后台回调，没有真实音视频 SDK、网络质量测量、视频帧或设备采集。

```text
按钮模拟 SDK 事件
 → 捕获当前 generation
 → Default 线程等待并产出事件
 → 返回 Main
 → 检查频道仍加入且代次匹配
 → 按 uid 更新远端列表
 → Compose 显示
```

## 三个回调分别做什么？

| 事件 | 本例动作 | 不代表什么 |
|---|---|---|
| 远端加入 | 按 uid 新增，重复则忽略 | 不代表已发布视频或有首帧 |
| 远端离开 | 移除对应 uid | 不等于本地用户退出频道 |
| 网络质量 | 更新已有用户质量 | 不等于远端已离开，不可凭它创建幽灵用户 |

真实 SDK 中网络质量可能包含上下行、不同方向和 unknown/offline 等枚举，语义依版本文档。本例“差”是固定文字，没有测速。

## 为什么同时检查 joined 和 generation？

只检查 joined 不够：旧频道事件还在等待时，用户退出又加入，此时 joined 已恢复 true，旧回调可能污染新列表。generation 在退出/重新加入时变化，回调必须携带发送时捕获的代次。

真实工程可用频道 id + 连接/引擎实例 id + 请求代次，并在退出时注销监听、取消任务。代次过滤不能替代 SDK 的真实资源释放；uid 也通常应与频道或账号范围组合，不假定跨频道永久唯一。

本课故意不在退出按钮取消待处理模拟事件，便于观察“旧事件被过滤”；离开整个 Compose 页面时 rememberCoroutineScope 会取消任务。这是实验安排，不代表生产中可以永远留下旧监听。

## 线程

按钮点击从 Main 发起，withContext(Default) 模拟后台回调，结束后返回 Main 更新状态。Logcat 搜索 Day58 对照两条线程日志。真实 SDK 回调线程不是由 Kotlin/Java 语言决定，必须查契约；不能把本例 Default 线程名当成某个厂商 SDK 的实际行为。

Java/Kotlin 对照思路：Java Listener 的 onUserJoined/onUserOffline/onNetworkQuality 等名称依 SDK 而定，内部使用主线程 Handler 或 Activity.runOnUiThread 交给统一状态处理器。不要在不同回调线程并发修改普通 ArrayList。

## 页面渲染与成员存在是两回事

远端用户存在、订阅媒体成功、远端摄像头已打开、收到首帧、渲染 View 已准备好，是不同条件。用户离开应解绑并清理对应渲染器；用户未发布视频时可以显示头像或占位，不要假装已收到画面。Surface 生命周期在 Day59 继续。

## 验收

1. 点击加入多次：uid=1001 只有一行。
2. 更新质量：已有用户变差；没有用户时不会新增。
3. 点击离开：移除该用户，重复离开也安全。
4. 点击延迟事件，立即退出再加入：旧事件被忽略，不污染新会话。
5. Logcat 对照后台事件与 Main 状态更新。
6. 能区分成员加入、视频首帧、网络质量和本地频道状态。

构建：`:app:assembleDemoDebug`、`:app:assembleProdDebug`。尚需真实 SDK/设备验证才能判断实际音视频行为。
