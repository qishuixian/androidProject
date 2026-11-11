# Day61 · EditText 与 TextField

入口：首页 → Day61 · EditText 与 TextField。
源码：[TextInputDemo.kt](../app/src/main/java/com/example/myapplication/day61/TextInputDemo.kt)。

本课使用真实 EditText、Compose OutlinedTextField，没有网络请求。两个框状态独立，便于比较。Day60 练习故障证据分类，本课进入输入与键盘学习阶段；Day62 再深入 IME、提交和组合文本。

## 用前端概念理解整体

传统 EditText 类似先创建 DOM，再通过 input/select 监听读取值，并按需设置 DOM 的 value、selectionStart、selectionEnd。

Compose TextField 类似受控输入：把 value 交给组件，onValueChange 收到新值后更新状态，组件再显示新状态。Android 的 TextFieldValue 不只包含字符串，还包含 selection 和 composition。不能每次输入都只拿字符串新建对象，否则可能丢失光标或输入法组合信息。

| 任务 | EditText | Compose |
|---|---|---|
| 当前文本 | editable / text | value.text |
| 用户输入通知 | TextWatcher | onValueChange |
| 光标或范围 | selectionStart、selectionEnd | value.selection |
| 主动改变选择 | setSelection(start, end) | copy(selection = TextRange(...)) |
| 请求焦点 | view.requestFocus() | FocusRequester.requestFocus() |
| 清空 | 本课先更新状态，再按需 setText | TextFieldValue("") |

## 先把执行过程串起来

### 进入页面

```text
创建/恢复两份 TextFieldValue
 → AndroidView.factory 创建 SelectionEditText
 → sync 初始内容与选区
 → 安装页面回调，保存当前 View 临时引用
 → Compose TextField 直接读取另一份状态
```

factory 创建传统 View，不是每次重组都创建。apply 配置后返回同一个 View，与 Day59 的写法一致。View 引用用 remember，不用 rememberSaveable；旋转后新建 View，再把保存的文字与选区恢复进去。

### 输入传统框

```text
用户打字或移动光标
 → afterTextChanged / onSelectionChanged
 → publishValue 读取控件文本和选区
 → onValueChanged 把值交给页面
 → nativeValue 更新
 → Compose 重组，旁边的状态文字更新
 → AndroidView.update 调用 sync
 → 内容和选区没变化，不重复 setText / setSelection
```

文本事件和选区事件可能分别发生，不把它们当成必然固定顺序。publishValue 每次读取当前值；选区限制到 0..文本长度，处理初期可能出现的 -1。

### 输入 Compose 框

```text
用户输入
 → OutlinedTextField 提供新的 TextFieldValue
 → onValueChange 保存整个值
 → Compose 读取新值
 → 显示文字、选区与输入过程
```

### 点击“全选”或“清空”

```text
按钮事件
 → 页面状态更新
 → 传统框：update → sync → 按需修改 View
 → Compose 框：直接读取新 value
```

全选按钮只调整选择，不主动请求焦点；“获取焦点”是另一个操作。选区存在时，没聚焦的控件不一定显示高亮，先看页面显示的 selection，再聚焦观察。清空文本也不自动清除焦点。

## selection 到底是什么

以 Android 为例，长度为 7：

```text
位置：0 1 2 3 4 5 6 7
字符： A n d r o i d
```

- TextRange(7)：起点终点都是 7，光标在末尾。
- TextRange(0)：光标在开头。
- TextRange(0, 7)：选中整个单词，终点不包含在字符范围中。
- TextRange(1, 4)：选中 ndr。

索引以 UTF-16 code unit 为基础，与 JavaScript 字符串索引类似。一个 emoji 可能占多个位置，不能把索引直接当作用户看到的字符数量。修改文本后选区必须仍在合法范围内。

## 为什么传统框不能每次重组都 setText

无条件 setText 可能触发 TextWatcher、重置光标、干扰输入法组合区，还可能形成：

```text
状态更新 → setText → 监听回调 → 状态更新 → 再 setText
```

本例两层处理：先比较内容/选区，只有不同才改；程序回写期间 synchronizing=true，忽略反向事件，finally 恢复监听。这个开关用于区分程序同步与用户编辑，不是线程锁，所有控件访问仍在主线程。

用户输入时 update 收到的通常就是 View 已有的内容，因此不会重写 Editable。本例不实现通用输入框封装、格式化或双控件共享输入状态，生产中还要更细致地处理 composing span、可访问性和输入法兼容。

## 为什么定义 SelectionEditText

TextWatcher 主要通知文字变化，但用户只移动光标时文字不变。继承 EditText 并重写 onSelectionChanged，可以观察选择变化。

`object : TextWatcher` 类似 Java 的 `new TextWatcher() { ... }`，需要实现三个方法。本课只在 afterTextChanged 中发布内容。初始化时安装一次；onRelease 清除页面回调并移除监听。

onSelectionChanged 可能在父类构造时就调用，所以回调设计为可空，没有回调时直接返回。不能假设自定义 View 的所有字段此时都已完整初始化。

## 焦点不是键盘

焦点表示哪个控件接收输入；键盘是否显示还受到窗口、系统、硬件键盘和输入法影响。requestFocus 不等于必须弹出键盘，clearFocus 也不应当作所有设备上的键盘控制 API。本课不把按钮点击、文字变化和输入法“完成”事件混为一谈；提交与键盘控制在 Day62 学习。

`FocusRequester` 必须通过 Modifier.focusRequester 绑定到输入框，本例在控件进入组合后由按钮使用。传统 View 则对保存的临时引用调用 requestFocus。rememberUpdatedState 让 View 保存的回调读取到最新处理函数，不需要每次重组重新注册监听。

## 状态恢复边界

TextFieldValue.Saver 用于保存文字与选区，不保存 View、Context、焦点请求器，也不保证恢复正在进行的输入法组合会话。旋转后内容与选择可恢复，但不承诺焦点和键盘状态完全一致；长期保存内容应按业务需求使用 ViewModel、SavedStateHandle 或持久化方案。

## 验收步骤

1. 两个框分别输入不同内容，确认互不覆盖。
2. 点击文字中间移动光标，观察 start=end；长按选择后观察两者不同。
3. 在各框点击“全选”，观察 selection 从 0 到文本长度，再输入一个字替换选择。
4. 点击“清空”，确认文本为空、选区为 0；它不等于主动失焦。
5. 分别点击“获取焦点”和“清除焦点”，对照焦点与键盘的区别。
6. 中文输入时不要无条件重置内容，观察拼音组合过程；本课不手动改写用户输入。
7. 旋转设备，核对文本和选区；不要以键盘没有自动弹出判定状态恢复失败。
8. 能说出传统框的“事件 → 页面状态 → 按需回写 View”和 Compose 的“新值 → 状态 → 重组”两条完整链。
