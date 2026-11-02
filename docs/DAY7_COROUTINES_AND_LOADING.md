# Day 7：协程、异步加载与重试

目标：理解 suspend、viewModelScope 与 StateFlow 如何配合，实现加载中、成功、空数据、失败和重试。

本文是学习教程，代码尚未接入 App。当前 [LessonRepository](../app/src/main/java/com/example/myapplication/LessonRepository.kt) 同步返回本地列表，[ViewModelDemo](../app/src/main/java/com/example/myapplication/ViewModelDemo.kt) 展示选中课程和计数。下面使用独立命名，便于后续分文件实现。

## 1. 从前端理解异步

JavaScript 中常见 async/await；Kotlin 使用协程和挂起函数组织异步操作。两者都可以按顺序书写等待过程，但执行模型不同：Kotlin 的 suspend 函数不是自动返回 Promise，也不会因为标记 suspend 就自动启动协程或切到后台线程。

```kotlin
suspend fun loadNames(): List<String> {
    delay(1000)
    return listOf("TextView", "Button")
}
```

- suspend：函数允许挂起，只能从协程或其他挂起函数调用。
- delay：暂停当前协程，等待期间不阻塞线程。
- Thread.sleep：阻塞当前线程，不能用来模拟主线程上的异步等待。
- launch：在一个作用域中启动协程，返回 Job，可用于取消任务。

## 2. 主线程与 Dispatcher

Android 主线程负责 UI 与输入响应。阻塞文件读取、重计算不能直接长时间占用它。

- Dispatchers.Main：UI 相关工作；viewModelScope 默认在主线程调度。
- Dispatchers.IO：阻塞式文件、数据库等 I/O。
- Dispatchers.Default：CPU 密集计算。

如果 Repository 调用的是阻塞式文件 API，可在 Repository 内使用 withContext(Dispatchers.IO)。支持挂起且主线程安全的库 API 不一定需要额外包一层 IO；应按库的约定使用。

**suspend 不等于后台线程；协程也不等于新建线程。**

## 3. 状态设计

建议文件：AsyncLessonUiState.kt。

```kotlin
sealed interface AsyncLessonUiState {
    data object Idle : AsyncLessonUiState
    data object Loading : AsyncLessonUiState
    data class Success(val names: List<String>) : AsyncLessonUiState
    data class Error(val message: String) : AsyncLessonUiState
}
```

sealed interface 限制状态种类，when 可以检查是否覆盖全部情况。这里一次只有一种状态，避免“正在加载”和“已经失败”同时为 true。空列表由 Success(emptyList()) 表达，再由 UI 展示空态。

```text
Idle → Loading → Success（有数据 / 空数据）
               → Error → 点击重试 → Loading
```

## 4. Repository：确定性的本地模拟

建议文件：AsyncLessonRepository.kt。无需接口服务，不用随机数，保证每次都能复现失败与成功。

```kotlin
import java.io.IOException
import kotlinx.coroutines.delay

interface AsyncLessonRepository {
    suspend fun loadNames(): List<String>
}

class DemoAsyncLessonRepository : AsyncLessonRepository {
    private var attempts = 0

    override suspend fun loadNames(): List<String> {
        val attempt = ++attempts
        delay(1000)
        if (attempt == 1) {
            throw IOException("首次加载模拟失败，请重试")
        }
        return listOf("TextView", "Button", "LinearLayout")
    }
}
```

此练习约定在同一 ViewModel 中串行调用：第一次失败，第二次起成功。必须复用同一个 Repository，不能每次重试重新创建，否则会一直首次失败。

## 5. ViewModel：启动任务并转换状态

建议文件：AsyncLessonViewModel.kt。

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AsyncLessonViewModel(
    private val repository: AsyncLessonRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<AsyncLessonUiState>(
        AsyncLessonUiState.Idle
    )
    val uiState = _uiState.asStateFlow()

    fun load() {
        // 加载时忽略重复点击，避免同时发起多个请求。
        if (_uiState.value == AsyncLessonUiState.Loading) return
        _uiState.value = AsyncLessonUiState.Loading

        viewModelScope.launch {
            try {
                val names = repository.loadNames()
                _uiState.value = AsyncLessonUiState.Success(names)
            } catch (cancelled: CancellationException) {
                // 取消是协程的正常控制流程，继续向上传递。
                throw cancelled
            } catch (error: IOException) {
                _uiState.value = AsyncLessonUiState.Error(
                    error.message ?: "加载失败，请重试"
                )
            }
        }
    }
}
```

逐步读：点击加载 → 先设置 Loading → launch 启动协程 → Repository 挂起等待 → 返回数据或抛出异常 → ViewModel 更新 StateFlow。

此例只捕获预期的 IOException；实际项目按数据源定义处理错误。不要随意吞掉所有异常，尤其不要把协程取消显示为网络错误。

viewModelScope 在 ViewModel 被清除时取消。旋转屏幕通常不会清除 ViewModel；进程终止后，ViewModel 中的内存状态不会自动保留。页面返回是否清除 ViewModel，取决于它属于 Activity 还是导航条目等范围。

## 6. Compose：按状态显示内容

建议文件：AsyncLessonDemo.kt。

```kotlin
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AsyncLessonDemo(viewModel: AsyncLessonViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column {
        when (val current = state) {
            AsyncLessonUiState.Idle -> {
                Button(onClick = viewModel::load) { Text("加载课程") }
            }
            AsyncLessonUiState.Loading -> {
                CircularProgressIndicator()
                Text("正在加载，请稍候…")
            }
            is AsyncLessonUiState.Success -> {
                if (current.names.isEmpty()) {
                    Text("暂无课程")
                } else {
                    current.names.forEach { Text(it) }
                }
                Button(onClick = viewModel::load) { Text("重新加载") }
            }
            is AsyncLessonUiState.Error -> {
                Text(current.message)
                Button(onClick = viewModel::load) { Text("重试") }
            }
        }
    }
}
```

collectAsStateWithLifecycle 在合适的生命周期状态收集 Flow，状态变化驱动 UI 重组。停止收集并不等于自动取消 ViewModel 中已启动的任务。

此例由点击开始加载。不要在 Composable 函数体中直接调用 viewModel.load()，否则重组可能重复请求。如果需要进入页面自动加载，可用 LaunchedEffect(viewModel)，并设计返回重进时是否重新加载的规则。

## 7. 延续 Day 6：通过 Factory 注入

建议文件：AsyncLessonViewModelFactory.kt。

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AsyncLessonViewModelFactory(
    private val repository: AsyncLessonRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == AsyncLessonViewModel::class.java)
        return AsyncLessonViewModel(repository) as T
    }
}
```

入口 Composable：

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AsyncLessonRoute() {
    val factory = remember {
        AsyncLessonViewModelFactory(DemoAsyncLessonRepository())
    }
    val vm: AsyncLessonViewModel = viewModel(factory = factory)
    AsyncLessonDemo(viewModel = vm)
}
```

Factory 把 Repository 传入 ViewModel，viewModel() 负责从当前 ViewModelStoreOwner 获取或创建实例。当前单 Activity 项目中默认通常为 Activity 范围，因此返回首页再进入时可复用原实例。

这些代码按文件组织后应使用相同的 package；需要 Lifecycle ViewModel、Lifecycle Compose、协程等库。本教程未新增依赖、菜单或源文件，也未对代码片段执行编译；实际接入时应检查依赖与 imports 并构建验证。

## 8. Java 与 Kotlin 混合时怎么分工

Java 可以继续承担数据模型或传统工具类；协程、Flow 和 Compose 层优先用 Kotlin。Java 不能像调用普通同步方法那样直接使用 Kotlin suspend 函数的返回值；需要专门的回调或其他适配接口。今天先学习 Kotlin 协程调用链，不把 suspend 当作 Java 普通方法调用。

## 9. 动手练习

1. 按上述文件分工接入独立入口，保留 Day 6 示例。
2. 点击“加载课程”，确认先显示进度，再出现模拟失败提示。
3. 点击“重试”，确认进度后显示三条课程。
4. 把成功返回值改为 emptyList()，检查“暂无课程”。
5. 加载中旋转屏幕，观察是否继续同一次任务。
6. 返回首页后重进，观察状态是否保留，并解释 Activity 范围的影响。
7. 为 ViewModel 注入另一个 Repository 实现，让它每次都成功，理解替换依赖无需修改 UI。

教程中的预期效果是练习验收标准，不代表 App 已实现或设备验证通过。

## 10. 完成标准

- 能解释 suspend 不会自动切换线程。
- 能区分 delay 与 Thread.sleep。
- 能说明 viewModelScope 的启动和取消时机。
- 能画出 Idle、Loading、Success、Error 的转换关系。
- 能解释重试为什么复用同一个 load() 方法和 Repository。
- 能说明 StateFlow 与 collectAsStateWithLifecycle 的配合方式。
- 能解释为什么必须继续传递 CancellationException。

下一课：Day 8，列表渲染与状态提升，将异步课程数据展示为可点击列表。

## 已接入项目的 Day 7 Demo

运行 App → 首页 → **Day 7 · 异步加载**。

点击“加载课程”后显示进度，1 秒后模拟失败；点击“重试”再等待 1 秒，即显示 TextView、Button、LinearLayout。重复加载继续成功，不访问网络。顶部返回和系统返回均回到首页。

源码按职责放在 `app/src/main/java/com/example/myapplication/day7/`：

- `AsyncLessonUiState.kt`：四种互斥状态。
- `AsyncLessonRepository.kt`：数据源接口与本地模拟实现。
- `AsyncLessonViewModel.kt`：启动协程、处理失败、忽略加载中的重复请求。
- `AsyncLessonViewModelFactory.kt`：通过构造方法注入 Repository。
- `AsyncLessonDemo.kt`：观察状态并展示按钮、进度和课程。

上文教学片段现已有对应实现；无需再按原文件名重复创建。当前 ViewModel 为 Activity 范围，旋转或回首页再进入会保留状态；结束应用进程并重新启动可重新体验首次失败。
