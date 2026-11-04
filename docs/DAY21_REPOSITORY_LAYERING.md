# Day 21：Repository 分层

目标：承接 Day16 的 ViewModel 和 Day20 的业务状态，把数据获取职责从 ViewModel 中抽到 Repository。

## 1. 三层职责

```text
UI
  ↓ 发送事件、收集状态
ViewModel
  ↓ 调用 Repository、转换状态
Repository
  ↓ 决定数据来自本地、网络还是数据库
```

- UI 不直接读取文件、网络或数据库。
- ViewModel 不关心数据具体来自哪个实现，只处理加载状态和事件。
- Repository 隐藏数据源细节，向上提供稳定的方法。

## 2. Java 与 Kotlin 接口

Java 接口示意：

```java
interface CourseRepository {
    List<Course> loadCourses();
}
```

Kotlin 使用挂起函数：

```kotlin
interface CourseRepository {
    suspend fun loadCourses(): List<Course>
}
```

`suspend` 允许 Repository 等待异步数据而不阻塞线程。当前 `LocalCourseRepository` 用 `delay` 模拟数据源，后续可以替换成 Retrofit 或 Room 实现。

## 3. 为什么使用接口

ViewModel 依赖接口：

```kotlin
class RepositoryViewModel(
    private val repository: CourseRepository
) : ViewModel()
```

生产实现可以是：

```text
LocalCourseRepository
NetworkCourseRepository
FakeCourseRepository
```

UI 不需要知道具体类名。测试时可以传入 Fake 实现，验证 Loading、Success、Error，而不用访问真实网络。

## 4. 当前调用链

入口：首页 → Day 21 · Repository 分层。

```text
点击“加载课程”
  → RepositoryViewModel.load()
  → state = Loading
  → repository.loadCourses()
  → state = Success(courses) 或 Error(message)
  → collectAsStateWithLifecycle 收集
  → Compose 根据状态重组
```

源码位于 `app/src/main/java/com/example/myapplication/day21/`：

- `CourseRepository.kt`：接口和本地实现。
- `RepositoryViewModel.kt`：状态、协程和 Repository 调用。
- `RepositoryDemo.kt`：只负责展示状态和发送点击事件。

## 5. 完成标准

- 能说出 UI、ViewModel、Repository 各自的职责。
- 能解释为什么 ViewModel 依赖接口而不是具体数据源。
- 能说明 `suspend` 在 Repository 中的作用。
- 能追踪点击到 Repository 再回到 UI 的完整链路。
- 能解释替换 Repository 实现为什么不需要修改 UI。
