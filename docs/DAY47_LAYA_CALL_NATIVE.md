# Day47 · Laya 调 Native

当前工程没有真实 Laya SDK，本课使用 `assets/day47/laya/index.html` 模拟一个引擎场景，练习的协议和线程规则可迁移到真实 Laya 页面，但不能把此页面当作真实 Laya 运行时。

## 调用链

```text
网页按钮
 → LayaNative.emit("course_complete", JSON)
 → Kotlin @JavascriptInterface
 → 记录桥接线程
 → runOnUiThread
 → 校验事件名和 courseId
 → 更新 Native 状态
 → evaluateJavascript(onNativeEventResult)
 → 网页显示结果
```

## 前端类比

可以把 `LayaNative.emit` 看成游戏脚本层调用宿主 SDK：事件名是 action type，JSON 是 payload，Native 返回的是异步事件。`LayaNative` 只是注入对象名，真正项目中可能由 `JSCommonAPI`、事件总线或自定义 Bridge 提供，需要从源码注册点确认。

## 为什么不能只判断事件名？

Native 同时校验：

```text
eventName == "course_complete"
courseId == "kotlin-47"
payload 长度在限制内
```

事件名相同但字段错误，仍应拒绝。生产协议还应校验版本、requestId、必填字段、类型和权限，不要把网页传入字符串直接拼成 SQL、Shell 或 JavaScript。

## 线程

`@JavascriptInterface` 回调可能运行在 WebView 的后台线程，不能直接修改 TextView 或调用要求主线程的 WebView 方法。本例使用 `runOnUiThread`，并在 Activity 销毁后用 `disposed` 丢弃迟到任务。

## 真实项目追踪方法

1. 找 WebView 创建和 `addJavascriptInterface` 注册点。
2. 找注入对象名、方法上的 `@JavascriptInterface`。
3. 找 Laya 脚本触发点、事件名和 payload 构造。
4. 追 Native 接收后是否进入 RxBus/Handler/业务 Listener。
5. 追结果是否回到正确 WebView、是否发生重复发送或页面销毁后的回调。

不要只看到 `laya` 字符串就下结论；需要引擎初始化、资源入口和运行日志证据。

## 验收

1. 点击网页按钮，Native 显示课程完成，网页显示 `ok=true`。
2. 将事件名改成未知值，观察 Native 拒绝；实验后恢复代码。
3. 将课程 id 改错，确认不会误处理。
4. Logcat 搜索 Day47，观察桥接线程和事件名。
5. 能解释 Day42（Native 调 JS）、Day43（普通 JS 调 Native）与 Day47（模拟 Laya 事件调 Native）的关系。

构建任务：`:app:assembleDemoDebug`、`:app:assembleProdDebug`。未引入真实 Laya SDK，也未声称完成真实引擎设备验证。
