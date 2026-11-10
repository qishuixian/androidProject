// Day42 演示 Android 主动调用本地网页函数，不包含 JS 调 Native 接口。
package com.example.myapplication.day42

// 显式 Intent 打开示例 Activity。
import android.content.Intent
// Bundle 用于 Activity 标准创建回调。
import android.os.Bundle
// Logcat 记录 evaluateJavascript 的原始 JSON 返回值。
import android.util.Log
// 销毁时解除 WebView 与父布局的关系。
import android.view.ViewGroup
// WebView API 提供 JS 执行与页面加载监听。
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
// 原生控件放在网页外面，明确区分 Native 按钮与 HTML 内容。
import android.widget.Button as NativeButton
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
// 独立 Activity 提供窗口和生命周期。
import androidx.activity.ComponentActivity
// Compose 只负责首页入口。
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
// 根据系统栏 Insets 保留内容安全区域。
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
// JSON 编码和解析避免直接拼接用户文本造成脚本语法错误或注入。
import org.json.JSONObject
import org.json.JSONTokener

// 首页调用此组件，点击后才创建 WebView 所在的 Activity。
@Composable
internal fun NativeJavaScriptDemo() {
    // 当前 Context 来自 Compose 宿主。
    val context = LocalContext.current
    // 使用显式类名打开已在 Manifest 注册的 Activity。
    Button(onClick = { context.startActivity(Intent(context, NativeJavaScriptActivity::class.java)) }) {
        // 描述本课通信方向。
        Text("打开 Day42：Native 调 JavaScript")
    }
}

// 新 Activity 与 Day41 独立，避免修改上一课的行为。
class NativeJavaScriptActivity : ComponentActivity() {
    // WebView 属于当前 Activity，不交给单例或 ViewModel 保存。
    private lateinit var webView: WebView
    // 标识当前实例是否已销毁，防止迟到的回调更新旧界面。
    private var disposed = false

    // 创建原生操作区和本地网页。
    override fun onCreate(savedInstanceState: Bundle?) {
        // 先完成父类初始化。
        super.onCreate(savedInstanceState)
        // 页面结构为输入框、调用按钮、返回值文字和网页。
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        // 处理 Android 边到边布局中的系统栏遮挡。
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            // 获取状态栏与导航栏占用区域。
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 根布局为子控件留出安全位置。
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            // 不吞掉 Insets。
            insets
        }
        // 默认输入带双引号，方便验证 JSON 编码。
        val input = EditText(this).apply { setText("Kotlin \"课程\"") }
        // 这是 Android 文字控件，显示 JS 回传的结果。
        val result = TextView(this).apply { text = "等待网页加载…" }
        // 加载前禁用按钮，避免网页函数尚未定义。
        val callButton = NativeButton(this).apply { text = "Native 调用网页函数"; isEnabled = false }
        // 按顺序添加操作区。
        root.addView(input)
        root.addView(callButton)
        root.addView(result)
        // 创建真正执行网页脚本的 WebView。
        webView = WebView(this)
        // 本课需要执行自己打包的脚本，因此显式开启 JavaScript。
        webView.settings.javaScriptEnabled = true
        // 只让示例访问本课固定本地页面。
        webView.webViewClient = object : WebViewClient() {
            // 拦截本示例之外的网页跳转。
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                request.url.toString() != PAGE_URL

            // 每次开始加载先禁止调用，以免操作尚未准备好的页面。
            override fun onPageStarted(view: WebView, url: String?, favicon: android.graphics.Bitmap?) {
                callButton.isEnabled = false
            }

            // 对本课同步内联脚本，用实际函数检查判断是否可调用。
            override fun onPageFinished(view: WebView, url: String?) {
                // evaluateJavascript 是异步执行，结果通过 UI 线程回调交付。
                view.evaluateJavascript("typeof window.receiveCourse === 'function'") { ready ->
                    // 退出后不再更新 View。
                    if (!disposed) {
                        // JS 布尔值会编码为 JSON 文本 true 或 false。
                        callButton.isEnabled = ready == "true"
                        // 不把单纯 onPageFinished 当成桥接函数必然就绪。
                        result.text = if (ready == "true") "网页函数已就绪" else "网页函数未就绪"
                    }
                }
            }
        }
        // 原生按钮点击才构造参数并执行脚本。
        callButton.setOnClickListener {
            // JSONObject 编码输入中的双引号、反斜杠和换行，不手工拼接 JS 字符串。
            val payload = JSONObject().put("id", "kotlin-42").put("title", input.text.toString())
            // JSON 对象表达式作为函数参数；同步返回值由 evaluateJavascript 回调接收。
            webView.evaluateJavascript("window.receiveCourse($payload)") { rawJson ->
                // 旧 Activity 已销毁就忽略迟到的结果。
                if (!disposed) {
                    // 回调给的是 JSON 编码文本，不一定是可直接展示的普通字符串。
                    Log.d("Day42", "JS 原始返回：$rawJson")
                    // 先解码，再按约定检查返回对象结构。
                    val reply = runCatching { JSONTokener(rawJson).nextValue() as? JSONObject }.getOrNull()
                    // 如果函数不存在或返回结构不符合约定，展示失败信息。
                    result.text = if (reply == null) "返回格式不符合约定：$rawJson"
                    else "JS 返回：${reply.optString("message")}；次数：${reply.optInt("count")}"
                }
            }
        }
        // 网页占用操作区下方剩余高度。
        root.addView(webView, LinearLayout.LayoutParams(-1, 0, 1f))
        // 设置 Activity 内容后加载固定网页。
        setContentView(root)
        // 本课重建会重新加载，网页内计数从零开始，不假装保存 JS 内存。
        webView.loadUrl(PAGE_URL)
    }

    // 宿主可交互时通知 WebView。
    override fun onResume() {
        super.onResume()
        // 不在这里重新 loadUrl，避免后台返回时重置网页。
        webView.onResume()
    }

    // 宿主暂停时通知当前 WebView。
    override fun onPause() {
        webView.onPause()
        // onPause 不会冻结网页所有 JavaScript。
        super.onPause()
    }

    // 退出或配置重建时释放旧 WebView。
    override fun onDestroy() {
        // 先让异步结果回调失效。
        disposed = true
        // 停止加载并解除捕获按钮等 View 的 Client 引用。
        webView.stopLoading()
        webView.webViewClient = WebViewClient()
        // 销毁前从父布局移除。
        (webView.parent as? ViewGroup)?.removeView(webView)
        // 后续不得再使用此 WebView 实例。
        webView.destroy()
        super.onDestroy()
    }

    // 仅保存固定 URL，不持有窗口或 Context。
    companion object {
        private const val PAGE_URL = "file:///android_asset/day42/index.html"
    }
}
