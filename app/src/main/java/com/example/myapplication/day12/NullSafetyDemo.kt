package com.example.myapplication.day12

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
import com.example.myapplication.day9.Course

@Composable
internal fun NullSafetyDemo() {
    val courses = listOf(
        Course("text-view", "TextView", "展示文本内容"),
        Course("button", "Button", "响应用户点击"),
        Course("linear-layout", "LinearLayout", "按方向排列子 View")
    )
    var selectedId by remember { mutableStateOf<String?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day 12 · 空值安全", style = MaterialTheme.typography.headlineSmall)
        Text("未选择课程时 selectedId 为 null，界面仍能安全展示。")
        courses.forEach { course ->
            Card(onClick = { selectedId = course.id }, modifier = Modifier.fillMaxWidth()) {
                Text(course.title, modifier = Modifier.padding(16.dp))
            }
        }
        val selected = courses.firstOrNull { it.id == selectedId }
        Text("当前课程：${selected?.title ?: "尚未选择"}")
    }
}
