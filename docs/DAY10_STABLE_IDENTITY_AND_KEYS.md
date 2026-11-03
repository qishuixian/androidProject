# Day 10：稳定身份与列表 key

目标：理解列表位置和数据身份的区别，使用稳定 `id` 支持排序、删除和后续状态保存。

## 1. 为什么不能使用下标

列表下标表示当前显示位置。反转或删除一项后，下标会变化；课程的 `id` 才表示它本身。真实项目应使用数据库 id 或服务端 id，不要使用随机数或当前下标作为身份。

## 2. Java 与 Kotlin

Java 通常通过 `equals` 和 `hashCode` 表示对象值是否相等；Kotlin `data class` 会自动生成这两个方法。Day9 的 `Course` 因此可以安全地比较、筛选和复制。

```kotlin
val remaining = courses.filterNot { it.id == removedId }
```

## 3. 当前 Demo

源码位于 `app/src/main/java/com/example/myapplication/day10/CourseIdentityDemo.kt`。点击“反转顺序”或删除课程，观察每项的稳定 id。长列表使用 `LazyColumn` 时，应写成 `items(courses, key = { it.id })`。

## 4. 完成标准

- 能区分列表位置和数据身份。
- 能解释 `data class` 的 `equals` 与 `hashCode` 用途。
- 能使用 id 删除指定课程。
- 能说明为什么 LazyColumn 需要稳定 key。
