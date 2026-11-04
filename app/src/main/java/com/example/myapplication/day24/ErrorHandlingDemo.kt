package com.example.myapplication.day24

import android.util.Log
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
import com.google.gson.JsonSyntaxException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response

internal enum class ErrorScenario(val label: String) {
    DNS("域名解析失败"), TIMEOUT("超时"), SERVER("HTTP 503"),
    NOT_FOUND("HTTP 404"), PARSE("解析失败"), EMPTY("空数据")
}

internal sealed interface ErrorDemoState {
    data object Idle : ErrorDemoState
    data object Loading : ErrorDemoState
    data class Success(val titles: List<String>) : ErrorDemoState
    data class Failure(val problem: RequestProblem) : ErrorDemoState
}

internal class ErrorHandlingViewModel : ViewModel() {
    private val _state = MutableStateFlow<ErrorDemoState>(ErrorDemoState.Idle)
    val state = _state.asStateFlow()
    private var scenario = ErrorScenario.DNS
    private var attempts = 0
    internal fun start(selected: ErrorScenario) {
        if (_state.value == ErrorDemoState.Loading) return
        scenario = selected
        attempts = 0
        request()
    }
    internal fun retry() {
        val failure = _state.value as? ErrorDemoState.Failure ?: return
        if (failure.problem.retryable) request()
    }
    private fun request() {
        _state.value = ErrorDemoState.Loading
        val attempt = ++attempts
        viewModelScope.launch {
            try {
                delay(500)
                // 确定性本地实验：首次失败，可重试场景第二次成功，不发网络请求。
                if (attempt == 1) when (scenario) {
                    ErrorScenario.DNS -> throw UnknownHostException()
                    ErrorScenario.TIMEOUT -> throw SocketTimeoutException()
                    ErrorScenario.SERVER -> throw HttpException(Response.error<Unit>(503, ResponseBody.create(null, "")))
                    ErrorScenario.NOT_FOUND -> throw HttpException(Response.error<Unit>(404, ResponseBody.create(null, "")))
                    ErrorScenario.PARSE -> throw JsonSyntaxException("模拟格式错误")
                    ErrorScenario.EMPTY -> Unit
                }
                _state.value = ErrorDemoState.Success(
                    if (scenario == ErrorScenario.EMPTY) emptyList() else listOf("TextView", "Button")
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.d("Day24", "scenario=$scenario attempt=$attempt type=${error.javaClass.simpleName}")
                _state.value = ErrorDemoState.Failure(classifyRequestError(error))
            }
        }
    }

}

@Composable
private fun ErrorContent(model: ErrorHandlingViewModel) {
        val current by model.state.collectAsStateWithLifecycle()
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Day 24 · 网络错误处理")
            Text("本页是本地错误模拟，不访问网络。选择场景后观察错误分类；允许重试的场景第二次成功。")
            when (val value = current) {
                ErrorDemoState.Idle -> Text("请选择场景")
                ErrorDemoState.Loading -> Text("加载中…")
                is ErrorDemoState.Success -> Text(if (value.titles.isEmpty()) "请求成功，但没有数据" else value.titles.joinToString())
                is ErrorDemoState.Failure -> {
                    Text(value.problem.message)
                    if (value.problem.retryable) Button(onClick = model::retry) { Text("重试") }
                }
            }
            ErrorScenario.entries.forEach { option ->
                Button(onClick = { model.start(option) }, enabled = current != ErrorDemoState.Loading) { Text(option.label) }
            }
        }
    }

@Composable
internal fun ErrorHandlingDemo() {
    val model: ErrorHandlingViewModel = viewModel()
    ErrorContent(model)
}
