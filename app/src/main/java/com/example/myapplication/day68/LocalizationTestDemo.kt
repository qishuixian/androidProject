// Day68 练习多语言 UI 的布局与格式化测试，不访问网络。
package com.example.myapplication.day68

// 用真实 Locale 格式化日期、数字和货币。
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale
// Compose 布局和裁剪用于手机宽度预览。
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
// 当前设备配置用于记录验收环境。
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

// 用固定时间避免每次重组改变测试结果。
private val sampleDate = Date(1_735_689_600_000L)

// 首页进入多语言测试实验。
@Composable
internal fun LocalizationTestDemo() {
    // 当前预览语言只影响本页格式化，不写系统设置。
    var localeTag by rememberSaveable { mutableStateOf("zh-CN") }
    // 溢出策略可以在页面上直接对照。
    var compact by rememberSaveable { mutableStateOf(false) }
    // 记录用户是否已经查看过检查清单。
    var checked by rememberSaveable { mutableStateOf(false) }
    // 将标准语言标签转换为格式化 API 使用的 Locale。
    val locale = remember(localeTag) { Locale.forLanguageTag(localeTag) }
    // 设备宽度与字体缩放是布局验收的重要上下文。
    val configuration = LocalConfiguration.current
    // 读取应用资源目录，展示当前实际设备环境。
    val context = LocalContext.current
    // 统一生成当前语言的数字和日期，不在 UI 中拼接分隔符。
    val number = remember(locale) { NumberFormat.getNumberInstance(locale).apply { maximumFractionDigits = 2 }.format(1234567.89) }
    // 货币格式的符号、分组和小数位由 Locale 决定。
    val money = remember(locale) { NumberFormat.getCurrencyInstance(locale).format(1234567.89) }
    // 日期顺序与月份文字由 Locale 决定。
    val date = remember(locale) { DateFormat.getDateInstance(DateFormat.LONG, locale).format(sampleDate) }
    // 故意使用很长的测试文案，验证手机宽度下的显示策略。
    val longText = when (localeTag) {
        "zh-CN" -> "这是一个较长的课程标题，用来验证小屏幕、多语言和字体放大时是否会被遮挡。"
        "zh-HK" -> "這是一個較長的課程標題，用來驗證小螢幕、多語言和字體放大時是否會被遮擋。"
        else -> "This is a deliberately long course title used to verify small screens, translated text, and enlarged fonts."
    }
    // 三种语言作为可重复的测试输入。
    val choices = listOf("zh-CN", "zh-HK", "en-US")
    // 外层课程页面负责滚动，示例不嵌套 LazyColumn。
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 说明本页是测试工具，不是完整翻译框架。
        Text("Day68 · 多语言测试")
        // 记录设备宽度、字体缩放和 locale，方便复现布局问题。
        Text("设备：${configuration.screenWidthDp}dp；fontScale=${configuration.fontScale}；系统=${configuration.locales.toLanguageTags()}")
        // 点击选择语言只更新本地测试输入。
        choices.forEach { choice ->
            // 语言改变后重新格式化日期和数字。
            Button(onClick = { localeTag = choice }) { Text("测试 $choice${if (localeTag == choice) "（当前）" else ""}") }
        }
        // 当前测试条件是可见的，避免只凭屏幕猜测。
        Text("长文案区域最宽 320dp（非设备模拟器）；格式化 Locale=$localeTag；资源 Context=${context.resources.configuration.locales.toLanguageTags()}")
        // 长文案使用省略策略时保持固定高度，方便比较。
        Text(
            text = longText,
            // 单行模式模拟标题栏等紧凑位置。
            maxLines = if (compact) 1 else 3,
            // 选择截断策略后显示省略号，而不是把控件撑出边界。
            overflow = TextOverflow.Ellipsis,
            // 最宽 320dp 的局部区域模拟窄布局；不等于真实手机窗口。
            modifier = Modifier.widthIn(max = 200.dp).fillMaxWidth().heightIn(min = 48.dp).clip(RoundedCornerShape(8.dp)).border(1.dp, Color.Gray).padding(10.dp),
            // 字号固定；字体放大由系统 configuration 控制。
            fontSize = 18.sp
        )
        // 手动切换标题测试策略，生产应由设计决定。
        Button(onClick = { compact = !compact }) { Text(if (compact) "改为最多三行" else "改为单行省略") }
        // 数字不使用字符串插值硬编码逗号。
        Text("数字：$number")
        // 货币符号和小数位由 Locale 决定。
        Text("货币：$money")
        // 日期格式由 Locale 决定顺序和文字。
        Text("日期：$date")
        // 这不是自动翻译，只是固定的测试说明。
        Text("同一个数值和时间，在不同 Locale 下格式可能不同；业务协议应传原始数值/时间，不传展示字符串。")
        // 点击后显示验收清单已阅读状态。
        Button(onClick = { checked = !checked }) { Text(if (checked) "已标记检查清单" else "标记检查清单") }
        // 将当前条件固化为人工验收记录模板。
        Text(if (checked) "检查：小屏宽度、长文案、fontScale、日期、数字、货币均已列入验收。" else "检查：请切换三种语言，并在系统显示大小/字体大小变化后重新观察。")
        // 明确日期固定，避免把当前时间变化误认为格式变化。
        Text("测试日期固定为同一瞬间；Day68 不验证系统语言切换 API，重点是布局和格式化。")
    }
}
