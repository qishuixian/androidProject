# MutableStateFlow 详细学习

本文从 Kotlin 基础开始，解释 `MutableStateFlow`、`StateFlow`、ViewModel 和 Compose 之间的关系。示例沿用本项目的课程列表场景。

## 1. 先理解“状态”

状态就是某个时刻需要保存的数据。例如课程页面可能有：

```kotlin
data class CoursePageState(
    val courses: List<Course> = emptyList(),
    val query: String = "",
    val selectedId: String? = null,
    val loading: Boolean = false
)
```

页面显示什么，取决于这些值。普通变量修改后不会自动通知 Compose 或其他观察者；`MutableStateFlow` 解决的是“保存最新值并通知观察者”。

## 2. Flow 与 StateFlow

Flow 是可以在未来持续发出多个值的异步数据流：

```text
Idle → Loading → Success → Loading → Error
```

普通 Flow 通常需要有人 `collect` 才开始执行。`StateFlow` 是专门表示当前状态的 Flow，它始终有初始值，并保存最新值；新的收集者开始收集时会立即收到当前值。

| 特性 | 普通 Flow | StateFlow |
|---|---|---|
| 是否必须初始值 | 不一定 | 必须 |
| 是否保存最新值 | 不负责 | 负责 |
| 新收集者能否立即拿当前值 | 不保证 | 可以 |
| 适合用途 | 一次任务、转换流程 | 页面当前状态 |

## 3. MutableStateFlow

`MutableStateFlow` 是可以被修改的 `StateFlow`：

```kotlin
private val _state = MutableStateFlow(CoursePageState())
```

`Mutable` 表示它有写入能力：

```kotlin
_state.value = CoursePageState(query = "Button")
```

它同时可以读取当前值：

```kotlin
val current = _state.value
```

可以把它理解成：

```text
当前值 + 可观察通知 + 可修改能力
```

## 4. 为什么要写 `_state` 和 `state`

```kotlin
private val _state = MutableStateFlow(CoursePageState())
val state: StateFlow<CoursePageState> = _state.asStateFlow()
```

这是“内部可写，外部只读”模式：

```text
ViewModel 内部  _state：MutableStateFlow，可读可写
Compose UI 外部 state：StateFlow，只读可收集
```

ViewModel 可以修改：

```kotlin
_state.update { it.copy(query = "Button") }
```

UI 不能修改：

```kotlin
model.state.value = newState // 编译错误
```

`asStateFlow()` 不会复制数据，只是返回同一个底层状态的只读视图。前端可以把它类比为 store 对外暴露只读 state，只能通过 action 修改内部值。

## 5. value、update、copy 和 map

直接替换当前值：

```kotlin
_state.value = _state.value.copy(query = "Button")
```

根据旧值计算新值时更推荐 `update`：

```kotlin
_state.update { oldState ->
    oldState.copy(query = "Button")
}
```

需要导入：

```kotlin
import kotlinx.coroutines.flow.update
```

列表更新示例：

```kotlin
_state.update { old ->
    old.copy(
        courses = old.courses.map { course ->
            if (course.id == id) {
                course.copy(completed = !course.completed)
            } else {
                course
            }
        }
    )
}
```

`copy` 创建新对象，`map` 创建新列表，不原地修改旧状态。`update` 的 lambda 应保持纯计算，不要在里面发网络请求、显示 Toast 或修改其他变量。

## 6. ViewModel 完整示例

```kotlin
class CourseViewModel : ViewModel() {
    private val _state = MutableStateFlow(CoursePageState())
    val state: StateFlow<CoursePageState> = _state.asStateFlow()

    fun search(query: String) {
        _state.update { it.copy(query = query) }
    }

    fun select(id: String) {
        _state.update { it.copy(selectedId = id) }
    }

    fun clearSearch() {
        _state.update { it.copy(query = "") }
    }
}
```

ViewModel 保存状态和处理事件，不直接操作 Compose 的 Text 或 Button。

## 7. Compose 如何收集 StateFlow

```kotlin
@Composable
fun CourseScreen(model: CourseViewModel = viewModel()) {
    val state by model.state.collectAsStateWithLifecycle()

    Text("搜索：${state.query}")
    Button(onClick = { model.clearSearch() }) {
        Text("清除")
    }
}
```

`collectAsStateWithLifecycle()` 大致完成：

```text
StateFlow → collect → Compose State → 读取 state.query → 状态变化时重组
```

它会结合页面生命周期，在页面不活跃时停止收集。不要在 Composable 中手动 `new CourseViewModel()`，应使用 `viewModel()` 或依赖注入。

### `by` 是什么

不用 `by`：

```kotlin
val stateState = model.state.collectAsStateWithLifecycle()
Text(stateState.value.query)
```

使用 `by`：

```kotlin
val state by model.state.collectAsStateWithLifecycle()
Text(state.query)
```

`by` 是 Kotlin 委托语法，帮助隐藏 `.value`，不改变 StateFlow。

## 8. StateFlow 与 Compose State

```kotlin
MutableStateFlow<CoursePageState> // Kotlin 协程层，适合 ViewModel
State<CoursePageState>            // Compose 层，适合 Composable 读取
```

`collectAsStateWithLifecycle()` 把前者接入后者。StateFlow 不依赖 Compose，可以在 ViewModel 和测试中使用；Compose State 不能直接使用 `collect`。

## 9. 去重、线程与不可变状态

StateFlow 会根据相等性抑制重复值。`data class` 自动生成 `equals`，适合状态模型：

```kotlin
_state.value = CoursePageState(query = "Button")
_state.value = CoursePageState(query = "Button")
```

相等的第二个状态通常不会让观察者产生一次有意义的更新。不要在状态中放入每次都变化的随机值，也不要原地修改可变 List。

协程中可以更新：

```kotlin
viewModelScope.launch {
    _state.update { it.copy(loading = true) }
    val courses = repository.loadCourses()
    _state.update { it.copy(courses = courses, loading = false) }
}
```

阻塞式 I/O 使用 `withContext(Dispatchers.IO)`。StateFlow 的并发安全不等于状态内部的可变对象安全，因此优先使用不可变数据。

## 10. StateFlow、SharedFlow 和 Channel

| 工具 | 适合表达 | 例子 |
|---|---|---|
| StateFlow | 当前状态 | Loading、列表、选中 id |
| SharedFlow | 广播事件 | Toast、导航、一次性提示 |
| Channel | 有接收者的事件队列 | 特定事件传递场景 |

加载结果需要被新页面立即知道，适合 StateFlow；导航通常是一次性事件，不应因为旋转重新收集而重复执行，需要单独设计事件消费。

## 11. 常见错误

### 把 MutableStateFlow 暴露给 UI

```kotlin
// 不推荐
val state = MutableStateFlow(CoursePageState())
```

外部代码可以随意修改。应使用 `_state` 加 `asStateFlow()`。

### 把 Flow 当普通值

```kotlin
Text(viewModel.state.toString()) // 这是 Flow 对象，不是状态内容
```

应先收集：

```kotlin
val state by viewModel.state.collectAsStateWithLifecycle()
Text(state.query)
```

### 在 update 中执行副作用

```kotlin
// 不推荐
_state.update {
    repository.loadCourses()
    it
}
```

先执行请求，再把结果交给 `update` 更新状态。更新函数应是根据旧状态返回新状态的纯计算。

### 把每个字段都拆成独立 Flow

独立 Flow 不是绝对错误，但相关页面状态拆得太散会增加同步成本。互相影响的页面状态可以聚合到一个 `UiState` 中。

## 12. 与 Day7～Day20 的完整链路

```text
Day7：Repository 加载数据
  ↓
Day14：sealed interface 表示 Idle/Loading/Success/Error
  ↓
Day16：ViewModel 持有 MutableStateFlow
  ↓
Day19：状态读取触发 Compose 重组
  ↓
Day20：业务数据、页面状态和派生结果分离
  ↓
Compose：collectAsStateWithLifecycle 收集并显示
```

一次点击的完整路径：

```text
用户点击
  → UI 调用 ViewModel 方法
  → ViewModel 使用 copy/map 生成新状态
  → MutableStateFlow 发出最新值
  → UI 收集到新值
  → 读取该值的 Composable 重组
```

## 13. 学习实验

1. 把 `query` 改成独立的 `MutableStateFlow<String>`，再恢复成一个 `CoursePageState`，比较读取和更新复杂度。
2. 加入 `load()`：先发 Loading，再发 Success 或 Error。
3. 记录 `state.query`、`state.selectedId`，区分一次收集和一次重组。
4. 临时把 `state` 暴露成 `MutableStateFlow`，观察 UI 直接修改如何破坏单向数据流，再恢复只读暴露。
5. 为状态增加 `sortAscending`，使用 `copy` 更新，使用 getter 计算排序结果。
6. 旋转屏幕，对比 StateFlow、`remember`、`rememberSaveable` 的生命周期。

## 14. 完成标准

- 能解释 Flow、StateFlow、MutableStateFlow 的关系。
- 能写出 `_state` 可写、`state` 只读的 ViewModel。
- 能解释 `.value`、`update`、`copy` 和 `map`。
- 能在 Compose 中使用 `collectAsStateWithLifecycle()`。
- 能区分 StateFlow、Compose State、SharedFlow 和 Channel。
- 能说明为什么状态模型应优先使用不可变数据。
- 能追踪“事件 → ViewModel → StateFlow → 收集 → 重组”的完整链路。
- 能识别暴露可变流、在 Composable 中 new ViewModel、在 update 中执行副作用等错误。
