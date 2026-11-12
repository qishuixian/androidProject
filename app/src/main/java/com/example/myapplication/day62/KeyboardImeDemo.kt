// Day62 使用真实输入控件和系统键盘接口，不接业务网络。
package com.example.myapplication.day62

// 传统键盘服务与 IME 动作常量。
import android.content.Context
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
// 控件文字变化监听会在 View 释放时移除。
import android.text.TextWatcher
import androidx.core.widget.doAfterTextChanged
// 纵向排列实验，滚动由首页负责。
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
// Compose 的键盘配置和提交回调。
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
// 输入框与操作按钮。
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
// 记忆可恢复内容、临时引用与最新回调。
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
// FocusRequester 处理焦点，keyboardController 处理键盘请求。
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
// TextFieldValue 保留选区与本次输入法的组合范围。
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
// 嵌入传统 View。
import androidx.compose.ui.viewinterop.AndroidView

// 首页进入的真实键盘与 IME 实验。
@Composable
internal fun KeyboardImeDemo() {
    // 只保存传统框文字，不能保存 EditText 本身。
    var nativeText by rememberSaveable { mutableStateOf("") }
    // Compose 保存文本和选区；组合会话不保证跨重建恢复。
    var value by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    // 当前传统输入框是临时引用。
    var nativeView by remember { mutableStateOf<EditText?>(null) }
    // 用于移除传统框文字监听，不参与恢复。
    var watcher by remember { mutableStateOf<TextWatcher?>(null) }
    // 请求器与当前组合绑定。
    val requester = remember { FocusRequester() }
    // 控制 Compose 软键盘，某些环境下可能为空。
    val keyboard = LocalSoftwareKeyboardController.current
    // 管理 Compose 焦点。
    val focusManager = LocalFocusManager.current
    // 展示键盘请求或提交结果，不把请求当成显示成功。
    var message by rememberSaveable { mutableStateOf("尚未提交") }
    // 只有通过校验的显式提交才增加次数。
    var submissions by rememberSaveable { mutableIntStateOf(0) }
    // 统一业务入口：文字变化不会调用它。
    fun submit(source: String, text: String) {
        // 空白校验在提交时处理，不在输入过程中 trim 并回写。
        if (text.isBlank()) {
            // 不发送空输入。
            message = "$source：内容为空，未提交"
        } else {
            // 本课仅在本地记录提交，不调用接口。
            submissions++
            // 显示来源，便于区分按钮和 IME。
            message = "$source：第 $submissions 次提交「$text」"
        }
    }
    // View 的长期监听通过状态容器读取最新的处理函数。
    val onNativeText by rememberUpdatedState<(String) -> Unit>({ nativeText = it })
    // 提交函数依赖当前页面状态，用最新回调避免旧闭包。
    val onNativeDone by rememberUpdatedState<(String) -> Unit>({ submit("EditText IME Done", it) })
    // 外层页面提供滚动，避免嵌套同方向滚动容器。
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 本课使用真实 IME，不模拟虚假的键盘显示结果。
        Text("Day62 · 键盘与 IME")
        // 显示提交统计，与输入事件区分。
        Text("$message；成功提交次数=$submissions")
        // AndroidView 创建传统单行输入框。
        AndroidView(
            // 创建时恢复文字并安装一次监听。
            factory = { context ->
                // apply 配置后返回这个输入框。
                EditText(context).apply {
                    // 单行输入采用 Done 动作。
                    setSingleLine(true)
                    // hint 不是实际文字。
                    hint = "EditText：尝试中文输入和完成键"
                    // 配置输入法动作按钮；实际外观由 IME 决定。
                    imeOptions = EditorInfo.IME_ACTION_DONE
                    // 在安装监听前恢复已有文字。
                    setText(nativeText)
                    // 初始化光标在末尾。
                    setSelection(length())
                    // 文字变化只同步内容，不触发提交。
                    watcher = doAfterTextChanged { onNativeText(it?.toString().orEmpty()) }
                    // 只处理明确的 Done 动作；硬件按键留待 Day63。
                    setOnEditorActionListener { view, actionId, _ ->
                        // actionId 是 IME 动作，不是 keyCode。
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            // 此刻读取控件真实文字，不依赖旧捕获值。
                            onNativeDone(view.text.toString())
                            // 消费本次动作，避免继续交给默认处理。
                            true
                        } else {
                            // 其他动作保留系统默认行为。
                            false
                        }
                    }
                    // 保存临时引用，供按钮请求焦点和键盘。
                    nativeView = this
                }
            },
            // 故意不在 update 中无条件 setText，避免破坏 IME 组合输入。
            modifier = Modifier.fillMaxWidth(),
            // 离开页面时清理监听与引用。
            onRelease = { view ->
                // 只有仍聚焦的旧输入框才请求隐藏自己的键盘。
                if (view.hasFocus()) {
                    // 获取与 UI Context 关联的系统服务。
                    val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    // 使用当前窗口令牌请求隐藏。
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
                // 移除文字监听。
                watcher?.let { view.removeTextChangedListener(it) }
                // 清理 editor action 回调。
                view.setOnEditorActionListener(null)
                // 清理当前临时引用。
                if (nativeView === view) { nativeView = null; watcher = null }
            }
        )
        // 输入只改变这份内容，不增加提交次数。
        Text("EditText 当前文字：$nativeText")
        // 请求键盘前先请求输入框焦点。
        Button(onClick = {
            // 取得本次点击时的有效输入框。
            nativeView?.let { view ->
                // 从 Compose 输入区切换到传统输入区。
                focusManager.clearFocus(force = true)
                // 焦点成功只是键盘显示的前提之一。
                view.requestFocus()
                // 等本次 UI 操作后再向系统发起请求。
                view.post {
                    // 页面退出后不继续请求旧窗口。
                    if (view.isAttachedToWindow && view.hasFocus() && view.hasWindowFocus()) {
                        // 不使用 applicationContext 创建或操作输入控件。
                        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        // 返回值不等于键盘已实际显示；硬件键盘和系统策略会影响结果。
                        imm.showSoftInput(view, 0)
                    }
                }
                // 明确这里只记录请求，不能据此推断显示成功。
                message = "已请求 EditText 焦点和软键盘，请观察屏幕"
            }
        }) { Text("EditText 获取焦点并请求键盘") }
        // Compose 将整个输入值作为状态。
        OutlinedTextField(
            // 保留文字、选区与 composition。
            value = value,
            // 不能用 TextFieldValue(it.text) 替代，否则组合区可能丢失。
            onValueChange = { value = it },
            // 标签说明操作目标。
            label = { Text("Compose：输入拼音，观察 composition") },
            // 单行配合 Done 动作。
            singleLine = true,
            // 请求输入法提供完成动作。
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            // IME 动作与普通文字变化是不同事件。
            keyboardActions = KeyboardActions(onDone = {
                // 业务读取最新文本，不把每个字符当成提交。
                submit("TextField IME Done", value.text)
            }),
            // 将请求器连接到实际输入框。
            modifier = Modifier.fillMaxWidth().focusRequester(requester)
        )
        // composition 非空表示输入法报告了组合范围，不是业务提交标志。
        Text("text=${value.text}；selection=${value.selection}；composition=${value.composition ?: "无组合范围"}")
        // 点击只改变焦点和请求键盘，不提交。
        Button(onClick = {
            // 避免传统框继续占据焦点。
            nativeView?.clearFocus()
            // 先将焦点交给已进入组合的 TextField。
            requester.requestFocus()
            // 向系统提出软键盘显示请求。
            keyboard?.show()
            // 请求成功与实际显示必须分别验证。
            message = "已请求 TextField 焦点和软键盘，请观察屏幕"
        }) { Text("TextField 获取焦点并请求键盘") }
        // 独立按钮复用业务入口，不冒充 IME 动作。
        Button(onClick = { submit("页面按钮", value.text) }) { Text("按钮提交 Compose 内容") }
        // 隐藏键盘时不主动清焦点，观察两个状态的区别。
        Button(onClick = {
            // Compose 控制器请求隐藏。
            keyboard?.hide()
            // 传统服务也可通过窗口令牌发起隐藏请求。
            nativeView?.let { view ->
                // 取得系统键盘服务。
                val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                // 当前窗口令牌用于定位输入窗口。
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            // 不宣称输入框已经失焦。
            message = "已请求隐藏键盘，未主动清除焦点"
        }) { Text("隐藏键盘，保留焦点") }
        // 给出中文输入的实际观察步骤。
        Text("中文 IME：输入拼音 → 观察组合范围 → 选词 → 再点完成。选词不等于业务提交；不同输入法上报时机可能不同。")
        // 明确实验边界，不把软键盘等同于按键流。
        Text("按返回通常先收起键盘；隐藏不等于失焦。软键盘输入不保证发送 ACTION_DOWN，硬件按键与 InputConnection 在 Day63 继续。")
    }
}
