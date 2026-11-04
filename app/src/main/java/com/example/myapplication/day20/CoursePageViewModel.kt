package com.example.myapplication.day20

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class CoursePageViewModel : ViewModel() {
    private val _state = MutableStateFlow(CoursePageState())
    val state = _state.asStateFlow()

    fun search(query: String) { _state.update { it.copy(query = query) } }
    fun select(id: String) { _state.update { it.copy(selectedId = id) } }
    fun filterIncomplete(enabled: Boolean) { _state.update { it.copy(onlyIncomplete = enabled) } }
    fun clearFilters() { _state.update { it.copy(query = "", onlyIncomplete = false) } }

    fun toggleCompleted(id: String) {
        _state.update { current ->
            current.copy(courses = current.courses.map { course ->
                if (course.id == id) course.copy(completed = !course.completed) else course
            })
        }
    }
}
