package com.example.myapplication.day30

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.day28.PagingDemoViewModel

@Composable
internal fun FinalCourseDemo() {
    val model: PagingDemoViewModel = viewModel()
    val state by model.state.collectAsStateWithLifecycle()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Day 30 · 课程综合项目")
        Text("整合：ViewModel、StateFlow、协程分页、列表状态和测试。")
        Text("课程数量：${state.items.size}")
        state.items.forEach { title -> Text("• $title") }
        Button(onClick = model::loadNextPage, enabled = !state.loading && state.hasNext) {
            Text(if (state.loading) "加载中…" else if (state.hasNext) "加载更多" else "已加载全部")
        }
        Text("发布前检查：测试、编译、签名、真机安装和核心流程验证。")
    }
}
