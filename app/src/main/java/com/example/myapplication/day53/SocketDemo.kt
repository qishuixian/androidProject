// Day53 是本地连接状态机，不打开真实 WebSocket。
package com.example.myapplication.day53

// 布局和操作按钮。
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
// State 和作用域随页面组合管理。
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
// Job 控制唯一连接循环，delay 模拟异步等待。
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 显式区分连接阶段，不用多个互相矛盾的 Boolean。
private enum class SocketPhase { IDLE, CONNECTING, OPEN, WAITING_RETRY, CLOSED }

// 首页进入本地练习；没有服务器地址、Token 或网络依赖。
@Composable
internal fun SocketDemo() {
    // 离开页面会取消所有连接、等待和心跳任务。
    val scope = rememberCoroutineScope()
    var phase by remember { mutableStateOf(SocketPhase.IDLE) }
    var loop by remember { mutableStateOf<Job?>(null) }
    var heartbeat by remember { mutableIntStateOf(0) }
    var retries by remember { mutableIntStateOf(0) }
    var dropNext by remember { mutableStateOf(false) }
    var failConnect by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("等待手动连接") }
    // 手动关闭不触发重连；取消同一 Job 同时终止心跳与退避等待。
    fun close() {
        loop?.cancel()
        loop = null
        phase = SocketPhase.CLOSED
        message = "手动关闭，不重连"
    }
    // 连接入口去重，避免多次点击启动多个循环。
    fun connect() {
        if (loop?.isActive == true) return
        retries = 0
        heartbeat = 0
        dropNext = false
        loop = scope.launch {
            // 有限重试，不无限后台循环。
            while (true) {
                phase = SocketPhase.CONNECTING
                message = "模拟握手中…"
                delay(300)
                if (!failConnect) {
                    phase = SocketPhase.OPEN
                    message = "已连接，等待模拟心跳"
                    // 每秒模拟一次 ping/pong，不是真实 TCP 或 WebSocket 帧。
                    while (!dropNext) {
                        delay(1000)
                        if (!dropNext) {
                            heartbeat++
                            message = "模拟 ping/pong 成功：$heartbeat"
                        }
                    }
                    // 本次故障已消费，下次连接默认可成功。
                    dropNext = false
                }
                // 连接失败或意外断开统一进入有限退避。
                if (retries >= 3) {
                    phase = SocketPhase.CLOSED
                    message = "达到重试上限，等待手动连接"
                    break
                }
                retries++
                val waitMs = 500L * (1L shl (retries - 1))
                phase = SocketPhase.WAITING_RETRY
                message = "意外失败：第 $retries 次重试，等待 ${waitMs}ms"
                // delay 可取消，手动关闭时不会继续偷偷连接。
                delay(waitMs)
            }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Day53 · WebSocket 连接与重连（本地模拟）")
        Text("状态：$phase；心跳：$heartbeat；重试：$retries/3")
        // 活跃期间只允许一条连接任务。
        Button(enabled = loop?.isActive != true, onClick = { connect() }) { Text("连接") }
        Button(enabled = phase == SocketPhase.OPEN, onClick = { dropNext = true }) { Text("模拟意外断开") }
        // 开启后后续握手失败，用来观察上限；关闭不会强行中断已打开连接。
        Button(onClick = { failConnect = !failConnect }) {
            Text("后续握手失败：${if (failConnect) "开" else "关"}")
        }
        Button(enabled = loop?.isActive == true, onClick = { close() }) { Text("手动关闭，不重连") }
        Text(message)
        Text("返回首页会取消页面任务；按 Home 不等于离开组合，本例不主动切断后台连接。")
    }
}
