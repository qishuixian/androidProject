# Day 20：业务数据与 UI 状态

目标：承接 Day16 的 ViewModel、Day19 的重组，把课程数据、页面操作状态和派生结果分清楚。

相关基础：[MutableStateFlow 详细学习](MUTABLE_STATE_FLOW_DETAILED.md)。如果不熟悉 `_state`、`asStateFlow()` 或 `collectAsStateWithLifecycle()`，建议先阅读这份文档。

## 1. 前端类比

类似前端 store 中的原始业务数据、页面搜索参数和 computed：课程是否完成是业务事实，搜索词是当前页面的操作条件，筛选列表是计算结果。

| 内容 | 本例归属 | 说明 |
|---|---|---|
| id、title、description、completed | Course 业务模型 | 离开这个页面仍有业务意义 |
| query、selectedId、onlyIncomplete | 页面操作状态 | 控制此页面如何展示课程 |
| visibleCourses、selectedCourse、完成总数 | 派生结果 | 从当前数据和条件计算，不重复存储 |

UI 状态是“界面渲染需要的信息”，可以包含业务数据的快照；不是说业务数据不能出现在 UI 状态中。本例 CoursePageState 聚合课程与操作条件，但不往 Course 塞搜索词或选中样式。

## 2. Java 与 Kotlin 的对象更新

假设 Java 的不可变 Course 有四字段构造方法与 getter，可以创建新对象：

```java
Course updated = new Course(
    course.getId(),
    course.getTitle(),
    course.getDescription(),
    !course.getCompleted()
);
```

Kotlin data class 的 copy 更简洁：

```kotlin
val updated = course.copy(completed = !course.completed)
```

它不修改旧 Course。copy 是浅复制，没有指定的属性沿用原值或引用，并不是递归深拷贝。Kotlin List 是只读接口，不保证底层永远不可变；本例不暴露可变列表，使用 map 生成新列表。

```kotlin
current.copy(courses = current.courses.map { course ->
    if (course.id == id) course.copy(completed = !course.completed) else course
})
```

外层 copy 创建新的页面状态；map 创建列表；只有目标课程创建新的 Course，其余沿用原对象。MutableStateFlow.update 根据当前状态更新；更新函数应保持无副作用，因为并发情况下可能被多次求值。

## 3. 派生属性与 computed

```kotlin
val visibleCourses: List<Course>
    get() = courses.filter {
        it.title.contains(query, ignoreCase = true) &&
            (!onlyIncomplete || !it.completed)
    }
```

`get()` 是 Kotlin 属性 getter，每次访问计算一次；它不是 Vue computed 那样自带缓存。本例只有三条数据，不需要额外缓存。完成情况或筛选条件变化后，读取它自然得到新结果。

## 4. DTO、业务模型、Entity 的边界

| 模型 | 职责 | 示例 |
|---|---|---|
| DTO | 表达接口传输协议 | course_id、title、completed_flag |
| Course | 表达应用使用的课程概念 | id、title、completed |
| Entity | 表达本地数据库存储结构 | 主键、表字段等 |
| CoursePageState | 表达页面渲染所需状态 | courses、query、selectedId |

DTO 中的 completed_flag 如果是 0/1，可在数据层转换成业务模型的 Boolean；数据库 Entity 按存储需求定义。当前 Demo 没有接口或数据库，所以只有 Course 与 CoursePageState，不创建无用途的 DTO/Entity。它们是职责概念，不要求任何项目都机械地创建四套类。

## 5. 当前调用链

`点击标记完成 → ViewModel.toggleCompleted(id) → update/copy/map → StateFlow 新状态 → collectAsStateWithLifecycle → 重组 → 重新计算列表与完成总数`。

- [CoursePageState.kt](../app/src/main/java/com/example/myapplication/day20/CoursePageState.kt)：页面状态和派生属性。
- [CoursePageViewModel.kt](../app/src/main/java/com/example/myapplication/day20/CoursePageViewModel.kt)：私有 MutableStateFlow、只读 state 和事件方法。
- [CoursePageDemo.kt](../app/src/main/java/com/example/myapplication/day20/CoursePageDemo.kt)：收集状态、显示 UI、发送事件。

collectAsStateWithLifecycle 默认在宿主达到 STARTED 时收集；UI 无需直接修改列表。当前 ViewModel 作用域是 Activity，因此旋转通常保留，返回首页重进也保留；进程终止不自动恢复。本例没有磁盘持久化。

## 6. 动手验证

1. 首页进入 Day20，选择 Button，再点击它的“标记完成”：完成总数为 1/3。
2. 开启“只看未完成”：Button 从可见列表消失，但当前选择仍是 Button，因为它仍在原始数据中。
3. 点击清除筛选：三门课程重新出现，Button 仍已完成，选择也保留。
4. 搜索不存在的标题：显示空结果，但总数仍基于完整课程列表，不变成 0/0。
5. 对 Button 标记未完成：总数回到 0/3。

以上是设备验收步骤，不代表已完成设备验证。

## 7. 完成标准

- 能区分课程业务字段、页面操作状态和派生结果。
- 能解释 copy 与 map 如何更新对象和列表，而不原地修改旧状态。
- 能解释 DTO、业务模型、Entity 与 UI 状态的职责。
- 能说明为什么清除筛选不应该清除课程完成情况。
- 能追踪按钮事件到 ViewModel、StateFlow 和重组的完整链路。
