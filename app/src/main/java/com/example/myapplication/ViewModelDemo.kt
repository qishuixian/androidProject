package com.example.myapplication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal data class LessonDemoUiState(
    val count: Int = 0,
    val selectedLesson: String = "尚未选择",
    val availableLessons: List<String> = emptyList()
)

internal class LessonDemoViewModel(
    private val repository: LessonRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        LessonDemoUiState(
            availableLessons = repository.getLessons().map { it.name }
        )
    )
    val uiState: StateFlow<LessonDemoUiState> = _uiState.asStateFlow()

    fun selectLesson(name: String) {
        _uiState.update { state ->
            state.copy(count = state.count + 1, selectedLesson = name)
        }
    }

    fun reset() {
        _uiState.value = LessonDemoUiState(
            availableLessons = repository.getLessons().map { it.name }
        )
    }
}

private class LessonDemoViewModelFactory(
    private val repository: LessonRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LessonDemoViewModel(repository) as T
    }
}

@Composable
internal fun ViewModelDemo(
    viewModel: LessonDemoViewModel = viewModel(
        factory = LessonDemoViewModelFactory(LessonRepository())
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("ViewModel Demo")
        Text("选中的课程：${state.selectedLesson}")
        Text("点击次数：${state.count}")
        Text("Repository 提供：${state.availableLessons.joinToString("、")}")
        Button(
            onClick = { viewModel.selectLesson("TextView") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("选择 TextView") }
        Button(
            onClick = { viewModel.selectLesson("LinearLayout") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("选择 LinearLayout") }
        Button(
            onClick = viewModel::reset,
            modifier = Modifier.fillMaxWidth()
        ) { Text("重置状态") }
        Text("点击按钮 → ViewModel 调用 Repository → 更新 StateFlow → Compose 重组页面")
    }
}
