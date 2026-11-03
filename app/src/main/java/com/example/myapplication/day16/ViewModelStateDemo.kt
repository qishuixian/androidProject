package com.example.myapplication.day16

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.dp

@Composable
internal fun ViewModelStateDemo() {
    val model: CourseStateViewModel = viewModel()
    val state by model.state.collectAsState()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day 16 · ViewModel 状态", style = MaterialTheme.typography.headlineSmall)
        Text("状态放在 ViewModel，界面只负责观察和发送事件。")
        when (val current = state) {
            CourseScreenState.Idle -> Text("等待操作")
            CourseScreenState.Loading -> Text("加载中…")
            is CourseScreenState.Success -> Text("课程数量：${current.count}")
            is CourseScreenState.Error -> Text("错误：${current.message}")
        }
        Button(onClick = model::loadCourses) { Text("加载课程") }
        Button(onClick = model::fail) { Text("模拟失败") }
    }
}
