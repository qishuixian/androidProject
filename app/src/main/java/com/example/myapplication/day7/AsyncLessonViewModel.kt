package com.example.myapplication.day7

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class AsyncLessonViewModel(
    private val repository: AsyncLessonRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<AsyncLessonUiState>(AsyncLessonUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun load() {
        // 主线程入口先改变状态，拦截连续点击产生的重复请求。
        if (_uiState.value == AsyncLessonUiState.Loading) return
        _uiState.value = AsyncLessonUiState.Loading
        viewModelScope.launch {
            try {
                _uiState.value = AsyncLessonUiState.Success(repository.loadNames())
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: IOException) {
                _uiState.value = AsyncLessonUiState.Error(error.message ?: "加载失败，请重试")
            }
        }
    }
}
