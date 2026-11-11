// Day55 用确定性本地状态模拟断网、重复、取消和日志脱敏。
package com.example.myapplication.day55

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 一次请求的状态，比 loading/error 两个 Boolean 更不容易出现矛盾组合。
private enum class TaskState { IDLE, RUNNING, SUCCESS, OFFLINE, CANCELLED, FAILED }

// 业务去重事件的唯一键；同一个 key 只能执行一次本地处理。
private data class Event(val id: String, val body: String)

// 本地演示的幂等处理器。
private class IdempotentProcessor {
    private val handled = mutableSetOf<String>()
    fun process(event: Event): String {
        if (!handled.add(event.id)) return "重复事件已忽略：${event.id}"
        return "已处理：${event.body}"
    }
}

// 首页进入诊断练习，不访问真实网络。
@Composable
internal fun NetworkDebugDemo() {
    val scope = rememberCoroutineScope()
    val processor = remember { IdempotentProcessor() }
    var online by remember { mutableStateOf(true) }
    var state by remember { mutableStateOf(TaskState.IDLE) }
    var task by remember { mutableStateOf<Job?>(null) }
    var message by remember { mutableStateOf("在线，等待操作") }
    var debugLog by remember { mutableStateOf("日志已脱敏") }
    // 发起可取消任务；重复点击在任务活动时被拒绝。
    fun startRequest() {
        if (task?.isActive == true) return
        state = TaskState.RUNNING
        message = "请求进行中…"
        debugLog = "requestId=req-55 stage=SEND online=$online token=<redacted>"
        task = scope.launch {
            try {
                delay(1200)
                if (!online) {
                    state = TaskState.OFFLINE
                    message = "断网：请求未完成，可恢复网络后重试"
                    debugLog = "requestId=req-55 stage=OFFLINE token=<redacted>"
                } else {
                    state = TaskState.SUCCESS
                    message = "请求成功（模拟）"
                    debugLog = "requestId=req-55 stage=REPLY status=200 token=<redacted>"
                }
            } catch (cancelled: CancellationException) {
                state = TaskState.CANCELLED
                message = "任务已取消，不显示为网络错误"
                debugLog = "requestId=req-55 stage=CANCELLED token=<redacted>"
                throw cancelled
            } finally {
                task = null
            }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Day55 · 断网、重复消息、取消和调试（本地模拟）")
        Button(onClick = { online = !online; message = if (online) "已恢复网络" else "已模拟断网" }) {
            Text(if (online) "模拟断网" else "恢复网络")
        }
        Button(enabled = task?.isActive != true, onClick = { startRequest() }) { Text("发起请求") }
        Button(enabled = task?.isActive == true, onClick = { task?.cancel() }) { Text("取消当前请求") }
        Button(onClick = {
            val result = processor.process(Event("event-55", "课程完成"))
            message = result
        }) { Text("处理事件 event-55") }
        Text("状态：$state；网络：${if (online) "在线" else "离线"}")
        Text(message)
        Text("调试日志：$debugLog")
        Text("返回首页会取消页面任务；日志展示 requestId 和阶段，不打印 Token/Cookie。")
    }
}
