# Day 22：依赖注入与对象创建边界

目标：承接 Day21 的 Repository 接口，理解依赖应该由谁创建、如何交给 ViewModel，以及 Koin 能替代哪些手动代码。

## 1. 依赖注入不是框架专属能力

```kotlin
class RepositoryViewModel(private val repository: CourseRepository)
```

对象从外部传进来，而不是 ViewModel 自己创建，这就是构造方法注入。Java 中也是相同思路：

```java
class CourseViewModel {
    private final CourseRepository repository;
    CourseViewModel(CourseRepository repository) {
        this.repository = repository;
    }
}
```

## 2. 当前 Demo 的依赖链

```text
CourseDependencies
  → LocalCourseRepository
  → RepositoryViewModelFactory
  → RepositoryViewModel
  → Compose UI
```

当前先用显式容器和 Factory 演示注入，避免把 Koin API 和依赖注入概念混在一起。UI 不直接创建 Repository；ViewModel 只依赖 CourseRepository 接口。

## 3. 对照 Koin

手动容器：

```kotlin
object CourseDependencies {
    val repository: CourseRepository by lazy { LocalCourseRepository() }
}
```

对应的 Koin 声明：

```kotlin
val courseModule = module {
    single<CourseRepository> { LocalCourseRepository() }
    viewModel { RepositoryViewModel(get()) }
}
```

`single` 表示容器内复用一个 Repository，`get()` 根据构造方法参数取得依赖，`viewModel` 按 Android ViewModel 作用域管理实例。

## 4. 当前项目状态

Day22 已接入手动依赖容器和 ViewModel Factory，尚未添加 Koin 依赖。真正接入 Koin 时，需要同步增加 Gradle 依赖、Application 初始化和 Manifest 注册，再把 UI 获取方式换成 `koinViewModel()`。

## 5. 完成标准

- 能解释依赖注入不等于 Koin。
- 能区分 Repository 创建、ViewModel 创建和 UI 展示。
- 能说明 Factory 为什么存在。
- 能把手动容器写法映射到 Koin `single`、`get()`、`viewModel`。
- 能解释替换 Repository 实现为何不需要改 ViewModel 的业务逻辑。
