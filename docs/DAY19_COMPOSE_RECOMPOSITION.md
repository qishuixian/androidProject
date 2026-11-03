# Day 19：Compose 重组

目标：承接 Day17、Day18，区分状态引起的重组、组件离开组合、Activity 重建；用父子组件实验理解状态读取与跳过。

## 1. 从已有前端经验理解

Compose 与 React/Vue 一样，由状态描述界面。状态变化时，框架更新相关 UI。Composable 函数体可能再次执行，这叫重组；不意味着 Activity 重新创建，也不意味着整页所有控件都重新绘制。

Compose 跟踪 Snapshot State 的读取。状态变化会使读取它的重组作用域失效，调度后续重组。本例特意在父组件读取 count/title，再传普通值给两个子组件，便于观察父组件重新执行时，参数未变的子组件可能被跳过。

## 2. Java View 与 Kotlin Compose

传统 Java View 常显式修改控件（示意，假定控件已初始化）：

```java
private int count = 0;

// 在页面初始化时绑定监听器
button.setOnClickListener(view -> {
    count++;
    textView.setText("计数：" + count);
});
```

Kotlin Compose 修改状态，界面由状态计算：

```kotlin
var count by remember { mutableIntStateOf(0) }
Text("计数：$count")
Button(onClick = { count++ }) { Text("增加") }
```

这是 UI 编程模型的区别，不是 Java 无法观察数据；Kotlin 也可以操作传统 View。普通 `var count = 0` 不会自动通知 Compose，`remember` 负责记住值，`mutableIntStateOf` 负责让读取与变更可被观察。

## 3. 参数与稳定性

```kotlin
CountPanel(currentCount)
TitlePanel(currentTitle)
```

Int、String 是稳定参数。父组件再次执行时，参数不变且满足编译器跳过条件的子组件可以跳过。这里增加 count，TitlePanel 通常可以跳过；切换 title，CountPanel 通常可以跳过。子组件如果自己读取的状态发生变化，即使参数不变也可能需要重组。

不要把 `data class` 等同于稳定类型，其属性类型与可变性也参与判断；也不要为追求跳过随意加 `@Stable`。实际跳过行为受编译器、配置和其他状态影响，以日志和工具测量为准。

## 4. 为什么用 SideEffect 打日志

```kotlin
SideEffect {
    Log.d("Day19", "CountPanel committed: count=$count")
}
```

SideEffect 在成功应用组合后执行，包含首次组合及后续执行到此处并成功应用的重组。它不是屏幕绘制次数，也不是点击次数。直接在函数体打印会记录计算尝试，而重组可能被取消。

本例不在 SideEffect 中增加可观察计数，避免日志统计本身引发新的重组。也不要在 Composable 函数体直接发请求；点击逻辑放回调，受 key 控制的协程副作用使用 LaunchedEffect 等合适机制。

`DisposableEffect(Unit)` 的 entered/disposed 用于观察该组合位置的进入/离开；普通重组不会反复触发它。SideEffect 则不是仅首次执行。

## 5. 操作实验

入口：首页 → Day 19 · Compose 重组。

源码：[RecompositionDemo.kt](../app/src/main/java/com/example/myapplication/day19/RecompositionDemo.kt)。Logcat 搜索 `Day19`。

| 操作 | 预期观察 |
|---|---|
| 首次进入 | Page entered，以及 Parent、CountPanel、TitlePanel 的 committed 日志 |
| 点击“计数 +1” | 数字更新，Parent 与 CountPanel 日志；TitlePanel 通常跳过 |
| 点击“切换课程标题” | 标题更新，Parent 与 TitlePanel 日志；CountPanel 通常跳过 |
| 点击“赋相同计数” | 出现 Click 日志，相同数值赋值本身不触发状态变更重组 |
| 返回首页再进入 | disposed/entered，普通 remember 状态初始化 |
| 旋转并确认 Activity 重建 | Day17 记录新实例，Day19 状态初始化 |

不要依赖日志严格顺序、总条数或“一次点击必定一次重组”；系统也可能因其他原因调度更新。本表是设备验收预期，不代表已完成实机验证。

## 6. 与 Day18 对照

重组时，当前组合位置的 remember 值仍在；Activity 重建后普通 remember 重新初始化。rememberSaveable 增加的是保存恢复能力，不是“阻止重组”。ViewModel 管理状态，也不意味着订阅状态的 UI 不再重组。

## 7. 练习与完成标准

1. 阅读日志，说明为什么计数变化时标题不必重新执行。
2. 暂时把 `TitlePanel` 的参数改成 `"$currentTitle / $currentCount"`，观察增加计数后它也更新，再恢复原代码。
3. 能解释状态读取、失效、重组和子组件跳过之间的关系。
4. 能区分 SideEffect、DisposableEffect 与 Activity 生命周期日志。
5. 能说明 Java 显式 setText 和 Compose 修改 State 的区别。
