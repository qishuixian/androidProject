# Day 4：Android UI 细节与交互

目标：掌握 TextView、Button 的常用属性，理解尺寸单位和对齐方式，并能为传统 XML 控件添加点击事件和无障碍文本。

## 1. TextView：显示文本

```xml
<TextView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="欢迎学习 Android"
    android:textSize="20sp"
    android:textColor="#172033"
    android:gravity="center"
    android:padding="16dp" />
```

常用属性：

- `text`：显示内容。
- `textSize`：文字大小，建议使用 `sp`。
- `textColor`：文字颜色。
- `textStyle`：例如 `bold`。
- `gravity`：文字在 TextView 内部的对齐方式。
- `padding`：文字与控件边缘之间的内边距。
- `maxLines`、`ellipsize`：限制行数并显示省略号。

TextView 默认只显示内容，不负责处理用户输入；需要用户输入时使用 `EditText`。

## 2. Button：触发操作

当前项目的 [demo_button.xml](../app/src/main/res/layout/demo_button.xml) 包含两个 Button：一个可用，一个禁用。

```xml
<Button
    android:id="@+id/start_learning_button"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="开始学习"
    android:contentDescription="开始学习按钮" />
```

- `text`：按钮上显示的文字。
- `enabled`：是否允许点击；设为 `false` 时按钮禁用。
- `clickable`：是否响应点击。
- `id`：让代码可以找到该控件。
- `contentDescription`：给读屏服务提供描述。

按钮上的文字已经能表达操作时，通常不必重复设置相同的 `contentDescription`；图标按钮则必须提供有意义的描述。

## 3. `dp` 与 `sp`

- `dp`（density-independent pixel）：布局尺寸、边距和间距使用 `dp`。
- `sp`（scale-independent pixel）：文字大小使用 `sp`，会尊重用户的字体缩放设置。
- `px`：物理像素，不建议直接用于 Android UI。

```xml
android:layout_width="120dp"
android:layout_height="48dp"
android:textSize="16sp"
```

前端类比：`dp` 和 `sp` 都是适配不同设备的逻辑单位，但它们还受 Android 屏幕密度和用户字体设置影响，不能简单等同于 CSS `px`。

## 4. `gravity` 与 `layout_gravity`

`gravity` 控制控件**内部内容**的位置：

```xml
<TextView
    android:layout_width="200dp"
    android:layout_height="80dp"
    android:gravity="center"
    android:text="文字居中" />
```

`layout_gravity` 控制控件**在父布局中**的位置，且是否生效取决于父布局：

```xml
<FrameLayout ...>
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|end"
        android:text="右下角" />
</FrameLayout>
```

记忆方法：

```text
gravity        = 我里面的内容怎么放
layout_gravity = 我在父容器里面怎么放
```

## 5. 给 XML Button 添加点击事件

传统 View 页面中，先给布局设置内容，再找到按钮：

```kotlin
setContentView(R.layout.demo_button)

val button = findViewById<Button>(R.id.start_learning_button)
button.setOnClickListener {
    Toast.makeText(this, "开始学习", Toast.LENGTH_SHORT).show()
}
```

调用链是：

```text
用户点击 Button
  → OnClickListener.onClick
  → 执行 Lambda
  → 显示 Toast 或更新页面
```

当前项目的 `demo_button.xml` 是通过 `AndroidView` 加载到 Compose 详情页的，因此暂时只展示 Button 样式，没有在 `MainActivity` 中为它单独注册点击事件。

Compose Button 写法是：

```kotlin
Button(onClick = { /* 处理点击 */ }) {
    Text("开始学习")
}
```

XML View 使用 `setOnClickListener`；Compose 使用 `onClick` 参数。

## 6. 无障碍基础

UI 不只要“看得见”，还要让 TalkBack 等读屏服务能理解：

- 图片按钮提供 `contentDescription`。
- 文字控件内容要有意义，避免只写“点击这里”。
- 可点击区域要足够大，常见触控目标至少约 `48dp`。
- 不要只用颜色表达状态，同时提供文字或图标提示。
- 输入框配合清晰的标签和错误提示。

如果 Button 的可见文字已经是“开始学习”，读屏服务通常可以直接读出这段文字；图标没有文字时，必须设置描述。

## 7. 动手练习

### 练习 A：调整 TextView

打开 [demo_textview.xml](../app/src/main/res/layout/demo_textview.xml)：

1. 将标题的 `textSize` 改为 `28sp`。
2. 将标题的 `gravity` 从 `center` 改为 `start`。
3. 给说明文字增加 `android:maxLines="2"`。
4. 观察文字大小、对齐和截断效果。

### 练习 B：调整 Button

打开 [demo_button.xml](../app/src/main/res/layout/demo_button.xml)：

1. 将第一个按钮的文字改为“打开 TextView 课程”。
2. 将第二个按钮的 `enabled` 改为 `true`。
3. 给按钮增加 `android:layout_marginTop="16dp"`。
4. 思考两个按钮的点击事件应该放在哪一层。

### 练习 C：判断属性作用

判断下面属性属于哪一类：

```text
textSize       → 文字显示
padding        → 控件内部间距
layout_width   → 控件尺寸
gravity        → 内容对齐
layout_gravity → 子控件在父布局中的位置
```

### 练习 D：完成一次点击

创建一个传统 View 的临时 Activity，加载 `demo_button.xml`，使用 `findViewById` 找到 `start_learning_button`，点击后显示 Toast。

## 8. Day 4 完成标准

- 能解释 TextView 和 Button 的用途。
- 能正确选择 `dp` 和 `sp`。
- 能区分 `gravity` 与 `layout_gravity`。
- 能使用 `findViewById` 和 `setOnClickListener`。
- 能写出 Compose Button 的 `onClick`。
- 能为图标按钮提供无障碍描述。

## 9. 下一步

Day 5 学习页面状态与导航：Compose 状态、Activity 返回、ViewModel 的职责，以及把点击事件传给页面状态。
