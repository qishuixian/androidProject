# Day 14：sealed UI 状态

目标：把 Day7 的异步状态提升为明确的 Kotlin `sealed interface`，让界面集中处理所有状态分支。

## Java 与 Kotlin

Java 通常使用枚举加字段，或多个类表示状态；Kotlin 可以用密封类型限制状态集合：

```kotlin
private sealed interface CourseUiState {
    data object Idle : CourseUiState
    data object Loading : CourseUiState
    data class Success(val count: Int) : CourseUiState
    data object Empty : CourseUiState
    data class Error(val message: String) : CourseUiState
}
```

`when` 处理 `CourseUiState` 时，编译器可以检查是否覆盖全部分支。`Success` 和 `Error` 可以携带不同数据。

## 状态链

```text
Idle → Loading → Success / Empty / Error
```

UI 不需要同时维护 `isLoading`、`hasError` 等互相可能冲突的布尔值。

## 当前 Demo

源码位于 `app/src/main/java/com/example/myapplication/day14/UiStateDemo.kt`。点击按钮切换状态，观察每个状态对应的界面文本。

## 完成标准

- 能解释 sealed 类型适合表示互斥 UI 状态。
- 能区分 `data object` 和携带数据的 `data class`。
- 能使用 `when` 覆盖所有状态分支。
- 能把 Day7 的 Loading、Success、Error 迁移到密封状态模型。
