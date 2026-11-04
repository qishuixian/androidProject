package com.example.myapplication.day23

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.day21.CourseRepository
import com.example.myapplication.day21.RepositoryUiState
import com.example.myapplication.day9.Course
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// 公共测试接口返回的是待办事项，此处映射成课程用于教学。
internal data class TodoDto(val id: Int, val title: String?, val completed: Boolean)

internal interface TodoApi {
    @GET("todos")
    suspend fun load(@Query("_limit") limit: Int): List<TodoDto>
}

internal class NetworkCourseRepository(private val api: TodoApi) : CourseRepository {
    override suspend fun loadCourses(): List<Course> = api.load(3).map {
        Course(it.id.toString(), it.title ?: "无标题", "来自公共待办测试接口", it.completed)
    }
}

private object NetworkDependencies {
    val repository: CourseRepository by lazy {
        val api = Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TodoApi::class.java)
        NetworkCourseRepository(api)
    }
    val factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == NetworkViewModel::class.java)
            return NetworkViewModel(repository) as T
        }
    }
}

internal class NetworkViewModel(private val repository: CourseRepository) : ViewModel() {
    private val _state = MutableStateFlow<RepositoryUiState>(RepositoryUiState.Idle)
    val state = _state.asStateFlow()

    fun load() {
        if (_state.value == RepositoryUiState.Loading) return
        _state.value = RepositoryUiState.Loading
        viewModelScope.launch {
            try {
                _state.value = RepositoryUiState.Success(repository.loadCourses())
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.value = RepositoryUiState.Error(error.message ?: "请求失败，请重试")
            }
        }
    }
}

@Composable
internal fun NetworkDemo() {
    val model: NetworkViewModel = viewModel(factory = NetworkDependencies.factory)
    val state by model.state.collectAsStateWithLifecycle()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day 23 · Retrofit 网络请求")
        Text("点击后访问 JSONPlaceholder 公共测试接口，将三条待办事项映射成课程。需要联网。")
        when (val current = state) {
            RepositoryUiState.Idle -> Text("等待请求")
            RepositoryUiState.Loading -> Text("请求中…")
            is RepositoryUiState.Success -> {
                if (current.courses.isEmpty()) Text("暂无数据")
                current.courses.forEach { Text("${it.id} · ${it.title}") }
            }
            is RepositoryUiState.Error -> Text("请求失败：${current.message}")
        }
        Button(onClick = model::load, enabled = state != RepositoryUiState.Loading) {
            Text(if (state is RepositoryUiState.Error) "重试" else "请求数据")
        }
    }
}
