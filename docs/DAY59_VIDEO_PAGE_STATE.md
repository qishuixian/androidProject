# Day59 · 音视频页面状态

## 先用前端概念串起来

把 SurfaceView 类比为承载播放器的 DOM 节点，Surface 类比为节点背后的可绘制目标。保存用户 ID 类似保存路由参数：页面重新挂载后可以找到同一个用户，但旧 DOM、播放器实例和绘制目标不能直接复用。

入口：首页 → Day59 · 音视频页面状态。
源码：[VideoPageStateDemo.kt](../app/src/main/java/com/example/myapplication/day59/VideoPageStateDemo.kt)。

本课使用真实 Android SurfaceView 和生命周期通知，但绑定器仅打印日志，没有 SDK、摄像头、网络连接或视频帧。Surface 尺寸就绪后会绘制蓝灰色背景和“Surface 已就绪”文字，仅用于确认画布可以绘制，不能当成真实视频播放验证。

## 从进入页面到显示目标的完整链路

```text
进入 Day59
 → rememberSaveable 创建或恢复 uid、showSurface
 → AndroidView.factory 创建 SurfaceView
 → 系统调用 surfaceCreated
 → 保存当前临时 Holder
 → Surface 存在 + 宿主 RESUMED + 页面显示画布
 → 模拟绑定当前 uid
 → 切换用户：解绑旧 uid，再绑定新 uid
```

factory 是创建 View；surfaceCreated 是底层绘制资源就绪。两者不是同一时刻。surfaceChanged 通知实际尺寸，真实 SDK 常需根据尺寸调整适配策略；本课只记录日志。用户加入频道也不代表 Surface 已就绪，更不代表已收到视频首帧。

## 代码里的状态各管什么

| 字段 | 保存方式 | 原因 |
|---|---|---|
| uid | rememberSaveable | 小型身份字段，可随 Activity 重建恢复 |
| showSurface | rememberSaveable | 用户是否展示画布的页面选择 |
| holder | remember | 当前 View 的运行期引用，销毁就失效 |
| resumed | 根据 Lifecycle 重新读取 | 前后台状态由系统决定，不恢复历史值 |
| binding | remember | 本次组合的模拟绑定器，必须重新创建 |

`var uid by rememberSaveable { mutableStateOf("1001") }` 中，mutableStateOf 创建状态容器，by 让读写 uid 等同于读写容器的 value，rememberSaveable 让可保存字段参与恢复。它不能保存 Surface、Context、View 或 SDK 实例。

## 为什么有多个清理入口

- onPause：本课选择暂停即解绑；画布可能仍存在，不必等 surfaceDestroyed。
- surfaceDestroyed：底层资源即将失效，必须立即停止使用；不能只等待 Compose 下一轮重组。
- DisposableEffect.onDispose：用户变化、条件变化或离开组合时，清理旧绑定。
- AndroidView.onRelease：View 被组合释放时再次兜底清理。

这些回调不应被理解为永远固定的一条顺序。解绑设计为幂等，多次调用也安全。真实 SDK 可能在后台保留音频、停止视频或完全离开频道，策略必须依据产品与 SDK 契约。本例的日志解绑不等同于真正停止一个跨线程渲染器；接 SDK 后还要满足其同步与线程要求。

## 旋转时具体发生什么

```text
旧 Activity 暂停 / 销毁旧 View 与 Surface / 清理绑定
 → 新 Activity 与组合创建
 → 恢复 uid、showSurface
 → 创建新的 SurfaceView 和 Surface
 → 新宿主恢复前台
 → 重新绑定当前 uid
```

先后细节以设备日志为准。恢复 uid=1002 只代表页面恢复选择，实际项目还要重新确认账号、频道、远端在线和视频发布状态。频道连接可以按架构由 ViewModel 或服务管理，但不要让 ViewModel 持有 Activity、SurfaceView 或旧 Holder。

rememberSaveable 不等于数据库持久化；用户移除页面、强制停止或重新启动等场景不能一概保证恢复。长久保存用数据库或 DataStore。进程死亡后也不会保留真实 SDK 实例。

## Java / Kotlin 对照

Java 的 `holder.addCallback(new SurfaceHolder.Callback() { ... })` 对应本例 Kotlin 的 `object : SurfaceHolder.Callback`：都是创建实现接口的匿名对象并注册监听。Java 常在 Activity 的 onResume/onPause 中配对处理，本例通过 LifecycleEventObserver 观察宿主事件，再用 DisposableEffect 管理注册和移除。

## 动手验证

1. 进入 Day59，应看到标题、按钮及蓝灰色占位画面。Logcat 搜索 Day59：观察 surfaceCreated、surfaceChanged 与模拟绑定日志。
2. 切换用户：观察旧用户解绑、新用户绑定。
3. 移除画布再重建：观察真实 Surface 销毁与新建，用户 ID 不变。
4. 按 Home 再返回：观察暂停解绑、恢复后重新绑定；不假定 Surface 一定销毁。
5. 切换到1002后旋转：ID 应恢复；观察新 Surface 与重新绑定日志。设备需允许旋转。
6. 返回首页：应清理观察者和绑定，再次进入创建新的页面实验。

## 学完应能说明

- Activity、Compose 组合、Surface 的生命周期不是同一个生命周期。
- 保存用户 ID 与保存运行期渲染资源的区别。
- 旋转恢复页面选择后，为什么仍需重新绑定画布。
- 用户存在、Surface 就绪、收到首帧之间的区别。
- 从按钮更新状态到旧绑定清理、新绑定建立的完整链路。

## 如果看到黑屏

旧版未向 Surface 提交任何画面，中间画布可能呈现黑色；新版在 surfaceChanged 中通过 lockCanvas / unlockCanvasAndPost 成对绘制静态提示。它不代表收到远端首帧。接入真实 SDK 后应移除这段占位绘制，避免与 SDK 争用 Surface。

若标题、按钮也全部不可见，这是整页显示异常，不能用“没有视频帧”解释；需要结合对应设备截图、Activity 和 Logcat 继续定位。
