// Day49 只模拟消息接收与分发，不依赖真实 Laya 或 RxJava。
package com.example.myapplication.day49

// Compose 布局和按钮用于可重复的本地实验。
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
// 组合状态只负责展示处理结果。
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
// 协程切换模拟后台收到消息再交给 UI 层。
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
// JSONObject 解析协议字符串，不执行其中的代码。
import org.json.JSONObject

// 把松散 JSON 转成有类型的业务事件。
internal data class CourseCompleted(val requestId: String, val courseId: String)

// 解码器只校验协议，不更新 UI，也不保存去重状态。
internal fun decodeCompletion(raw: String): CourseCompleted {
    // 限制输入大小，避免解析无界字符串。
    require(raw.length <= 4096) { "消息过长" }
    // 语法错误会抛 JSONException，由调用方展示错误。
    val json = JSONObject(raw)
    // 协议版本和协议号必须是明确的整数，而不是宽松转换的字符串。
    require(json.opt("version") == 1) { "不支持的协议版本" }
    require(json.opt("protocol") == 1001) { "未知协议号" }
    // requestId 是消息身份，不等于业务课程 id。
    val requestId = json.opt("requestId") as? String ?: error("缺少 requestId")
    require(requestId.isNotBlank() && requestId.length <= 80) { "requestId 无效" }
    // payload 必须是对象，字段必须是字符串。
    val payload = json.getJSONObject("payload")
    val courseId = payload.opt("courseId") as? String ?: error("缺少 courseId")
    require(courseId == "kotlin-49") { "课程 id 无效" }
    // 只有验证成功才产生业务事件。
    return CourseCompleted(requestId, courseId)
}

// 小型显式路由器展示事件分发，不伪装成 RxBus 实现。
internal class CompletionRouter(private val onComplete: (CourseCompleted) -> Unit) {
    // 当前页面会话的去重记录；不是跨进程持久化保证。
    private val handled = linkedSetOf<String>()

    // 调用方保证在主线程调用，因此无需跨线程共享集合。
    fun dispatch(event: CourseCompleted): String {
        // 相同请求 id 不重复执行此业务动作。
        if (!handled.add(event.requestId)) return "忽略重复消息：${event.requestId}"
        // 限制记录容量；淘汰后再次收到旧 id 仍可能重新处理。
        if (handled.size > 100) handled.remove(handled.first())
        // 同步调用当前页面注册的业务处理函数。
        onComplete(event)
        return "已分发：${event.requestId}"
    }
}

// 首页进入此组件，点击按钮模拟正常、重复和无效协议。
@Composable
internal fun ProtocolDemo() {
    // 页面离开组合时会取消未完成协程，避免回调旧 UI。
    val scope = rememberCoroutineScope()
    var count by remember { mutableIntStateOf(0) }
    var result by remember { mutableStateOf("等待消息") }
    var busy by remember { mutableStateOf(false) }
    // 路由器与页面同生命周期，不存到静态单例。
    val router = remember { CompletionRouter { count++ } }
    // 默认消息编码为 JSON，重复按钮故意复用同一个 requestId。
    val valid = """{"version":1,"protocol":1001,"requestId":"req-49","payload":{"courseId":"kotlin-49"}}"""
    // 所有入口共用一条接收链，避免各个按钮各自处理业务。
    fun receive(raw: String) {
        if (busy) return
        busy = true
        scope.launch {
            try {
                // Default 线程执行解码；这是模拟桥接后台消息，不是 UI 更新。
                val event = withContext(Dispatchers.Default) { decodeCompletion(raw) }
                // 返回主线程后再分发和更新 Compose 状态。
                result = router.dispatch(event)
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                // 组合移除等取消应继续传播，不能显示为协议错误。
                throw cancelled
            } catch (error: Exception) {
                // 校验失败不进入业务处理函数，计数不增加。
                result = "拒绝消息：${error.message}"
            } finally {
                busy = false
            }
        }
    }
    // 不再嵌套滚动区域，交给首页已有 LazyColumn 承载。
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Day49 · 协议号、JSON、事件分发")
        Text("实际业务处理次数：$count")
        Button(enabled = !busy, onClick = { receive(valid) }) { Text("发送 req-49（再次点击模拟重复）") }
        Button(enabled = !busy, onClick = { receive(valid.replace("1001", "9999")) }) { Text("发送未知协议") }
        Button(enabled = !busy, onClick = { receive("{broken") }) { Text("发送损坏 JSON") }
        Text(result)
        Text("JSON → 校验 → 类型事件 → 主线程路由 → 去重 → 业务处理")
        Text("本例是显式回调路由器，不是 RxBus；离开页面后去重记录重置。")
    }
}
