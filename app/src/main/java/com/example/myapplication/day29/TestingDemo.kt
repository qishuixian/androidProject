package com.example.myapplication.day29

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
internal fun TestingDemo() {
    val model: PagingDemoViewModel = viewModel()
    val state by model.state.collectAsStateWithLifecycle()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day 29 · 测试与调试")
        Text("本页对应 PagingDemoViewModelTest.kt，验证分页状态转换。")
        Text("已加载：${state.items.size} 条；第 ${state.page} 页；还有更多：${state.hasNext}")
        Button(onClick = model::loadNextPage, enabled = !state.loading && state.hasNext) {
            Text("执行一次加载")
        }
        Text("命令行：.\\gradlew.bat :app:testDebugUnitTest --no-configuration-cache")
    }
}
