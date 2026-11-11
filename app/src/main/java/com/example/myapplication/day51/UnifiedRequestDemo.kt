// Day51 通过本地假传输演示统一请求层，不发送真实 Token 或网络请求。
package com.example.myapplication.day51

// Compose 布局和交互控件。
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
// 状态和协程作用域随组合生命周期管理。
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

// HTTP 状态与服务端业务响应是两层信息，不混成一个 code。
internal data class BusinessBody(val code: Int, val message: String, val data: String?)
internal data class TransportReply(val httpStatus: Int, val body: BusinessBody?)

// UI 只认识统一结果，不需要知道底层异常类。
internal sealed interface RequestResult {
    data class Success(val value: String) : RequestResult
    data object LoginExpired : RequestResult
    data class Failure(val reason: String) : RequestResult
}

// Token 仓库仅在本地内存保存演示值，不记录到日志或界面。
internal class DemoSession {
    var token: String? = null
        private set
    // 实际工程通常由登录响应写入，并按安全需求选择存储方式。
    fun login() { token = "local-demo-token" }
    // 统一登录失效处理清理会话，不在网络层直接持有 Activity。
    fun clear() { token = null }
}

// 假服务端接收公共请求头，便于验证封装并非只切换展示文字。
internal suspend fun fakeTransport(scene: Int, headers: Map<String, String>): TransportReply {
    // delay 可取消，不阻塞主线程。
    delay(350)
    // 没有合法演示凭证时优先返回 HTTP 401。
    if (headers["Authorization"] != "Bearer local-demo-token") return TransportReply(401, null)
    // 各场景固定返回，方便重复实验。
    return when (scene) {
        0 -> TransportReply(200, BusinessBody(0, "成功", "Kotlin 课程"))
        1 -> TransportReply(503, null)
        2 -> TransportReply(200, BusinessBody(1002, "课程暂不可选", null))
        3 -> TransportReply(200, BusinessBody(40101, "登录失效", null))
        else -> throw IOException("模拟网络断开")
    }
}

// 请求层统一组装 Token、区分 HTTP 与业务错误、转换预期网络异常。
internal class UnifiedRequester(private val session: DemoSession) {
    suspend fun load(scene: Int): RequestResult {
        // 本次请求取会话快照；生产并发退出/重新登录还需要会话版本检查。
        val token = session.token
        val headers = if (token == null) emptyMap() else mapOf("Authorization" to "Bearer $token")
        return try {
            val reply = fakeTransport(scene, headers)
            // 先判断 HTTP 401，不假定非 2xx 也带正常业务 body。
            if (reply.httpStatus == 401) {
                session.clear()
                RequestResult.LoginExpired
            } else if (reply.httpStatus !in 200..299) {
                RequestResult.Failure("HTTP 错误：${reply.httpStatus}")
            } else {
                val body = reply.body
                // 2xx 仍需判断业务码与数据结构。
                when {
                    body == null -> RequestResult.Failure("响应格式错误：缺少业务响应")
                    body.code == 40101 -> {
                        session.clear()
                        RequestResult.LoginExpired
                    }
                    body.code != 0 -> RequestResult.Failure("业务错误 ${body.code}：${body.message}")
                    body.data == null -> RequestResult.Failure("响应格式错误：成功响应缺少数据")
                    else -> RequestResult.Success(body.data)
                }
            }
        } catch (cancelled: CancellationException) {
            // 页面离开导致的取消继续传播，不能伪装为网络失败。
            throw cancelled
        } catch (network: IOException) {
            // 只转换预期传输异常，不把所有编程错误吞掉。
            RequestResult.Failure("网络连接失败（模拟）")
        }
    }
}

// 首页可直接操作五种确定性场景。
@Composable
internal fun UnifiedRequestDemo() {
    // 会话和请求器属于当前页面实验，离开后重置。
    val session = remember { DemoSession() }
    val requester = remember { UnifiedRequester(session) }
    val scope = rememberCoroutineScope()
    var signedIn by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("先模拟登录，再选择请求场景") }
    // 多种请求共享同一条处理链，按钮只传场景编号。
    fun request(scene: Int) {
        if (loading) return
        loading = true
        scope.launch {
            try {
                // UI 不直接处理 HTTP 或异常，只消费统一结果。
                message = when (val result = requester.load(scene)) {
                    is RequestResult.Success -> "成功：${result.value}"
                    RequestResult.LoginExpired -> "登录已失效，请重新登录（本例不自动导航）"
                    is RequestResult.Failure -> result.reason
                }
                signedIn = session.token != null
            } finally {
                loading = false
            }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Day51 · 统一请求、Token、业务错误码（本地模拟）")
        Text("会话：${if (signedIn) "已登录" else "未登录"}；请求中：$loading")
        // 不显示凭证本身，也不在日志中记录 Authorization。
        Button(enabled = !loading, onClick = { session.login(); signedIn = true; message = "模拟登录成功" }) {
            Text("模拟登录 / 重新登录")
        }
        // 同样的请求入口用于成功、HTTP、业务、鉴权和网络错误。
        listOf("成功", "HTTP 503", "业务错误", "业务登录失效", "网络失败").forEachIndexed { index, title ->
            Button(enabled = !loading, onClick = { request(index) }) { Text(title) }
        }
        Text(message)
        Text("未登录时任意请求返回 HTTP 401；先登录才会进入所选模拟场景。")
    }
}
