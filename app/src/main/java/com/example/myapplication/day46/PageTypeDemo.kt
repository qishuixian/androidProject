// Day46 只使用本地资源演示页面类型识别，不引入未经验证的 Laya SDK。
package com.example.myapplication.day46

// Intent 打开独立识别页面。
import android.content.Intent
import android.os.Bundle
// WebView 与资源加载回调。
import android.webkit.WebView
import android.webkit.WebViewClient
// Native 控件显示诊断结果。
import android.widget.Button as NativeButton
import android.widget.LinearLayout
import android.widget.TextView
// Activity 承载识别实验。
import androidx.activity.ComponentActivity
// Compose 入口。
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// 首页入口按钮。
@Composable
internal fun PageTypeDemo() {
    // 获取宿主 Context，用于启动 Activity。
    val context = LocalContext.current
    Button(onClick = { context.startActivity(Intent(context, PageTypeActivity::class.java)) }) {
        // 说明今天关注页面识别，不是桥接实现。
        Text("打开 Day46：Native、H5、Laya 页面识别")
    }
}

// 独立 Activity 展示三类页面的证据对照。
class PageTypeActivity : ComponentActivity() {
    // 当前用于加载本地演示资源的 WebView。
    private lateinit var webView: WebView

    // 创建诊断页面。
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 控件从上到下排列：说明、Native 页面、网页、判断结果。
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        // 这是 Activity 原生 TextView，证明当前外层是 Native。
        root.addView(TextView(this).apply {
            text = "外层 Activity：PageTypeActivity（Native）"
        })
        // 显示一个完全不经过 WebView 的原生按钮。
        root.addView(NativeButton(this).apply {
            text = "Native View：点击不会加载网页"
            setOnClickListener { status.text = "证据：这是 Android View 的点击回调" }
        })
        // 下面两个按钮切换不同资源路径，用于观察 WebView 证据。
        root.addView(NativeButton(this).apply {
            text = "加载普通 H5 资源"
            setOnClickListener { webView.loadUrl(H5_URL) }
        })
        root.addView(NativeButton(this).apply {
            text = "加载模拟 Laya 资源"
            setOnClickListener { webView.loadUrl(LAYA_URL) }
        })
        // status 只展示判断结论，不把“模拟”说成真实引擎。
        val status = TextView(this).apply {
            text = "尚未加载网页：先观察 Activity 与 WebView 的区别"
        }
        // 允许上面的点击回调访问已经创建的状态控件。
        this.status = status
        root.addView(status)
        // 创建 WebView；它是 H5/Laya 可能使用的承载容器，不等于引擎本身。
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String?) {
                    // 仅根据已知本地资源路径给出候选判断，最终需结合引擎初始化证据。
                    status.text = when (url) {
                        H5_URL -> "候选：普通 H5；证据是 WebView + assets/day46/h5.html"
                        LAYA_URL -> "候选：Laya 资源样例；仍未证明有 Laya 引擎运行"
                        else -> "未知资源：$url"
                    }
                }
            }
        }
        // 网页使用剩余空间。
        root.addView(webView, LinearLayout.LayoutParams(-1, 0, 1f))
        // 显示完整页面。
        setContentView(root)
    }

    // 状态控件在 onCreate 中初始化，属性本身不跨 Activity 保存。
    private lateinit var status: TextView

    override fun onDestroy() {
        // 释放 WebView，避免旧 Activity 被页面回调持有。
        webView.stopLoading()
        webView.webViewClient = WebViewClient()
        webView.destroy()
        super.onDestroy()
    }

    companion object {
        // 普通网页资源路径，只能证明这是本地 H5 内容。
        private const val H5_URL = "file:///android_asset/day46/h5.html"
        // 模拟引擎资源目录名称，故意不引入真正 Laya runtime。
        private const val LAYA_URL = "file:///android_asset/day46/laya/index.html"
    }
}
