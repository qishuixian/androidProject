// Day45 演示 WebView 白屏排查、调试开关和导航安全边界。
package com.example.myapplication.day45

// Bundle 与 Intent 用于创建和启动 Activity。
import android.content.Intent
import android.os.Bundle
// Logcat 记录主框架错误和导航决策。
import android.util.Log
// WebView 调试、加载回调和请求类型。
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceError
// Native 控件展示诊断信息。
import android.widget.Button as NativeButton
import android.widget.LinearLayout
import android.widget.TextView
// 独立 Activity 管理 WebView 生命周期。
import androidx.activity.ComponentActivity
// Compose 入口使用按钮。
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
// 系统栏安全区域。
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
// BuildConfig 由当前 app 变体自动生成，用于区分 Debug 和 Release。
import com.example.myapplication.BuildConfig

// 首页进入白屏排查实验。
@Composable
internal fun WebViewDiagnosticsDemo() {
    // 获取当前宿主环境。
    val context = LocalContext.current
    // 点击才打开诊断 Activity。
    Button(onClick = { context.startActivity(Intent(context, WebViewDiagnosticsActivity::class.java)) }) {
        Text("打开 Day45：WebView 白屏与调试")
    }
}

// 诊断 Activity 将网页、状态和操作集中在一起。
class WebViewDiagnosticsActivity : ComponentActivity() {
    // 当前页面的 WebView 实例。
    private lateinit var webView: WebView
    // 记录是否已销毁，忽略迟到回调。
    private var disposed = false
    // Native 状态条引用，回调中更新。
    private lateinit var status: TextView

    // 创建诊断页面。
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 根布局纵向排列状态、操作按钮和 WebView。
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        // 处理系统栏占用区域。
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        // 显示阶段、地址和错误摘要，不把完整敏感 URL 写入日志。
        status = TextView(this).apply { text = "准备加载" }
        root.addView(status)
        // 点击按钮载入正常本地页，验证基础路径。
        root.addView(NativeButton(this).apply {
            text = "加载正常页"
            setOnClickListener { webView.loadUrl(HOME_URL) }
        })
        // 点击按钮载入不存在的主资源，验证错误回调。
        root.addView(NativeButton(this).apply {
            text = "加载错误页（练习）"
            setOnClickListener { webView.loadUrl(BROKEN_URL) }
        })
        // 显示当前变体是否允许开启调试，不把开关绑定到生产环境。
        root.addView(TextView(this).apply {
            text = "WebView 调试策略：${if (BuildConfig.DEBUG) "Debug 可开启" else "Release 已关闭"}"
        })
        // 创建 WebView 并开启基本设置。
        webView = WebView(this)
        // 仅本地示例需要 JavaScript，现实项目按页面需要最小化开启。
        webView.settings.javaScriptEnabled = true
        // 只允许白名单页面继续导航。
        webView.webViewClient = object : WebViewClient() {
            // 处理页面内导航请求，首次 loadUrl 不保证经过这里。
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val allowed = request.url.toString() in ALLOWED_URLS
                // true 表示拦截，false 允许 WebView 继续加载。
                Log.d("Day45", "导航 allowed=$allowed scheme=${request.url.scheme}")
                return !allowed
            }

            // 主页面开始加载，先清掉旧的“加载完成”状态。
            override fun onPageStarted(view: WebView, url: String?, favicon: android.graphics.Bitmap?) {
                status.text = "加载中：${url ?: "未知地址"}"
                Log.d("Day45", "onPageStarted")
            }

            // 只将主框架错误视为页面级错误。
            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (request.isForMainFrame) {
                    status.text = "主页面错误：${error.errorCode} ${error.description}"
                    Log.e("Day45", "main frame load error=${error.errorCode}")
                }
            }

            // 页面加载结束后检查 title 和 URL，帮助区分空白页与正常页。
            override fun onPageFinished(view: WebView, url: String?) {
                if (!disposed) {
                    status.text = "完成：title=${view.title ?: "无标题"}；canGoBack=${view.canGoBack()}"
                    Log.d("Day45", "onPageFinished")
                }
            }
        }
        // 网页占据剩余空间。
        root.addView(webView, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        // 仅在 Debug 构建启用 WebView 远程调试；Release 不应无条件开启。
        if (BuildConfig.DEBUG) WebView.setWebContentsDebuggingEnabled(true)
        // 首次加载正常页。
        webView.loadUrl(HOME_URL)
    }

    // 退出时释放 WebView，避免诊断回调持有 Activity。
    override fun onDestroy() {
        disposed = true
        webView.stopLoading()
        webView.webViewClient = WebViewClient()
        webView.destroy()
        super.onDestroy()
    }

    companion object {
        // 本地正常页面。
        private const val HOME_URL = "file:///android_asset/day45/index.html"
        // 不存在的本地主页面，用于练习错误回调。
        private const val BROKEN_URL = "file:///android_asset/day45/missing.html"
        // 限制可继续加载的地址，防止示例变成任意网页浏览器。
        private val ALLOWED_URLS = setOf(HOME_URL, BROKEN_URL)
    }
}
