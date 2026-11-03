package com.example.myapplication.day9

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

@Composable
internal fun LazyColumnPerformanceDemo() {
    var courses by remember { mutableStateOf(listOf("TextView", "Button", "LinearLayout")) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day 9 · 列表性能", style = MaterialTheme.typography.headlineSmall)
        Text("每项使用稳定 id 作为 key；重新排序时，Compose 可以保留对应项的状态。")
        Button(onClick = { courses = courses.reversed() }) { Text("反转列表") }
        courses.forEach { course ->
            CourseItem(key = course, name = course)
        }
    }
}

@Composable
private fun CourseItem(key: String, name: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text("key=$key  ·  $name", modifier = Modifier.padding(16.dp))
    }
}
