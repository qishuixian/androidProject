# Day 13：集合转换

目标：掌握 Java 集合处理与 Kotlin `map`、`filter`，把课程数据转换成界面需要的结果。

## Java 与 Kotlin

Java 常见写法使用循环或 Stream：

```java
List<String> titles = courses.stream()
        .filter(course -> course.getTitle().contains(query))
        .map(Course::getTitle)
        .toList();
```

Kotlin 使用集合扩展函数表达同样的流程：

```kotlin
val titles = courses
    .filter { it.title.contains(query, ignoreCase = true) }
    .map { it.title }
```

`filter` 保留符合条件的元素，`map` 把每个元素转换成另一种值。它们不会直接修改原列表。

## 当前 Demo

源码位于 `app/src/main/java/com/example/myapplication/day13/CollectionTransformDemo.kt`。输入课程名即可筛选，页面同时演示从 `Course` 对象提取标题。

## 完成标准

- 能区分 `filter` 和 `map`。
- 能用 Kotlin 链式处理课程集合。
- 能解释不可变集合转换不会修改原列表。
- 能处理筛选结果为空的情况。
