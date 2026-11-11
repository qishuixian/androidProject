# Day53 · WebSocket 连接与重连

## 前端类比与流程

类似浏览器 `new WebSocket(url)` 后监听 open/message/error/close，但 Android 通常通过 OkHttp 等客户端回调管理。两端都需要区分连接状态、意外断线和主动关闭，而不是只用一个 isConnected。

入口：首页 → Day53。源码：[SocketDemo.kt](../app/src/main/java/com/example/myapplication/day53/SocketDemo.kt)。本课只模拟状态机、心跳与退避，不建立真实网络连接。

```text
连接按钮 → CONNECTING → OPEN → 模拟 ping/pong
                         ↓ 意外断线
                    WAITING_RETRY
                         ↓ 退避等待
                    CONNECTING
手动关闭/离开页面 → 取消任务，不重连
```

## 示例规则

- 只允许一个连接循环，防止重复点击建立多个连接与心跳任务。
- 每秒模拟 ping/pong；真实客户端应基于 pong/业务响应时间判断超时，不能“定时器还在跑”就当作连接健康。
- 连接失败或断开后等待 500/1000/2000ms，最多重试三次；本例预算在本次手动连接内累计，连接成功不立即清零。
- 生产一般使用带上限的指数退避和随机抖动，避免大量客户端同时重连；本例不用随机数，以便观察固定结果。
- 手动关闭取消同一 Job，等待中的重连也被取消；返回首页同样由组合作用域取消。

## 真实客户端映射（示意，不是本例代码）

```kotlin
val request = Request.Builder().url("wss://example.invalid/socket").build()
val socket = client.newWebSocket(request, object : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) { /* 已握手 */ }
    override fun onMessage(webSocket: WebSocket, text: String) { /* 校验协议后分发 */ }
    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) { /* 分类后重连 */ }
    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) { /* 根据主动/被动关闭决定 */ }
})
```

Java 同样继承 WebSocketListener 重写这些回调。回调线程以客户端约定为准，更新 UI 要调度到 Main。`newWebSocket` 返回对象不代表握手成功；send 成功入队也不等于服务端已处理，业务确认协议在下一课继续。

## 登录、连接与生命周期

握手可能需要 Day51 的 Token，登录失效应按服务端规则停止或刷新凭证，不能对所有鉴权错误无限重连。使用 wss，日志不打印 Token 或完整敏感消息。

本课作用域属于页面：离开组合取消任务；按系统 Home 只是 Activity 进入后台，不一定取消组合。真实项目要明确前台聊天、跨页面会话或后台保活的需求，再选择 ViewModel、应用级管理器或合规后台机制，不要无限保存 Activity。

协程取消不自动等于真实 socket.close：接入客户端后还需显式关闭/取消连接、停心跳、注销监听。优雅 close 与立即 cancel 不同；重连产生新实例后还要过滤旧实例的迟到回调，避免把新连接误标成断开。

## 验收

1. 连接后看到 OPEN 与模拟心跳增加。
2. 意外断开后进入退避，再回 OPEN。
3. 打开后续握手失败开关再断开，观察三次重试后停止。
4. 等待重连时手动关闭，确认不会继续重连。
5. 返回首页再进入，状态回到 IDLE；按 Home 的行为与返回首页不同。
6. 能区分模拟心跳、真实 pong、连接建立与业务消息确认。

构建：`:app:assembleDemoDebug`、`:app:assembleProdDebug`。本课未连接真实服务器，未验证真实网络切换或 WebSocket 帧。
