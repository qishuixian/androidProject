// Day54 模拟 RTM 消息，不连接真实聊天服务或 SDK。
package com.example.myapplication.day54

// 布局、输入、按钮与文字组成消息实验页面。
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
// 页面状态与任务随组合生命周期管理。
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 消息状态不使用多个互相冲突的 Boolean。
internal enum class Delivery { SENDING, SENT, FAILED }

// clientId 用于关联发送和确认，serverSeq 用于当前会话内的服务端顺序。
internal data class ChatMessage(
    val clientId: String,
    val text: String,
    val serverSeq: Long? = null,
    val delivery: Delivery = Delivery.SENDING
)

// 统一合并确认或接收事件，相同 clientId 更新已有消息而不追加重复行。
internal fun mergeMessage(current: List<ChatMessage>, incoming: ChatMessage): List<ChatMessage> {
    // 本例已确认的消息不被较旧的失败/发送状态降级。
    val previous = current.firstOrNull { it.clientId == incoming.clientId }
    val accepted = if (previous?.delivery == Delivery.SENT && incoming.delivery != Delivery.SENT) previous else incoming
    // 创建新列表，不原地修改旧状态；未确认消息排在已确认消息之后。
    return (current.filterNot { it.clientId == accepted.clientId } + accepted)
        .sortedWith(compareBy<ChatMessage> { it.serverSeq ?: Long.MAX_VALUE }.thenBy { it.clientId })
}

// 首页进入单会话本地实验，离开后消息重置。
@Composable
internal fun ChatDemo() {
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("你好，Kotlin") }
    var messages by remember { mutableStateOf(emptyList<ChatMessage>()) }
    var counter by remember { mutableIntStateOf(0) }
    var failNext by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf("消息只保存在当前页面") }
    // 用同一发送函数处理首次发送和失败重试，重试复用 clientId。
    fun transmit(message: ChatMessage) {
        // 固定本次故障配置，不让等待期间的开关改变在途请求。
        val shouldFail = failNext
        failNext = false
        messages = mergeMessage(messages, message.copy(delivery = Delivery.SENDING))
        scope.launch {
            // 模拟 SDK/服务端异步结果；发送入队不等于确认成功。
            delay(400)
            messages = mergeMessage(messages, if (shouldFail) {
                message.copy(delivery = Delivery.FAILED)
            } else {
                // 演示服务端为自己的消息分配稳定序号，不使用设备时钟排序。
                message.copy(serverSeq = 100L + message.clientId.removePrefix("local-").toLong(), delivery = Delivery.SENT)
            })
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day54 · RTM 聊天消息（本地模拟）")
        OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("消息内容") })
        // 限制演示消息数量，避免单个首页列表项无限增长。
        Button(enabled = input.isNotBlank() && input.length <= 200 && counter < 10, onClick = {
            counter++
            transmit(ChatMessage("local-$counter", input.trim()))
        }) { Text("发送（最多 10 条）") }
        Button(onClick = { failNext = !failNext }) { Text("下次发送失败：$failNext") }
        Button(onClick = {
            // 模拟先收到 seq=2，再收到 seq=1，最后重复收到 seq=2。
            val second = ChatMessage("remote-2", "对方的第二条", 2, Delivery.SENT)
            messages = mergeMessage(messages, second)
            messages = mergeMessage(messages, ChatMessage("remote-1", "对方的第一条", 1, Delivery.SENT))
            messages = mergeMessage(messages, second)
            notice = "已按序号排序，重复 remote-2 不新增行"
        }) { Text("接收乱序和重复消息") }
        Text(notice)
        // 展示统一状态；失败行提供重试，保留原 clientId。
        messages.forEach { message ->
            Text("${message.clientId} / seq=${message.serverSeq ?: "待确认"} / ${message.delivery}：${message.text}")
            if (message.delivery == Delivery.FAILED) {
                Button(onClick = { transmit(message) }) { Text("重试 ${message.clientId}") }
            }
        }
        Text("SENT 仅表示本例服务端确认，不代表对方已读。")
    }
}
