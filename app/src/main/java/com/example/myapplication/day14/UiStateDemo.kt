package com.example.myapplication.day14

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp

private sealed interface CourseUiState {
    data object Idle : CourseUiState
    data object Loading : CourseUiState
    data class Success(val count: Int) : CourseUiState
    data object Empty : CourseUiState
    data class Error(val message: String) : CourseUiState
}

@Composable
internal fun UiStateDemo() {
    var state by remember { mutableStateOf<CourseUiState>(CourseUiState.Idle) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day 14 · sealed 状态", style = MaterialTheme.typography.headlineSmall)
        Text("每次只有一种明确状态，when 会要求处理所有分支。")
        when (val current = state) {
            CourseUiState.Idle -> Text("尚未加载")
            CourseUiState.Loading -> Text("加载中…")
            is CourseUiState.Success -> Text("加载成功：${current.count} 门课程")
            CourseUiState.Empty -> Text("暂无课程")
            is CourseUiState.Error -> Text("加载失败：${current.message}")
        }
        Button(onClick = { state = CourseUiState.Loading }) { Text("加载中") }
        Button(onClick = { state = CourseUiState.Success(3) }) { Text("成功") }
        Button(onClick = { state = CourseUiState.Empty }) { Text("空数据") }
        Button(onClick = { state = CourseUiState.Error("请稍后重试") }) { Text("失败") }
    }
}
