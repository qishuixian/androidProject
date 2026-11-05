package com.example.myapplication.day26

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.myapplication.day23.TodoApi
import com.example.myapplication.day24.classifyRequestError
import com.example.myapplication.day25.CourseDatabase
import com.example.myapplication.day25.CourseEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal data class OfflineState(
    val courses: List<CourseEntity> = emptyList(),
    val reading: Boolean = true,
    val refreshing: Boolean = false,
    val readError: String? = null,
    val refreshMessage: String? = null
)

internal class OfflineViewModel(private val repository: OfflineRepository) : ViewModel() {
    private val _state = MutableStateFlow(OfflineState())
    val state = _state.asStateFlow()
    init {
        viewModelScope.launch {
            try {
                repository.courses.collect { rows ->
                    _state.update { it.copy(courses = rows, reading = false) }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.update { it.copy(reading = false, readError = "缓存读取失败：${error.javaClass.simpleName}") }
            }
        }
        refresh()
    }
    fun refresh() {
        if (_state.value.refreshing) return
        _state.update { it.copy(refreshing = true, refreshMessage = null) }
        viewModelScope.launch {
            try {
                repository.refresh()
                _state.update { it.copy(refreshMessage = "网络数据已写入缓存") }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.update { it.copy(refreshMessage = "刷新失败，保留旧缓存：${classifyRequestError(error).message}") }
            } finally {
                _state.update { it.copy(refreshing = false) }
            }
        }
    }
}

private object OfflineDependencies {
    private var repository: OfflineRepository? = null
    @Synchronized
    fun get(context: Context): OfflineRepository = repository ?: run {
        // 独立文件，避免覆盖 Day25 用户标记的完成情况。
        val database = Room.databaseBuilder(context.applicationContext, CourseDatabase::class.java, "day26-cache.db").build()
        val api = Retrofit.Builder().baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create()).build().create(TodoApi::class.java)
        OfflineRepository(database, api).also { repository = it }
    }
}

@Composable
internal fun OfflineDemo() {
    val context = LocalContext.current.applicationContext
    val factory = remember(context) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass == OfflineViewModel::class.java)
                return OfflineViewModel(OfflineDependencies.get(context)) as T
            }
        }
    }
    val model: OfflineViewModel = viewModel(factory = factory)
    val state by model.state.collectAsStateWithLifecycle()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day 26 · 离线优先与缓存")
        Text("页面显示本地数据库内容；首次进入会请求公共待办测试接口并更新缓存。")
        if (state.reading) Text("读取缓存中…")
        state.readError?.let { Text(it) }
        if (!state.reading && state.readError == null && state.courses.isEmpty()) Text("本地缓存为空，联网刷新后可离线查看。")
        state.courses.forEach { Text("${it.id} · ${it.title}") }
        if (state.refreshing) Text("正在刷新，已有缓存继续显示…")
        state.refreshMessage?.let { Text(it) }
        Button(onClick = model::refresh, enabled = !state.refreshing) { Text("刷新缓存") }
    }
}
