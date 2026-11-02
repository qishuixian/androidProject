# Day 2：Kotlin 基础与 Java 对照

目标：能读懂当前项目中的 Kotlin 代码，并把简单 Java 类改写成 Kotlin。

## 1. `val` 与 `var`

```kotlin
val title = "TextView" // 只读引用
var count = 0          // 可以重新赋值
count++
```

优先使用 `val`。只有确实需要改变引用时才使用 `var`。

## 2. 函数

```kotlin
fun formatTitle(name: String): String {
    return "学习：$name"
}

fun shortTitle(name: String) = "学习：$name"
```

Kotlin 可以通过字符串模板插入变量，也可以省略简单函数的返回类型和大括号。

Java：

```java
public String formatTitle(String name) {
    return "学习：" + name;
}
```

## 3. `data class`

昨天的 Java 类：

```java
public class Lesson {
    private final String name;
    private final String title;

    public Lesson(String name, String title) {
        this.name = name;
        this.title = title;
    }

    public String getName() { return name; }
    public String getTitle() { return title; }
}
```

Kotlin 写法：

```kotlin
data class Lesson(
    val name: String,
    val title: String
)
```

`data class` 会自动提供构造方法、属性访问、`toString`、`equals`、`hashCode` 和 `copy`。

当前项目的 `LayoutLesson` 就是这种写法：它保存布局名称、说明、XML 资源和练习内容。

## 4. 空安全

```kotlin
var title: String? = null
val length = title?.length ?: 0
```

- `String`：不能为 `null`。
- `String?`：可以为 `null`。
- `?.`：对象不为空时才调用。
- `?:`：为空时提供默认值。
- `!!`：强制认为不为空，初学阶段尽量少用。

当前项目中的代码：

```kotlin
val lesson = layoutLessons.firstOrNull { it.name == page }
```

`firstOrNull` 找不到内容时返回 `null`，所以后面使用 `lesson?.title` 是安全读取。

## 5. 集合函数

```kotlin
val layouts = listOf("TextView", "LinearLayout", "GridLayout")
val names = layouts.filter { it.contains("Layout") }
val upperNames = names.map { it.uppercase() }
```

- `filter`：保留符合条件的元素。
- `map`：把每个元素转换成新值。
- `find` / `firstOrNull`：查找第一个符合条件的元素。
- `forEach`：遍历并执行操作。

前端类比：用法接近 JavaScript 的 `filter`、`map`、`find`，但 Kotlin 的 Lambda 参数和类型由编译器检查。

## 6. Lambda

```kotlin
val isLayout = { name: String -> name.endsWith("Layout") }
val result = isLayout("LinearLayout")
```

在当前项目中：

```kotlin
layoutLessons.forEach { entry ->
    LearningCard(entry.name, entry.summary) { page = entry.name }
}
```

外层 Lambda 遍历课程，内层 Lambda 是点击卡片时执行的操作。

## 7. 练习：改写 Java 类

将昨天的 Java `Lesson` 改写为：

```kotlin
data class Lesson(
    val name: String,
    val title: String
)

fun main() {
    val lessons = listOf(
        Lesson("TextView", "文本框详解"),
        Lesson("LinearLayout", "线性布局")
    )

    lessons
        .filter { it.name.contains("Layout") }
        .forEach { println("${it.name}：${it.title}") }
}
```

预期输出：

```text
LinearLayout：线性布局
```

## 8. 练习：修改当前项目

打开 [LayoutLessons.kt](../app/src/main/java/com/example/myapplication/LayoutLessons.kt)：

1. 找到 `LayoutLesson`，说出每个属性的类型。
2. 给某个布局的 `exercise` 修改一句中文练习。
3. 把 `layoutLessons.firstOrNull` 改写成先 `filter` 再取第一个结果。
4. 观察 `lesson == null` 和 `lesson != null` 两个分支如何决定页面内容。

不要删除 `AbsoluteLayout` 的废弃说明；它是学习旧代码的重要提示。

## 9. Day 2 完成标准

- 能区分 `val` 与 `var`。
- 能解释 `String` 与 `String?`。
- 能写一个 `data class`。
- 能使用 `filter`、`map`、`find` 和 `forEach`。
- 能读懂当前项目的页面状态与点击 Lambda。
- 能把一个简单 Java 类改写为 Kotlin。

## 10. 下一步

Day 3 学习 Android 页面基础：Activity 生命周期、Manifest、资源目录、XML 与 Compose 的关系，并追踪一次“点击布局入口”的完整调用链。
