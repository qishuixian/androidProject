package com.example.myapplication.day28

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class PagingState(
    val items: List<String> = emptyList(),
    val page: Int = 0,
    val loading: Boolean = false,
    val hasNext: Boolean = true
)

internal class PagingDemoViewModel : ViewModel() {
    private val _state = MutableStateFlow(PagingState())
    val state = _state.asStateFlow()

    fun loadNextPage() {
        val current = _state.value
        if (current.loading || !current.hasNext) return

        _state.update { it.copy(loading = true) }
        viewModelScope.launch {
            delay(400)
            val nextPage = current.page + 1
            val newItems = (1..5).map { index ->
                "课程 ${(nextPage - 1) * 5 + index}"
            }
            _state.update {
                it.copy(
                    items = it.items + newItems,
                    page = nextPage,
                    loading = false,
                    hasNext = nextPage < 4
                )
            }
        }
    }
}

@Composable
internal fun PagingDemo() {
    val model: PagingDemoViewModel = viewModel()
    val state by model.state.collectAsStateWithLifecycle()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Day 28 · 分页列表")
        Text("每次只加载 5 条并追加到现有列表，最多演示 4 页。")

        state.items.forEach { item ->
            Text(item)
        }

        Button(
            onClick = model::loadNextPage,
            enabled = !state.loading && state.hasNext
        ) {
            Text(
                when {
                    state.loading -> "加载中…"
                    state.hasNext -> "加载下一页"
                    else -> "没有更多了"
                }
            )
        }

        Text("当前 ${state.items.size} 条，第 ${state.page} 页")
    }
}
