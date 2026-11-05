package com.example.myapplication.day27

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal data class ConcurrentState(
    val loading: Boolean = false,
    val message: String = "等待加载"
)

internal class ConcurrentViewModel : ViewModel() {
    private val _state = MutableStateFlow(ConcurrentState())
    val state = _state.asStateFlow()

    fun loadInParallel() {
        if (_state.value.loading) return
        _state.value = ConcurrentState(loading = true, message = "并行加载课程和详情…")
        viewModelScope.launch {
            try {
                val (courses, details) = kotlinx.coroutines.coroutineScope {
                    val courses = async { loadCourses() }
                    val details = async { loadDetails() }
                    courses.await() to details.await()
                }
                _state.value = ConcurrentState(message = "完成：$courses；$details")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.value = ConcurrentState(message = "并行任务失败：${error.message}")
            }
        }
    }

    private suspend fun loadCourses(): String {
        delay(900)
        return "课程列表"
    }

    private suspend fun loadDetails(): String {
        delay(600)
        return "课程详情"
    }
}

@Composable
internal fun ConcurrentLoadingDemo() {
    val model: ConcurrentViewModel = viewModel()
    val state by model.state.collectAsStateWithLifecycle()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day 27 · 协程并发")
        Text(state.message)
        Button(onClick = model::loadInParallel, enabled = !state.loading) {
            Text(if (state.loading) "加载中…" else "并行加载")
        }
        Text("两个任务分别等待 900ms 和 600ms，并行总耗时接近 900ms，而不是 1500ms。")
    }
}
