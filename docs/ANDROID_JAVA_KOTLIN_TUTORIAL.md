# Android Java + Kotlin 混合学习教程

这份教程面向有前端经验的 Android 初学者，目标是同时具备两种能力：能用 Kotlin 开发现代 Android 功能，也能用 Java 阅读和维护传统 Android 项目。

## 一、先建立整体认识

Android 应用通常由这些部分组成：

```text
Activity / Compose 页面
        ↓ 用户操作
ViewModel（页面状态）
        ↓
Repository（数据来源）
        ↓
网络、数据库或本地数据
```

Java 和 Kotlin 都可以编写上面任意一层。语言和框架是不同概念：Java、Kotlin 是语言；Android SDK 是平台 API；Compose 是 UI 工具包；Koin 是依赖注入框架。

当前项目的入口链路是：

```text
MainActivity.onCreate()
  → setContent()
  → LayoutLearningApp()
  → 点击“布局”
  → 修改 page 状态
  → 显示布局学习详情
  → AndroidView 加载 XML 示例
```

入口代码见 [MainActivity.kt](../app/src/main/java/com/example/myapplication/MainActivity.kt)，布局数据见 [LayoutLessons.kt](../app/src/main/java/com/example/myapplication/LayoutLessons.kt)。

## 二、Java 基础

### 2.1 变量、方法和类

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

需要掌握：

- 基本类型与引用类型
- `if`、`for`、`while`
- 方法参数和返回值
- 类、对象、构造方法
- `private`、`public`、`final`
- 继承、接口和多态
- `List`、`Map`、泛型和异常

### 2.2 Android 中常见的 Java 写法

```java
public class JavaActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java);

        TextView title = findViewById(R.id.title);
        title.setText("Java 页面");
    }
}
```

这里要理解：`onCreate` 是生命周期回调，`setContentView` 加载 XML，`findViewById` 找到控件，`setText` 修改文本。

## 三、Kotlin 基础

同样的模型用 Kotlin 可以写得更简洁：

```kotlin
data class Lesson(val name: String)
```

重点学习：

- `val`（只读引用）与 `var`（可重新赋值）
- 可空类型：`String?`、`?.`、`?:`
- `data class`
- Lambda 和高阶函数
- 扩展函数
- 集合操作：`map`、`filter`、`find`
- 协程、`suspend`、Flow

例如当前项目使用集合保存课程：

```kotlin
val lesson = layoutLessons.firstOrNull { it.name == page }
```

前端类比：Kotlin 的 `data class` 类似 TypeScript 的数据类型加对象结构；`map` 和 `filter` 的使用方式与 JavaScript 相近，但 Kotlin 有自己的类型系统和运行规则。

## 四、Java 与 Kotlin 互相调用

### 4.1 Kotlin 调用 Java

Java 类：

```java
public class JavaLessonRepository {
    public String getTitle() {
        return "Java 提供的数据";
    }
}
```

Kotlin 调用：

```kotlin
val repository = JavaLessonRepository()
val title = repository.title
```

### 4.2 Java 调用 Kotlin

Kotlin 类：

```kotlin
class KotlinLessonRepository {
    fun getTitle(): String = "Kotlin 提供的数据"
}
```

Java 调用：

```java
KotlinLessonRepository repository = new KotlinLessonRepository();
String title = repository.getTitle();
```

### 4.3 混合项目的原则

1. 旧 Java 文件先保持稳定，不为迁移而迁移。
2. 新页面和新业务默认使用 Kotlin。
3. Java 与 Kotlin 之间通过清晰的接口或 Repository 交互。
4. 不要让 Activity 同时承担 UI、网络和数据处理。
5. 逐个文件迁移，迁移后运行测试。

## 五、Android 基础与 XML 布局

先学习传统 View 系统，因为它能帮助理解 Android 的测量、布局和控件关系。

- `TextView`：显示文本。
- `LinearLayout`：水平或垂直排列。
- `RelativeLayout`：根据父容器或其他控件定位。
- `TableLayout`：按行列组织表格。
- `FrameLayout`：层叠子控件。
- `GridLayout`：按网格排列。

当前示例位于 [app/src/main/res/layout](../app/src/main/res/layout)。

TextView 常见属性：

```xml
<TextView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="欢迎学习 Android"
    android:textSize="20sp"
    android:textColor="@android:color/black"
    android:gravity="center"
    android:padding="16dp" />
```

注意区分：`gravity` 控制文字在 TextView 内部的对齐；`layout_gravity` 在父布局支持时控制 TextView 在父容器中的位置；`layout_width` 和 `layout_height` 控制控件尺寸。

`AbsoluteLayout` 已废弃，只用来理解旧代码，不用于新页面。

## 六、Jetpack Compose

Compose 使用 Kotlin 以声明式方式构建 UI：

```kotlin
@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name")
}
```

常用组件：

- `Text`：文本。
- `Row`：水平排列，类似水平 `LinearLayout`。
- `Column`：垂直排列，类似垂直 `LinearLayout`。
- `Box`：层叠内容，类似 `FrameLayout`。
- `LazyColumn`：高效滚动列表。
- `Modifier`：尺寸、间距、点击和样式。

状态示例：

```kotlin
var count by rememberSaveable { mutableStateOf(0) }
Button(onClick = { count++ }) {
    Text("点击次数：$count")
}
```

状态变化会触发相关 Composable 重组。`rememberSaveable` 还能在配置变化时保存可保存的状态。

## 七、ViewModel 与分层

当页面变复杂时，把状态和业务逻辑移出 Activity 或 Composable：

```text
UI：只负责显示和发送用户操作
ViewModel：保存页面状态、调用业务方法
Repository：提供网络、数据库或本地数据
```

Java 也可以写 ViewModel，Kotlin 通常更简洁。学习顺序建议是：先手动构造 Repository，再学习 Koin 自动提供依赖。

## 八、Koin 依赖注入

Koin 解决“对象由谁创建、如何传给使用者”的问题。

不用依赖注入时：

```kotlin
class LessonViewModel : ViewModel() {
    private val repository = LessonRepository()
}
```

使用构造方法注入后：

```kotlin
class LessonViewModel(
    private val repository: LessonRepository
) : ViewModel()
```

再由 Koin 声明对象：

```kotlin
val appModule = module {
    single { LessonRepository() }
    viewModel { LessonViewModel(get()) }
}
```

应用启动时初始化：

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApplication)
            modules(appModule)
        }
    }
}
```

常见定义：

- `single`：容器中复用同一个实例。
- `factory`：每次获取创建新实例。
- `viewModel`：按 ViewModel 生命周期提供实例。
- `get()`：获取模块中已经声明的依赖。

当前项目**还没有接入 Koin**。正式接入前要根据项目 Kotlin、Compose 和 Android Gradle Plugin 版本选择兼容的 Koin 版本，并同步修改依赖、Application、Manifest 和测试配置。

## 九、推荐实战顺序

### 练习 1：Java 数据类

新增一个 Java `LessonEntity`，包含 `name`、`title` 和 `summary`，由 Kotlin 页面读取并显示。

### 练习 2：Java Repository + Kotlin UI

用 Java 编写 `LessonRepository`，提供六种布局名称；Kotlin Compose 页面调用它并显示列表。

### 练习 3：Kotlin ViewModel

把首页 `page` 状态移动到 ViewModel，由 UI 发送 `OpenLesson` 事件。

### 练习 4：Koin

使用 Koin 提供 Repository 和 ViewModel，在测试中替换成 Fake Repository。

### 练习 5：网络与本地数据

在 Repository 层增加本地 JSON 或网络数据，UI 只观察状态，不直接处理数据请求。

## 十、每次开发的检查清单

- 先确认修改的是 Java、Kotlin、XML 还是 Gradle 文件。
- 新功能先画出 `UI → ViewModel → Repository` 调用链。
- XML 检查 `layout_width`、`layout_height`、`dp`、`sp` 和可访问性文本。
- Compose 检查状态是否放在合适的层级，避免无意义的重复状态。
- Java/Kotlin 混用时，接口和数据模型保持清晰，避免互相创建复杂对象。
- 修改依赖后等待 Gradle Sync，再运行单元测试和真机检查。
- 使用 Koin 前先确认依赖版本和初始化位置。

## 十一、启动与验证

在仓库根目录执行：

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
```

连接设备后可以安装并启动：

```powershell
.\gradlew.bat :app:installDebug
adb shell am start -n com.example.myapplication/.MainActivity
```

本教程中的命令是验证方法，不代表每次执行都已经通过。首次构建前需要完成 Gradle 依赖同步；没有缓存依赖时不要使用离线模式。

## 十二、学习资料

- [Java 官方学习](https://dev.java/learn/)
- [Kotlin 官方文档](https://kotlinlang.org/docs/home.html)
- [Kotlin 中文文档](https://book.kotlincn.net/text/d-language-reference.html)
- [Android 开发课程](https://developer.android.com/courses)
- [Compose 文档](https://developer.android.com/develop/ui/compose/documentation)
- [Koin 官方文档](https://insert-koin.io/docs/quickstart/android/)
