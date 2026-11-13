# Day71 · 前台 Service

入口：首页 → Day71 · 前台 Service。
源码：[ForegroundServiceDemo.kt](../app/src/main/java/com/example/myapplication/day71/ForegroundServiceDemo.kt)。

接 Day70：普通通知只是系统提示，前台 Service 则是应用组件承担任务并绑定持续通知。Day72 再学习 MediaProjection；本课不提前获取录屏、相机或麦克风能力。

## 从前端概念理解

可以把页面理解为组件，把 Service 理解为 Android 系统管理的应用组件：它不依附某个 Compose 子树，因此页面离开不必结束任务。但它不是浏览器 Service Worker，也不是自动创建的后台线程。

Service 的 onCreate、onStartCommand、onDestroy 默认在应用主线程。阻塞 IO、死循环和重计算仍会卡住 UI。本课协程使用 Main，delay 挂起等待不阻塞；真实 IO/计算需选择合适调度器，并设计取消与资源释放。

## 实验范围

真实启动前台 Service，真实绑定和更新通知，30秒后停止；页面按钮和通知动作都可停止。任务只是本地每秒倒计时，没有下载、同步、录音、录屏、网络或无限保活。

Android14+ 使用 shortService，Manifest 声明 FOREGROUND_SERVICE 基础权限及 shortService 类型。该类型没有额外的类型专属权限；其时限约三分钟，实际以系统契约为准。本课30秒自动结束，不故意触发系统超时。

API24/25 没有通知渠道；API26+ 创建渠道。API34 以下没有本课 shortService 类型限制，但示例仍自行限时30秒。不同类型如 microphone、mediaProjection 有各自前置条件和权限，不能把 shortService 当其他任务的绕过方案。实际发布还需满足相应平台与分发渠道政策。

## 启动完整链

```text
可见页面点击启动
 → ContextCompat.startForegroundService
 → 系统检查当前启动资格
 → 首次实例 onCreate
 → 创建稳定通知渠道
 → onStartCommand
 → 立即 ServiceCompat.startForeground + 有效通知
 → 再启动30秒协程任务
 → 每秒更新同一个通知与本进程观察状态
 → 完成后 stopForeground + stopSelf
 → onDestroy 取消作用域
```

startForegroundService 是请求，不代表提升前台已成功。启动后必须在系统允许的很短时间内调用 startForeground，不能先等网络、权限弹窗或长初始化。本课直接在 onStartCommand 中执行，不依赖页面继续存活。

页面异常反馈分两层：启动请求可能被拒绝；服务创建后提升前台也可能失败。都不能把本地 running=true 当成功证据。

## 三个相似 API 的区别

| API | 做什么 | 不代表什么 |
|---|---|---|
| startForegroundService | 请求启动将提升前台的服务 | 已经前台或已经完成任务 |
| Service.startForeground | 绑定有效通知并提升为前台服务 | 获得无限执行和任意硬件权限 |
| stopForeground | 移除前台状态，可移除通知 | Service 已停止 |
| stopSelf / stopService | 请求停止服务组件 | 所有平台强杀都会回调 onDestroy |

结束时不能只移除通知而忘了停止服务。本课每条停止路径都取消任务并最终 stopSelf/stopService。通知使用与 Day70 不同的渠道/id，避免互相覆盖。

## Service 生命周期不等于 Activity

```text
首次启动：onCreate → onStartCommand
重复启动：同一实例再次 onStartCommand
正常停止：onDestroy
```

本课不调用 bindService，onBind 返回 null。重复点击启动不创建多份协程，也不重置30秒业务计时。onStartCommand 返回 START_NOT_STICKY，表示不要求系统在杀掉服务后自动重建本课任务；它不是“系统永不停止”的承诺。

返回首页或按Home不会由 Composable 主动停止。旋转可能重建 Activity，服务组件可继续运行；进程被系统回收、用户从系统任务管理停止、强行停止或厂商策略则可能结束服务。onDestroy 不保证在强杀时执行，因此真实重要任务需持久化必要进度并设计恢复，不靠 finally 或 onDestroy 保证写盘。

## 前台不等于无限后台权限

Android12+ 对后台启动前台服务有限制，只有特定豁免允许。本课由前台可见 Activity 的明确点击启动，不做后台广播、开机或定时偷偷拉起。

Android14+ 要根据实际用途声明类型，某些类型还有 while-in-use 权限与后台启动条件。系统限制随 targetSdk、OS 和类型变化，不要把一个版本能启动推断成所有系统都允许。短任务应尽快结束；可延迟任务通常应评估 WorkManager 等机制，不为了保活随便上前台服务。

shortService 超时会回调 onTimeout(startId)，必须尽快停止，否则可能触发 ANR。仅 stopForeground 不够，本例还取消任务并 stopSelf。正常实验30秒已停止，超时处理只是系统兜底，未主动验证超时行为。

## 通知权限与前台服务的关系

Android13+ POST_NOTIFICATIONS 可以在 Day70 申请。本课不把它作为所有前台服务启动的绝对前提：用户拒绝通知权限时，前台服务仍必须提交有效通知；通知栏可能不展示该通知，但系统活动应用/任务管理界面仍可能显示服务。

拒绝授权不代表可以省略 startForeground 的通知，也不保证通知的停止按钮可见。页面仍提供停止入口，任务本身限时自动停止。用户关闭渠道或静音不应被反复重建渠道绕过。

通知正文通过 immutable PendingIntent 打开 MainActivity，沿用原有首页路由，未承诺自动打开 Day71。停止动作直接向内部 Service 发送固定 STOP 命令，不拉起 Activity；这不属于 Day70 所述“用 Service 当跳转中转站”。exported=false 限制组件外部直接启动入口，应用创建的 PendingIntent 可代表本应用执行明确动作。

## 页面怎样知道服务真的执行了

ServiceObservation 用本进程 StateFlow 发布生命周期和计时信息，Compose 使用 collectAsStateWithLifecycle 展示。页面离开组合不取消 Service 作用域；Service.onDestroy 才取消其协程。

该观察器不是 bindService，不是跨进程查询或持久化数据库。进程重建后初始文字是“尚未收到生命周期事件”，不等于证明系统全局没有服务。本课默认 Service 与 Activity 同进程，不使用 android:process。

日志 Day71 带服务实例 id 和线程名，可对照重复启动、页面重建和服务销毁。状态从真实回调产生，启动按钮只记录请求。

## 验收清单

1. 可见页面点击启动，检查 onCreate/onStartCommand、实例 id、线程与真实通知。
2. 连点启动，确认同一任务，不出现两份倒计时；本课计时不因点击重新开始。
3. 按Home再返回，30秒内任务可继续；到期后通知移除、状态出现 onDestroy。
4. 运行中点击页面停止，确认通知和任务结束；未运行时停止不创建新服务。
5. 再启动，从通知的停止动作结束，核对生命周期日志。
6. 点击通知正文回应用，观察返回栈与原路由，不假定必须回Day71。
7. 在Android13+允许/拒绝通知权限分别观察通知栏和系统任务管理；记录系统实际表现。
8. 旋转时对照Activity与Service实例；页面重建不应创建额外计时任务。
9. API34+确认Manifest类型与运行类型一致，不通过超过时限来验证“保活”。

真机、系统版本、通知权限、渠道状态、前后台操作和系统停止行为应分别记录。构建通过不能替代通知实际展示或后台行为验证。清理实验时主动停止服务或等待30秒。

## 学完应能说明

- Service为什么不是后台线程，页面生命周期为什么不等于服务生命周期。
- 启动请求、提升前台、绑定通知、停止前台状态和停止组件的区别。
- 后台启动限制、shortService时限及系统超时的处理责任。
- 通知权限拒绝为什么不等于无需提供前台通知。
- StateFlow观察、服务作用域与任务取消的完整链路。
