# Day 16：ViewModel 与 StateFlow

目标：把页面业务状态从 Composable 提升到 ViewModel，让旋转屏幕后状态和业务逻辑保持稳定。

## 1. 为什么使用 ViewModel

Composable 负责显示 UI，ViewModel 负责保存页面业务状态和处理事件。这样 Activity 重建时，ViewModel 通常仍然存在，网络结果和状态转换不会因为重组而重新创建。

## 2. StateFlow 的职责

```kotlin
private val _state = MutableStateFlow<CourseScreenState>(CourseScreenState.Idle)
val state: StateFlow<CourseScreenState> = _state.asStateFlow()
```

ViewModel 持有可修改的私有状态，只向 UI 暴露只读 `StateFlow`。Compose 使用 `collectAsState()` 观察它：

```kotlin
val state by model.state.collectAsState()
```

状态变化后，依赖它的 UI 会重新组合。

## 3. 当前 Demo

源码位于 `app/src/main/java/com/example/myapplication/day16/`。点击加载或模拟失败，观察 ViewModel 更新 StateFlow，界面根据状态显示不同内容。

## 4. 完成标准

- 能区分 UI 状态和 ViewModel 业务状态。
- 能解释 `MutableStateFlow` 与只读 `StateFlow` 的区别。
- 能说明 UI 为什么只收集状态并发送事件。
- 能把 Day14 的 sealed 状态迁移到 ViewModel。
