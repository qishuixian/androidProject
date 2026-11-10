# Day42 · Native 调 JavaScript

## 用前端串起完整流程

本课把 Android 当作外层宿主，WebView 内的 HTML 当作被调用的网页。类似外层控制器调用嵌入页面的函数，但不是 postMessage：evaluateJavascript 在 WebView 当前页面上下文执行脚本。

```text
首页 Day42 → Intent → NativeJavaScriptActivity
 → 开启 JavaScript → 加载本地 HTML
 → 检查 window.receiveCourse 是否存在
 → 启用原生按钮
 → 用户输入课程标题并点击
 → JSONObject 编码参数
 → evaluateJavascript("window.receiveCourse({...})")
 → 网页函数更新 DOM 并 return 对象
 → WebView 将返回值编码为 JSON 文本
 → Native 回调解析 JSON → 更新原生 TextView
```

入口：首页 → Day42 → 打开示例。源码：[NativeJavaScriptDemo.kt](../app/src/main/java/com/example/myapplication/day42/NativeJavaScriptDemo.kt)，网页：[index.html](../app/src/main/assets/day42/index.html)。Activity 在 app Manifest 注册；exported=false 允许本应用内部启动，不向其他普通应用开放。

## 两边各写什么？

网页定义可调用函数：

```javascript
window.receiveCourse = function (course) {
    document.getElementById('course').textContent = course.title;
    return { message: '收到 ' + course.title };
};
```

Android 发起调用：

```kotlin
val payload = JSONObject().put("id", "kotlin-42").put("title", input.text.toString())
webView.evaluateJavascript("window.receiveCourse($payload)") { rawJson ->
    // 这里收到编码后的结果，不是 evaluateJavascript 方法本身的返回值。
}
```

`$payload` 调用对象的字符串表示，得到合法 JSON 对象表达式。与之相反，直接把用户输入塞进 `receiveCourse('$input')` 容易被单引号、反斜杠和换行打断，甚至改变脚本含义。标题使用 textContent 展示，不当作 innerHTML 执行。

## 异步在哪里？

evaluateJavascript 必须在 UI 线程调用，结果回调也在 UI 线程。本例点击事件和 WebViewClient 回调本来就在 UI 线程，所以不用额外切线程；如果从后台协程调用，需要切到 Main 或使用 webView.post，并检查生命周期。

Android 调用是异步的，不能写完 evaluateJavascript 后立刻读取“刚得到的变量”。结果处理放进回调。网页函数本身在本例同步 return 普通对象；若返回 Promise，不能假定回调会等待 Promise resolve。复杂异步业务需要后续请求 id/桥接回调协议，不属于本课单次返回。

Java 对照：

```java
webView.evaluateJavascript("window.receiveCourse(" + payload.toString() + ")", rawJson -> {
    // 解析 JSON，再更新原生 UI。
});
```

## 返回值为什么不能直接当普通字符串？

| JS 返回 | Native 回调文本 |
|---|---|
| `42` | `42` |
| `true` | `true` |
| `'你好'` | `"你好"`（文本里包含 JSON 引号） |
| `{message: '收到'}` | `{"message":"收到"}` |
| null 或无法得到有效返回值 | 可能得到 `null`，不能据此当作成功 |

本例约定返回 JSONObject，用 JSONTokener 解码并检查类型，而不是简单删除首尾引号。Logcat 搜索 Day42 查看原始结果。JS 报错或未定义函数时，普通结果回调不是完整的异常通道；本例显示返回格式不符，Console 调试后续继续学习。

## 页面就绪与生命周期

- Day41 默认关闭 JS，本课为受控本地脚本显式开启；不加载任意外部站点，不注册 addJavascriptInterface。
- onPageFinished 不保证所有网页异步业务就绪。本例脚本同步内联，之后再检查函数存在才启用按钮；复杂页面需显式 ready 协议。
- 每次页面开始加载禁用按钮，防止旧网页离开后仍调用未准备好的函数。
- 退出时先设置 disposed，忽略迟到结果，再 stopLoading、解除 Client、移除并 destroy。
- 旋转后本课重新加载网页，输入和 JS 计数可能重置；不声称保存了 JS 内存。按 Home 再返回不主动刷新。
- 今天的返回回调仍属于 Native 发起调用的应答；网页主动调用 Android 是 Day43 的另一个方向。

## 验收实验

1. 等待“网页函数已就绪”，点击按钮，网页 DOM 与原生返回值都更新。
2. 输入含双引号、单引号、反斜杠、中文的标题，确认按文字传递，不破坏脚本。
3. 连续点击，网页计数增加，原生显示对应返回次数。
4. Logcat 搜索 Day42，区分 JSON 原始文本与解析后的显示文字。
5. 按 Home 再返回，计数仍在；旋转重建后重新加载并从零计数。
6. 能按完整链路解释：原生事件 → 编码 → WebView 执行 → JS return → JSON 回调 → 原生 UI。

构建使用 `:app:assembleDemoDebug` 或 `:app:assembleProdDebug`。构建验证不代替设备上的 WebView 脚本和生命周期测试。
