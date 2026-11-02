# Day 8：列表渲染与状态提升

目标：使用 `LazyColumn` 渲染课程列表，理解稳定的列表数据、点击事件和状态提升之间的关系。

## 1. 从前端理解列表

Compose 的 `LazyColumn` 类似前端框架中的列表组件：它接收数据集合，为每一项创建 UI，并只在需要时组合可见内容。

```kotlin
LazyColumn {
    items(courses) { course ->
        CourseRow(course)
    }
}
```

`items` 的代码块会为每个数据项执行。列表项应使用数据渲染，而不是为每一项复制一份布局代码。

## 2. 状态提升

选中课程属于列表页面的状态，因此由父组件 `ListStateDemo` 保存：

```kotlin
var selected by remember { mutableStateOf<String?>(null) }
CourseRow(course, selected == course) { selected = course }
```

子组件 `CourseRow` 只接收 `name`、`selected` 和 `onClick`。它负责展示状态并上报事件，不直接持有选中状态。这让父组件成为唯一可信状态来源，也方便以后把选中项交给详情面板或 ViewModel。

## 3. 数据、事件与单向数据流

页面遵循这条链路：

```text
课程列表 → 父组件读取数据 → CourseRow 展示
用户点击 → onClick 回调 → 父组件更新 selected → Compose 重组
```

状态更新后，只有依赖 `selected` 的内容需要重新组合。不要在子项内部偷偷修改父组件状态，也不要用多个布尔值表示同一个选中项。

## 4. 当前 Demo

源码位于 [ListStateDemo.kt](../app/src/main/java/com/example/myapplication/day8/ListStateDemo.kt)。当前使用本地课程数据：

```kotlin
private val courses = listOf("TextView", "Button", "LinearLayout")
```

运行 App → 首页 → **Day 8 · 列表渲染**。点击任意课程后，该项显示勾选标记，页面底部同步显示“当前课程”。顶部返回和系统返回均可回到首页。

## 5. 动手练习

1. 为课程增加 `description` 字段，在选中项下面显示课程说明。
2. 把 `courses` 改成数据类列表，并为 `LazyColumn` 增加稳定 `key`。
3. 增加“清除选择”按钮，点击后把 `selected` 设为 `null`。
4. 观察旋转屏幕后选中项是否保留，并思考 `remember` 与 `rememberSaveable` 的差异。
5. 将列表数据提升到 ViewModel，比较页面状态和业务状态的边界。

## 6. 完成标准

- 能解释 `LazyColumn` 和普通 `Column` 的使用场景。
- 能说明为什么列表项不应该各自保存同一份选中状态。
- 能画出“点击 → 回调 → 更新状态 → 重组”的事件链。
- 能区分展示状态、用户事件和数据源。
- 能使用数据类和稳定 key 扩展列表，而不复制 UI 代码。

