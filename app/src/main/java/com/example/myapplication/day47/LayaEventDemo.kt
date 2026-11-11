// Day47 模拟 Laya JavaScript 事件调 Native；当前工程没有真实 Laya SDK。
package com.example.myapplication.day47

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.json.JSONObject

// 首页入口：打开模拟 Laya 场景页面。
@Composable
internal fun LayaEventDemo() {
    // 获取当前 Compose 宿主，用于启动 Activity。
    val context = LocalContext.current
    Button(onClick = { context.startActivity(Intent(context, LayaEventActivity::class.java)) }) {
        // 明确这是模拟协议，不代表真实引擎。
        Text("打开 Day47：Laya 调 Native（模拟）")
    }
}

// Activity 负责 WebView 生命周期和 Native 事件处理。
class LayaEventActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private lateinit var status: TextView
    private var disposed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 原生状态条和网页场景上下排列。
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        status = TextView(this).apply { text = "等待 Laya 事件…" }
        root.addView(status)
        webView = WebView(this).apply {
            // 模拟 Laya 场景使用 JavaScript；真实项目需按可信来源开启。
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
        }
        // 注册最小桥接面，只暴露一个事件入口。
        webView.addJavascriptInterface(LayaBridge { eventName, rawPayload ->
            // JavascriptInterface 回调可能不在主线程，先记录再切换。
            Log.d("Day47", "event=$eventName thread=${Thread.currentThread().name}")
            runOnUiThread {
                if (disposed) return@runOnUiThread
                val payload = runCatching { JSONObject(rawPayload) }.getOrNull()
                // 按事件名和字段校验，不能把任意 JS 字符串当成业务命令。
                if (eventName == "course_complete" && payload?.optString("courseId") == "kotlin-47") {
                    status.text = "Native 已处理：${payload.optString("courseId")} 完成"
                    // 回传统一结果，网页用事件回调更新自己的文字。
                    val result = JSONObject().put("ok", true).put("event", eventName)
                    webView.evaluateJavascript("window.onNativeEventResult($result)", null)
                } else {
                    status.text = "Native 拒绝未知或无效事件：$eventName"
                }
            }
        }, "LayaNative")
        root.addView(webView, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        webView.loadUrl(PAGE_URL)
    }

    override fun onDestroy() {
        disposed = true
        webView.removeJavascriptInterface("LayaNative")
        webView.stopLoading()
        webView.webViewClient = WebViewClient()
        (webView.parent as? ViewGroup)?.removeView(webView)
        webView.destroy()
        super.onDestroy()
    }

    companion object { private const val PAGE_URL = "file:///android_asset/day47/laya/index.html" }
}

// 只负责把 JS 消息转交给 Activity，不直接操作 View。
private class LayaBridge(private val onEvent: (String, String) -> Unit) {
    // 仅此 public 注解方法可被网页调用。
    @JavascriptInterface
    fun emit(eventName: String, payload: String) {
        // 限制长度，避免异常大消息进入业务层。
        if (eventName.length <= 80 && payload.length <= 4096) onEvent(eventName, payload)
    }
}
