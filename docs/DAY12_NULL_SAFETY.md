# Day 12：空值安全

目标：理解 Java 的 `null` 判空和 Kotlin 的可空类型，避免在课程详情不存在时崩溃。

## Java 与 Kotlin

Java 变量默认可能为 `null`，通常需要手动判断：

```java
if (course != null) {
    return course.getTitle();
}
return "尚未选择";
```

Kotlin 会把可空性写进类型：

```kotlin
var selectedId: String? = null
val title = selected?.title ?: "尚未选择"
```

- `?`：声明或安全访问可空值。
- `?.`：只有不为空时才继续访问。
- `?:`：为空时使用默认值。
- `!!`：强制断言不为空，应谨慎使用。

## 当前 Demo

源码位于 `app/src/main/java/com/example/myapplication/day12/NullSafetyDemo.kt`。进入页面时没有选中项，点击课程后显示标题；整个过程不需要使用 `!!`。

## 完成标准

- 能区分 Java 运行时判空和 Kotlin 编译期可空类型。
- 能使用 `?.` 和 `?:` 安全显示可选数据。
- 能解释为什么不应随意使用 `!!`。
