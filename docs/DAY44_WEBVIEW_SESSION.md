# Day44 · Cookie、Token、登录态

## 前端类比和实验边界

本课把 WebView 看成一个嵌入式浏览器：网页点击“模拟登录”写入 Cookie 和 localStorage Token，Android 原生按钮读取/清理 Cookie，再通知网页删除 Token。页面使用本地 HTML，不连接服务器，不产生真实账号和凭证。

```text
网页模拟登录
  → document.cookie = day44_session
  → localStorage.setItem(day44_token)
  → WebView 保存会话
原生读取
  → CookieManager.getCookie(url)
原生退出
  → removeAllCookies 回调
  → flush
  → evaluateJavascript 删除 localStorage Token
  → reload
```

入口：首页 → Day44 → 打开页面。源码：[WebSessionDemo.kt](../app/src/main/java/com/example/myapplication/day44/WebSessionDemo.kt)，网页：[index.html](../app/src/main/assets/day44/index.html)。

## Cookie、Token、localStorage 不一样

| 项目 | 谁管理 | 作用 | 本例清理方式 |
|---|---|---|---|
| Cookie | WebView CookieManager/网页 | 随匹配请求自动发送的会话数据 | `removeAllCookies` + `flush` |
| localStorage Token | 网页 JS 存储 | 页面自己读取的持久数据 | `evaluateJavascript` 调 `localStorage.removeItem` |
| Android SharedPreferences/DataStore | Native | Native 自己的登录状态 | 本课没有写入 |

清理 Cookie 不会自动删除 localStorage；删除网页 Token 也不等于删除 Cookie。真实退出需要根据后端契约同时清理相关位置，并在服务器端使 refresh token 失效，不能只依靠客户端删除文字。

## 为什么读取要传 URL？

```kotlin
val raw = cookieManager.getCookie(PAGE_URL).orEmpty()
```

Cookie 受域名、路径、安全属性、同站策略等规则影响。`getCookie(url)` 查询的是与这个 URL 匹配的 Cookie；不能把一个页面的 Cookie 当成全局字符串随便使用。示例只把是否存在和长度显示到 Native 状态条，不把 token 内容写入 Logcat。

## 生命周期与异步清理

`removeAllCookies` 是异步 API，回调中 `removed` 只说明是否删除了匹配数据。回调可能在 Activity 结束后到达，因此先检查 `disposed`，再更新 UI。`flush()` 把内存中的 Cookie 变更写入持久化存储；它不是网络登出。

WebView 退出时仍然要 stopLoading、解除回调引用并 destroy；销毁 WebView 不等于清除 Cookie。用户没有点击退出时，本例保留会话，重新打开页面可能仍看到 Cookie；这是为了对比“页面销毁”和“登录退出”不是一回事。

## 登录恢复与网络请求

本例网页刷新后通过 `document.cookie` 和 localStorage 判断状态。真实项目的登录恢复通常是：启动 → 读取本地 refresh token/会话 → 请求服务器验证 → 成功后加载业务页，失败则清理并回到登录页。不要把“Cookie 存在”当成服务器一定认可。

涉及 HTTPS 时要理解 Secure、SameSite、域名和重定向；不要为了让测试通过全局关闭安全策略。CookieManager 的全局性还意味着多个 WebView/Activity 可能共享 Cookie，退出功能应明确影响范围。

## Java 对照

```java
CookieManager manager = CookieManager.getInstance();
manager.setAcceptCookie(true);
String cookie = manager.getCookie(url);
manager.removeAllCookies(removed -> {
    manager.flush();
});
```

localStorage 仍需通过 `evaluateJavascript` 执行网页代码；它不是 CookieManager 的 API。

## 验收实验

1. 点击网页“模拟登录”：网页显示 Cookie 和 Token 存在，原生“读取当前 Cookie”显示有 Cookie。
2. 退出登录：原生状态显示 Cookie 清理结果，网页刷新后两种状态都不存在。
3. 只旋转/退出 Activity 不点击退出：解释为什么会话可能保留。
4. Logcat 搜索 Day44，确认没有完整 token 内容。
5. 能解释 Cookie 清理、localStorage 删除、服务器登出分别解决什么问题。
6. 能说明构建成功、WebView 显示、服务器认可会话是三个不同验证层次。

构建使用 `:app:assembleDemoDebug` 或 `:app:assembleProdDebug`；本课未进行真实网络登录验证。
