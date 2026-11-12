# Day64 · Native 与 Laya 输入

入口：首页 → Day64 · Native 与 Laya 输入 → 打开实验页。
源码：[DualInputDemo.kt](../app/src/main/java/com/example/myapplication/day64/DualInputDemo.kt)。
网页：[input.html](../app/src/main/assets/day64/input.html)。

## 实验范围先说清楚

本课两边都能真实输入：Native 是 EditText，另一边是 WebView 内的 HTML input。网页只模拟 Laya 一侧输入目标，没有真实 Laya 引擎、SDK、引擎事件总线或 Laya 原生输入代理。因此实验可以验证 Android/WebView/DOM 焦点交接，不能据此证明生产 Laya 的输入机制相同。

延迟请求由 Handler 可控排队，不是读取某个真实 SDK 的回调。页面没有网络请求和业务提交，不清空输入来假装交接成功。日志只记录固定事件、焦点和代次，不记录输入正文。

## 从前端经验理解

可以把它想成一个原生输入框与一个嵌入网页中的 input：父层认为自己有焦点，不代表 iframe/网页内部某个 input 就有焦点。反过来，DOM 中保留 activeElement，也不代表 Android 当前正在把输入交给这份网页。

生产 Laya 可能使用引擎自己的输入抽象、隐藏 DOM 输入框或 Native 代理，具体要追踪当前 SDK 和项目，不能凭控件看起来像文本框就推定实现。

## 四个状态分开看

| 状态 | 本课检查点 | 不能直接推出 |
|---|---|---|
| Activity 窗口焦点 | hasWindowFocus | 哪个输入框收文字 |
| Native View 焦点 | nativeInput.hasFocus | 系统软键盘一定显示 |
| WebView View 焦点 | web.hasFocus | DOM input 一定聚焦 |
| DOM 输入焦点 | document.activeElement.id | 宿主窗口始终可交互 |

键盘显示是额外的系统/IME状态，不是焦点所有权本身。“隐藏键盘”不会在本课中主动清除文本或释放两个输入目标的焦点。快照显示请求完成时附近的状态，不是跨多线程原子快照。

## 整体执行链

```text
首页点击课程
 → Compose 入口通过 Intent 打开 DualInputActivity
 → Manifest 中注册的内部 Activity 创建
 → 创建 EditText 与 WebView
 → 从 assets 读取固定 HTML 字符串
 → loadDataWithBaseURL 加载
 → onPageFinished 开放网页交接按钮
 → 用户选择输入目标
 → 明确释放旧目标并激活新目标
 → 请求系统键盘
 → 快照/DOM事件/Logcat 对照结果
```

Activity 必须在 Manifest 注册，exported=false 表示不对其他应用公开启动入口。页面使用独立 Activity，避免课程 LazyColumn 与 WebView 滚动互相干扰。窗口 Insets 同时考虑系统栏与 IME区域，操作按钮可通过外层 ScrollView 滚动访问。

## 交接到 Native 为什么不能只 requestFocus

```text
点击“交接到 Native”
 → generation 增加，捕获本次代次
 → evaluateJavascript 执行 DOM input.blur()
 → 异步结果返回
 → 校验 Activity 未销毁且代次仍匹配
 → WebView.clearFocus
 → EditText.requestFocus
 → 检查 View/窗口仍有效
 → InputMethodManager.showSoftInput
```

DOM blur 处理网页内部目标，WebView.clearFocus 处理 Android 容器目标。两者不是同一调用。evaluateJavascript 是异步的：用户快速改选目标后，旧回调不能再把焦点切回去。

blur 可能让输入法结束或调整当前组合文本，具体依浏览器与输入法行为；本例不强行改 text，不主动伪造 compositionend。

## 交接到模拟 Laya 为什么分两层

```text
点击“交接到模拟 Laya”
 → 新代次
 → Native EditText.clearFocus
 → WebView.requestFocus
 → JS 定位固定 input 并 focus
 → 读取 document.activeElement.id
 → 校验代次后请求键盘
```

页面会显示实际返回的 DOM id，不把执行 focus() 自动当成成功证明。WebView 对脚本聚焦与软键盘显示可能有系统/版本限制，真实结果仍要看设备；点击网页输入框本身是另一个真实用户路径。

InputMethodManager.showSoftInput 只是请求，系统、IME、硬件键盘和窗口状态可能影响显示。本例不使用 applicationContext 创建输入控件，也不通过强制清空文本解决焦点冲突。

## 迟到请求对照实验

### 无保护

1. 点击“排队旧 Native 请求（无保护）”。
2. 三秒内点击“交接到模拟 Laya 输入框”。
3. 等旧任务到达，会重新发起 Native 交接；观察焦点回到原生框。

```text
t0 排队 Native 请求，captured=1
 → t1 用户选网页，generation=2
 → t3 旧任务不检查代次，重新发起 Native 请求
 → 新选择被旧意图覆盖
```

这是故意制造的教学缺陷：旧任务执行时调用了统一的 focusNative，形成新的抢焦点请求。不是随机故障，也不是生产 Laya SDK 缺陷证据。

### 有保护

1. 点击“排队旧 Native 请求（代次保护）”。
2. 三秒内交接到网页。
3. 旧任务检查 captured != generation，显示“已忽略旧焦点请求”，不抢回焦点。

直接触摸任一输入侧也会推进代次，使之前的受保护请求失效。输入事件监听返回 false，保留系统/网页本来的触摸处理。读取快照不会主动切换输入目标。

代次过滤与 Day58 的旧频道事件过滤是同一个思路：发送时捕获旧身份，执行时与当前身份比较。它不能替代真实 SDK 的注销监听、取消任务和资源释放。

## 为什么不能在失焦回调里强行抢回来

如果 Native 的 onFocusChange(false) 立刻 requestFocus，而网页的 blur 也立刻 focus，两套系统就可能循环竞争：

```text
Native失焦 → 强行恢复Native
 → 网页失焦 → 强行恢复网页
 → Native再次失焦 → ...
```

因此本课焦点/DOM事件只用于记录，显式用户动作才发起目标切换。不要通过不停 showSoftInput、restartInput 或重建输入框掩盖竞争问题；先定位真正重复请求的调用方。

## 页面与 JS 如何通信

Native 用 evaluateJavascript 调用固定 DOM 操作，不拼接用户输入。网页用 console.log 输出固定事件，WebChromeClient 只接收 DOM_FOCUS、DOM_BLUR、COMPOSITION_START、COMPOSITION_END。这是本地实验调试通道，不是生产业务桥接协议。

不注册 JavaScriptInterface，不加载远程脚本；HTML 的 CSP 禁止远程子资源和表单跳转。day64.invalid 只是本地内容的虚拟来源，不代表向该域名发请求。

## 生命周期与恢复边界

onPause 推进代次并移除延迟任务，无保护实验也不会故意跨后台继续抢焦点。onDestroy 设置 disposed、移除任务、停止加载、将 WebView 从父容器移除并 destroy；异步 JS 回调还要检查 disposed 与代次。

本课没有实现跨 Activity 重建的输入内容恢复，旋转会重新创建网页、清空本次实验字段，不能据此判定焦点交接失败。真实业务若要恢复，保存必要文本/业务 id，再重新创建 View、网页和连接；不保存旧 WebView 或 IME 会话。

## 接着 Day63 怎么定位生产问题

按同一时间线对齐：用户点击 → 当前宿主与窗口 → 目标 View → DOM/引擎输入目标 → InputConnection → 文字变化。再查页面退出后是否有旧监听、自动聚焦、延迟任务和多个控制器同时发请求。

Native 本课特指 Android 控件，不是 Day60 的 C/C++ 崩溃含义。生产项目要从实际 Activity、WebView/Laya宿主及桥接调用追踪，不能凭页面外观推断输入路径。

## 验收

1. 两边输入不同测试文字，切换时文字不被主动清除。
2. 双向点击交接按钮，用快照核对 Native View、WebView 与 DOM 焦点。
3. 运行无保护与有保护实验，区分旧请求抢焦点和被忽略。
4. 直接点击两个输入框，观察真实焦点变化与代次推进。
5. 中文输入过程中切换目标，观察 composition 事件，不假设所有输入法顺序一致。
6. 隐藏键盘后读取焦点快照，说明隐藏与失焦不同。
7. 任务排队后离开页面/切后台，确认不继续跨后台抢焦点。
8. 能明确说出哪些是真实系统行为，哪些仅模拟 Laya 和旧任务。
