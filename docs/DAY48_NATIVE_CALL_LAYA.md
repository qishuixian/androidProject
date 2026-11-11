# Day48 · Native 调 Laya

当前工程没有真实 Laya SDK。本课使用模拟 Laya 资源，重点练习 Native 主动调用网页函数、协议号和返回值；真实项目仍需以引擎 API 与源码证据为准。

## 调用链

```text
Native Button
 → payload = { courseId, color }
 → evaluateJavascript("window.onNativeProtocol(1001, payload)")
 → 模拟 Laya 函数校验协议号
 → 更新场景文字
 → return { ok, protocol, courseId }
 → Native 回调收到 JSON 文本
 → 更新原生状态条
```

入口：首页 → Day48。源码：[NativeLayaDemo.kt](../app/src/main/java/com/example/myapplication/day48/NativeLayaDemo.kt)，页面：[index.html](../app/src/main/assets/day48/laya/index.html)。

## 为什么要协议号？

```javascript
window.onNativeProtocol(1001, payload)
```

`1001` 是协议号，表示一种明确动作；payload 是该动作的数据。相比暴露许多随意函数，协议号便于版本管理、日志检索和统一校验。

本例先判断：

```text
protocol == 1001
payload.courseId == kotlin-48
```

真实项目还应校验协议版本、字段类型、requestId、超时和页面状态。协议号不是安全权限，也不能替代来源控制。

## 返回值与异步

`evaluateJavascript` 的回调收到的是 JSON 编码文本，不是 Kotlin 直接得到的 JSONObject。JS 函数可以同步 return 普通对象，但 Native 仍通过异步回调取得结果。若 Laya 内部操作是异步动画、资源加载或网络请求，应设计 requestId 和明确的完成事件，不能假定函数 return 就代表场景动作完成。

## 与 Day47 的区别

```text
Day47：网页事件 → Native 校验 → Native 回网页
Day48：Native 协议 → Laya 函数 → Laya 回 Native
```

两边都需要 JSON、协议和生命周期检查；方向不同，线程和回调边界也要分别验证。

## 生命周期

- Native 按钮只能在 WebView 页面函数准备好后调用；本例实际检查入口函数存在后启用按钮，复杂异步场景还需业务 ready 协议。
- Activity 销毁时停止加载、移除父子关系并 destroy WebView；不能把旧 WebView 放入 ViewModel 或单例。
- 旋转重建会创建新 WebView，页面 JS 内存和场景对象不自动迁移；需要业务状态时另行保存 id/状态。
- 回调可能晚于页面销毁，本例使用 disposed 标记过滤退出后的结果，复杂协议还应按请求表过滤。

## 真实 Laya 源码追踪

先找 Native 调用点（`evaluateJavascript`、桥接 API 或事件发送器），再找协议号常量和 JSON 构造；进入网页/引擎后找协议分发、场景节点修改和回调发送。只看到 `laya` 目录或协议数字，不能证明当前 Activity 正在运行真实 Laya 引擎。

## 验收

1. 点击 Native 按钮，网页显示协议 1001 和课程高亮，Native 状态显示回调 JSON。
2. 临时改协议号或课程 id，观察网页返回 `ok=false`；实验后恢复。
3. 能解释协议号、payload、回调结果三个层次。
4. 能说明为什么 Day47 与 Day48 是两个方向的通信。

构建任务：`:app:assembleDemoDebug`、`:app:assembleProdDebug`。未引入真实 Laya SDK。

