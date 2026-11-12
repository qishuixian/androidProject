// Day64 通过原生 View 与本地 HTML 演示双输入系统，不使用真实 Laya 引擎。
package com.example.myapplication.day64

// Activity 生命周期与 UI 线程延迟任务。
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
// Android 视图、输入法和本地网页。
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button as NativeButton
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
// 生命周期宿主与课程入口。
// 适配当前 target SDK 的状态栏、导航栏和键盘区域。
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.ComponentActivity
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
// 固定本地调试事件，不记录用户正文。
import android.util.Log

// 首页只负责打开独立实验页，避免 WebView 与课程滚动相互干扰。
@Composable
internal fun DualInputDemo() {
    // 启动 Activity 使用当前 UI Context。
    val context = LocalContext.current
    // 进入后操作真实的原生和网页输入框。
    Button(onClick = { context.startActivity(Intent(context, DualInputActivity::class.java)) }) {
        // 按钮说明模拟范围。
        Text("打开 Day64：Native 与模拟 Laya 输入")
    }
}

// 两套输入系统共享同一宿主窗口与系统 IME。
class DualInputActivity : ComponentActivity() {
    // 保存本次 Activity 中的控件，不放进全局或 ViewModel。
    private lateinit var nativeInput: EditText
    private lateinit var web: WebView
    private lateinit var status: TextView
    private lateinit var webButton: NativeButton
    // 主线程处理延迟焦点请求。
    private val handler = Handler(Looper.getMainLooper())
    // 每次选择输入目标都推进代次，过滤旧请求。
    private var generation = 0
    // 网页加载完成前不执行 DOM 操作。
    private var ready = false
    // 销毁后不继续访问旧 View。
    private var disposed = false

    // 创建页面与两个输入控件。
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        // 保留父类初始化逻辑。
        super.onCreate(savedInstanceState)
        // 使用传统线性布局明确两套控件边界。
        val root = LinearLayout(this).apply {
            // 内容纵向排列。
            orientation = LinearLayout.VERTICAL
            // 留出易读的边距。
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        // 用滚动容器适应手机及软键盘出现后的可用区域。
        val scroll = ScrollView(this).apply { addView(root) }
        // 将根容器设置为 Activity 内容。
        setContentView(scroll)
        // 键盘出现后仍让滚动区位于可用窗口内。
        ViewCompat.setOnApplyWindowInsetsListener(scroll) { view, insets ->
            // 合并系统栏与键盘底部占用，避免操作按钮被遮挡。
            val space = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            // 保留控件内部的 dp 边距，外层只处理系统区域。
            view.setPadding(space.left, space.top, space.right, space.bottom)
            // 不消费，允许子视图继续感知窗口状态。
            insets
        }
        // 标题明确没有真实引擎。
        root.addView(TextView(this).apply { text = "Day64 · Native 与模拟 Laya 输入\n网页使用本地 HTML input，没有真实 Laya SDK。" })
        // 状态用于展示请求与事件，不伪称键盘显示成功。
        status = TextView(this).apply { text = "等待网页加载；请使用测试文字。" }
        // 在顶部持续展示最近一次动作。
        root.addView(status)
        // 创建真实原生单行输入框。
        nativeInput = EditText(this).apply {
            // 与网页输入框各自保存内容。
            hint = "Native EditText"
            // 单行便于观察切换。
            setSingleLine(true)
            // 用户直接点击原生框也使之前排队的请求失效。
            setOnTouchListener { _, event ->
                // 触摸开始只推进代次，不吞掉输入行为。
                if (event.actionMasked == MotionEvent.ACTION_DOWN) generation++
                // 保留系统触摸和点击处理。
                false
            }
            // 焦点变化不等于输入法显隐变化。
            setOnFocusChangeListener { _, focused -> show("Native View focus=$focused") }
        }
        // 将原生输入框加入页面。
        root.addView(nativeInput)
        // 显式交接到 Native，先处理网页 DOM 失焦。
        root.addView(button("交接到 Native 输入框") { focusNative() })
        // 网页未就绪时禁用入口。
        webButton = button("交接到模拟 Laya 输入框") { focusWeb() }.apply { isEnabled = false }
        // 先放交接按钮，再放网页。
        root.addView(webButton)
        // WebView 是真实网页容器，里面的 input 由浏览器管理。
        web = WebView(this).apply {
            // 只执行本地固定脚本，不注册 JavaScriptInterface。
            settings.javaScriptEnabled = true
            // 页面从资产读成字符串，不需要文件系统访问能力。
            settings.allowFileAccess = false
            // 不允许通过 content URL 读取设备数据。
            settings.allowContentAccess = false
            // 仅接收固定前缀的调试消息。
            webChromeClient = object : WebChromeClient() {
                // WebView 控制台事件在 UI 线程处理。
                override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                    // 限定本地页面已知事件，避免记录输入正文。
                    val event = message.message().removePrefix("DAY64:")
                    // 只接受固定枚举。
                    if (message.message().startsWith("DAY64:") && event in setOf("DOM_FOCUS", "DOM_BLUR", "COMPOSITION_START", "COMPOSITION_END")) {
                        // 事件仅用于观察，不反向抢焦点。
                        show("网页事件=$event")
                        // 表示该控制台消息已处理。
                        return true
                    }
                    // 其他消息交给默认处理。
                    return super.onConsoleMessage(message)
                }
            }
            // 加载结束后才允许操作固定 DOM。
            webViewClient = object : WebViewClient() {
                // 本课只加载一份本地固定页面。
                override fun onPageFinished(view: WebView, url: String?) {
                    // 忽略销毁后的回调。
                    if (disposed) return
                    // 标记可执行 DOM 操作。
                    ready = true
                    // 开放交接按钮。
                    webButton.isEnabled = true
                    // 明确只代表页面加载，不代表键盘或输入法会话已建立。
                    show("本地网页加载完成，可以切换输入目标")
                }
            }
            // 用户直接点网页时，使旧延迟请求失效。
            setOnTouchListener { _, event ->
                // 不阻断 DOM 自己的点击与输入。
                if (event.actionMasked == MotionEvent.ACTION_DOWN) generation++
                // 必须返回 false 保留网页交互。
                false
            }
        }
        // 网页有明确高度，不占满整个窗口。
        root.addView(web, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(230)))
        // 三秒延迟给用户留出切换目标的时间。
        root.addView(button("排队旧 Native 请求（无保护，3 秒后抢回焦点）") { scheduleNative(false) })
        // 同样任务加上请求代次检查，形成可复现对照。
        root.addView(button("排队旧 Native 请求（代次保护）") { scheduleNative(true) })
        // 快照区分 View 焦点与 DOM 焦点。
        root.addView(button("读取 View / DOM 焦点快照") {
            // JS 执行前先保存快照请求所属代次。
            val captured = generation
            // 只读取固定字段，不把用户输入带回 Native。
            web.evaluateJavascript("document.activeElement ? document.activeElement.id : ''") { active ->
                // 迟到的快照也不覆盖新会话状态。
                if (!disposed && captured == generation) show("Native焦点=${nativeInput.hasFocus()} WebView焦点=${web.hasFocus()} DOM=$active window焦点=${web.hasWindowFocus()}")
            }
        })
        // 隐藏只作用于键盘，不清空文字或自动改输入所有权。
        root.addView(button("请求隐藏键盘（不清除输入内容）") {
            // 当前窗口共用一个系统 IME。
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            // 请求隐藏；不宣称此时已经不可见。
            imm.hideSoftInputFromWindow(web.windowToken, 0)
            // 和焦点快照对照理解。
            show("已请求隐藏键盘；焦点和文字未主动清除")
        })
        // 内容从本地资产读取，不加载远程页面。
        val html = assets.open("day64/input.html").bufferedReader().use { it.readText() }
        // 虚拟基址仅确定网页来源；CSP 禁止任何网络子资源。
        web.loadDataWithBaseURL("https://day64.invalid/", html, "text/html", "UTF-8", null)
    }

    // dp 转像素供传统布局使用。
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    // 创建统一按钮，点击后执行指定实验动作。
    private fun button(label: String, action: () -> Unit) = NativeButton(this).apply {
        // 使用明确的中文说明。
        text = label
        // 将 Android 点击事件转交实验函数。
        setOnClickListener { action() }
    }

    // 仅记录固定调试事件与状态，不记录输入文字。
    private fun show(message: String) {
        // 生命周期结束后停止更新 View。
        if (disposed) return
        // 带上代次便于追踪迟到请求。
        status.text = "generation=$generation\n$message"
        // Logcat 保留完整观察历史。
        Log.d("Day64", "generation=$generation $message")
    }

    // 请求 Native 焦点：先释放网页 DOM，再请求系统键盘。
    private fun focusNative() {
        // 一个新的用户目标选择使旧请求失效。
        val captured = ++generation
        // 处理网页还没加载的情况，不访问不存在的 DOM。
        if (!ready) { activateNative(captured); return }
        // DOM blur 是异步操作，必须在回调里检查是否仍是当前请求。
        web.evaluateJavascript("document.getElementById('engineInput').blur()") {
            // 只执行最新请求，避免快速点击时旧回调抢焦点。
            if (!disposed && captured == generation) activateNative(captured)
        }
    }

    // 已完成网页侧释放后，执行原生侧聚焦。
    private fun activateNative(captured: Int) {
        // 双重校验保证异步阶段没有切换目标。
        if (disposed || captured != generation) return
        // WebView 焦点与 DOM 焦点都要考虑。
        web.clearFocus()
        // 把输入目标交给原生输入框。
        nativeInput.requestFocus()
        // 请求当前原生控件的软键盘。
        requestKeyboard(nativeInput)
        // 不把 showSoftInput 请求视为实际显示成功。
        show("目标=Native，已请求焦点和键盘")
    }

    // 请求模拟引擎输入目标：View 焦点和 DOM 焦点缺一不可。
    private fun focusWeb() {
        // 网页未就绪或 Activity 销毁时不发请求。
        if (!ready || disposed) return
        // 捕获当前目标选择所属代次。
        val captured = ++generation
        // 释放旧原生输入框，不清空文字。
        nativeInput.clearFocus()
        // 先使网页容器成为可接收输入的 View。
        web.requestFocus()
        // 再把 DOM 焦点交给固定输入框。
        web.evaluateJavascript("document.getElementById('engineInput').focus(); document.activeElement.id") { active ->
            // 迟到回调不恢复旧目标。
            if (!disposed && captured == generation) {
                // 请求键盘仍受系统/输入法策略约束。
                requestKeyboard(web)
                // DOM 回传用于确认真实目标，而非假定 focus 一定成功。
                show("目标=模拟Laya，DOM=$active；已请求键盘")
            }
        }
    }

    // 对当前有效 View 发起键盘请求，不自动切换其他焦点。
    private fun requestKeyboard(view: android.view.View) {
        // 窗口或 View 尚未就绪时不强制显示。
        if (!view.isAttachedToWindow || !view.hasFocus() || !view.hasWindowFocus()) return
        // 使用当前 Activity 的输入法服务。
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        // 不使用已弃用的显示标志。
        imm.showSoftInput(view, 0)
    }

    // 通过可控延迟模拟旧监听/旧异步任务回调。
    private fun scheduleNative(guarded: Boolean) {
        // 任务捕获排队时的代次，不在执行时假装属于新目标。
        val captured = ++generation
        // 提示在三秒内切换到网页输入框。
        show("旧请求已排队：请在 3 秒内交接到网页；保护=$guarded")
        // 延迟任务跟随 Activity，销毁时统一移除。
        handler.postDelayed({
            // 无论是否保护都不能访问销毁后的页面。
            if (disposed) return@postDelayed
            // 有保护时，切换目标后旧任务被忽略。
            if (guarded && captured != generation) {
                // 对照无保护时的抢焦点现象。
                show("已忽略旧焦点请求 captured=$captured")
            } else {
                // 无保护故意重新发起 Native 请求，演示迟到任务覆盖用户选择。
                focusNative()
            }
        }, 3000)
    }

    // 离开前台时让待处理的旧焦点请求失效。
    override fun onPause() {
        // 推进请求代次。
        generation++
        // 无保护实验也不应跨后台继续抢焦点。
        handler.removeCallbacksAndMessages(null)
        // 保留父类生命周期处理。
        super.onPause()
    }

    // 清理 WebView、延迟任务和旧窗口引用。
    override fun onDestroy() {
        // 在任何资源释放之前禁止回调更新。
        disposed = true
        // 使所有已捕获代次失效。
        generation++
        // 本 Handler 仅用于当前页面实验。
        handler.removeCallbacksAndMessages(null)
        // 停止页面加载。
        web.stopLoading()
        // 从父容器移除再销毁。
        (web.parent as? ViewGroup)?.removeView(web)
        // 释放网页运行期资源；旋转后需要新建。
        web.destroy()
        // 完成宿主清理。
        super.onDestroy()
    }
}
