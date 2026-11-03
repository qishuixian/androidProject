package com.example.myapplication.day9

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun LazyColumnPerformanceDemo() {
    var courses by remember {
        mutableStateOf(
            listOf(
                Course("text-view", "TextView", "展示文本内容"),
                Course("button", "Button", "响应用户点击"),
                Course("linear-layout", "LinearLayout", "按方向排列子 View")
            )
        )
    }
    var selectedId by remember { mutableStateOf<String?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day 9 · 列表数据建模", style = MaterialTheme.typography.headlineSmall)
        Text("课程由 Course 对象表示，父组件持有选中状态，子项通过回调上报点击。")
        courses.forEach { course ->
            CourseItem(course, selectedId == course.id) { selectedId = course.id }
        }
        selectedId?.let { id -> courses.firstOrNull { it.id == id }?.let { Text("当前课程：${it.title}") } }
    }
}

@Composable
private fun CourseItem(course: Course, selected: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(if (selected) "✓ ${course.title}" else course.title)
            Text(course.description, style = MaterialTheme.typography.bodySmall)
            Text("id=${course.id}", style = MaterialTheme.typography.labelSmall)
        }
    }
}
