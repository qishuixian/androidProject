package com.example.myapplication.day18

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.dp
import com.example.myapplication.day9.Course

private val courses = listOf(
    Course("text-view", "TextView", "展示文本内容"),
    Course("button", "Button", "响应用户点击"),
    Course("linear-layout", "LinearLayout", "按方向排列子 View")
)

@Composable
internal fun ConfigurationStateDemo() {
    // 只保存恢复界面必需的小型状态，不保存整份列表。
    var query by rememberSaveable { mutableStateOf("") }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var savedCount by rememberSaveable { mutableIntStateOf(0) }
    var temporaryCount by remember { mutableIntStateOf(0) }
    val selected = courses.firstOrNull { it.id == selectedId }
    val visible = courses.filter { it.title.contains(query, ignoreCase = true) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day 18 · 配置变化与状态恢复", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(query, { query = it }, label = { Text("筛选课程") }, singleLine = true)
        visible.forEach { course ->
            Button(onClick = {
                selectedId = course.id
                savedCount++
                temporaryCount++
            }) { Text(course.title) }
        }
        if (visible.isEmpty()) Text("没有匹配课程")
        Text("当前选择：${selected?.title ?: "未选择"}")
        Text("可恢复次数：$savedCount；普通 remember 次数：$temporaryCount")
        Text("筛选只改变显示列表，不清除选择。旋转重建后，关键词、选择与可恢复次数应保留，普通次数重置。")
        Text("Logcat 搜索 Day17，对照 MainActivity 实例是否变化。返回首页会移除此组件，不等同于配置重建。")
        Button(onClick = {
            query = ""
            selectedId = null
            savedCount = 0
            temporaryCount = 0
        }) { Text("重置实验") }
    }
}
