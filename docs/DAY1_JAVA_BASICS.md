# Day 1：Java 基础与 Android 代码阅读

目标：理解 Android 项目中最常见的 Java 语法，并能读懂 Activity、数据类和 Repository 的基本结构。

## 1. 今天学习什么

1. 变量与基本类型
2. 方法的参数和返回值
3. 类、对象和构造方法
4. 封装与 `private`
5. `List` 集合和循环
6. Java 与 Kotlin 的对应写法

## 2. 变量与基本类型

```java
int count = 6;
boolean enabled = true;
String title = "TextView";
```

- `int`：整数
- `double`：小数
- `boolean`：`true` 或 `false`
- `String`：文本

Java 中变量先写类型，再写名称。变量名建议使用小驼峰，例如 `lessonTitle`。

## 3. 方法

```java
public String formatTitle(String name) {
    return "学习：" + name;
}
```

结构是：

```text
访问修饰符 + 返回类型 + 方法名 + 参数
```

`String` 是返回类型；`name` 是参数；`return` 返回结果。没有返回值时使用 `void`：

```java
public void printTitle(String title) {
    System.out.println(title);
}
```

## 4. 类与对象

```java
public class Lesson {
    private final String name;

    public Lesson(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
```

创建对象：

```java
Lesson lesson = new Lesson("LinearLayout");
String name = lesson.getName();
```

`private` 表示只能在类内部访问；构造方法在创建对象时执行；`this.name` 表示当前对象的字段。

## 5. 集合

```java
List<String> layouts = Arrays.asList(
        "LinearLayout",
        "RelativeLayout",
        "TextView"
);

for (String layout : layouts) {
    System.out.println(layout);
}
```

Android 业务代码中经常使用 `List<Model>` 保存接口数据或页面数据。先掌握 `List`，之后再学习 `Map`、泛型和可变集合。

## 6. Java 与 Kotlin 对照

Java：

```java
public class Lesson {
    private final String name;

    public Lesson(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
```

Kotlin：

```kotlin
data class Lesson(val name: String)
```

Kotlin 自动生成构造方法、`getName` 等常用代码，因此更适合新功能；Java 版本更适合阅读旧项目和理解底层结构。

## 7. Android 阅读练习

打开 [MainActivity.kt](../app/src/main/java/com/example/myapplication/MainActivity.kt)，找到：

```kotlin
class MainActivity : ComponentActivity()
```

今天只回答三个问题：

1. `MainActivity` 是什么？
2. `ComponentActivity` 在这里表示什么关系？
3. `onCreate` 为什么会在页面启动时执行？

再打开 [LayoutLessons.kt](../app/src/main/java/com/example/myapplication/LayoutLessons.kt)，找到：

```kotlin
data class LayoutLesson(...)
```

把它对应到 Java 思维：它相当于一个保存多个字段的 `Lesson` 类，`layoutLessons` 相当于一个课程对象列表。

## 8. 动手练习

### 练习 A：不用 Android API 写 Java 类

创建临时文件 `Lesson.java`：

```java
import java.util.Arrays;
import java.util.List;

public class Lesson {
    private final String name;
    private final String title;

    public Lesson(String name, String title) {
        this.name = name;
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public static void main(String[] args) {
        List<Lesson> lessons = Arrays.asList(
                new Lesson("TextView", "文本框详解"),
                new Lesson("LinearLayout", "线性布局")
        );

        for (Lesson lesson : lessons) {
            System.out.println(lesson.getName() + "：" + lesson.getTitle());
        }
    }
}
```

预期输出：

```text
TextView：文本框详解
LinearLayout：线性布局
```

### 练习 B：思考题

1. 为什么字段通常写成 `private final`？
2. `new Lesson(...)` 做了什么？
3. 如果要新增 `summary` 字段，需要修改哪些位置？
4. Kotlin 的 `data class` 为 Java 类省去了哪些代码？

## 9. Day 1 完成标准

完成以下内容就可以进入 Day 2：

- 能解释变量、方法、类、对象和构造方法。
- 能读懂 `List<Lesson>` 和增强 `for` 循环。
- 能看懂一个简单 Java Activity 的 `onCreate`。
- 能解释 Java 类与 Kotlin `data class` 的对应关系。
- 能运行练习 A 并得到预期输出。

## 10. 下一天

Day 2 学习 Kotlin 基础：`val`、`var`、空安全、`data class`、Lambda，以及把 Java 练习改写成 Kotlin。
