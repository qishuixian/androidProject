package com.example.myapplication.day11

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import com.example.myapplication.day9.Course

@Composable
internal fun CourseEventsDemo() {
    var courses by remember {
        mutableStateOf(
            listOf(
                Course("text-view", "TextView", "展示文本内容"),
                Course("button", "Button", "响应用户点击"),
                Course("linear-layout", "LinearLayout", "按方向排列子 View")
            )
        )
    }
    var message by remember { mutableStateOf("等待操作") }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day 11 · 事件与回调", style = MaterialTheme.typography.headlineSmall)
        Text("子项只上报事件，父组件统一修改列表和提示文本。")
        Text(message, color = MaterialTheme.colorScheme.primary)
        courses.forEach { course ->
            CourseRow(
                course = course,
                onSelect = { message = "已选择：${it.title}" },
                onDelete = { courseId ->
                    courses = courses.filterNot { it.id == courseId }
                    message = "已删除：${course.title}"
                }
            )
        }
    }
}

@Composable
private fun CourseRow(course: Course, onSelect: (Course) -> Unit, onDelete: (String) -> Unit) {
    Card(onClick = { onSelect(course) }, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(course.title, style = MaterialTheme.typography.titleMedium)
            Text(course.description)
            Button(onClick = { onDelete(course.id) }) { Text("删除") }
        }
    }
}
