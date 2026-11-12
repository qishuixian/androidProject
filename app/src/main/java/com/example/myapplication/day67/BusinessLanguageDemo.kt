// Day67 区分界面语言、报告语言与网页语言字典，不修改全局设置。
package com.example.myapplication.day67

// 每种语言使用独立配置 Context，不污染宿主。
import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
// 模拟 Laya 侧采用真实本地 WebView，但没有真实引擎。
import android.webkit.WebView
import android.webkit.WebViewClient
// 纵向展示三个语言边界。
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
// 保存选择和已生成的报告快照，不保存 View。
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
// Native 文案来自编译资源。
import com.example.myapplication.R
// JSON 正确编码字符串，避免直接拼接脚本参数。
import org.json.JSONObject
import java.util.Locale

// 创建单一语言的独立 Context，与 Day66 使用同一思路。
private fun localeContext(base: Context, source: Configuration, tag: String): Context {
    // 配置先复制，再修改语言。
    val config = Configuration(source).apply { setLocales(LocaleList(Locale.forLanguageTag(tag))) }
    // 不调用全局 updateConfiguration。
    return base.createConfigurationContext(config)
}

// 网页只接收界面语言；加载完成前暂存，不向不存在的函数发消息。
private class LanguageWebView(context: Context) : WebView(context) {
    // 记录最新界面语言，不捕获首次创建时的旧值。
    private var pending = "zh-CN"
    // 页面每次重新加载时允许重新发送。
    private var sent: String? = null
    // 函数定义加载完成后才能发送。
    private var loaded = false
    // 销毁后禁止继续访问页面。
    private var released = false
    // 配置仅用于本地教学页面。
    init {
        // 允许执行固定本地函数。
        settings.javaScriptEnabled = true
        // 资产读为字符串，无需开放文件或 content 访问。
        settings.allowFileAccess = false
        // 不读取设备内容 URI。
        settings.allowContentAccess = false
        // 监听网页加载生命周期。
        webViewClient = object : WebViewClient() {
            // 新加载必须重新等待 JS 就绪。
            override fun onPageStarted(view: WebView, url: String?, favicon: android.graphics.Bitmap?) {
                // 不对旧页面继续假定函数可调用。
                loaded = false
                // 新文档还没有接收语言消息。
                sent = null
            }
            // 本地静态脚本在页面完成时可调用。
            override fun onPageFinished(view: WebView, url: String?) {
                // 忽略销毁后的回调。
                if (released) return
                // 标记当前文档可接收消息。
                loaded = true
                // 使用等待期间的最新选择，而非最早选择。
                sendPending()
            }
        }
        // 从本地资产读取固定 HTML，不访问远端服务。
        val html = context.assets.open("day67/language.html").bufferedReader().use { it.readText() }
        // 虚拟来源不代表发起该域名请求，CSP 禁止子资源加载。
        loadDataWithBaseURL("https://day67.invalid/", html, "text/html", "UTF-8", null)
    }
    // 重组时同步当前界面语言，报告变化不会改变消息内容。
    fun setAppLanguage(tag: String) {
        // 始终保留最新值。
        pending = tag
        // 有效且未发送时才执行脚本。
        sendPending()
    }
    // 同一文档中相同语言不重复发送。
    private fun sendPending() {
        // 检查页面和实例仍有效。
        if (released || !loaded || sent == pending) return
        // 正确引用字符串，不能用未转义的用户值构造 JS。
        val argument = JSONObject.quote(pending)
        // 调用固定函数；本课不注册 JavaScriptInterface。
        evaluateJavascript("window.setAppLanguage($argument)", null)
        // 本课记录已发出请求，不将此字段当作远端确认。
        sent = pending
    }
    // 释放网页运行期资源。
    fun release() {
        // 阻止加载回调再次发送。
        released = true
        // 停止未完成加载。
        stopLoading()
        // AndroidView 已移除 View，销毁网页。
        destroy()
    }
}

// 首页直接进入业务语言边界实验。
@Composable
internal fun BusinessLanguageDemo() {
    // UI 语言只影响本课 Native 预览和模拟网页。
    var appLanguage by rememberSaveable { mutableStateOf("zh-CN") }
    // 报告语言是另一个独立业务选择。
    var reportLanguage by rememberSaveable { mutableStateOf("en-US") }
    // 最近一次生成结果与其语言一起保存，不能自动跟随新选择。
    var snapshotLanguage by rememberSaveable { mutableStateOf("") }
    // 保存已生成的本地报告内容，强调它是生成结果而非实时 UI 文案。
    var snapshotText by rememberSaveable { mutableStateOf("") }
    // 宿主 Context 不被任何选择原地修改。
    val base = LocalContext.current
    // 保留宿主其他配置。
    val configuration = LocalConfiguration.current
    // 界面语言 Context 只依赖界面选择。
    val uiContext = remember(base, configuration, appLanguage) { localeContext(base, configuration, appLanguage) }
    // 报告 Context 只依赖报告选择，不能复用 uiContext。
    val reportContext = remember(base, configuration, reportLanguage) { localeContext(base, configuration, reportLanguage) }
    // 三种选择都能形成交叉组合。
    val languages = listOf("zh-CN", "zh-HK", "en-US")
    // 外层课程页面负责滚动。
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 说明这是局部实验，不是生产系统的全局语言设置。
        Text("Day67 · 业务语言边界（局部模拟）")
        // 当前两个字段明确分开显示。
        Text("界面 appLanguage=$appLanguage；报告 reportLanguage=$reportLanguage")
        // UI 选择不操作报告字段。
        languages.forEach { tag ->
            // 只更新界面目标语言。
            Button(onClick = { appLanguage = tag }) { Text("界面语言 → $tag") }
        }
        // Native 组件只在局部 UI 配置中读取资源。
        CompositionLocalProvider(LocalContext provides uiContext, LocalConfiguration provides uiContext.resources.configuration) {
            // 标题跟随界面语言，不跟随报告语言。
            Text(stringResource(R.string.day67_native_title))
            // 动作文案同样属于界面层。
            Text(stringResource(R.string.day67_native_action))
        }
        // 实际交给网页的消息只有界面语言，不混入报告字段。
        Text("模拟网页消息（本地）：${JSONObject().put("appLanguage", appLanguage)}")
        // WebView 不依赖 UI Context 重建，通过显式消息同步。
        AndroidView(
            // 用宿主 UI Context 创建一次网页容器。
            factory = { LanguageWebView(it).apply { setAppLanguage(appLanguage) } },
            // 更新只同步界面语言，重复值被控件内部过滤。
            update = { it.setAppLanguage(appLanguage) },
            // 明确网页预览区域尺寸。
            modifier = Modifier.fillMaxWidth().height(180.dp),
            // 离开组合后清理网页，不保存旧实例。
            onRelease = { it.release() }
        )
        // 报告选择不会操作界面或网页字段。
        languages.forEach { tag ->
            // 只改变未来生成报告时的语言。
            Button(onClick = { reportLanguage = tag }) { Text("报告语言 → $tag") }
        }
        // 这个 JSON 是教学契约，不声称对应现有生产接口字段。
        Text("报告请求草稿（未发送）：${JSONObject().put("reportLanguage", reportLanguage)}")
        // 本地生成过程使用显式报告语言快照。
        Button(onClick = {
            // 先记录这次生成使用的业务选择。
            snapshotLanguage = reportLanguage
            // 用报告 Context 读取内容，不从 UI Context 偷取语言。
            snapshotText = reportContext.getString(R.string.day67_report_summary)
        }) { Text("按报告语言生成本地报告") }
        // 展示快照，切 UI 或修改下次报告选择不会重写已经生成的内容。
        Text(if (snapshotLanguage.isEmpty()) "尚未生成报告" else "最近生成：language=$snapshotLanguage\n$snapshotText")
        // 对照范围与下一步。
        Text("网页仅模拟 Laya 侧独立字典，不含真实引擎；JSON 未发送网络。改选择不等于重译历史报告。")
    }
}
