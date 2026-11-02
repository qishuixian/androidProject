# Day 5：页面状态、返回与 ViewModel

目标：理解“点击后为什么页面会变化”，区分 Compose 状态、Activity 导航和 ViewModel 的职责。

## 1. 页面状态是什么

页面状态就是决定当前 UI 显示内容的数据。例如当前项目用 `page` 表示页面：

```kotlin
var page by rememberSaveable { mutableStateOf("home") }
```

它可能有三个状态：

```text
home       首页
layouts    布局列表
TextView   TextView 详情
```

状态变化：

```kotlin
page = "layouts"
```

Compose 会重新执行相关 Composable，根据新值显示布局列表。

前端类比：类似 React 的 `useState` 或 Vue 的响应式状态，但 Compose 通过重组重新计算受影响的 UI。

## 2. `remember` 与 `rememberSaveable`

```kotlin
var count by remember { mutableStateOf(0) }
```

`remember` 在当前 Composable 仍存在时保存值。页面因配置变化重新创建时，普通 `remember` 的值可能丢失。

```kotlin
var page by rememberSaveable { mutableStateOf("home") }
```

`rememberSaveable` 会尝试保存可保存的值，例如字符串、数字和布尔值，适合保存简单页面状态。

它不是数据库，也不是完整业务状态管理方案。复杂数据应放入 ViewModel 或 Repository。

## 3. 当前项目的状态调用链

打开 [LayoutLearningScreen.kt](../app/src/main/java/com/example/myapplication/LayoutLearningScreen.kt)：

```text
page = "home"
  → homeItems 显示首页卡片
  → 点击布局
  → page = "layouts"
  → layoutListItems 显示课程列表
  → 点击 TextView
  → page = "TextView"
  → lesson = layoutLessons.firstOrNull { it.name == page }
  → lessonItems 显示详情
```

`LayoutLesson.kt` 保存静态课程数据；`LayoutLearningScreen.kt` 根据当前状态决定显示什么。这样课程数据和页面展示职责已经分开。

## 4. 返回操作

当前页面使用 `BackHandler`：

```kotlin
val back = { page = if (lesson != null) "layouts" else "home" }
BackHandler(enabled = page != "home", onBack = back)
```

返回规则：

```text
详情页 → 布局列表
布局列表 → 首页
首页 → 交给系统默认行为
```

顶部返回按钮复用同一个 `back` 操作，因此顶部按钮和系统返回键保持一致。

## 5. 为什么需要 ViewModel

当前 `page` 很简单，放在 Composable 中可以理解。但真实页面还会有网络请求、加载中、错误和重试状态，这时不应该把全部逻辑写在 UI 函数里。

推荐分层：

```text
Compose UI
  ↓ 发送事件、观察状态
ViewModel
  ↓ 处理业务动作
Repository
  ↓ 获取数据
网络 / 数据库 / 本地文件
```

ViewModel 的作用：

- 在配置变化后保留页面状态。
- 调用 Repository 获取数据。
- 把加载、成功、失败统一成 UI 状态。
- 让 UI 只负责显示和转发用户操作。

## 6. 简单 ViewModel 示例

```kotlin
data class LessonUiState(
    val selectedName: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class LessonViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LessonUiState())
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    fun selectLesson(name: String) {
        _uiState.update { it.copy(selectedName = name, error = null) }
    }
}
```

UI 中观察：

```kotlin
@Composable
fun LessonScreen(viewModel: LessonViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Button(onClick = { viewModel.selectLesson("TextView") }) {
        Text(state.selectedName ?: "选择课程")
    }
}
```

这段示例需要额外的 Lifecycle ViewModel、StateFlow 和 Compose 集成依赖；当前项目尚未加入这些依赖，先理解职责和调用方向。

## 7. 事件与状态不要混淆

事件是一次动作：

```text
用户点击 TextView
```

状态是动作后的结果：

```text
当前选中的课程是 TextView
```

推荐链路：

```text
点击事件 → ViewModel 方法 → 更新 StateFlow → UI 重组
```

不要把“点击事件”长期保存成布尔值来代替页面状态；例如 `isTextViewClicked` 不能表达当前页面、返回层级和重新进入页面后的状态。

## 8. 动手练习

### 练习 A：画状态图

画出当前项目的状态转换：

```text
home → layouts → TextView
  ↑       ↑         ↓
  └───────┴─────────┘
```

在每条箭头旁边写出触发动作：点击布局、点击课程、顶部返回、系统返回。

### 练习 B：增加一个状态

在当前页面中增加一个简单的 `showCode` 状态：

```kotlin
var showCode by rememberSaveable { mutableStateOf(false) }
```

增加一个按钮，点击后切换关键 XML 的显示或隐藏。注意：状态属于页面，课程内容仍属于 `LayoutLesson`。

### 练习 C：重构思考

回答：

1. `layoutLessons` 为什么不应该放进 ViewModel？
2. 如果课程来自网络，哪一层负责请求？
3. 如果请求失败，UI 需要哪些状态？
4. 为什么 ViewModel 不应该持有 Activity 或 View 引用？

## 9. Day 5 完成标准

- 能解释状态变化如何触发 Compose 重组。
- 能区分 `remember` 和 `rememberSaveable`。
- 能复述当前项目的首页、列表、详情状态链路。
- 能解释 `BackHandler` 的返回规则。
- 能说明 ViewModel 和 Repository 的职责。
- 能区分事件与状态。

## 10. 下一步

Day 6 学习 Activity、ViewModel、Repository 和 Koin 的依赖关系，先手动注入依赖，再接入 Koin。
