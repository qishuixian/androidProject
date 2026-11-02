package com.example.myapplication.day7

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun AsyncLessonDemo() {
    val factory = remember {
        AsyncLessonViewModelFactory(DemoAsyncLessonRepository())
    }
    // 使用当前 Activity 的 ViewModel 范围，旋转或返回首页重进时保留状态。
    val model: AsyncLessonViewModel = viewModel(factory = factory)
    val state by model.uiState.collectAsStateWithLifecycle()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Day 7 · 异步加载", style = MaterialTheme.typography.headlineSmall)
        Text("本地模拟：首次加载等待 1 秒后失败，重试后显示课程，无需联网。")
        when (val current = state) {
            AsyncLessonUiState.Idle -> {
                Text("尚未加载")
                Button(onClick = model::load) { Text("加载课程") }
            }
            AsyncLessonUiState.Loading -> {
                CircularProgressIndicator()
                Text("正在加载，请稍候…")
            }
            is AsyncLessonUiState.Success -> {
                if (current.names.isEmpty()) Text("暂无课程")
                else {
                    Text("加载成功", style = MaterialTheme.typography.titleLarge)
                    current.names.forEach { Text("• $it") }
                }
                Button(onClick = model::load) { Text("重新加载") }
            }
            is AsyncLessonUiState.Error -> {
                Text(current.message, color = MaterialTheme.colorScheme.error)
                Button(onClick = model::load) { Text("重试") }
            }
        }
        Text("点击 → ViewModel → Repository 挂起等待 → StateFlow 更新 → UI 重组")
        Text("返回首页再进入会保留本次状态；要重新体验首次失败，请结束应用进程后重新启动。")
    }
}
