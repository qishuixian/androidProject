package com.example.myapplication.day21

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.dp

@Composable
internal fun RepositoryDemo() {
    val model: RepositoryViewModel = viewModel()
    val state by model.state.collectAsStateWithLifecycle()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day 21 · Repository 分层", style = MaterialTheme.typography.headlineSmall)
        Text("UI 发送事件，ViewModel 管状态，Repository 负责提供课程数据。")
        when (val current = state) {
            RepositoryUiState.Idle -> Text("还没有加载")
            RepositoryUiState.Loading -> Text("Repository 加载中…")
            is RepositoryUiState.Success -> Text("加载成功：${current.courses.joinToString { it.title }}")
            is RepositoryUiState.Error -> Text("加载失败：${current.message}")
        }
        Button(onClick = model::load) { Text("加载课程") }
        Text("替换 CourseRepository 的实现，不需要修改这个 UI。")
    }
}
