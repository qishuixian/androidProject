// Day58 模拟 SDK 后台回调，不连接真实频道或采集媒体。
package com.example.myapplication.day58

// 日志用于观察模拟回调与状态处理的线程。
import android.util.Log
// 布局与按钮展示远端用户状态。
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
// 页面状态与作用域跟随组合生命周期。
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
// Default 模拟 SDK 后台线程，返回 Main 后才更新 UI。
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// uid 是当前频道内的身份，不把列表下标当用户身份。
private data class RemoteUser(val uid: Long, val quality: String = "未知")

// 模拟 SDK 的三类事件，携带会话代次过滤旧频道迟到事件。
private data class RemoteEvent(val generation: Int, val kind: String, val uid: Long)

// 首页进入当前页面会话的本地实验。
@Composable
internal fun RemoteUserDemo() {
    // 离开组合自动取消未完成的模拟回调任务。
    val scope = rememberCoroutineScope()
    var generation by remember { mutableIntStateOf(1) }
    var joined by remember { mutableStateOf(true) }
    var users by remember { mutableStateOf(emptyList<RemoteUser>()) }
    var message by remember { mutableStateOf("已模拟加入频道，等待远端事件") }
    // 统一事件入口：后台产生事件，Main 串行处理本页面状态。
    fun emit(kind: String, delayMs: Long = 150) {
        // 发送时捕获代次，不能在回调时拿新代次冒充旧事件。
        val captured = generation
        scope.launch {
            val event = withContext(Dispatchers.Default) {
                delay(delayMs)
                Log.d("Day58", "模拟 SDK 回调 thread=${Thread.currentThread().name} generation=$captured kind=$kind")
                RemoteEvent(captured, kind, 1001L)
            }
            // withContext 完成后返回调用作用域的 Main 线程。
            Log.d("Day58", "状态处理 thread=${Thread.currentThread().name}")
            if (!joined || event.generation != generation) {
                message = "忽略旧频道事件：${event.kind} / generation=${event.generation}"
            } else {
                // 重复加入按 uid 更新，质量事件不创建不存在的用户。
                when (event.kind) {
                    "join" -> {
                        if (users.none { it.uid == event.uid }) users = users + RemoteUser(event.uid)
                        message = "远端用户加入：${event.uid}（重复回调不新增行）"
                    }
                    "leave" -> {
                        users = users.filterNot { it.uid == event.uid }
                        message = "远端用户离开：${event.uid}"
                    }
                    "quality" -> {
                        users = users.map { if (it.uid == event.uid) it.copy(quality = "差（模拟）") else it }
                        message = "质量事件已处理；未知用户不会被自动加入"
                    }
                }
            }
        }
    }
    // 当前频道与远端列表是不同状态，本例只模拟一名远端用户。
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Day58 · 远端用户回调（本地模拟）")
        Text("频道已加入：$joined；会话代次：$generation")
        Button(enabled = joined, onClick = { emit("join") }) { Text("模拟用户 1001 加入 / 重复加入") }
        Button(enabled = joined, onClick = { emit("quality") }) { Text("模拟远端网络质量差") }
        Button(enabled = joined, onClick = { emit("leave") }) { Text("模拟用户离开") }
        Button(enabled = joined, onClick = { emit("join", 1500) }) { Text("延迟 1.5 秒的加入事件") }
        // 退出立即更新代次，避免旧事件影响下一次加入。
        Button(onClick = {
            generation++
            joined = !joined
            users = emptyList()
            message = if (joined) "已模拟重新加入，新会话" else "已退出频道，清空远端列表"
        }) { Text(if (joined) "退出频道" else "重新加入频道") }
        users.forEach { user -> Text("uid=${user.uid}；网络质量=${user.quality}") }
        Text(message)
        Text("用户加入不等于有视频首帧；网络质量差不等于用户离开。Logcat 搜索 Day58。")
    }
}
