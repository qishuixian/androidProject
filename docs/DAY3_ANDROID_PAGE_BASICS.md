# Day 3：Android 页面基础与运行链路

目标：理解一个 Android 页面从启动到显示的过程，并能追踪当前项目“点击布局”后的代码调用链。

## 1. Android 项目的核心文件

当前项目的主要结构：

```text
app/
├── src/main/AndroidManifest.xml       应用与 Activity 声明
├── src/main/java/.../MainActivity.kt  页面入口
├── src/main/java/.../LayoutLessons.kt 学习数据
├── src/main/res/layout/               XML View 布局
├── src/main/res/values/               字符串、主题、颜色
└── app/build.gradle.kts                Android 配置与依赖
```

- `AndroidManifest.xml`：告诉系统有哪些 Activity，以及哪个 Activity 是启动入口。
- `MainActivity.kt`：Activity 的 Kotlin 实现。
- `res`：不直接写业务逻辑的资源，例如布局、图标、字符串和主题。
- `R`：Android 为资源生成的 Kotlin/Java 引用类，例如 `R.layout.demo_textview`。

## 2. Activity 是什么

Activity 可以理解为一个 Android 页面容器。系统创建它、恢复它、暂停它或销毁它，都会调用对应生命周期方法。

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                LayoutLearningApp()
            }
        }
    }
}
```

这里的执行顺序是：

1. 系统根据 Manifest 启动 `MainActivity`。
2. Android 调用 `onCreate`。
3. `super.onCreate` 让父类完成初始化。
4. `setContent` 设置 Compose 内容。
5. `MyApplicationTheme` 提供主题。
6. `LayoutLearningApp` 构建页面。

## 3. Activity 生命周期

```text
onCreate → onStart → onResume
                         ↓ 页面可交互
onPause → onStop → onDestroy
```

常见含义：

- `onCreate`：首次创建或重新创建页面，初始化内容。
- `onStart`：页面进入可见状态。
- `onResume`：页面进入前台，可以交互。
- `onPause`：页面暂时失去焦点。
- `onStop`：页面不可见。
- `onDestroy`：页面实例被销毁。

初学阶段把一次性初始化放到 `onCreate`。不要把长时间网络请求直接写在主线程中；后续用 ViewModel、协程或其他异步方案处理。

## 4. Manifest 如何找到入口

当前 Manifest 中的入口配置：

```xml
<activity
    android:name=".MainActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

- `android:name=".MainActivity"` 对应 `com.example.myapplication.MainActivity`。
- `MAIN` 表示主入口动作。
- `LAUNCHER` 表示可以从桌面启动。
- `exported="true"` 允许系统启动这个带入口过滤器的 Activity。

## 5. XML View 与 Compose 的关系

传统 XML 页面：

```kotlin
setContentView(R.layout.activity_main)
val title = findViewById<TextView>(R.id.title)
title.text = "欢迎"
```

XML 负责描述 View 树，Activity 通过 `findViewById` 找控件。

Compose 页面：

```kotlin
setContent {
    Text("欢迎")
}
```

Compose 直接用 Kotlin 描述 UI，不需要 XML 文件和 `findViewById`。

当前项目采用混合方式：

- 页面外壳：Compose。
- 六种传统布局示例：XML。
- `AndroidView`：把 XML View 加载到 Compose 中。

```kotlin
AndroidView(
    factory = { context ->
        LayoutInflater.from(context).inflate(lesson.resource, null, false)
    }
)
```

这段代码先从 `lesson.resource` 得到布局资源，再通过 `LayoutInflater` 创建 View，最后显示在 Compose 页面里。

## 6. 当前项目的点击调用链

从首页进入布局列表：

```text
MainActivity.onCreate
  → setContent
  → LayoutLearningApp
  → page 初始值为 "home"
  → 显示 LearningCard("布局", ...)
  → 用户点击卡片
  → page = "layouts"
  → lesson == null
  → 显示六种布局列表
```

点击某一种布局，例如 TextView：

```text
点击 TextView 卡片
  → page = "TextView"
  → layoutLessons.firstOrNull { it.name == page }
  → 找到 LayoutLesson
  → 显示 lesson.summary、analogy、explanation
  → AndroidView 加载 R.layout.demo_textview
```

返回时：

```text
点击顶部返回 / 系统返回键
  → BackHandler 执行 back
  → 详情页返回 layouts
  → 布局列表返回 home
```

这里的页面切换是在一个 Activity 内修改 Compose 状态，没有创建多个 Activity。

## 7. 资源与 R 引用

文件名会映射为资源引用：

```text
app/src/main/res/layout/demo_textview.xml
                         ↓
R.layout.demo_textview
```

资源命名通常使用小写字母、数字和下划线。XML 中可以引用系统资源：

```xml
android:textColor="@android:color/black"
```

应用自己的字符串应放在 `res/values/strings.xml`，便于多语言和统一维护。

## 8. 今日练习

### 练习 A：追踪启动入口

打开 [AndroidManifest.xml](../app/src/main/AndroidManifest.xml)，回答：

1. 哪个 Activity 带有 `MAIN` 和 `LAUNCHER`？
2. 它的完整类名是什么？
3. Android 为什么能找到 `MainActivity`？

### 练习 B：追踪布局入口

在 [MainActivity.kt](../app/src/main/java/com/example/myapplication/MainActivity.kt) 中找到：

1. `page` 的初始值。
2. 首页“布局”卡片的点击赋值。
3. 六种布局卡片的 `page` 赋值。
4. `AndroidView` 如何使用 `lesson.resource`。

### 练习 C：新增一个 XML 示例

1. 复制 `demo_textview.xml`，命名为 `demo_button.xml`。
2. 将根节点和内容修改为 Button 示例。
3. 在 `LayoutLessons.kt` 增加一个 `LayoutLesson`。
4. 指向 `R.layout.demo_button`。
5. 运行 App，确认列表和详情页都能显示。

## 9. Day 3 完成标准

- 能说明 Manifest、Activity、资源目录和 `R` 的作用。
- 能说出 Activity 的主要生命周期顺序。
- 能区分 XML View 与 Compose 的页面写法。
- 能完整复述当前项目从启动到点击布局详情的调用链。
- 能新增一个 XML 资源并通过 `R.layout` 加载。

## 10. 下一步

Day 4 学习 Android UI 细节：TextView、Button、尺寸单位、`gravity` 与 `layout_gravity`、点击事件和无障碍文本。
