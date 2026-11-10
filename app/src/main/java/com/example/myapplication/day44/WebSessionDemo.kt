// Day44 使用本地网页模拟 WebView 登录态，不连接真实服务器。
package com.example.myapplication.day44

// Intent 启动独立会话页面。
import android.content.Intent
import android.os.Bundle
// CookieManager 管理 WebView 的 Cookie；Log 观察清理流程。
import android.webkit.CookieManager
import android.util.Log
// WebView 生命周期和导航拦截。
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
// Native 状态条与网页放在同一个根布局。
import android.widget.Button as NativeButton
import android.widget.LinearLayout
import android.widget.TextView
// Activity 负责窗口和返回键。
import androidx.activity.ComponentActivity
// Compose 入口。
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
// 系统栏安全区域。
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// 首页进入会话实验页。
@Composable
internal fun WebSessionDemo() {
    // Context 只用于显式启动 Activity，不保存到全局。
    val context = LocalContext.current
    Button(onClick = { context.startActivity(Intent(context, WebSessionActivity::class.java)) }) {
        // 说明今天学习的是会话状态。
        Text("打开 Day44：Cookie、Token、登录态")
    }
}

// 独立 Activity 管理一次 WebView 会话。
class WebSessionActivity : ComponentActivity() {
    // CookieManager 是进程级 WebView Cookie 管理器，不是 Activity 私有对象。
    private lateinit var cookieManager: CookieManager
    // 当前页面实例只属于当前 Activity。
    private lateinit var webView: WebView
    // 避免清理后迟到任务更新页面。
    private var disposed = false

    // 创建页面并恢复会话实验状态。
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 根布局上方是 Native 控制区，下方是网页。
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        // 给边到边窗口留出系统栏空间。
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        // 原生状态用于显示 Cookie 清理结果。
        val status = TextView(this).apply { text = "准备会话" }
        // 清除按钮模拟退出登录：先清网页 Cookie，再刷新页面。
        val logout = NativeButton(this).apply { text = "退出登录并清除 Cookie" }
        // 读取 Cookie 按钮用于观察当前 WebView Cookie 值。
        val inspect = NativeButton(this).apply { text = "读取当前 Cookie" }
        root.addView(status)
        root.addView(logout)
        root.addView(inspect)
        // 使用当前 Activity Context 创建 WebView。
        webView = WebView(this)
        // 读取和写入 Cookie 需要 WebView 支持 Cookie；本地页面仍不访问网络。
        cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        // 只允许 Day44 的固定本地页面导航。
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                request.url.toString() != PAGE_URL
            // 页面加载完成后读取本页面对应的 Cookie。
            override fun onPageFinished(view: WebView, url: String?) {
                if (!disposed) updateCookieStatus(status)
            }
        }
        // 将 WebView 放到控制区下方。
        root.addView(webView, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        // 点击退出先异步清理 Cookie，再删除网页自己的 localStorage 并刷新。
        logout.setOnClickListener {
            // 清理回调返回 UI 线程，只有完成后才更新状态。
            cookieManager.removeAllCookies { removed ->
                if (disposed) return@removeAllCookies
                // flush 将 CookieManager 内存变化写入持久化存储。
                cookieManager.flush()
                // localStorage 是另一种存储，Cookie 清理不会自动删除它。
                webView.evaluateJavascript("localStorage.removeItem('day44_token')", null)
                status.text = "已退出：Cookie 清理=$removed；Token 已请求删除"
                // 重新加载页面，让网页显示未登录状态。
                webView.reload()
            }
        }
        // 读取按钮使用当前 URL 查询 Cookie，不把完整 token 写进日志或界面。
        inspect.setOnClickListener { updateCookieStatus(status) }
        // 首次加载本地登录模拟页。
        webView.loadUrl(PAGE_URL)
    }

    // 将 Cookie 脱敏后展示，避免学习日志泄露完整会话值。
    private fun updateCookieStatus(status: TextView) {
        // Cookie 绑定 URL，不能只调用一个无地址的全局读取。
        val raw = cookieManager.getCookie(PAGE_URL).orEmpty()
        // 只显示是否存在和长度，不显示 token 内容。
        status.text = if (raw.isBlank()) "当前无 Cookie" else "当前有 Cookie（长度 ${raw.length}）"
        // Log 只记录存在与否。
        Log.d("Day44", "Cookie exists=${raw.isNotBlank()}")
    }

    // Activity 恢复时继续 WebView，不主动重载以免重置页面。
    override fun onResume() { super.onResume(); webView.onResume() }

    // Activity 暂停时通知 WebView。
    override fun onPause() { webView.onPause(); super.onPause() }

    // 退出时释放 WebView，Cookie 是否清理由用户操作决定。
    override fun onDestroy() {
        disposed = true
        webView.stopLoading()
        webView.webViewClient = WebViewClient()
        webView.removeJavascriptInterface("unused")
        webView.destroy()
        super.onDestroy()
    }

    companion object {
        // 本地地址对应 assets/day44/index.html。
        private const val PAGE_URL = "file:///android_asset/day44/index.html"
    }
}
