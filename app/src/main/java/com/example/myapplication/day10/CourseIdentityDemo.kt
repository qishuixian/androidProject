package com.example.myapplication.day10

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
internal fun CourseIdentityDemo() {
    var courses by remember {
        mutableStateOf(
            listOf(
                Course("text-view", "TextView", "展示文本内容"),
                Course("button", "Button", "响应用户点击"),
                Course("linear-layout", "LinearLayout", "按方向排列子 View")
            )
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day 10 · 稳定身份", style = MaterialTheme.typography.headlineSmall)
        Text("排序和删除会改变位置，但 Course.id 始终表示同一个课程。")
        Button(onClick = { courses = courses.reversed() }) { Text("反转顺序") }
        courses.forEach { course ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(course.title, style = MaterialTheme.typography.titleMedium)
                    Text("稳定 id：${course.id}")
                    Button(onClick = { courses = courses.filterNot { it.id == course.id } }) {
                        Text("删除")
                    }
                }
            }
        }
    }
}
