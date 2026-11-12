# Day63 · 复杂输入调试

入口：首页 → Day63 · 复杂输入调试。
源码：[InputTraceDemo.kt](../app/src/main/java/com/example/myapplication/day63/InputTraceDemo.kt)。

本课使用真实 EditText，包装它的 InputConnection 进行观察，不模拟输入法事件，不主动生成按键，不改动输入结果，也没有业务提交或网络。页面保留最近十二条事件，Logcat 搜索 Day63。只使用测试内容；日志记录长度与事件，不记录输入正文。

## 从前端经验理解链路

前端的 keydown、input、compositionupdate、submit 不相同：粘贴、语音、拼音选字不一定是一次按键对应一个字符。Android 同理，不能把 ACTION_DOWN 当作所有文字变化的入口。

InputConnection 可以理解为输入法与编辑器之间的编辑协议接口，支持提交文字、更新组合文本、删除、修改选区和执行编辑器动作等。它不是普通 onKeyDown 监听器。EditText 提供标准实现，本课只套一层 InputConnectionWrapper，记录后转交原实现。

## 两条不同的常见路径

```text
软键盘拼音 / 选词
 → IME 调用 InputConnection 的编辑方法
 → 包装器记录方法名与长度
 → 转发给原始连接
 → EditText 内容或组合区变化
 → TextWatcher 观察实际文字变化
 → 页面更新长度
```

```text
硬件键盘或 IME 发送 KeyEvent（视输入方式而定）
 → 某些事件先经过 pre-IME
 → IME/系统处理与分发
 → View.dispatchKeyEvent
 → 控件默认按键处理
 → 可能产生文字变化
```

两条路径可能交叉，不能据此写死所有设备事件顺序。InputConnection.sendKeyEvent 也可能触发 View 按键分发，所以同一操作的多条日志不等于用户输入了多次。没有 dispatchKey 的日志但文字正常变化，可能只是走了直接编辑协议。

## 逐层看代码为什么这样写

### onCreateInputConnection

系统需要与当前编辑器建立连接时调用它，可能因焦点、重启输入等重复创建，不是每输入一个字都调用一次。

先调用 super 获取 EditText 标准连接及 EditorInfo；原连接为空就返回 null，不伪造可编辑状态。再返回 InputConnectionWrapper(original, false)：目标不允许通过 setTarget 换成其他连接。本例不是自己实现完整输入法协议。

### 包装器记录什么

| 方法 | 大致语义 | 不代表什么 |
|---|---|---|
| setComposingText | 设置/替换尚在组合的文字 | 不等于业务提交 |
| commitText | 确认/提交文字到编辑器 | 不等于表单提交或发送请求 |
| finishComposingText | 结束组合状态 | 不一定改变文字，也不等于点击 Done |
| deleteSurroundingText | 删除光标或选区周边文字 | 不保证先出现 Delete 键事件 |
| sendKeyEvent | 通过连接发送按键 | 不代表所有软输入都走这里 |
| performEditorAction | 输入法请求 Done 等编辑器动作 | 与 commitText 不同 |

每个方法必须继续 return super.xxx(...)。如果只打印后返回 true，实际操作可能被吞掉；如果额外调用 setText，可能破坏组合输入。此处记录方法进入，不据此宣称下游已经成功，应结合实际文本变化判断。

本课覆盖常见旧版签名。新系统/输入法还可能使用带 TextAttribute 的重载、deleteSurroundingTextInCodePoints、setComposingRegion、批量编辑等其他接口。这是最小观察器，不是完整 IME 协议抓包器；某条日志未出现不证明该类操作没有发生。日志看起来不完整时，应先核对实际调用的签名与 SDK 版本。

### TextWatcher 为什么仍然需要

连接日志说明收到过何种请求，TextWatcher 说明实际文字发生了变化。只改变组合样式或焦点不一定改变文字。用户粘贴、自动填充或程序修改也可能触发 TextWatcher，不应把每次回调都归因于某个 KeyEvent。

本例 factory 恢复内容后再安装页面回调，之后不在 update 中反复 setText。TextWatcher 保存文字用于旋转恢复，不执行 submit。

## ACTION_DOWN 与 ACTION_UP

KeyEvent.ACTION_DOWN=0，ACTION_UP=1。它们描述按键按下与抬起，不是文字内容。长按可能产生 repeatCount>0 的重复 DOWN。

不要在 DOWN 和 UP 中各提交一次，不要把 actionId 与 keyCode 混用，不要把 keyCode 转成字符来替代 IME 编辑协议。硬件 Enter 和 IME Done 可能通过不同入口到达应用，生产中应明确提交策略并避免重复；本课只观察，不处理业务提交。

## onKeyPreIme 不是“所有输入之前”

名字表示经过这个分发入口的按键在交给 IME 前被观察，不表示所有 commitText 或组合文本之前都会调用。拼音组合输入可以没有对应的 preIME ACTION_DOWN。

历史上有项目用它观察返回键收键盘，但现代 Android 返回处理、预测返回、IME 与厂商实现可能绕开传统 KeyEvent 路径。不能要求每次关闭键盘都出现 preIME BACK DOWN；应结合系统版本和实际返回 API 排查。本课 return super，不拦截返回行为。

## 焦点链路怎么查

点击“获取焦点并请求键盘”或“记录焦点快照”，检查：

1. attached：输入框是否已经附着在窗口。
2. viewFocus：输入目标是不是这个 EditText。
3. windowFocus：宿主窗口是否拥有交互焦点。
4. createInputConnection：系统是否为当前编辑器建立过连接，返回是否非空。
5. 连接事件、按键事件和实际文字变化分别到达哪一层。

快照只代表点击时的状态，不是永久状态。输入焦点、窗口焦点和软键盘可见性是不同概念。连接已经存在时，再点击请求按钮不一定重建连接，所以没有新的 createInputConnection 也不一定有故障。

如果焦点被 Dialog、其他 View 或另一套引擎输入控件抢走，要继续追踪真正的输入目标，不能看到 viewFocus=false 就继续怀疑 TextWatcher。Day64 会对照两套输入系统的冲突。

## 为什么日志用 post

自定义 View 的焦点和连接回调可能在构造、布局或输入过程中发生。trace 将 UI 日志投递到 View 消息队列，不直接在布局期间写 Compose 状态。日志编号代表被记录的顺序，不是精确到纳秒的所有底层调用顺序。

post 执行时重新读取可空 onTrace；onRelease 将回调置空，排队的旧任务不再回写页面。同步的 editor action listener 可能先显示，排队的 IC 日志后显示，不能拿页面顺序证明底层调用次序倒转。更严格的时序分析需记录入口时间、线程、连接实例编号，并对齐源事件。

## 状态与清理

- rememberSaveable：保存普通文字，不保存旧 InputConnection、View、焦点或 IME 会话。
- remember：保留本次 View 引用、十二条日志与编号，页面移除后重置。
- rememberUpdatedState：让 View 的长期回调访问最新页面记录函数与文字更新逻辑。
- onRelease：移除 TextWatcher、editor action listener 和页面回调。

自定义 View 的重写方法可能在父类构造期间被调用，所以 onTrace / onContent 必须允许为空。不要在这种入口假定所有子类字段已经完成初始化。

## 实际观察实验

| 操作 | 重点观察 | 不要先假定 |
|---|---|---|
| 点击输入框、请求键盘 | View/Window focus、连接创建 | 每次点击都重建连接 |
| 中文拼音、选词 | composing/commit、文字变化 | 每个字先有 DOWN |
| 删除字符 | 删除协议或按键、文字变化 | 所有删除只走一个接口 |
| 长按粘贴测试文本 | 内容变化和可能的编辑协议 | 一定发送逐字 KeyEvent |
| 点击 IME 完成 | performEditorAction、editorAction | commitText 就是 Done |
| 连接硬件键盘再按字母/Enter | dispatchKey、action、repeat | 与软键盘日志相同 |
| 按系统返回关闭键盘 | 实际屏幕与日志 | 一定出现 preIME BACK |
| 旋转或返回首页 | 新 View/连接与清理 | 旧 IME 会话原样恢复 |

不同输入法和系统会走不同路径。没有硬件键盘时，硬件键盘实验记为未执行，不能把软键盘上显示的按键外观当硬件事件证据。

## 学完应能说明

1. 为什么文字已经变化，却没有 ACTION_DOWN。
2. InputConnectionWrapper 如何记录再转发，以及为什么不能随便 return true。
3. commitText、文字变化、IME Done 与业务提交的区别。
4. View 焦点、窗口焦点、连接创建和键盘显示之间的关系。
5. 缺少一条日志为何不能直接证明输入法故障。
6. 从用户操作到控件、连接、状态和页面展示的完整链路。
