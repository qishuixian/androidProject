package com.example.myapplication.day16

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface CourseScreenState {
    data object Idle : CourseScreenState
    data object Loading : CourseScreenState
    data class Success(val count: Int) : CourseScreenState
    data class Error(val message: String) : CourseScreenState
}

internal class CourseStateViewModel : ViewModel() {
    private val _state = MutableStateFlow<CourseScreenState>(CourseScreenState.Idle)
    val state: StateFlow<CourseScreenState> = _state.asStateFlow()

    fun loadCourses() {
        _state.value = CourseScreenState.Loading
        _state.value = CourseScreenState.Success(3)
    }

    fun fail() {
        _state.value = CourseScreenState.Error("课程加载失败")
    }
}
