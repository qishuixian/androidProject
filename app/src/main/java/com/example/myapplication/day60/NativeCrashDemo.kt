// Day60 通过固定日志练习 Native 排错，不触发真实崩溃。
package com.example.myapplication.day60

// 读取当前设备信息，和下方虚构日志明确分开。
import android.os.Build
// 记录练习选择，而不是伪造系统崩溃日志。
import android.util.Log
// 页面布局仍交给首页的外层滚动容器。
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
// 按钮选择案例，文字展示证据和诊断。
import androidx.compose.material3.Button
import androidx.compose.material3.Text
// Compose 状态保存选择，不保存设备或异常对象。
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
// 让原始日志保持等宽显示。
import androidx.compose.ui.text.font.FontFamily
// dp 用于页面间距。
import androidx.compose.ui.unit.dp
// 当前 APK 的版本与变体来自 Gradle 生成的配置。
import com.example.myapplication.BuildConfig

// 每个案例把可见证据、允许的结论与下一步区分开。
private data class CrashCase(
    // 按钮标题。
    val title: String,
    // 教学日志，不是本机运行结果。
    val evidence: String,
    // 仅根据日志能得出的结论。
    val conclusion: String,
    // 继续定位所需的证据或实验。
    val next: String
)

// 列表仅在文件初始化时创建，切换案例不会运行 JNI。
private val crashCases = listOf(
    // Native 崩溃有信号和本地堆栈；库名不能直接证明责任方。
    CrashCase(
        title = "SIGSEGV · 渲染线程",
        evidence = """
            pid: 1234, tid: 1250, name: RenderWorker >>> example.app <<<
            signal 11 (SIGSEGV), code 1 (SEGV_MAPERR), fault addr 0x0
            ABI: 'arm64'
            backtrace:
              #00 pc 0000000000012340 liblesson_rtc.so (BuildId: abc123)
              #01 pc 0000000000023450 liblesson_rtc.so (BuildId: abc123)
        """.trimIndent(),
        conclusion = "这是示意 C/C++ 非法内存访问。0x0 提示空地址访问，但不能仅凭它确定是哪个对象、哪行代码或哪方责任。",
        next = "保留完整 tombstone、触发步骤、版本与同构建同 ABI 符号；对齐 Day59 的解绑/Surface 销毁时间线，再符号化定位。"
    ),
    // 托管层空指针和 SIGSEGV 的证据不同。
    CrashCase(
        title = "Java/Kotlin · 未捕获异常",
        evidence = "FATAL EXCEPTION: main\nProcess: example.app, PID: 1234\njava.lang.NullPointerException\n  at example.LessonPage.render(LessonPage.kt:42)",
        conclusion = "这是托管层未捕获异常，不是 SIGSEGV。Kotlin 也可能因 !!、Java 互操作等产生空指针。",
        next = "查看完整异常、Caused by 链和首个相关应用栈帧；对应源码、线程与生命周期。类名和行号在此都是示例。"
    ),
    // 加载错误通常先检查打包和 ABI，不能混称本地信号崩溃。
    CrashCase(
        title = ".so · 加载失败",
        evidence = "java.lang.UnsatisfiedLinkError: dlopen failed: library \"liblesson_rtc.so\" not found",
        conclusion = "这是 JNI/动态库加载失败的错误，尚不能据此认定发生了 SIGSEGV，也不能直接断定一定是 ABI 不匹配。",
        next = "核对 APK 的 lib/<abi>/、当前进程位数、目标库及其依赖、System.loadLibrary 名称与完整错误信息。"
    ),
    // 厂商系统日志不能脱离进程与时间关联来解释应用故障。
    CrashCase(
        title = "只有系统 E 日志",
        evidence = "11-11 10:00:00.000 1178 1178 E cnss-daemon: Failed to get socket\n（没有应用进程崩溃栈）",
        conclusion = "单独这条系统服务日志不足以解释应用闪退，也不能据此确定应用没有问题。",
        next = "先确认应用是否退出或 ANR；按触发时间保留全量日志，再关联应用包名、PID、AndroidRuntime、DEBUG 与 tombstone。"
    )
)

// 首页进入的可点击日志判读实验。
@Composable
internal fun NativeCrashDemo() {
    // 旋转后恢复所选案例编号。
    var selected by rememberSaveable { mutableIntStateOf(0) }
    // 先自行阅读证据，点击按钮后再展示参考分析。
    var revealed by rememberSaveable { mutableStateOf(false) }
    // 案例内容由选择编号派生，不重复保存一份内容。
    val current = crashCases[selected]
    // 纵向布局由外层 LazyColumn 滚动。
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 明确实验不会故意触发真实信号。
        Text("Day60 · Native 崩溃排查（日志判读实验）")
        // 上半部分读取真实系统信息。
        Text("当前设备：${Build.MANUFACTURER} ${Build.MODEL}；Android ${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}")
        // 设备支持 ABI 不等于本次应用进程的实际 ABI。
        Text("设备支持 ABI：${Build.SUPPORTED_ABIS.joinToString()}（不是进程 ABI 结论）")
        // 给不同安装包与日志提供版本对照。
        Text("当前 APK：${BuildConfig.APPLICATION_ID}；${BuildConfig.FLAVOR}/${BuildConfig.BUILD_TYPE}；版本 ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        // 明确下面的库名和 Build ID 都是虚构材料。
        Text("以下均为教学样例，不是当前设备故障；没有真实 JNI、SDK 或 Native 崩溃。")
        // 每个按钮切换一份证据。
        crashCases.forEachIndexed { index, item ->
            // 切换后隐藏答案，便于再次练习。
            Button(onClick = {
                // 更新选择会使 Compose 重新显示对应日志。
                selected = index
                // 重置参考分析展示状态。
                revealed = false
                // 只记录教学选择，不输出伪造 Fatal signal。
                Log.d("Day60", "simulation=true case=$index")
            }) {
                // 展示案例名称。
                Text(item.title)
            }
        }
        // 等宽显示原始日志，方便辨认信号与堆栈。
        Text("样例证据：\n${current.evidence}", fontFamily = FontFamily.Monospace)
        // 先带着问题阅读日志，再查看解释。
        Text("先判断：这是哪一层？现有证据能证明什么？还缺什么？")
        // 点击只是显示答案，不执行任何崩溃操作。
        Button(onClick = { revealed = !revealed }) {
            // 标题跟随展示状态变化。
            Text(if (revealed) "收起分析" else "查看参考分析")
        }
        // 控制分析区是否进入组合。
        if (revealed) {
            // 先给出证据支持的结论。
            Text("能确定：${current.conclusion}")
            // 再给出下一步，而不是提前宣称找到根因。
            Text("下一步：${current.next}")
        }
        // 给出 Day59 到 Day60 的连续学习线索。
        Text("接 Day59：对照暂停 → 解绑 → surfaceDestroyed → 迟到回调；先取证，不能看到 SIGSEGV 就认定是 Surface 问题。")
        // 将部署差异作为对照变量，不当作预设根因。
        Text("对照实验：固定设备和操作，比较完整 APK 安装与 Apply Changes；再比较真机与模拟器。步骤与记录表见 Day60 文档。")
    }
}
