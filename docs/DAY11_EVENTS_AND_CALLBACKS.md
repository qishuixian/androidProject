# Day 11：用户事件与回调

目标：理解子组件如何报告点击、删除等事件，以及父组件为什么负责修改状态。

## 1. 前端经验的对应关系

这和前端组件的 `props` 与事件回调相同：父组件传入数据和函数，子组件发生点击时调用函数。子组件不直接修改父组件的数据。

## 2. Java 与 Kotlin 写法

Java 常用监听器接口表示回调：

```java
interface OnCourseClickListener {
    void onClick(Course course);
}
```

Kotlin 可以直接使用函数类型：

```kotlin
onSelect: (Course) -> Unit
onDelete: (String) -> Unit
```

`Unit` 表示回调只产生事件，不返回业务值。

## 3. 单向事件链

```text
用户点击 → CourseRow 调用回调 → 父组件更新 courses/message → Compose 重组
```

## 4. 当前 Demo

源码位于 `app/src/main/java/com/example/myapplication/day11/CourseEventsDemo.kt`。点击课程会更新提示，点击删除会由父组件按 `id` 移除对应课程。

## 5. 完成标准

- 能区分数据下传和事件上报。
- 能用 Java 接口表达回调。
- 能用 Kotlin 函数类型表达回调。
- 能解释为什么子组件不应直接修改父组件列表。
