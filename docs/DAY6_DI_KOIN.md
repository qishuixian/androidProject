# Day 6：依赖注入与 Koin

目标：理解 Repository、ViewModel 和 UI 之间如何传递依赖，并知道 Koin 为什么能减少手动创建对象的代码。

## 1. 什么是依赖

如果 ViewModel 需要 Repository，Repository 就是 ViewModel 的依赖：

```kotlin
class LessonViewModel(
    private val repository: LessonRepository
) : ViewModel()
```

ViewModel 不负责创建 Repository，而是通过构造方法接收它。这样更容易替换实现和测试。

## 2. 手动依赖注入

先定义 Repository：

```kotlin
class LessonRepository {
    fun getLessons(): List<LayoutLesson> = layoutLessons
}
```

再定义 ViewModel：

```kotlin
class LessonViewModel(
    private val repository: LessonRepository
) : ViewModel() {
    fun loadLessons(): List<LayoutLesson> {
        return repository.getLessons()
    }
}
```

创建时手动传入：

```kotlin
val repository = LessonRepository()
val viewModel = LessonViewModel(repository)
```

这就是最基本的依赖注入：对象由外部创建，再传给使用者。

## 3. 为什么需要 Koin

当项目有很多对象时，手动创建会变得复杂：

```text
Repository 需要 ApiService
ApiService 需要 OkHttpClient
ViewModel 需要 Repository
页面需要 ViewModel
```

Koin 可以集中声明这些对象的创建关系：

```kotlin
val appModule = module {
    single { OkHttpClient() }
    single { ApiService(get()) }
    single { LessonRepository(get()) }
    viewModel { LessonViewModel(get()) }
}
```

`get()` 表示从 Koin 容器中获取已经声明的依赖。

## 4. Koin 常用定义

### `single`

整个容器复用一个实例：

```kotlin
single { LessonRepository() }
```

适合 Repository、数据库、网络客户端等长生命周期对象。

### `factory`

每次获取时创建新对象：

```kotlin
factory { LessonFormatter() }
```

适合无状态、创建成本低的工具对象。

### `viewModel`

声明 ViewModel：

```kotlin
viewModel { LessonViewModel(get()) }
```

Koin 会按照 ViewModel 的生命周期管理实例。

## 5. Application 中启动 Koin

```kotlin
class LearningApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@LearningApplication)
            modules(appModule)
        }
    }
}
```

Manifest 中声明：

```xml
<application
    android:name=".LearningApplication"
    ... />
```

当前项目还没有接入 Koin，因此这段代码是学习示例；真正添加时需要先在 Gradle 中加入与项目版本兼容的 Koin 依赖。

## 6. Compose 中获取 ViewModel

```kotlin
@Composable
fun LessonPage(
    viewModel: LessonViewModel = koinViewModel()
) {
    val lessons = viewModel.loadLessons()
    // 显示 lessons
}
```

页面只需要声明“我需要一个 LessonViewModel”，不需要自己创建 Repository。

## 7. 与当前 ViewModel Demo 对照

当前项目的 [ViewModelDemo.kt](../app/src/main/java/com/example/myapplication/ViewModelDemo.kt) 使用无依赖 ViewModel：

```kotlin
internal class LessonDemoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LessonDemoUiState())
}
```

下一步改造思路：

```text
当前：ViewModel 直接修改状态

改造后：
UI → LessonViewModel
       ↓
LessonRepository
       ↓
layoutLessons / 网络 / 数据库
```

这样 ViewModel 不需要知道数据具体来自哪里。

## 8. 测试的好处

手动注入或 Koin 注入后，可以在测试中替换 Fake Repository：

```kotlin
class FakeLessonRepository : LessonRepository {
    override fun getLessons() = listOf(
        LayoutLesson(/* 测试数据 */)
    )
}
```

测试 ViewModel 时不需要访问真实网络或数据库，结果更稳定。

## 9. 动手练习

### 练习 A：手动注入

1. 创建 `LessonRepository`。
2. 给 `LessonDemoViewModel` 增加 Repository 构造参数。
3. 添加 `loadFirstLesson()` 方法。
4. 在 UI 中显示 Repository 返回的第一条课程。

### 练习 B：画依赖关系

```text
LessonDemoViewModel
        ↓ 需要
LessonRepository
        ↓ 提供
List<LayoutLesson>
```

回答：如果课程从网络加载，应该修改 UI、ViewModel 还是 Repository？

### 练习 C：Koin 模块

不运行代码，先写出：

```kotlin
val appModule = module {
    single { LessonRepository() }
    viewModel { LessonDemoViewModel(get()) }
}
```

说明 `single`、`viewModel` 和 `get()` 的作用。

## 10. Day 6 完成标准

- 能解释什么是依赖。
- 能使用构造方法手动注入 Repository。
- 能说明 `single`、`factory`、`viewModel` 和 `get()`。
- 能说出 Koin 初始化的位置。
- 能画出 UI、ViewModel、Repository 的依赖关系。
- 能说明依赖注入如何帮助测试。

## 11. 下一步

Day 7 学习异步数据：协程、`suspend`、StateFlow 的加载状态、错误状态与重试。
