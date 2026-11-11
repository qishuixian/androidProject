// Day61 对照传统 EditText 与 Compose TextField，使用真实输入控件。
package com.example.myapplication.day61

// 传统输入控件需要 UI Context 与文本变化监听。
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
// 按纵向排列两个独立输入实验。
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
// Compose 输入框与按钮。
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
// 记忆状态、View 引用与焦点请求器。
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
// Compose 焦点与传统 View 焦点分别操作。
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
// 文本和选区一起管理，避免只保存字符串而丢失光标。
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
// 将传统控件接入 Compose。
import androidx.compose.ui.viewinterop.AndroidView

// 传统输入框把文本和选区事件统一通知给页面。
private class SelectionEditText(context: Context) : EditText(context) {
    // 页面提供回调；构造早期或释放后可能为空。
    var onValueChanged: ((TextFieldValue) -> Unit)? = null
    // 程序回写时抑制监听，防止 setText 引发反馈循环。
    private var synchronizing = false
    // 文本变化时也通知，覆盖内容变化但光标未变化的情况。
    private val watcher = object : TextWatcher {
        // 修改前不处理业务。
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        // 修改中不重复同步，等待修改完成。
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        // 用户修改完成后发布当前内容和选区。
        override fun afterTextChanged(s: Editable?) { publishValue() }
    }
    // 创建控件时安装一次监听，不在每次重组时重复安装。
    init {
        // 单行输入便于本课观察光标。
        setSingleLine(true)
        // 安装文本监听。
        addTextChangedListener(watcher)
    }
    // 用户移动光标或长按选择时，系统也会调用此方法。
    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        // 保留父类的选择行为。
        super.onSelectionChanged(selStart, selEnd)
        // 构造期间无回调时不发布事件。
        publishValue()
    }
    // 从控件读取真实值，转换为页面状态。
    private fun publishValue() {
        // 程序回写时不把中间事件反向写回状态。
        if (synchronizing || onValueChanged == null) return
        // 读取文本，空引用按空字符串处理。
        val content = text?.toString().orEmpty()
        // 将可能的 -1 或越界位置限制到有效区间。
        val start = selectionStart.coerceIn(0, content.length)
        // selectionEnd 与 start 不一定相同，支持范围选择。
        val end = selectionEnd.coerceIn(0, content.length)
        // 只传文字与选区，不试图把传统 composing span 转成 Compose 状态。
        onValueChanged?.invoke(TextFieldValue(content, TextRange(start, end)))
    }
    // 页面主动清空、选中或恢复时，把状态同步到 View。
    fun sync(value: TextFieldValue) {
        // 同步过程中暂停反馈。
        synchronizing = true
        // 即使未来同步逻辑抛异常，也恢复监听开关。
        try {
            // 内容未改变时不能无条件 setText，否则影响选区和输入法组合文本。
            if (text.toString() != value.text) setText(value.text)
            // 将目标选区限制到当前文本长度。
            val start = value.selection.start.coerceIn(0, length())
            // 分别计算选区终点。
            val end = value.selection.end.coerceIn(0, length())
            // 只在选区变化时调用，避免无意义的反馈。
            if (selectionStart != start || selectionEnd != end) setSelection(start, end)
        } finally {
            // 后续真实用户操作重新正常通知。
            synchronizing = false
        }
    }
    // View 释放时移除监听与页面引用。
    fun release() {
        // 不再持有页面状态回调。
        onValueChanged = null
        // 移除文本监听。
        removeTextChangedListener(watcher)
    }
}

// 两个输入框各自保存内容，不互相覆盖，方便对照观察。
@Composable
internal fun TextInputDemo() {
    // 传统控件状态也放在页面，便于主动操作和旋转恢复。
    var nativeValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        // 初始光标放在文本末尾。
        mutableStateOf(TextFieldValue("Android", TextRange(7)))
    }
    // Compose 通过 TextFieldValue 同时保存文本与选区。
    var composeValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        // 两个框故意使用相同初始内容，但状态互相独立。
        mutableStateOf(TextFieldValue("Android", TextRange(7)))
    }
    // View 引用仅在当前组合有效，不参与保存。
    var nativeView by remember { mutableStateOf<SelectionEditText?>(null) }
    // Compose 的焦点请求器只在当前组合中复用。
    val requester = remember { FocusRequester() }
    // 当前焦点管理器负责清除 Compose 焦点。
    val focusManager = LocalFocusManager.current
    // 防止 View 的长期回调捕获过时的页面处理函数。
    val onNativeChange by rememberUpdatedState<(TextFieldValue) -> Unit>({ nativeValue = it })
    // 页面由外层 LazyColumn 滚动。
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 标题明确本课对照的控件。
        Text("Day61 · EditText 与 TextField")
        // 先说明两个框各管自己的内容。
        Text("两个框独立输入：观察文本、光标、选区和焦点。选区位置从 0 开始。")
        // 传统控件通过 AndroidView 创建和更新。
        AndroidView(
            // 每次 View 创建时配置一次。
            factory = { context ->
                // apply 完成配置后返回原控件。
                SelectionEditText(context).apply {
                    // 提示不会变成实际文本。
                    hint = "传统 EditText"
                    // 程序回写初始值，避免首次显示为空。
                    sync(nativeValue)
                    // 用户事件向上更新页面状态。
                    onValueChanged = { onNativeChange(it) }
                    // 保存临时引用，供焦点按钮使用。
                    nativeView = this
                }
            },
            // 重组时按需同步；不能每次都无条件 setText。
            update = { it.sync(nativeValue) },
            // 输入框占满页面可用宽度。
            modifier = Modifier.fillMaxWidth(),
            // 真正移除控件时清理回调与引用。
            onRelease = { view ->
                // 移除监听，避免保留旧页面。
                view.release()
                // 只清理当前正在释放的 View。
                if (nativeView === view) nativeView = null
            }
        )
        // 显示真实文本和选区；start=end 表示光标位置。
        Text("EditText：${nativeValue.text}；selection=${nativeValue.selection.start}..${nativeValue.selection.end}")
        // 聚焦传统 View，不保证键盘一定自动弹出。
        Button(onClick = { nativeView?.requestFocus() }) { Text("EditText 获取焦点") }
        // 改状态，再由 AndroidView.update 同步选区。
        Button(onClick = { nativeValue = nativeValue.copy(selection = TextRange(0, nativeValue.text.length)) }) { Text("EditText 全选") }
        // 清空内容和选区，但不主动改变焦点。
        Button(onClick = { nativeValue = TextFieldValue("") }) { Text("EditText 清空") }
        // Compose 控件直接由状态驱动。
        OutlinedTextField(
            // 文本、选区和输入法组合信息作为整体交给控件。
            value = composeValue,
            // 保留输入框返回的整个值，不只取其中 text。
            onValueChange = { composeValue = it },
            // 标签用于识别第二个输入框。
            label = { Text("Compose TextField") },
            // 单行便于比较。
            singleLine = true,
            // 绑定焦点请求器，按钮才能向该输入框请求焦点。
            modifier = Modifier.fillMaxWidth().focusRequester(requester)
        )
        // 展示 Compose 状态中的选区。
        Text("TextField：${composeValue.text}；selection=${composeValue.selection.start}..${composeValue.selection.end}")
        // 调用请求器，将焦点交给第二个输入框。
        Button(onClick = { requester.requestFocus() }) { Text("TextField 获取焦点") }
        // copy 保留文本，仅调整选区；明确结束旧的组合输入。
        Button(onClick = { composeValue = composeValue.copy(selection = TextRange(0, composeValue.text.length), composition = null) }) { Text("TextField 全选") }
        // 新建空值，文本与选区一起重置。
        Button(onClick = { composeValue = TextFieldValue("") }) { Text("TextField 清空") }
        // 两套焦点接口分别清理，不把焦点等同于键盘显隐。
        Button(onClick = {
            // 清除传统 View 的焦点。
            nativeView?.clearFocus()
            // 清除 Compose 的焦点。
            focusManager.clearFocus(force = true)
        }) { Text("清除焦点") }
        // 给下一课保留键盘与组合文本主题。
        Text("焦点、文本、选区是不同状态；获取焦点不等于保证弹出键盘，清空也不等于失焦。键盘与 IME 在 Day62 继续。")
    }
}
