// Day56 用本地状态机模拟音视频 SDK，不引入真实厂商 SDK。
package com.example.myapplication.day56

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// SDK 生命周期必须有明确阶段，避免重复初始化或重复释放。
private enum class EngineState { NOT_INITIALIZED, INITIALIZING, READY, JOINED, LEAVING, RELEASED }

// 首页进入本地音视频 SDK 生命周期练习。
@Composable
internal fun VideoSdkDemo() {
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf(EngineState.NOT_INITIALIZED) }
    var message by remember { mutableStateOf("尚未初始化") }
    var task by remember { mutableStateOf<Job?>(null) }
    // 初始化、加入和退出都通过同一条状态链，避免并发操作。
    fun run(action: suspend () -> Unit) {
        if (task?.isActive == true) return
        task = scope.launch { action(); task = null }
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Day56 · 音视频 SDK 生命周期（本地模拟）")
        Text("SDK 状态：$state")
        Button(enabled = state == EngineState.NOT_INITIALIZED, onClick = {
            run {
                state = EngineState.INITIALIZING
                message = "加载 SDK 配置和引擎资源…"
                delay(500)
                state = EngineState.READY
                message = "SDK 初始化完成，可以加入频道"
            }
        }) { Text("初始化 SDK") }
        Button(enabled = state == EngineState.READY, onClick = {
            run {
                message = "正在加入频道…"
                delay(500)
                state = EngineState.JOINED
                message = "已加入频道（本地模拟）"
            }
        }) { Text("加入频道") }
        Button(enabled = state == EngineState.JOINED, onClick = {
            run {
                state = EngineState.LEAVING
                message = "停止发布、取消回调、离开频道…"
                delay(400)
                state = EngineState.READY
                message = "已离开频道；SDK 仍可再次加入"
            }
        }) { Text("退出频道") }
        Button(enabled = state == EngineState.READY, onClick = {
            state = EngineState.RELEASED
            message = "SDK 已释放；页面结束前不能继续使用"
        }) { Text("释放 SDK") }
        Button(enabled = state == EngineState.RELEASED, onClick = {
            state = EngineState.NOT_INITIALIZED
            message = "允许下一次完整初始化"
        }) { Text("重新开始生命周期") }
        Text(message)
        Text("本例没有真实麦克风、摄像头、频道或网络权限。")
    }
}
