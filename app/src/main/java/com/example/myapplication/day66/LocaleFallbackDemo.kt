// Day66 只改变局部预览配置，不切换整个 App 或系统语言。
package com.example.myapplication.day66

// 创建独立配置，不修改宿主 Resources。
import android.content.res.Configuration
import android.os.LocaleList
import android.view.LayoutInflater
import android.widget.Toast
// 垂直展示语言选择与匹配结果。
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
// 语言标签保存为小字段，Context 不参与持久化。
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
// 同时提供局部 Context 与配置，供 Compose 资源和 View 使用。
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
// app 与库的 R 引用不同符号，但使用当前配置读取最终资源表。
import com.example.myapplication.R
import com.example.myapplication.BuildConfig
import com.example.coursecore.R as CoreR
// 使用标准语言标签创建 Locale，不把目录名直接传入。
import java.util.Locale

// 首页进入局部资源匹配实验。
@Composable
internal fun LocaleFallbackDemo() {
    // 默认预览中文简体，选择不会写入系统语言设置。
    var tag by rememberSaveable { mutableStateOf("zh-CN") }
    // 读取真正宿主 Context，用它创建一个独立的配置 Context。
    val baseContext = LocalContext.current
    // 跟随宿主尺寸、方向等配置，不能只保留语言而丢掉其他配置。
    val baseConfiguration = LocalConfiguration.current
    // 只缓存配置对象，不缓存已经解析出的翻译文本。
    val previewContext = remember(baseContext, baseConfiguration, tag) {
        // 复制配置，绝不原地修改 LocalConfiguration。
        val config = Configuration(baseConfiguration).apply {
            // 本实验明确只指定一个语言，避免系统多语言列表影响读数。
            setLocales(LocaleList(Locale.forLanguageTag(tag)))
        }
        // 新 Context 复用资源资产，但以新的配置进行查询。
        baseContext.createConfigurationContext(config)
    }
    // 选择列表是语言 tag，不是 Android 资源目录名称。
    val choices = listOf("zh-CN", "zh-TW", "zh-HK", "en-US", "fr-FR")
    // 课程外壳保持中文，预览区域才参与语言变化。
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 明确这是资源匹配实验，不是完整 App 语言设置。
        Text("Day66 · 多语言匹配与 fallback（局部预览）")
        // 对照同一个 APK 下运行期语言变化。
        Text("当前 APK：${BuildConfig.FLAVOR}/${BuildConfig.BUILD_TYPE}；宿主语言=${baseConfiguration.locales.toLanguageTags()}")
        // 每个按钮只更新标签状态。
        choices.forEach { choice ->
            // 修改后重组，创建对应的局部 Context。
            Button(onClick = { tag = choice }) { Text("预览 $choice${if (tag == choice) "（当前）" else ""}") }
        }
        // 展示当前资源选择入口，不伪装为设备系统语言已修改。
        Text("请求语言=$tag；下面每条文案的方括号是教学来源标记。")
        // 同时提供 Context 与配置，让资源函数读取相同的局部环境。
        CompositionLocalProvider(
            // 普通 getString、布局解析和点击事件取此 Context。
            LocalContext provides previewContext,
            // Compose 根据这个配置感知语言变化。
            LocalConfiguration provides previewContext.resources.configuration
        ) {
            // 语言切换时重建传统 View，避免 factory 留下旧 Context。
            key(previewContext) { LocalePreviewPanel() }
        }
        // 把实验结论与系统算法边界一起说明。
        Text("按资源逐项匹配，不是先选一个目录后整页都从它读取。具体候选选择由系统版本、语言/字形/地区和可用资源决定。")
        // 强调两个阶段，避免把语言选择当 Flavor 选择。
        Text("Gradle 先合并 app、Flavor 和依赖库资源，再由当前 Resources 配置匹配。库 R 不是独立语言设置。")
    }
}

// 这个子组合完整使用预览区的局部配置。
@Composable
private fun LocalePreviewPanel() {
    // 获取预览 Context，不回到 applicationContext 查询。
    val context = LocalContext.current
    // 此计数只属于当前语言预览；换语言后 key 会让它重置。
    var count by remember { mutableIntStateOf(0) }
    // 在组合执行时解析最新文案，不用 remember 缓存字符串。
    Text("Compose：${stringResource(R.string.day66_greeting)}")
    // 香港目录没有这个键，观察系统实际选择的其他候选。
    Text("另一条资源：${stringResource(R.string.day66_region)}")
    // 所有语言都缺少对应翻译，此条目稳定使用默认资源。
    Text("默认兜底：${stringResource(R.string.day66_default_only)}")
    // 库资源也走当前局部配置和最终合并资源表。
    Text("Library：${stringResource(CoreR.string.day66_library_message)}")
    // 普通 Kotlin 与 Compose 应读取同一配置下的同一条目。
    Text("Context.getString：${context.getString(R.string.day66_greeting)}")
    // 传统 XML 的 @string 同样由预览 Context 解析。
    AndroidView(
        // 使用传入的局部 UI Context，而非全局 Context。
        factory = { uiContext -> LayoutInflater.from(uiContext).inflate(R.layout.day66_locale_panel, null, false) },
        // 明确布局宽度，不让根 View 的参数决定课程页面布局。
        modifier = Modifier.fillMaxWidth()
    )
    // 点击普通回调时不能调用 Composable 资源函数。
    Button(onClick = {
        // 只改变当前预览计数，不改变语言或 Flavor。
        count++
        // 用局部 Context 在事件发生时解析参数文案。
        Toast.makeText(context, context.getString(R.string.day66_click, count), Toast.LENGTH_SHORT).show()
    }) { Text("用预览语言显示 Toast（$count）") }
}
