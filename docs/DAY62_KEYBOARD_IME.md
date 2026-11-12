# Day62 · 键盘与 IME

入口：首页 → Day62 · 键盘与 IME。
源码：[KeyboardImeDemo.kt](../app/src/main/java/com/example/myapplication/day62/KeyboardImeDemo.kt)。

接 Day61：已经能区分文字、selection 和焦点；本课再把系统软键盘、IME 动作和 composition 分开。控件和系统键盘接口是真实的，提交只是本地计数，不发网络请求。

## 先用前端概念串起来

- requestFocus 类似 input.focus()，决定当前输入目标。
- 软键盘由系统输入法管理，不是页面里的一个 DOM 元素。
- onValueChange / TextWatcher 类似 input 事件，表示文字正在变化。
- IME Done 是输入法动作，类似用户明确要求完成，不等于每次 input，也不等于所有 Enter 按键。
- composition 类似浏览器 compositionstart/update/end 过程中的组合输入。Android 用范围描述它，不是直接照搬浏览器三个事件。

输入拼音、选择候选词、点击输入法完成、点击页面按钮，是四种不同操作。它们可能先后发生，但不能把一种事件当作另一种事件。

## InputMethodManager 是什么

这是 Android 的输入法系统服务，不是输入框本身。传统 View 用它向系统提出显示或隐藏键盘的请求。

```kotlin
// 用输入框自己的 UI Context 取得系统服务。
val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
// 先请求焦点，确定输入目标。
view.requestFocus()
// 请求显示；不代表这一行执行后键盘已实际显示。
imm.showSoftInput(view, 0)
// 用当前窗口令牌请求隐藏，不会自动清除输入框焦点。
imm.hideSoftInputFromWindow(view.windowToken, 0)
```

本例使用 view.post 延后请求，并检查 View 仍附着、拥有焦点且窗口有焦点。post 是投递到 UI 消息队列，不是“保证键盘成功”的魔法；窗口尚未就绪、硬件键盘、系统设置或 IME 策略仍可能影响显示。

showSoftInput 的返回值不能当作键盘已经可见的证明。严谨工程应结合实际屏幕、窗口 Insets 等验证；本例显示“已请求”，不显示未经验证的“键盘已弹出”。

## Compose 对应写法

```kotlin
// 当前组合中复用焦点请求器。
val requester = remember { FocusRequester() }
// 获取当前 Compose 软件键盘控制器，某些环境下可能为空。
val keyboard = LocalSoftwareKeyboardController.current
// TextField 通过 Modifier.focusRequester(requester) 绑定请求器。
// 按钮事件中先请求输入焦点。
requester.requestFocus()
// 然后请求系统显示键盘。
keyboard?.show()
```

控制器管理键盘请求，FocusRequester 管理焦点，两者职责不同。获取焦点不保证键盘显示；隐藏键盘后焦点可以仍留在输入框。控制器 show/hide 应在事件或恰当的副作用中调用，不应每次重组都直接调用。

## 真实执行链：点击请求键盘

```text
传统按钮
 → 清除 Compose 焦点
 → EditText.requestFocus
 → view.post
 → 校验附着/输入焦点/窗口焦点
 → InputMethodManager.showSoftInput
 → 系统与 IME 决定显示
```

```text
Compose 按钮
 → 清除传统框焦点
 → FocusRequester.requestFocus
 → KeyboardController.show
 → 系统与 IME 决定显示
```

反复点击不是改变当前框的文字，也不是提交。页面故意把文字与键盘请求分开。

## IME 的 Done 怎么进入业务

传统框通过 imeOptions = EditorInfo.IME_ACTION_DONE 请求提供完成动作，再由 setOnEditorActionListener 接收。actionId 是输入法动作编号，不是 KeyEvent.keyCode。

```text
用户点击 IME 完成
 → OnEditorActionListener 收到 IME_ACTION_DONE
 → 从当前 EditText 读取文字
 → 最新回调 onNativeDone
 → submit("EditText IME Done", text)
 → 空白校验
 → 本地计数和结果更新
```

返回 true 表示消费此动作，不保证系统接下来一定隐藏键盘。本例不在 Done 回调里自动隐藏或清焦点，方便分开观察；可以点击独立的隐藏按钮。

Compose 通过 KeyboardOptions(imeAction = ImeAction.Done) 配置，再通过 KeyboardActions(onDone = { ... }) 处理。页面提交按钮也调用同一个 submit，但会标记来源为“页面按钮”。

submit 只在非空输入上增加次数，空白校验发生在提交时，不在每次输入时 trim 并回写。重复显式提交会再次计数，这是实验行为；未来业务需要防重复请求时，应单独设计。

本例仅处理 Done，不把硬件键盘的 Enter DOWN/UP 都当提交，也不保证覆盖所有厂商 IME 的特殊行为。某些输入法先用完成键确认候选词，再发送动作，实际以该设备为准。Day63 再追踪硬件按键和 InputConnection。

## composition 到底是什么

TextFieldValue 有三个重要字段：

| 字段 | 含义 |
|---|---|
| text | 当前输入内容，可能包括尚在组合的文字 |
| selection | 光标/选择范围 |
| composition | IME 报告的正在组合范围，或 null |

例如中文输入法正在输入拼音时，可能先出现组合范围；选择“你好”后，组合区可能结束，也可能继续下一轮组合，具体取决于输入法。composition=null 不等于用户已提交业务；非空也不等于输入有错误。

本例正确保留整个值：

```kotlin
// 保留输入框返回的文字、选区、composition。
onValueChange = { value = it }
```

不要在每次回调中改成：

```kotlin
// 这种写法重新创建对象，只保留字符串，可能丢失光标和组合信息。
onValueChange = { value = TextFieldValue(it.text) }
```

也不要每次字符变化就无条件 setText、过滤文字或触发业务提交。尤其不能以监听到 ACTION_DOWN 作为所有软键盘输入的前提；IME 可以通过 InputConnection 提交文本或更新组合区，并不一定逐字发送按键事件。

传统框本课只通过 doAfterTextChanged 保存文字，没有完整显示 composing span；Compose 的组合范围是当前真实输入法回传的状态，不是伪造范围。TextFieldValue.Saver 恢复文本和选区，不保证恢复正在进行的 IME 会话。

## 再看 rememberUpdatedState

传统 View 的监听器在 factory 中只安装一次，而 submit 读取当前页面状态。rememberUpdatedState 保存最新处理函数，让这个长期存在的监听器在事件发生时读到最新逻辑，不必每次重组重新安装。

退出页面时 onRelease 移除 TextWatcher 和 editor action listener，清除临时 View 引用。View 正聚焦时还会请求隐藏对应窗口的键盘，不把旧输入控件保存进 Bundle。

## 本课与 Day63 的边界

本课追踪按钮、文字变化和 IME Done，不重写 InputConnection、不拦截 pre-IME，也不把 Action Down 当作提交。下一课学习输入链路调试，避免把“收不到按键”误判成“输入法没有输入”。

## 真机验收

1. 点击传统框请求键盘按钮，检查焦点与屏幕实际键盘；记录是否连接了硬件键盘及系统软键盘设置。
2. 输入文字，确认页面内容变化但提交次数不变；点击 IME Done，确认来源为 EditText。
3. 切到 Compose 框，输入拼音，观察 text、selection 和 composition，选择候选词后继续观察。
4. 仅选词不应直接进入业务 submit；IME 是否发送动作以输入法实际行为为准。
5. 分别点击 Compose IME Done 和页面提交按钮，对照结果来源。空白内容不增加次数。
6. 点击“隐藏键盘，保留焦点”，检查键盘与光标，不把隐藏等同于失焦；再点请求按钮重新显示。
7. 旋转设备，核对文字恢复；不要求焦点、键盘和正在组合的拼音会话原样恢复。
8. 离开 Day62 再回来，确认没有旧 View 回调继续影响新页面。

如果 IME 动作不符合预期，记录设备、系统、输入法名称版本、硬件键盘状态、焦点与完整操作，再区分是文字事件、动作事件还是窗口问题，不先归因 Kotlin 语法。
