// Day63 记录真实输入链路，所有输入仍交给原始控件处理。
package com.example.myapplication.day63

// 自定义传统输入框所需的平台类型。
import android.content.Context
import android.graphics.Rect
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
// 在文本实际改变后记录结果，不把它等同于提交。
import androidx.core.widget.doAfterTextChanged
// 页面布局与操作控件。
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
// 文字参与恢复，调试日志与 View 引用只属于当前页面。
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

// 观察控件级焦点、按键与 IME 输入连接。
private class TracedEditText(context: Context) : EditText(context) {
    // 构造早期与释放后没有接收者，使用可空回调。
    var onTrace: ((String) -> Unit)? = null
    // 页面只保存当前文字，不保存控件本身。
    var onContent: ((String) -> Unit)? = null
    // 安装一次文字监听，离开页面移除。
    private val watcher = doAfterTextChanged {
        // 只记录长度，Logcat 不输出用户实际输入内容。
        trace("afterTextChanged length=${it?.length ?: 0}")
        // 内容交给页面保存，不触发业务提交。
        onContent?.invoke(it?.toString().orEmpty())
    }
    // 日志投递到 View 队列，避免构造或布局期间直接写 Compose 状态。
    private fun trace(event: String) {
        // 触发时无监听就不排队；执行时也重新检查回调。
        if (onTrace != null) post { onTrace?.invoke(event) }
    }
    // 输入框焦点与窗口焦点是两层条件。
    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        // 保留系统原有焦点逻辑。
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        // 记录此控件是否获得输入焦点。
        trace("View focus=$focused")
    }
    // 宿主窗口进入或失去交互焦点时通知。
    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        // 不阻断系统窗口处理。
        super.onWindowFocusChanged(hasWindowFocus)
        // 窗口焦点与输入框焦点可能不同。
        trace("Window focus=$hasWindowFocus")
    }
    // pre-IME 只观察经过该入口的按键，不能代表所有文本输入。
    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        // 记录 action 数字：DOWN=0，UP=1；不是文字变化。
        trace("preIME key=$keyCode action=${event.action}")
        // 不返回固定 true，保留系统处理。
        return super.onKeyPreIme(keyCode, event)
    }
    // 观察实际分发到 View 的按键，而非假定软键盘逐字发按键。
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // repeatCount 可帮助判断长按重复。
        trace("dispatchKey key=${event.keyCode} action=${event.action} repeat=${event.repeatCount}")
        // 转交父类，不在 DOWN 和 UP 各提交一次。
        return super.dispatchKeyEvent(event)
    }
    // 系统向文本编辑器索取输入连接时执行，不是每输入一个字都创建。
    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        // 先让 EditText 建立标准连接和 EditorInfo。
        val original = super.onCreateInputConnection(outAttrs)
        // 记录连接创建；可能发生多次，不等于发生文字输入。
        trace("createInputConnection available=${original != null} imeOptions=${outAttrs.imeOptions}")
        // 没有有效连接时不伪造一个。
        if (original == null) return null
        // 包装真实连接，只观察再转发；不自行实现编辑器协议。
        return object : InputConnectionWrapper(original, false) {
            // 常见文字确认入口，但它不是业务表单提交。
            override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
                // 不记录用户内容，仅记录方法与长度。
                trace("IC.commitText length=${text?.length ?: 0} cursor=$newCursorPosition")
                // 必须保留原连接返回结果。
                return super.commitText(text, newCursorPosition)
            }
            // 中文拼音等可能通过此方法更新尚未确认的组合文本。
            override fun setComposingText(text: CharSequence?, newCursorPosition: Int): Boolean {
                // 这不是 ACTION_DOWN，也不是点击提交。
                trace("IC.setComposingText length=${text?.length ?: 0} cursor=$newCursorPosition")
                // 交给 EditText 更新 composing span。
                return super.setComposingText(text, newCursorPosition)
            }
            // 结束组合文本不自动代表业务完成。
            override fun finishComposingText(): Boolean {
                // 记录边界，不主动清空文字。
                trace("IC.finishComposingText")
                // 转发原始操作。
                return super.finishComposingText()
            }
            // 删除也可能直接调用连接，而不是发送 Delete 按键。
            override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
                // 长度单位由此 API 定义，不能理解为可见字符数。
                trace("IC.deleteSurroundingText before=$beforeLength after=$afterLength")
                // 保留系统删除逻辑。
                return super.deleteSurroundingText(beforeLength, afterLength)
            }
            // 某些 IME 会通过连接发送按键，但不是必经路径。
            override fun sendKeyEvent(event: KeyEvent): Boolean {
                // 和 View 分发日志对照同一事件可能经过的不同层。
                trace("IC.sendKeyEvent key=${event.keyCode} action=${event.action}")
                // 不消费事件，只转交。
                return super.sendKeyEvent(event)
            }
            // 输入法完成动作与 commitText 是不同语义。
            override fun performEditorAction(actionCode: Int): Boolean {
                // Done 的编号通常为 EditorInfo.IME_ACTION_DONE，页面也记录 listener。
                trace("IC.performEditorAction action=$actionCode")
                // 允许原连接继续触发编辑器监听。
                return super.performEditorAction(actionCode)
            }
        }
    }
    // View 真正释放时断开页面引用与监听。
    fun release() {
        // 排队的 trace 执行时将读到 null，不回写旧页面。
        onTrace = null
        // 清除文字回调。
        onContent = null
        // 移除文本监听。
        removeTextChangedListener(watcher)
        // 移除输入法动作监听。
        setOnEditorActionListener(null)
    }
}

// 首页进入的输入链路调试实验。
@Composable
internal fun InputTraceDemo() {
    // 恢复已输入文字，不恢复旧的 IME 连接。
    var content by rememberSaveable { mutableStateOf("") }
    // 调试事件只保留当前组合中的最近十二条。
    var events by remember { mutableStateOf(emptyList<String>()) }
    // 单调编号区分同一操作引出的多个事件。
    var sequence by remember { mutableIntStateOf(0) }
    // 控件引用不能保存在 Bundle 中。
    var input by remember { mutableStateOf<TracedEditText?>(null) }
    // View 长期监听读取最新的记录逻辑。
    val record by rememberUpdatedState<(String) -> Unit>({ event ->
        // 编号只属于本次页面实验。
        sequence++
        // 限制页面日志长度，不无限堆积。
        events = (events + "$sequence $event").takeLast(12)
        // 日志不包含实际输入内容。
        Log.d("Day63", "$sequence $event")
    })
    // 文字变化只同步页面内容。
    val updateContent by rememberUpdatedState<(String) -> Unit>({ content = it })
    // 使用外层课程页面滚动。
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 本课是实际控件日志，不是模拟日志。
        Text("Day63 · 复杂输入调试")
        // 不输出密码、Token 等敏感信息来进行实验。
        Text("请使用测试文字。观察 InputConnection、按键、焦点与文字变化；Logcat 搜索 Day63。")
        // 传统输入框可重写输入连接入口。
        AndroidView(
            // 每次 View 创建时初始化一次。
            factory = { context ->
                // 创建并配置跟踪输入框。
                TracedEditText(context).apply {
                    // 单行配合 Done，便于与 Day62 对照。
                    setSingleLine(true)
                    // 指定输入法完成动作。
                    imeOptions = EditorInfo.IME_ACTION_DONE
                    // 设置提示而非实际输入内容。
                    hint = "输入拼音、选词、删除，再点完成"
                    // 先恢复文字，再安装页面回调。
                    setText(content)
                    // 光标初始放到末尾。
                    setSelection(length())
                    // 跟踪事件交给页面与 Logcat。
                    onTrace = { record(it) }
                    // 用户文字变化保存为可恢复的小字段。
                    onContent = { updateContent(it) }
                    // 观察动作但不拦截原生默认行为。
                    setOnEditorActionListener { _, action, event ->
                        // actionId 和 KeyEvent 不是同一种编号。
                        record("editorAction id=$action keyAction=${event?.action ?: "none"}")
                        // 不消费，保留系统默认处理；本课没有业务 submit。
                        false
                    }
                    // 保存临时引用。
                    input = this
                }
            },
            // 故意不在 update 中重写文本，避免破坏组合输入。
            modifier = Modifier.fillMaxWidth(),
            // 页面退出时清理监听和引用。
            onRelease = { view ->
                // 断开旧 View 的页面回调。
                view.release()
                // 仅清理当前释放的输入框。
                if (input === view) input = null
            }
        )
        // 页面只显示长度，不把用户内容写入调试日志。
        Text("当前长度=${content.length}；DOWN=0，UP=1；清空日志不会清空输入。")
        // 请求键盘前先取得输入焦点。
        Button(onClick = {
            // 用当前有效 View 发起操作。
            input?.let { view ->
                // 焦点是连接建立的重要前提之一。
                view.requestFocus()
                // 等本轮 UI 操作完成后请求键盘。
                view.post {
                    // 页面移除或窗口失焦时不请求旧目标。
                    if (view.isAttachedToWindow && view.hasFocus() && view.hasWindowFocus()) {
                        // 使用当前 UI Context 的输入法服务。
                        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        // 这是请求，不宣称实际已经显示。
                        imm.showSoftInput(view, 0)
                    }
                }
                // 读取当前焦点链路快照。
                record("requestKeyboard attached=${view.isAttachedToWindow} viewFocus=${view.hasFocus()} windowFocus=${view.hasWindowFocus()}")
            }
        }) { Text("获取焦点并请求键盘") }
        // 不通过程序造按键，只读取当前控件状态。
        Button(onClick = {
            // 快照是本次点击时的值，不是持续观察器。
            input?.let { record("snapshot attached=${it.isAttachedToWindow} viewFocus=${it.hasFocus()} windowFocus=${it.hasWindowFocus()} selection=${it.selectionStart}..${it.selectionEnd}") }
        }) { Text("记录焦点快照") }
        // 仅重置调试列表，避免 setText 影响实验。
        Button(onClick = { events = emptyList() }) { Text("清空页面日志") }
        // 等宽显示最近事件，详细历史到 Logcat 查阅。
        Text(events.joinToString("\n").ifEmpty { "等待输入事件…" }, fontFamily = FontFamily.Monospace)
        // 解释事件之间的层次关系。
        Text("commitText 是确认编辑内容，不是业务提交；preIME 不是所有软键盘输入的入口。")
        // 明确跟踪器覆盖范围，避免以缺失事件直接认定输入法故障。
        Text("这里只覆盖常见旧版 InputConnection 方法；IME 也可能使用新版重载、批量编辑或其他删除接口。没看到某条日志，不代表没有输入。")
    }
}
