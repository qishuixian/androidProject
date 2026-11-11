// Day50 使用固定案例学习定位，不主动制造真实崩溃。
package com.example.myapplication.day50

// Log 输出本次模拟请求的阶段，便于与页面案例对照。
import android.util.Log
// Compose 布局与交互控件。
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
// 状态保存当前选择的案例编号。
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.dp

// 每个案例同时提供日志证据、最后成功阶段和下一步动作。
private data class BridgeCase(val title: String, val evidence: String, val nextStep: String)

// 这些日志是教学样例，不是设备当前发生的故障。
private val bridgeCases = listOf(
    // 请求已进入 Native，但业务字段校验失败。
    BridgeCase("协议校验失败", "requestId=r50 protocol=1001 stage=RECEIVE\nrequestId=r50 stage=REJECT reason=missing_courseId",
        "接收成功，校验失败。对照发送端 JSON 和字段类型，不要先查引擎渲染。"),
    // 网页自己的 JS 错误不等于 Java/Kotlin 崩溃。
    BridgeCase("网页脚本错误", "Uncaught ReferenceError: onNativeProtocol is not defined (scene.js:12)",
        "检查脚本路径、加载顺序和 ready 时机；在 Debug WebView 的 Console 查看堆栈。本课没有实际执行 scene.js。"),
    // 托管层崩溃要找到异常原因与应用源码位置。
    BridgeCase("Java/Kotlin 崩溃", "FATAL EXCEPTION: main\nIllegalStateException: WebView has been destroyed\nat example.Bridge.reply(Bridge.kt:42)",
        "读取异常类型、Caused by 和首个应用栈帧；检查页面退出后是否仍回调旧 WebView。这里的类名和行号是示意。"),
    // 资源失败与桥接业务失败要分开记录。
    BridgeCase("引擎资源失败", "requestId=r50 stage=SCENE_LOAD\nFailed to load scene/resource.json",
        "核对入口地址、资源是否打包、大小写、版本与资源加载日志；仅凭目录名不能证明真实 Laya 已运行。"),
    // Native 此处特指 C/C++ 层，而非泛指 Android 页面。
    BridgeCase("C/C++ 崩溃", "Fatal signal 11 (SIGSEGV)\nbacktrace: #00 pc ... libexample.so",
        "保留 tombstone/backtrace、ABI、应用版本、Build ID 与匹配符号，符号化后定位。不能把它直接当 Kotlin 空指针修。"),
    // 接收日志后没有完成，仍不足以证明原因是线程或网络。
    BridgeCase("缺少完成日志", "requestId=r50 stage=RECEIVE\nrequestId=r50 stage=DISPATCH",
        "最后可见阶段是分发；继续查处理器进入、完成、页面销毁和超时日志。日志缺失本身不是根因证据。")
)

// 首页直接展示可重复的本地诊断练习。
@Composable
internal fun BridgeDiagnosisDemo() {
    // 旋转时保留案例编号，不保存异常对象或 Activity。
    var selected by rememberSaveable { mutableIntStateOf(0) }
    // 当前案例是编号的派生数据。
    val current = bridgeCases[selected]
    // 本页面由外层 LazyColumn 滚动，不再嵌套长列表。
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Day50 · Bridge 日志和崩溃定位（模拟）")
        // 选择案例只切换教学材料，不真的造成应用闪退。
        bridgeCases.forEachIndexed { index, item ->
            Button(onClick = {
                selected = index
                // 日志不包含完整 payload、Cookie 或 Token。
                Log.d("Day50", "simulation=true requestId=r50 protocol=1001 case=$index stage=SELECT")
            }) { Text(item.title) }
        }
        // 明确区分样例证据与当前设备状态。
        Text("样例日志：\n${current.evidence}")
        Text("下一步：${current.nextStep}")
        Text("按 requestId 串联 SEND → RECEIVE → VALIDATE → DISPATCH → HANDLE → REPLY；失败另记 ERROR/REJECT。")
    }
}
