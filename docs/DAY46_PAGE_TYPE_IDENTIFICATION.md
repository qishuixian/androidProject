# Day46 · Native、H5、Laya 页面识别

## 为什么先学识别

真实项目中看到一个“像网页”的页面，不能只凭文件名或目录名断言它是 Laya。要从 Activity、容器、资源路径、引擎初始化和实际调用链收集证据。本课当前工程没有 Laya SDK，所以使用普通 H5 和一个名字像 Laya 的本地目录做对照，并明确标注“模拟”。

入口：首页 → Day46。源码：[PageTypeDemo.kt](../app/src/main/java/com/example/myapplication/day46/PageTypeDemo.kt)。

## 三类对象的关系

| 看到的对象 | 能证明什么 | 不能证明什么 |
|---|---|---|
| `Activity`、`TextView`、`Button` | 当前有 Native 页面层 | 页面没有 H5/Laya |
| `WebView` | 当前有网页承载容器 | WebView 内一定是普通 H5 或 Laya |
| `assets/*.html` | APK 有本地网页资源 | 资源使用了 Laya 引擎 |
| `laya/` 目录名 | 只能说明命名 | 没有 runtime、引擎初始化和资源调用证据 |
| Laya runtime 初始化日志/类 | 更强的引擎证据 | 仍需确认当前页面实际加载了它 |

前端类比：`iframe` 只证明存在嵌入文档，不能证明文档使用了 Vue、Canvas 引擎或某个游戏框架；要看脚本、runtime 初始化和实际渲染调用。

## 当前实验的调用链

```text
Compose Day46 按钮
  → Intent
  → PageTypeActivity（Native）
  → 原生 TextView/Button
  → WebView
  → loadUrl(file:///android_asset/day46/h5.html 或 laya/index.html)
  → WebViewClient.onPageFinished
  → 根据已知资源路径显示候选结论
```

点击“加载普通 H5 资源”只能确认 WebView 加载了普通 HTML。点击“加载模拟 Laya 资源”只能确认目录名称像 Laya；页面文字故意提醒没有真实 runtime，不能把它当作 Laya 页面。

## 真实 Laya 页面应继续找什么？

在目标项目中按以下顺序查：

1. Activity/Fragment 是否创建 WebView、SurfaceView 或游戏引擎容器。
2. `loadUrl`、`loadData`、资源入口或引擎启动类在哪里调用。
3. assets/压缩包中是否有 Laya 运行时脚本、版本文件和入口资源。
4. Native 是否在页面加载后注册特定 Bridge/事件监听器。
5. Logcat 是否出现引擎初始化、资源加载、渲染或 JS 事件日志。
6. 通过用户点击追踪到 `evaluateJavascript`、`addJavascriptInterface`、事件分发和最终业务处理。

单独看到 `laya` 字符串、一个 `.html` 文件、一个 WebView 或一条网络日志，都不足以完成判断。应把“源码推断”和“设备运行验证”分开记录。

## Native、H5、Laya 的边界

- **Native**：Android View/Compose/Activity/Fragment 直接绘制和处理事件。
- **H5**：HTML/CSS/JavaScript 在 WebView 中运行，WebView 是承载者。
- **Laya**：通常是 H5/Canvas/WebGL 游戏引擎运行时，仍可能由 WebView 承载；“用了 WebView”不等于“用了 Laya”。
- **Bridge**：Native 和网页/引擎之间的协议通道，方向、线程、参数和生命周期都需要单独确认。

Day42 是 Native 调 JS，Day43 是 JS 调 Native；Day46 暂不添加桥接，以免把资源识别和通信协议混在一起。后续 Day47 再在证据明确的受控页面中学习 Laya 调 Native。

## Java/Kotlin 观察代码对照

```java
WebView webView = new WebView(this);
webView.setWebViewClient(new WebViewClient() {
    @Override public void onPageFinished(WebView view, String url) {
        // 这里能确认页面完成了 WebView 导航阶段。
    }
});
webView.loadUrl("file:///android_asset/day46/h5.html");
```

这段代码只能证明 WebView 导航，不会自动证明 Laya runtime 存在。引擎判断还要追资源和初始化链。

## 验收实验

1. 先观察外层 Native 标题和按钮，说明 Activity 不等于网页。
2. 加载普通 H5，记录资源路径与 WebView 回调。
3. 加载模拟 Laya 路径，解释为什么仍不能称为真实 Laya。
4. 能列出真实项目中需要寻找的引擎 runtime、初始化、资源和日志证据。
5. 能区分“页面类型识别”“Bridge 通信”“运行时设备验证”三个任务。

本课构建使用 `:app:assembleDemoDebug` 或 `:app:assembleProdDebug`，未引入第三方 Laya 依赖，也未声称完成真实 Laya 设备验证。
