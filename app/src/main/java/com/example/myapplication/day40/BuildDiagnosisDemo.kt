// Day40 提供确定性的本地排错练习，不在手机上执行 Gradle 或读取电脑环境。
package com.example.myapplication.day40

// 布局组件负责纵向展示练习按钮和结果。
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
// Material 按钮和文字用于交互。
import androidx.compose.material3.Button
import androidx.compose.material3.Text
// Composable 标记界面函数，委托运算符让 State 可按普通变量读写。
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
// 可保存状态保留当前选择的题目编号，不保存环境对象。
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
// dp 用于界面间距。
import androidx.compose.ui.unit.dp

// 每个案例分别保存日志、问题阶段和下一步动作。
private data class DiagnosisCase(val title: String, val log: String, val explanation: String)

// 练习数据只模拟日志，不会真的破坏 JDK 或安装应用。
private val cases = listOf(
    // 找不到 jlink 是构建环境证据，不能据此判断 Kotlin 代码写错。
    DiagnosisCase("JDK 路径失效", "jlink executable .../bin/jlink.exe does not exist",
        "构建阶段：核对 Daemon JVM criteria、Gradle 实际 JVM 与该路径下 jlink。仅改终端 JAVA_HOME 不一定覆盖 Daemon JVM criteria。"),
    // SDK 路径和 JDK 路径是两套配置。
    DiagnosisCase("SDK 未定位", "SDK location not found",
        "配置阶段：检查本机 local.properties 的 sdk.dir 或 SDK 环境配置，以及目录是否存在。不要把 Android Studio 安装目录当 SDK。"),
    // 找不到符号需要先看失败任务属于哪个模块和变体。
    DiagnosisCase("Kotlin 编译失败", "compileDemoDebugKotlin: Unresolved reference: CourseLabel",
        "源码/依赖阶段：检查 import、模块依赖与 api 暴露范围；clean 不会自动补上缺失依赖。"),
    // APK 已存在但安装失败，应读安装工具返回值。
    DiagnosisCase("签名不一致", "INSTALL_FAILED_UPDATE_INCOMPATIBLE",
        "安装阶段：设备已有同安装 id、不同签名的包。使用一致签名或独立安装 id；卸载会删除本地数据，不能当作默认修复。"),
    // 运行异常要看应用堆栈，不是系统服务噪声。
    DiagnosisCase("启动后崩溃", "FATAL EXCEPTION: main / Process: com.example.myapplication.demo",
        "运行阶段：读取异常类型、Caused by 和首个应用源码栈帧；构建成功不代表启动一定成功。")
)

// 首页进入此组件，只进行案例选择和文字展示。
@Composable
internal fun BuildDiagnosisDemo() {
    // 默认选择第一个案例，旋转后恢复所选编号。
    var selected by rememberSaveable { mutableIntStateOf(0) }
    // 当前案例由编号派生，无需再保存第二份状态。
    val current = cases[selected]
    // 本组件由首页 LazyColumn 承载，不再嵌套滚动容器。
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 说明模拟边界，避免把手机结果误认为电脑环境检查。
        Text("Day40 · 构建失败排查（本地模拟练习）")
        Text("先区分：配置 → 编译 → 打包 → 安装 → 运行")
        // 遍历固定案例，点击后仅更新编号。
        cases.forEachIndexed { index, item ->
            // 点击回调中才改变状态，由 Compose 更新案例说明。
            Button(onClick = { selected = index }) { Text(item.title) }
        }
        // 展示原始日志特征与针对性排查动作。
        Text("日志：${current.log}")
        Text(current.explanation)
        // 不将清缓存当作所有问题的第一步。
        Text("先保留失败日志，定位失败任务和原因，再修改相关配置；不要反复全量 clean。")
    }
}
