# Day45 · WebView 白屏、调试和安全

## 先按前端排错思路分层

看到白屏，不要立即改 JavaScript 或清缓存。先问页面在哪一层失败：

```text
Activity 是否启动
 → WebView 是否创建并放进父布局
 → URL 是否正确、assets 文件是否打包
 → WebViewClient 是否收到主页面开始/结束/错误
 → HTML 是否能渲染
 → JS/接口/网络资源是否继续失败
```

入口：首页 → Day45 → 打开诊断页。源码：[WebViewDiagnosticsDemo.kt](../app/src/main/java/com/example/myapplication/day45/WebViewDiagnosticsDemo.kt)，正常页面：[index.html](../app/src/main/assets/day45/index.html)。

## 示例中的三种验证

1. **加载正常页**：检查 Activity、WebView、assets 路径和基本渲染。
2. **加载错误页**：请求不存在的主资源，观察 `onReceivedError` 和状态条。
3. **调试开关**：Debug 构建显示 `WebView.getWebContentsDebuggingEnabled()`，代码只在 `BuildConfig.DEBUG` 时开启远程调试。

本地错误页不等同于 HTTP 404；`file:///android_asset/.../missing.html` 是资源读取失败练习。网络 HTTP 状态、证书错误、DNS、JS 异常还需分别看对应回调和 Chrome Console。

## 回调该怎么看？

| 回调 | 能说明什么 | 不能单独说明什么 |
|---|---|---|
| `shouldOverrideUrlLoading` | 某次导航请求到达拦截点 | 首次 `loadUrl` 一定会经过它 |
| `onPageStarted` | 主框架开始导航 | 页面一定可见或一定会成功 |
| `onReceivedError` | 某资源出现加载错误 | 每种 HTTP 状态都只由它报告 |
| `onPageFinished` | 主框架导航阶段结束 | JS 异步业务、图片、接口全部成功 |

`onReceivedError` 可能来自子资源，所以本例检查 `request.isForMainFrame`；否则图片失败也可能把整个页面标成白屏。实际应用应同时记录 request URL、isForMainFrame、errorCode 和页面状态，避免只留一句“加载失败”。

## Chrome 调试与构建边界

Debug 时调用：

```kotlin
if (BuildConfig.DEBUG) WebView.setWebContentsDebuggingEnabled(true)
```

然后设备开启 USB 调试，在桌面 Chrome 的 `chrome://inspect` 查看 WebView 页面和 Console。需要选择实际设备进程，不能把 Android Studio Logcat 当作网页 Console。Release 不应无条件打开远程调试；`BuildConfig.DEBUG` 是构建配置，不是服务器环境是否生产的充分证明。

## 安全白名单

本例允许固定的两个 `file:///android_asset/day45/...` 地址，其他导航返回 true 拦截。白名单是导航控制，不是完整安全模型：还需考虑重定向、iframe、子资源、文件访问、Cookie、混合内容和桥接对象。

不要把任意 URL 都交给带 `addJavascriptInterface` 的 WebView，也不要以为 `exported=false` 能解决网页来源问题。Day43 的桥接页面和 Day45 的诊断页面都只加载 APK 内置内容，减少不可信来源。

如果业务页面需要网络，使用 HTTPS、明确域名允许范围、处理证书/网络错误，不要为了排查白屏全局关闭安全校验。复杂离线资源可学习 WebViewAssetLoader，把资源映射到受控 HTTPS 地址。

## 常见误判

- Activity 启动失败：看 Android `FATAL EXCEPTION`，不是网页回调。
- WebView 空白：先确认 `setContentView`、布局尺寸非 0、URL 和 assets 文件路径。
- `onPageFinished` 有日志但页面空：可能 HTML/CSS/JS 自身问题，或内容颜色与背景相同。
- 只有网络图片失败：不能直接标记主页面失败。
- Release 看不到 Chrome 调试：可能是代码按设计未启用，不等于页面渲染失败。
- 修改 `app/build/...`：那是生成物，源码应改 `src/main` 或对应 source set。

## Java 对照

```java
webView.setWebViewClient(new WebViewClient() {
    @Override public void onPageFinished(WebView view, String url) {
        // 主框架阶段结束，仍需检查网页业务状态。
    }
});
if (BuildConfig.DEBUG) WebView.setWebContentsDebuggingEnabled(true);
```

## 验收实验

1. 正常页显示标题，状态条出现完成信息。
2. 错误页触发主页面错误状态，Logcat 搜索 Day45。
3. 说明为什么 `onPageFinished` 不等于接口成功。
4. Debug 构建可在 `chrome://inspect` 找到页面；不要把生产调试开关打开。
5. 能解释白名单返回 true/false 的含义和它的安全边界。
6. 按 Activity、WebView、资源、网页、网络/JS 分层报告问题。

构建任务：`:app:assembleDemoDebug`、`:app:assembleProdDebug`。本课只做本地页面构建和诊断，未进行真实线上 WebView 验证。
