package com.example.myapplication.day21

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.day9.Course
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal sealed interface RepositoryUiState {
    data object Idle : RepositoryUiState
    data object Loading : RepositoryUiState
    data class Success(val courses: List<Course>) : RepositoryUiState
    data class Error(val message: String) : RepositoryUiState
}

internal class RepositoryViewModel(
    private val repository: CourseRepository = LocalCourseRepository()
) : ViewModel() {
    private val _state = MutableStateFlow<RepositoryUiState>(RepositoryUiState.Idle)
    val state = _state.asStateFlow()

    fun load() {
        if (_state.value is RepositoryUiState.Loading) return
        viewModelScope.launch {
            _state.value = RepositoryUiState.Loading
            runCatching { repository.loadCourses() }
                .onSuccess { _state.value = RepositoryUiState.Success(it) }
                .onFailure { _state.value = RepositoryUiState.Error(it.message ?: "加载失败") }
        }
    }
}
