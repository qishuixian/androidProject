package com.example.myapplication.day7

internal sealed interface AsyncLessonUiState {
    data object Idle : AsyncLessonUiState
    data object Loading : AsyncLessonUiState
    data class Success(val names: List<String>) : AsyncLessonUiState
    data class Error(val message: String) : AsyncLessonUiState
}
