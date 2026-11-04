package com.example.myapplication.day20

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun CoursePageDemo() {
    val model: CoursePageViewModel = viewModel()
    val state by model.state.collectAsStateWithLifecycle()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day 20 · 数据与 UI 状态", style = MaterialTheme.typography.headlineSmall)
        Text("完成情况属于课程；搜索词、筛选开关和选中 id 属于页面。")
        OutlinedTextField(state.query, model::search, label = { Text("搜索课程") }, singleLine = true)
        Row {
            Checkbox(state.onlyIncomplete, model::filterIncomplete)
            Text("只看未完成")
        }
        Text("已完成 ${state.courses.count { it.completed }} / ${state.courses.size}")
        Text("当前选择：${state.selectedCourse?.title ?: "未选择"}")
        val visible = state.visibleCourses
        if (visible.isEmpty()) Text("没有匹配课程，清除筛选可查看全部。")
        visible.forEach { course ->
            Card(onClick = { model.select(course.id) }) {
                Column(Modifier.padding(12.dp)) {
                    Text("${if (state.selectedId == course.id) "✓ " else ""}${course.title}")
                    Text(if (course.completed) "已完成" else "未完成")
                    Button(onClick = { model.toggleCompleted(course.id) }) {
                        Text(if (course.completed) "标记未完成" else "标记完成")
                    }
                }
            }
        }
        Button(onClick = model::clearFilters) { Text("清除筛选（保留课程完成情况）") }
        Text("数据仅保存在当前 Activity 的 ViewModel 中，尚未写入数据库。")
    }
}
