# Day51 · 统一请求封装、Token、业务错误码

## 用前端串起来

类似 Axios 请求拦截器注入 Authorization，再由响应适配器把 HTTP、业务码与连接错误转换成统一结果。Kotlin 用 suspend 表达异步等待，用 sealed interface 表达结果分支；不是把所有请求都写进一个巨大的函数。

入口：首页 → Day51。源码：[UnifiedRequestDemo.kt](../app/src/main/java/com/example/myapplication/day51/UnifiedRequestDemo.kt)。本课只用本地假传输，不访问真实服务器，不写真实 Token，不修改之前 Retrofit/WebView 示例。

```text
按钮场景 → UnifiedRequester.load
 → DemoSession 取 Token → Authorization: Bearer ...
 → fakeTransport 校验请求头
 → 先判断 HTTP 状态
 → 再判断 body.code、data
 → RequestResult
 → UI 显示成功、失败或重新登录提示
```

## 三类错误不能混用

| 情况 | 本例结果 | 会话处理 |
|---|---|---|
| 无凭证 → HTTP 401 | LoginExpired | 清除 |
| HTTP 503 | HTTP 错误 | 保留 |
| HTTP 200 + code=1002 | 业务错误 | 保留 |
| HTTP 200 + code=40101 | LoginExpired | 清除 |
| IOException | 网络错误 | 保留 |
| 取消 | 继续抛 CancellationException | 不当成错误提示 |

HTTP 200 只是传输层状态，不代表业务成功。0、1002、40101 都是本课自定义协议，真实项目必须读取后端约定，不能假定所有后端都这样编码。HTTP 403 也不能不分原因就当作 Token 过期。

## Token 和 Cookie 的边界

本课用 Authorization 请求头，Day44 使用 WebView CookieManager，两者不会因为都叫“登录态”就自动同步。Token 是凭证数据，Cookie 是浏览器会话传输/存储机制之一，localStorage 是另一种存储。

真实工程通常由 OkHttp Interceptor 添加请求头，Retrofit 接口提供结构化请求，Repository 负责业务转换。不要在每个按钮里拼 Token，也不要让拦截器直接持有 Activity 弹登录页。认证状态可交给应用级 Session/ViewModel，UI 决定导航。

Java 思路相同：请求头 map 或 Interceptor 注入 Token，if/switch 判断业务码，用结果类/回调回 UI；语言不改变 HTTP 与业务协议的分层。Kotlin 的 suspend 不表示自动使用 IO 线程，本例 delay 不阻塞，真实阻塞 IO 需正确调度或使用支持挂起的网络 API。

## 并发失效和刷新不能省略设计

本例禁用并发按钮，Token 仅属于页面内存，退出页面即重置。生产中旧请求返回 401 可能晚于新登录，需要会话版本/Token 快照比较，避免清掉新会话；多个请求同时失效还需要聚合通知，防止重复跳转。

Refresh Token、单飞刷新、重放上限、POST 幂等和刷新失败退出，本课只介绍边界，没有实现。不要对所有 401 无限制递归重试，也不要自动重放可能产生重复业务的请求。日志不打印 Authorization、Cookie、Token 或完整敏感响应。

## 验收实验

1. 未登录点成功：实际返回 HTTP 401，提示重新登录。
2. 模拟登录后点成功：得到 Kotlin 课程。
3. HTTP 503 与业务错误显示不同提示，且保留会话。
4. 业务登录失效清会话，再点任意请求变成 HTTP 401。
5. 重新登录后网络失败：显示网络提示，会话仍在。
6. 能解释按钮 → 请求头 → 假传输 → HTTP → 业务码 → UI 的链路。

构建仍用 `:app:assembleDemoDebug`、`:app:assembleProdDebug`。本课不声称完成真实认证服务、持久化凭证或刷新协议验证。
