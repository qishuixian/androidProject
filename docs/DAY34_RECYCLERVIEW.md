# Day34 · RecyclerView、Adapter、ViewHolder

## 先用前端理解

| Android | 前端类比 |
|---|---|
| RecyclerView | 可复用 DOM 的长列表容器 |
| Adapter | 把数组数据映射成列表行的渲染器 |
| ViewHolder | 一行 DOM/View 的缓存引用 |
| `onBindViewHolder` | 把当前 item 重新渲染到这一行 |
| DiffUtil | 比较旧数组和新数组，计算增删改移动 |
| stable id | `key` 或业务主键，不是数组下标 |

RecyclerView 不为 60 条课程永久创建 60 个 View。它只创建屏幕附近需要的少量行，滚动时让旧行重新绑定新数据。因此 `bind` 必须覆盖每一个可变显示属性。

## 入口和流程

首页 → Day34 → 打开 RecyclerView 课程列表。

```text
Compose 入口
  -> Intent
  -> RecyclerCourseActivity
  -> RecyclerView + LinearLayoutManager
  -> CourseAdapter.submitList(courses)
  -> onCreateViewHolder（需要新行时）
  -> onBindViewHolder（某行显示某课程时）
  -> CourseHolder.bind(course)
```

源码：[RecyclerCourseDemo.kt](../app/src/main/java/com/example/myapplication/day34/RecyclerCourseDemo.kt)。行布局：[day34_course_item.xml](../app/src/main/res/layout/day34_course_item.xml)。

## 一次点击如何更新

按钮位于 ViewHolder，但它只回调 `course.id`：

```kotlin
binding.toggleButton.setOnClickListener { onToggle(course.id) }
```

Activity 负责业务状态：

```kotlin
courses = courses.map { course ->
    if (course.id == id) course.copy(completed = !course.completed) else course
}
courseAdapter.submitList(courses)
```

这条链路类似前端单向数据流：

```text
行按钮 click
  -> 回传 id
  -> Activity 更新 state
  -> submitList 新数组
  -> DiffUtil 比较
  -> 只有需要的行重新 bind
```

不要在 ViewHolder 里保存“当前课程是否完成”作为业务真相；ViewHolder 会复用，滚动后它可能显示另一条课程。

## `areItemsTheSame` 与 `areContentsTheSame`

```kotlin
override fun areItemsTheSame(oldItem, newItem) = oldItem.id == newItem.id
override fun areContentsTheSame(oldItem, newItem) = oldItem == newItem
```

- `areItemsTheSame`：还是不是同一条业务课程？看稳定 id。
- `areContentsTheSame`：同一条课程的标题或完成状态有没有变化？`data class` 的 `==` 会比较属性。

反转列表时，id 没变但位置变化，DiffUtil 识别为移动。删除第一条时，删除的是当前顺序第一项，不能把位置 0 当成课程身份。

## 实验

1. 滚动列表，在 Logcat 搜索 `Day34`，比较“创建 ViewHolder”和“绑定课程”次数。
2. 点击任意课程的完成按钮，观察只有对应内容变化。
3. 反转顺序，再点击课程，确认完成状态跟着 id 移动。
4. 删除第一条，确认删除的是当前第一项，而不是固定 id=1。
5. 旋转页面，观察保存的 id 顺序和完成 id 恢复；这属于配置恢复，不是数据库长期持久化。

## 需要记住

- `RecyclerView` 管滚动和复用，`Adapter` 管数据到行的连接，`ViewHolder` 管一行控件引用。
- `position` 是当前显示位置，`id` 才是业务身份。
- `submitList` 提交新列表；`copy`、`map`、`reversed`、`drop` 产生新状态，避免原地修改。
- `DiffUtil` 需要正确的身份比较和内容比较。
