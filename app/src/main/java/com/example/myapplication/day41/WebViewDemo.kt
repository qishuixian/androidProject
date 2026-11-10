// Day41 在独立 Activity 中展示 WebView，避免与首页滚动容器嵌套。
package com.example.myapplication.day41

// Intent 打开独立页面，Bundle 保存网页导航状态。
import android.content.Intent
import android.os.Bundle
// Logcat 观察加载及销毁过程。
import android.util.Log
// ViewGroup 用于销毁前从父容器移除 WebView。
import android.view.ViewGroup
// 以下 API 提供内嵌网页、导航回调与页面错误信息。
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceError
// Activity 使用原生控件显示状态和网页。
import android.widget.LinearLayout
import android.widget.TextView
// ComponentActivity 和回调负责系统返回操作。
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
// Compose 入口使用按钮和文字。
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
// Insets 防止网页操作区被状态栏和导航栏遮挡。
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// 首页入口只启动 Activity，不提前创建 WebView。
@Composable
internal fun WebViewDemo() {
    // 获取当前宿主 Context，用于构造显式 Intent。
    val context = LocalContext.current
    // 点击后打开网页学习页。
    Button(onClick = { context.startActivity(Intent(context, WebLessonActivity::class.java)) }) {
        // 按钮文案表明无需网络。
        Text("打开 Day41：本地 WebView")
    }
}

// WebView 与当前 Activity 同生共灭，不放进单例或 ViewModel。
class WebLessonActivity : ComponentActivity() {
    // 在 onCreate 初始化，后续生命周期回调复用此引用。
    private lateinit var webView: WebView

    // 创建界面或配置重建后恢复导航状态。
    override fun onCreate(savedInstanceState: Bundle?) {
        // 先让父类恢复生命周期和状态基础设施。
        super.onCreate(savedInstanceState)
        // 根布局上方显示状态，下方显示网页。
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        // 根据系统栏实际高度设置边距，不硬编码像素偏移。
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            // 系统栏包括状态栏和底部导航栏。
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 给所有子内容保留安全显示空间。
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            // 原样返回，允许正常传播 Insets。
            insets
        }
        // 状态文字属于 Android View，不是网页 DOM。
        val status = TextView(this).apply { text = "准备加载本地网页" }
        // 先添加状态条，网页随后占用剩余高度。
        root.addView(status)
        // 使用 Activity Context 创建需要显示在当前窗口中的 WebView。
        webView = WebView(this)
        // 配置网页导航和加载回调；本课保留默认关闭的 JavaScript。
        webView.webViewClient = object : WebViewClient() {
            // 用户点击链接时决定是否交给 WebView 加载；首次 loadUrl 不保证经过这里。
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                // 本示例只允许 assets/day41 下的两页，其他地址拦截。
                val allowed = request.url.toString() in LOCAL_PAGES
                // true 表示宿主已处理/拦截，false 表示继续默认加载。
                return !allowed
            }

            // 主页面开始加载时通知状态；这不是 Activity.onStart。
            override fun onPageStarted(view: WebView, url: String?, favicon: android.graphics.Bitmap?) {
                // 显示正在加载的地址，便于观察导航。
                status.text = "加载中：$url"
                // 用固定标签记录事件。
                Log.d("Day41", "onPageStarted $url")
            }

            // 主框架加载完成，不代表所有 JS 异步任务或业务数据都完成。
            override fun onPageFinished(view: WebView, url: String?) {
                // 展示后退能力，结合网页内链接进行实验。
                status.text = "页面结束加载；可网页后退：${view.canGoBack()}"
                // 日志帮助区分网页回调与宿主生命周期。
                Log.d("Day41", "onPageFinished $url")
            }

            // API 23+ 的错误回调可能来自子资源，必须判断主框架。
            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                // 避免把图片等子资源失败当成整页失败。
                if (request.isForMainFrame) {
                    // 保存可观察的错误信息；本例正常路径全部来自本地 assets。
                    status.text = "主页面加载失败：${error.description}"
                    // 错误回调不同于 HTTP 状态错误回调。
                    Log.e("Day41", "main frame error=${error.errorCode}")
                }
            }
        }
        // 0 高度与权重 1 让网页占据状态条下面的剩余空间。
        root.addView(webView, LinearLayout.LayoutParams(-1, 0, 1f))
        // 将原生容器显示到 Activity 窗口。
        setContentView(root)
        // restoreState 恢复历史记录，不是完整保存 DOM 或网页内 JS 内存。
        val restored = savedInstanceState?.getBundle("web_state")?.let { webView.restoreState(it) }
        // 首次打开或恢复失败才加载首页，避免重建时覆盖已恢复的详情页。
        if (restored == null) webView .loadUrl(HOME_URL)
        // 返回操作优先处理网页历史，历史为空才退出 Activity。
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            // 系统返回触发时执行，不在注册时执行。
            override fun handleOnBackPressed() {
                // goBack 是 WebView 内部历史导航，不是 Fragment 返回栈。
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }

    // 宿主恢复可交互时通知当前 WebView。
    override fun onResume() {
        // 先执行父类生命周期处理。
        super.onResume()
        // 恢复 WebView 的部分处理，不等于所有网页任务都能自动恢复。
        webView.onResume()
        // 与网页加载回调使用同一标签便于对照。
        Log.d("Day41", "Activity onResume")
    }

    // 宿主进入暂停时通知 WebView；不是彻底冻结网页全部 JavaScript。
    override fun onPause() {
        // 本课不使用影响进程内所有 WebView 的 pauseTimers。
        webView.onPause()
        // 再执行父类处理。
        super.onPause()
    }

    // 保存网页历史，支持旋转等重建场景。
    override fun onSaveInstanceState(outState: Bundle) {
        // 单独 Bundle 避免与其他 Activity 状态键混杂。
        val webState = Bundle()
        // 将 WebView 导航历史写入容器，不保存 WebView 对象。
        webView.saveState(webState)
        // 保存这个容器供下次 onCreate 读取。
        outState.putBundle("web_state", webState)
        // 父类继续保存 Activity 与 View 的状态。
        super.onSaveInstanceState(outState)
    }

    // 退出或配置重建时释放旧实例，不能拿旧 WebView 接到新 Activity。
    override fun onDestroy() {
        // 停止旧页面仍在进行的加载。
        webView.stopLoading()
        // 替换捕获 status 的回调对象，解除旧界面引用。
        webView.webViewClient = WebViewClient()
        // 先从父布局移除，再销毁平台资源。
        (webView.parent as? ViewGroup)?.removeView(webView)
        // destroy 后不再调用旧实例的方法。
        webView.destroy()
        // 输出销毁日志，方便验证返回和旋转行为。
        Log.d("Day41", "WebView destroyed")
        // 最后完成父类销毁流程。
        super.onDestroy()
    }

    // 静态配置只保存地址字符串，不持有 Activity 或 View。
    companion object {
        // assets 中的文件随 APK 打包，正常实验无需网络。
        private const val HOME_URL = "file:///android_asset/day41/index.html"
        // 严格限定示例导航范围，不开放任意外部页面。
        private val LOCAL_PAGES = setOf(HOME_URL, "file:///android_asset/day41/detail.html")
    }
}
