# Day41 · WebView 加载与生命周期

## 先用前端概念串起来

Activity 是原生页面外壳，WebView 是外壳中的浏览器控件，HTML 是控件加载的内容。可以借助 iframe 理解“原生页面嵌入网页”，但 WebView 是真实 Android View，不是浏览器 DOM 里的 iframe。

```text
首页 Day41 按钮
 → Intent 打开 WebLessonActivity
 → 创建状态 TextView 和 WebView
 → 设置 WebViewClient
 → loadUrl 本地 index.html
 → onPageStarted / onPageFinished
 → 点击 HTML 链接进入 detail.html
 → 系统返回调用 goBack
 → 网页历史为空时 finish Activity
 → onDestroy 释放 WebView
```

入口：首页 → Day41 → 打开本地 WebView。源码：[WebViewDemo.kt](../app/src/main/java/com/example/myapplication/day41/WebViewDemo.kt)。网页位于 `app/src/main/assets/day41/`，无需网络。所有变体共用 main 中的网页。

## 谁负责什么？

| 对象/API | 职责 | 前端近似理解 |
|---|---|---|
| Activity | 创建、暂停、销毁宿主 | 外层应用页面 |
| WebView | 承载网页、保存导航历史 | 内嵌浏览器 |
| loadUrl | 请求加载指定地址 | 导航到一个 URL |
| WebViewClient | 网页导航、加载与部分错误回调 | 导航钩子、加载事件 |
| HTML | 页面实际内容 | 已熟悉的网页 |

`file:///android_asset/day41/index.html` 对应 APK 中 assets/day41/index.html，不是电脑绝对路径，也不是 res/raw。assets 没有每个文件对应的 R 编号。

## 加载回调不是宿主生命周期

`onPageStarted` 表示主页面开始加载；`onPageFinished` 表示主框架加载完成，不保证网页所有业务请求、图片绘制或 JS 异步工作都已完成。错误场景也可能继续触发 onPageFinished，生产代码应单独维护错误状态，不能只凭此回调宣告业务成功。

`shouldOverrideUrlLoading` 返回 false 让 WebView 继续默认加载，true 表示应用接管或拦截；不要对同一导航再 loadUrl 然后返回 false。本课严格只允许两页本地地址，首次直接调用 loadUrl 不保证进入此回调。

`onReceivedError` 可能报告子资源失败，因此使用 request.isForMainFrame 区分整页错误。HTTP 404/503 属于另一类 HTTP 状态回调，不可假定都由 onReceivedError 报告；网络错误专题以后继续深入。

## 网页返回与 Activity 返回

```kotlin
if (webView.canGoBack()) webView.goBack() else finish()
```

先查 WebView 的页面历史，不是 FragmentManager 返回栈。详情页 → 系统返回 → 网页列表；再返回 → 退出 WebLessonActivity → 原 Compose Day41 页面。

## 暂停、旋转和销毁

- onPause/onResume 将宿主状态通知 WebView；onPause 不会彻底暂停全部 JavaScript。
- pauseTimers/resumeTimers 会影响进程内所有 WebView，本例不使用，避免影响其他页面。
- saveState/restoreState 保存和恢复导航历史，不是完整 DOM/JS 运行内存快照，也不是长期持久化。
- 旋转时创建新 Activity 和新 WebView，恢复导航；不要每次 onResume 都 loadUrl，否则返回前台会重复刷新。
- onDestroy 中停止加载、解除持有旧 View 的回调、移除父子关系，再 destroy；之后不能再调用旧实例。
- WebView 不放入 ViewModel、单例或静态变量，防止持有旧 Activity。

## Java 对照

```java
WebView webView = new WebView(this);
webView.setWebViewClient(new WebViewClient());
webView.loadUrl("file:///android_asset/day41/index.html");
// 用户返回时判断 WebView 历史。
if (webView.canGoBack()) webView.goBack();
else finish();
```

Kotlin 的 `webView.webViewClient = ...` 对应 Java setter；object : WebViewClient() 是匿名子类。页面中没有 JavaScript Bridge，默认不启用 JavaScript；下一课再学习 Native 调 JavaScript。

本地 file URL 用于受控入门实验，不作为加载任意外部内容的通用方案；后续复杂离线站点可学习 WebViewAssetLoader 的 HTTPS 地址映射。

## 验收实验

1. 无网络进入页面，仍可打开本地首页与详情。
2. 顶部是原生状态条，下面是 HTML，能指出各自代码位置。
3. 点击详情后返回，先退网页，再退出 Activity。
4. Logcat 搜索 Day41，区分 Activity onResume 与网页加载事件。
5. 在详情页旋转，观察新 WebView 恢复导航，而非始终强制首页。
6. 按 Home 再回来，不主动 loadUrl；退出时看到 WebView destroyed。

构建命令仍是 `:app:assembleDemoDebug` 或 `:app:assembleProdDebug`。构建通过不等于设备上的 WebView 渲染和返回键实验已经验证。
