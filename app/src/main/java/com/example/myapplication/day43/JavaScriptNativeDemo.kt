// Day43 在受控本地页面中演示 JS 主动调用 Native。
package com.example.myapplication.day43

// Intent 与 Bundle 用于标准 Activity 启动和创建。
import android.content.Intent
import android.os.Bundle
// 日志输出实际桥接线程，避免把 WebView 线程当作 UI 线程。
import android.util.Log
// 用于销毁前移除 WebView。
import android.view.ViewGroup
// 注解声明允许 JavaScript 访问的方法。
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
// 原生状态条与网页分开显示。
import android.widget.LinearLayout
import android.widget.TextView
// 独立 Activity 承载网页。
import androidx.activity.ComponentActivity
// Compose 仅提供首页入口。
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
// Insets 为系统栏保留空间。
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
// JSON 是双方约定的消息格式，不执行网页传入的代码。
import org.json.JSONObject

// 首页点击后进入桥接实验页面。
@Composable
internal fun JavaScriptNativeDemo() {
    // 取得宿主 Context，构造显式 Intent。
    val context = LocalContext.current
    Button(onClick = { context.startActivity(Intent(context, JavaScriptNativeActivity::class.java)) }) {
        // 表明今天由网页发起，而不是原生按钮发起。
        Text("打开 Day43：JavaScript 调 Native")
    }
}

// Activity 管理桥接对象、网页与 UI 生命周期。
class JavaScriptNativeActivity : ComponentActivity() {
    // 旧 WebView 不跨 Activity 重建保存。
    private lateinit var webView: WebView
    // 仅在主线程检查和修改，过滤销毁后的待执行 UI 任务。
    private var disposed = false

    // 创建本地页面和最小化桥接接口。
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 状态条位于网页上方，便于分辨哪一端更新了内容。
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        // 防止系统栏遮挡页面按钮。
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        // 初始文字由 Native 设置，后续由桥接事件更新。
        val status = TextView(this).apply { text = "等待网页请求…" }
        root.addView(status)
        // 创建当前窗口所属的 WebView。
        webView = WebView(this)
        // 本课受控页面需要 JS；不向任意外部页面提供桥接对象。
        webView.settings.javaScriptEnabled = true
        // 限制导航，页面无 iframe、远程脚本或外部子资源。
        webView.webViewClient = object : WebViewClient() {
            // 所有非本课固定地址的导航都被拦截。
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                request.url.toString() != PAGE_URL
        }
        // 在加载之前注册：网页中的 window.AndroidCourse 对应这个 Kotlin 对象。
        webView.addJavascriptInterface(CourseBridge { raw ->
            // 本回调来自 WebView 私有后台线程，不能直接更新 TextView。
            Log.d("Day43", "桥接线程：${Thread.currentThread().name}；请求：$raw")
            // 切回 UI 线程，网页调用本身不会同步等待这个 UI 结果。
            runOnUiThread {
                // Activity 已退出就丢弃任务，不再使用销毁的 WebView。
                if (!disposed) {
                    // 解析 JSON，异常输入转为错误结果而不是崩溃。
                    val request = runCatching { JSONObject(raw) }.getOrNull()
                    // 请求 id 仅用于关联本次响应，不是课程 id。
                    val requestId = request?.optString("requestId").orEmpty()
                    val courseId = request?.optString("courseId").orEmpty()
                    // 本课只允许一个明确动作和一个固定课程，不暴露任意 Native 能力。
                    val valid = request?.optString("action") == "complete" &&
                        courseId == "kotlin-43" && requestId.isNotBlank() && requestId.length <= 80
                    // 业务演示只更新界面，不写数据库或请求网络。
                    status.text = if (valid) "Native：$courseId 已完成" else "Native：请求参数无效"
                    // 构造统一响应对象，字符串交给 JSONObject 编码。
                    val response = JSONObject().put("requestId", requestId).put("ok", valid)
                        .put("message", if (valid) "Android 已处理课程完成" else "请求参数无效")
                    // 复用 Day42 的方向把结果回传网页，形成异步闭环。
                    webView.evaluateJavascript("window.onNativeResult($response)", null)
                }
            }
        }, "AndroidCourse")
        // WebView 占据状态条下方剩余空间。
        root.addView(webView, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        // 注册完成再加载，否则网页初始化时可能找不到对象。
        webView.loadUrl(PAGE_URL)
    }

    // 恢复当前 WebView，不重新加载页面。
    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    // 暂停当前控件，不使用影响所有 WebView 的 pauseTimers。
    override fun onPause() {
        webView.onPause()
        super.onPause()
    }

    // 旋转或退出时解除桥接引用并释放旧窗口资源。
    override fun onDestroy() {
        disposed = true
        // 移除对象，避免旧页面继续调用桥接；已有排队任务仍需 disposed 检查。
        webView.removeJavascriptInterface("AndroidCourse")
        webView.stopLoading()
        webView.webViewClient = WebViewClient()
        (webView.parent as? ViewGroup)?.removeView(webView)
        webView.destroy()
        super.onDestroy()
    }

    // 常量只保存受控地址，不持有 Context。
    companion object {
        private const val PAGE_URL = "file:///android_asset/day43/index.html"
    }
}

// 桥接类本身不直接操作 View，由 Activity 负责切线程和业务处理。
class CourseBridge(private val onRequest: (String) -> Unit) {
    // 只有显式注解的 public 方法可供 JS 调用；不把整个 Activity 暴露给 JS。
    @JavascriptInterface
    fun postMessage(raw: String) {
        // 限制请求体长度，避免把任意大输入直接交给 UI 解析。
        if (raw.length <= 4096) onRequest(raw)
    }
}
