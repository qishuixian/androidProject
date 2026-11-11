// Day52 使用固定虚拟文件模拟上传，不读取手机文件、不连接 OSS。
package com.example.myapplication.day52

// Compose 组件显示状态、进度和操作。
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
// 状态与协程跟随当前页面生命周期。
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

// 临时授权只保留演示所需的对象范围和过期标记，不包含真实密钥。
internal data class UploadGrant(val objectKey: String, val expired: Boolean)

// 假上传器只模拟已发送字节，不假装真正发送 multipart 请求。
internal suspend fun simulateUpload(
    grant: UploadGrant,
    failHalfway: Boolean,
    onProgress: (Long, Long) -> Unit
): String {
    // 发送前先验证授权，过期时不开始上传。
    require(!grant.expired) { "临时授权过期，请重新申请" }
    // 固定 1 MiB 虚拟文件，避免申请文件权限或访问私人文件。
    val total = 1024L * 1024
    for (part in 1..20) {
        // delay 是可取消等待，不阻塞 UI 线程。
        delay(100)
        // 可重复模拟一次传输中断，调用方决定是否发起新尝试。
        if (failHalfway && part == 10) throw IOException("模拟上传中断")
        // 回调已发送字节与总字节，UI 根据比例展示进度。
        onProgress(total * part / 20, total)
    }
    // 返回对象 key，不返回虚构的公网下载地址。
    return grant.objectKey
}

// 首页进入本组件；所有授权和上传都只是本地模拟。
@Composable
internal fun UploadDemo() {
    // 离开页面自动取消当前作用域中的上传任务。
    val scope = rememberCoroutineScope()
    var task by remember { mutableStateOf<Job?>(null) }
    var running by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var message by remember { mutableStateOf("虚拟文件：lesson.pdf，1 MiB；尚未上传") }
    var attemptedFailure by remember { mutableStateOf(false) }
    // 所有入口共用同一条发送链；没有并发上传或自动无限重试。
    fun start(fail: Boolean, expired: Boolean = false) {
        if (running) return
        running = true
        progress = 0f
        task = scope.launch {
            try {
                message = "申请临时授权（模拟）"
                // 与 Day51 类似，真实授权应从自己的业务服务获取。
                delay(150)
                val grant = UploadGrant("learning/day52/lesson.pdf", expired)
                message = "正在上传（模拟）"
                val key = simulateUpload(grant, fail) { sent, total ->
                    // 本例协程在主线程，真实 SDK 回调需核对线程再更新 UI。
                    progress = sent.toFloat() / total
                }
                message = "服务端确认成功（模拟），对象 key：$key"
            } catch (cancelled: CancellationException) {
                // 取消不是网络错误；继续抛出，finally 仍会执行。
                message = "已取消；已发送进度不代表对象已成功保存"
                throw cancelled
            } catch (error: IOException) {
                // 只有传输失败才启用本例手动重试。
                attemptedFailure = true
                message = "上传失败：${error.message}；可从头重试"
            } catch (expiredGrant: IllegalArgumentException) {
                // 授权失败和传输失败展示不同原因。
                message = expiredGrant.message ?: "授权无效"
            } finally {
                running = false
            }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Day52 · 文件上传与 OSS（本地模拟）")
        Text("进度：${(progress * 100).toInt()}%")
        LinearProgressIndicator(progress = { progress })
        Button(enabled = !running, onClick = { attemptedFailure = false; start(false) }) { Text("模拟成功上传") }
        Button(enabled = !running, onClick = { attemptedFailure = false; start(true) }) { Text("模拟中途失败") }
        Button(enabled = !running && attemptedFailure, onClick = { attemptedFailure = false; start(false) }) {
            Text("重新申请授权并从头重试")
        }
        Button(enabled = !running, onClick = { attemptedFailure = false; start(false, true) }) { Text("模拟授权过期") }
        // cancel 触发协程取消，由上传链 finally 统一恢复按钮状态。
        Button(enabled = running, onClick = { task?.cancel() }) { Text("取消上传") }
        Text(message)
        Text("没有选择真实文件，没有发送网络请求，也没有保存真实 OSS 凭证。")
    }
}
