package com.example.myapplication.day13

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
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
internal fun CollectionTransformDemo() {
    val courses = listOf(
        Course("text-view", "TextView", "展示文本内容"),
        Course("button", "Button", "响应用户点击"),
        Course("linear-layout", "LinearLayout", "按方向排列子 View")
    )
    var query by remember { mutableStateOf("") }
    val visibleCourses = courses.filter { it.title.contains(query, ignoreCase = true) }
    val titles = visibleCourses.map { it.title }
    Log.d("Day13", "titles=$titles")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day 13 · 集合转换", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
        Text("filter 筛选对象，map 提取标题；复杂逻辑仍放在父组件。")
        OutlinedTextField(query, { query = it }, label = { Text("搜索课程") }, modifier = Modifier.fillMaxWidth())
        Text(if (titles.isEmpty()) "没有匹配课程" else titles.joinToString("、"))
    }
}
