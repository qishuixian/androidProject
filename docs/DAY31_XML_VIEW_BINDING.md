# Day 31：XML、ViewBinding 与 Context

目标：开始传统 Android 页面体系，理解 XML 布局、ViewBinding 和 Context，并把 XML View 嵌入当前 Compose 页面。

## 1. XML 布局

XML 描述 View 树，类似前端模板描述页面结构。它描述初始结构和属性，点击逻辑仍在 Kotlin/Java 中设置。

## 2. ViewBinding

开启 `viewBinding` 后，`day31_view_binding.xml` 会生成 `Day31ViewBinding`：

```kotlin
val binding = Day31ViewBinding.inflate(LayoutInflater.from(context))
binding.courseTitle.text = "标题"
binding.actionButton.setOnClickListener { }
```

这比 `findViewById` 更安全。ViewBinding 不负责业务状态，也不会替代 ViewModel。

Java 对照：

```java
Day31ViewBinding binding = Day31ViewBinding.inflate(getLayoutInflater());
binding.courseTitle.setText("标题");
```

## 3. Context

Context 是访问 Android 环境能力的入口。当前示例用它创建 XML View、显示 Toast、访问资源和主题。不要把 Activity Context 长期保存到 Repository 或单例中，避免泄漏；长生命周期对象通常使用 applicationContext。

## 4. 当前 Demo

入口：首页 → Day31。源码是 `XmlViewBindingDemo.kt`，布局是 `res/layout/day31_view_binding.xml`。`AndroidView` 将传统 XML View 放进 Compose，按钮直接修改绑定的 TextView。

## 5. 完成标准

- 能解释 XML、View、ViewBinding 三者关系。
- 能写出 Java 和 Kotlin 的 ViewBinding 访问方式。
- 能说明 Context 在创建 View 和访问资源中的作用。
- 能区分 `findViewById`、ViewBinding 和 Compose 状态。
- 能从 XML id 追踪到 binding 属性和点击回调。
