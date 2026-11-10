# Day43 · JavaScript 调 Native

## 先用前端理解整条链

Day42 是外层宿主调用网页，今天反过来：网页调用 Android 注入的对象。类似前端访问宿主 SDK 的 `window.NativeSDK.postMessage()`，不是 HTTP 请求，也不是直接 import Kotlin 文件。

```text
Activity 创建 CourseBridge 对象
 → addJavascriptInterface(对象, "AndroidCourse")
 → 加载本地 HTML
 → 网页点击按钮
 → window.AndroidCourse.postMessage(JSON 字符串)
 → CourseBridge.postMessage（WebView 后台线程）
 → Activity runOnUiThread
 → 校验参数、更新原生文字
 → evaluateJavascript 调用 window.onNativeResult
 → 网页更新结果 DOM
```

入口：首页 → Day43 → 打开示例。源码：[JavaScriptNativeDemo.kt](../app/src/main/java/com/example/myapplication/day43/JavaScriptNativeDemo.kt)。网页：[index.html](../app/src/main/assets/day43/index.html)。代码配有中文注释。

## 三个名字不要混淆

| 名字 | 定义在哪 | 意义 |
|---|---|---|
| CourseBridge | Kotlin 类 | 注入对象的实现 |
| AndroidCourse | addJavascriptInterface 的第二参数 | 网页使用的全局对象名 |
| postMessage | 带注解的 Kotlin 方法 | 网页调用的方法名，不是浏览器内置 postMessage |

```kotlin
webView.addJavascriptInterface(CourseBridge { raw -> /* 转发请求 */ }, "AndroidCourse")
```

```javascript
window.AndroidCourse.postMessage(JSON.stringify({
    requestId: 'request-1', action: 'complete', courseId: 'kotlin-43'
}));
```

`@JavascriptInterface` 标记允许网页访问的方法，项目 minSdk 24 下遵循现代注解暴露规则。JS 只能按对象名和公开注解方法调用，不会因为 Kotlin import 就自动出现网页对象。桥接须在网页加载前注册。

## 线程与返回值

桥接方法通常在 WebView 私有后台线程执行，不能直接修改 TextView、弹窗或执行要求 UI 线程的 WebView 方法。本例 Logcat 搜索 Day43 观察线程名，再使用 runOnUiThread 处理。

JS 调用进入桥接方法并不等于之后排队的 UI 工作已经完成。postMessage 返回 Unit，不同步返回业务结果；Native 处理后复用 Day42 的 evaluateJavascript 回传，网页通过 onNativeResult 接收。requestId 用于关联响应，复杂项目可扩展为 Promise + 待处理请求表，本课没有实现 Promise 封装。

Java 等价注解形式：

```java
public class CourseBridge {
    @android.webkit.JavascriptInterface
    public void postMessage(String raw) {
        // 先校验，再调度到 UI 线程处理，不能在这里直接操作 View。
    }
}
```

## 参数、业务与生命周期

- 网页用 JSON.stringify 编码，Native 用 JSONObject 解析，响应也用 JSON 编码后作为脚本对象参数。
- requestId 标识一次调用；courseId 标识业务课程；action 表示允许执行的动作。
- 本例只允许 complete 和 kotlin-43，限制输入长度，不暴露任意方法执行能力。
- 演示只更新 UI，未写数据库、未请求网络；旋转会重新加载网页并重置请求编号和结果。
- 退出时先设置 disposed，移除接口、停止加载、移除父布局，再 destroy；已排队任务仍需检查退出状态。
- 超过长度限制的请求被直接丢弃，本例网页不会发送此类请求；正式协议通常应返回明确错误和实现超时。

## 安全边界要和 API 能力区分

addJavascriptInterface 的对象会暴露给页面中的各个 frame，接口本身无法可靠判断调用者的来源。只拦截顶层导航不构成完整的来源校验；exported=false 也不等于网页桥接天然安全。

所以本例仅加载随 APK 打包的固定 HTML，无 iframe、远程脚本、外部子资源，导航只允许固定页面。不要把这个桥直接复用到任意 URL、第三方网页或不可信 iframe 中，也不要暴露整个 Activity。后续复杂工程需要可信来源管理、最小接口与更严格的消息机制。

## 实验与验收

1. 点击 HTML 中“完成 Kotlin 课程”，原生状态条与网页结果同时更新。
2. 连续点击，网页显示不同 requestId，能追踪每次请求和响应。
3. Logcat 搜索 Day43，解释为何回调要切回 UI 线程。
4. 临时把 action 改成其他值再重新打包，观察参数无效响应，完成后恢复。
5. 在普通浏览器打开 HTML，显示没有 Native 桥接，说明对象不是网页自带能力。
6. 能解释 Day42 单向调用与今天 JS 请求、Native 应答闭环的关系。

构建任务为 `:app:assembleDemoDebug` 和 `:app:assembleProdDebug`。构建成功不等于设备 WebView 的桥接行为已经验证。
