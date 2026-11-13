# Day74 · Application 启动链

入口：首页 → Day74 · Application 启动链。
源码：[StartupChainDemo.kt](../app/src/main/java/com/example/myapplication/day74/StartupChainDemo.kt)。

## 启动顺序

```text
进程创建
 → Application.onCreate
 → Launcher Activity.onCreate
 → setContent / Splash
 → 读取登录态
 → 登录页或主页面
```

`Application` 是进程级对象，适合初始化日志、依赖容器和进程范围配置。它不等于 Activity，不能持有 Activity、View、Context 的短生命周期引用，也不应在这里直接跳页面或显示业务 UI。进程被系统回收后，Application 会重新创建，不能用单例假设进程永远存在。

本课的 `LearningApplication` 只记录进程号，帮助 Logcat 对照 Application 与 Activity 的先后。真实工程若在 Application 初始化 SDK，应明确耗时、线程、失败策略和是否必须阻塞首屏；不要把所有初始化都塞到 onCreate。

## Splash 的职责

本例用 Compose 的短暂状态模拟 Splash，800ms 后进入下一阶段。它只是教学占位，不是实际系统 SplashScreen API 的完整接入。

```text
Splash 可见
 → 不显示业务按钮
 → 完成最小启动准备
 → 根据登录态进入登录/主页面
```

生产项目应让 Splash 尽快结束，避免用固定延迟掩盖真实初始化。需要等待的任务应有明确超时、取消和错误页面；Splash 不应成为无限 loading。

## 登录态与主页面

本课的 `loggedIn` 只是当前 Composable 的教学状态，模拟登录和退出，不保存账号、Token、密码，也不访问网络。真实登录态应由可靠的存储和服务端会话决定，启动时读取后再选择路由；Token 过期、账号切换、撤销和网络失败都需要独立处理。

主页面依赖登录结果，但不代表登录页面销毁就能停止 Application 初始化。Activity、Compose 组合、ViewModel、Repository 与 Application 的职责要分开：Application 提供进程级依赖，ViewModel 管页面状态，Repository 负责数据来源。

## 为什么启动链要有多个层次

| 层次 | 生命周期 | 适合职责 |
|---|---|---|
| Application | 进程 | 进程级初始化、依赖容器 |
| Activity | 窗口 | 入口、系统回调、页面宿主 |
| Compose 组合 | 当前 UI 位置 | 渲染和 UI 状态 |
| 登录态存储 | 业务持久化 | 会话信息和过期策略 |

不要用 Application 持有 Activity 来“方便跳转”，不要用页面布尔值证明服务端已登录，也不要把 Splash 的显示时间当成数据加载完成证据。

## 验收

1. 从首页进入 Day74，观察 Splash 后出现登录页面。
2. 点击模拟登录，观察主页面；再退出回到登录页。
3. Logcat 搜索 Day74，确认 Application 初始化早于 Activity 内容显示。
4. 返回首页再进入，区分页面重新创建与进程级 Application 是否重建。
5. 旋转页面，说明本例状态是教学状态，未模拟真实账号持久化。
6. 能解释 Application、Splash、登录态和主页面各自的责任。

本课未接真实登录、Token、服务端或系统 Splash API；设备点击结果需要单独验证。
