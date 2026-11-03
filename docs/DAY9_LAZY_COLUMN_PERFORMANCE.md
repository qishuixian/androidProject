# Day 9：列表数据建模

目标：在 Day8 列表和状态提升的基础上，把字符串列表提升为类型明确的课程对象，并同时理解 Java 类与 Kotlin `data class`。

## 1. 为什么要建模

Day8 使用 `listOf("TextView", "Button", "LinearLayout")`，适合演示列表渲染。真实课程还会有 id、标题、简介和完成状态，因此需要一个对象承载这些字段。

## 2. Java 写法

```java
public final class Course {
    private final String id;
    private final String title;

    public Course(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
}
```

## 3. Kotlin 写法

```kotlin
data class Course(
    val id: String,
    val title: String,
    val completed: Boolean = false
)
```

`data class` 自动生成常用的 `equals`、`hashCode`、`toString` 和 `copy`，适合表示 UI 和数据层使用的值对象。

## 4. 与 Day8 的连接

列表仍由父组件持有，子组件仍通过回调上报点击；变化是列表元素从 `String` 变成 `Course`，以后可以安全增加字段，而不需要改变组件职责。

## 5. 完成标准

- 能解释为什么字符串列表无法承载完整课程信息。
- 能写出 Java 类和 Kotlin `data class`。
- 能使用 `Course.id` 作为列表项稳定身份。
- 能保持 Day8 的父组件状态提升结构。
