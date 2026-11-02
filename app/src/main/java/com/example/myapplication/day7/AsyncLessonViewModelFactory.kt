package com.example.myapplication.day7

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

internal class AsyncLessonViewModelFactory(
    private val repository: AsyncLessonRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == AsyncLessonViewModel::class.java)
        return AsyncLessonViewModel(repository) as T
    }
}
