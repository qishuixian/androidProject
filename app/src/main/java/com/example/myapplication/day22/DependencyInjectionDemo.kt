package com.example.myapplication.day22

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.unit.dp
import com.example.myapplication.day21.CourseRepository
import com.example.myapplication.day21.LocalCourseRepository
import com.example.myapplication.day21.RepositoryViewModel

/** Day22 先用显式容器演示依赖注入，容器边界与 Koin module 对应。 */
private object CourseDependencies {
    val repository: CourseRepository by lazy { LocalCourseRepository() }
}

@Composable
internal fun DependencyInjectionDemo() {
    val model: RepositoryViewModel = viewModel(
        factory = RepositoryViewModelFactory(CourseDependencies.repository)
    )
    val state by model.state.collectAsStateWithLifecycle()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day 22 · 依赖注入", style = MaterialTheme.typography.headlineSmall)
        Text("页面不创建 Repository，Factory 从依赖容器提供给 ViewModel。")
        Text(state.describe())
        Button(onClick = model::load) { Text("加载课程") }
        Text("将来接入 Koin 时，CourseDependencies 可替换为 module { single { LocalCourseRepository() } }。")
    }
}

private fun com.example.myapplication.day21.RepositoryUiState.describe(): String = when (this) {
    com.example.myapplication.day21.RepositoryUiState.Idle -> "等待加载"
    com.example.myapplication.day21.RepositoryUiState.Loading -> "加载中…"
    is com.example.myapplication.day21.RepositoryUiState.Success -> "课程：${courses.joinToString { it.title }}"
    is com.example.myapplication.day21.RepositoryUiState.Error -> "错误：$message"
}

private class RepositoryViewModelFactory(
    private val repository: CourseRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return RepositoryViewModel(repository) as T
    }
}

