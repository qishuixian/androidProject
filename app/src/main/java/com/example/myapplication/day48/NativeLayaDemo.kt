// Day48 模拟 Native 主动调用 Laya 页面。
package com.example.myapplication.day48

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button as NativeButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.json.JSONObject

// 首页入口：打开 Native 调 Laya 的模拟页面。
@Composable
internal fun NativeLayaDemo() {
    // 使用当前宿主 Context 启动 Activity。
    val context = LocalContext.current
    Button(onClick = { context.startActivity(Intent(context, NativeLayaActivity::class.java)) }) {
        // 明确本课通信方向。
        Text("打开 Day48：Native 调 Laya（模拟）")
    }
}

// Activity 提供原生按钮，并承载模拟 Laya WebView。
class NativeLayaActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private lateinit var status: TextView
    // 生命周期结束后忽略迟到回调。
    private var disposed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Native 控制区位于模拟 Laya 页面上方。
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        status = TextView(this).apply { text = "等待 Native 协议调用…" }
        root.addView(status)
        // 这个按钮属于 Native，点击后调用网页中的固定函数。
        val sendButton = NativeButton(this).apply {
            // 等实际函数检查通过后才允许发送。
            isEnabled = false
            text = "Native 发送课程高亮协议"
            setOnClickListener {
                // 协议号和 payload 分开，便于版本化和日志追踪。
                val payload = JSONObject().put("courseId", "kotlin-48").put("color", "gold")
                // JSON 对象作为脚本参数传给网页，返回值通过回调异步处理。
                webView.evaluateJavascript("window.onNativeProtocol(1001, $payload)") { raw ->
                    if (!disposed) status.text = "Laya 回调：$raw"
                }
            }
        }
        root.addView(sendButton)
        // 创建承载模拟 Laya 场景的 WebView。
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            webViewClient = object : WebViewClient() {
                // 不以加载结束代替函数就绪，实际检查页面入口。
                override fun onPageFinished(view: WebView, url: String?) {
                    view.evaluateJavascript("typeof window.onNativeProtocol === 'function'") { ready ->
                        if (!disposed) sendButton.isEnabled = ready == "true"
                    }
                }
            }
        }
        // 让 WebView 占据剩余空间。
        root.addView(webView, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        // 先加载页面，点击按钮前确保函数已经定义。
        webView.loadUrl(PAGE_URL)
    }

    override fun onDestroy() {
        disposed = true
        // Activity 销毁后不保留旧 WebView。
        webView.stopLoading()
        webView.webViewClient = WebViewClient()
        (webView.parent as? ViewGroup)?.removeView(webView)
        webView.destroy()
        super.onDestroy()
    }

    companion object { private const val PAGE_URL = "file:///android_asset/day48/laya/index.html" }
}
