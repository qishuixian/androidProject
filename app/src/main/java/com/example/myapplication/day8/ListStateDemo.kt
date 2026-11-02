package com.example.myapplication.day8

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

private val courses = listOf("TextView", "Button", "LinearLayout")

@Composable
internal fun ListStateDemo() {
    var selected by remember { mutableStateOf<String?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Day 8 · 列表渲染", style = MaterialTheme.typography.headlineSmall)
        Text("列表负责展示，选中项由父组件持有，再传给子项。")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            courses.forEach { course ->
                CourseRow(course, selected == course) { selected = course }
            }
        }
        selected?.let { Text("当前课程：$it", style = MaterialTheme.typography.titleMedium) }
    }
}

@Composable
private fun CourseRow(name: String, selected: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (selected) "✓ $name" else name,
            modifier = Modifier.padding(16.dp),
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
