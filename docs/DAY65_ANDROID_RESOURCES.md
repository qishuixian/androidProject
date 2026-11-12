# Day65 · Android 资源

入口：首页 → Day65 · Android 资源。
源码：[AndroidResourcesDemo.kt](../app/src/main/java/com/example/myapplication/day65/AndroidResourcesDemo.kt)。

接 Day39：已经认识 source set 合并。本课把“资源定义 → 生成 R → XML/Kotlin/Compose 读取 → UI 显示”完整串起来。Day66 再专门学习语言、地区限定符与回退。本课不实现应用内语言切换，不修改已有课程文案。

## 从前端概念理解

可以把字符串资源类比为 i18n 文案字典：key 是 day65_course_title，值是“Android 资源课程”。XML、Kotlin、Compose 都可以引用这个 key，不必复制一遍文字。

区别是 Android 在构建时编译资源并生成 R 的符号，运行时再根据当前 Resources 配置选值。R.string 不是 JavaScript 对象中直接存储的字符串，也不决定依赖哪个模块或使用哪种语言。

## 文件位置与职责

| 文件 | 职责 |
|---|---|
| app/src/main/res/values/day65_strings.xml | 默认文案、参数模板和 plurals |
| app/src/main/res/layout/day65_resource_panel.xml | 用 @string 设置传统 TextView 标题 |
| app/src/demo/res/values/day65_strings.xml | demo Flavor 对来源文案的覆盖 |
| app/src/prod/res/values/day65_strings.xml | prod Flavor 对来源文案的覆盖 |
| course-core/src/main/res/values/day65_strings.xml | Library 独有文案与可被 app 覆盖的默认文案 |

values 下的 XML 文件可以叫 strings.xml，也可以按功能叫 day65_strings.xml。最终引用按资源类型和 name 生成，不会因为文件名改变而变成 R.day65_strings。layout 则以布局文件名生成 R.layout.day65_resource_panel。不要把两种规则混为一谈。

## 从定义到显示的完整链路

```text
<string name="day65_course_title">Android 资源课程</string>
 → Gradle 选择当前变体的 source set 和依赖库
 → Android 资源工具合并、编译、链接资源
 → 生成/关联 R.string.day65_course_title
 → 资源表及配置化条目进入 APK
 → 当前 UI Context/Resources 按配置查找值
 → XML TextView、Compose Text 或 Toast 显示
```

具体构建任务由工具版本组织，不能把 import R 当成生成或配置动作。修改资源后需要资源构建成功；缺少 R 字段应先核对名字、source set、namespace、依赖和前面的资源编译错误，而不是手写一个 R。

## 三种读取写法

### XML：声明控件属性

```xml
<!-- 布局解析时根据当前 Context 的资源配置读取。 -->
android:text="@string/day65_course_title"
```

@string 表示字符串资源类型，不是某个 Kotlin 变量，也不是名为 string 的文件夹。布局里的 @string 可以来自最终资源集合；本课恰好引用 app 自己定义的条目。

### Kotlin：普通代码查询

```kotlin
// R.string 字段是资源 id，不是最终标题字符串。
val title = context.getString(R.string.day65_course_title)
```

用于事件、Toast、传统 View 等普通 Kotlin/Java 代码。Java 对应 context.getString(R.string.day65_course_title)，资源查询链相同，语法不同。

### Compose：组合中查询

```kotlin
// 在 Composable 中读取，配合当前 UI 配置参与更新。
val title = stringResource(R.string.day65_course_title)
// 再把字符串交给 Text 显示。
Text(title)
```

stringResource 是 Composable 函数，不能直接在 onClick 这种普通回调中调用。可以提前读取，或像本课 Toast 那样在事件发生时使用当前 Context.getString。

不要用无 key 的 remember 缓存已经解析好的文案，否则配置变化后可能保留旧字符串。缓存资源 id 和缓存当前语言文案不是一件事。

## R 的名字、id 与模块

页面显示 resources.getResourceName(id) 和十六进制 id，帮助区分：

- day65_course_title：声明名称。
- R.string.day65_course_title：代码中的资源符号。
- 0x...：当前 APK 的资源数字 id，不能当稳定业务编号持久化或通过接口约定。
- getString(id)：从当前资源配置解析出的实际文案。

库使用自己的 namespace：com.example.coursecore.R，本课用 CoreR 别名与 app.R 区分。alias 只处理 Kotlin 名称冲突，不会复制资源或改变合并优先级。

app 通过现有 implementation(project(":course-core")) 依赖该库，构建才能链接资源。非传递 R 的工程不要假设库独有条目一定也暴露在 app.R 中；库资源使用库自己的 R。

## 参数模板不是拼接

```xml
<!-- %1$s 是第一个字符串参数，%2$d 是第二个整数参数。 -->
<string name="day65_progress">课程：%1$s；点击次数：%2$d</string>
```

```kotlin
// 参数顺序与类型必须和模板对应。
Text(stringResource(R.string.day65_progress, title, count))
```

相比在代码里把多个碎片拼接，完整句子模板更便于以后翻译和调整语序。参数缺失、类型不匹配可能在构建检查或运行格式化时失败，不把它当网络或 Compose 重组问题。

## 数量文案为什么传两次 count

```kotlin
// 第一个 count 选择数量类别，第二个 count 替换模板中的 %d。
pluralStringResource(R.plurals.day65_clicks, count, count)
```

quantity 参数不会自动替换模板占位符。one/other 是当前语言的数量规则，不是代码中的 if(count == 1)。中文通常使用 other，即使 count=1 也不保证选到 one；本课默认中文的两个模板特意显示同样形式，不能凭句子外观断定选中了哪个类别。Day66 再深入语言选择。

## 资源合并到底发生在哪里

对同类型、同名称、同限定配置资源，本工程的单 Flavor 维度可按以下优先级理解：

```text
完整变体 > BuildType > Flavor > main > Library
```

Day39 已提供完整优先级实验；Day65 不再复制所有层，只展示 Flavor 与 Library：

- demoDebug 的 day65_source 来自 app/src/demo/res/values。
- prodDebug 的 day65_source 来自 app/src/prod/res/values。
- app/main 的 day65_shared_label 覆盖 Library 的同名默认条目。
- 即使用 CoreR.string.day65_shared_label 读取，运行时也解析最终覆盖值，库的 R 不是独立资源沙箱。
- day65_library_only 没有被 app 覆盖，保留库自身值。

不同限定配置不是简单互相抹掉。例如 main 的 values 与 values-zh 可以一起进入最终资源集合。构建合并后，运行时还要按设备或应用配置做匹配。多 Flavor 维度还受维度优先级影响，不能无限套用本课单维度简图。

## 语言与回退先建立完整认识

资源读取链不止 R：先由 Gradle 依赖和 source set 形成最终资源表，再由当前 Resources 的 locale/其他配置选择匹配条目，缺少适合的语言条目时查找可用回退，默认 values 必须提供必要资源。

系统语言、应用语言配置和 Context 的配置可能影响最终选择。import 哪一个 R 不是选择语言的开关；values-zh 等目录也不是另一个 BuildType。具体地区匹配和回退顺序在 Day66 通过实验验证。

## 点击按钮的完整过程

```text
点击“增加次数并显示资源 Toast”
 → count++
 → Context.getString 按新次数格式化 Toast
 → Toast.show
 → Compose 读取变化后的 count
 → 参数文案和数量文案更新
 → Flavor/Library 合并结果不变
```

count 用 rememberSaveable 恢复交互数据，资源值仍从当前 Resources 读取。重置 count 不切换构建环境。安装 demoDebug 与 prodDebug 是两份 APK 对照，不是页面内运行时切换 Flavor。

## 验收

1. 进入页面，XML TextView 和 Compose 显示同一个课程标题。
2. 点击增加，参数文案、数量文案、Toast 中次数一致；重置回到 0。
3. 旋转后次数可恢复，不把资源文本本身保存为业务状态。
4. 分别安装 demoDebug/prodDebug，核对 BuildConfig 与 day65_source 对应的 Flavor。
5. 库独有文案能显示，共享文案显示 app 覆盖值。
6. 能解释 values XML 文件名不决定 R.string 字段名，而 layout 文件名决定布局字段名。
7. 能完整追踪定义、依赖、构建合并、配置匹配与 UI 读取，区分两阶段。

## 常见错误

- R 未生成：先看资源编译错误与命名，不要手写 R。
- 找不到库资源：核对 namespace、Gradle 依赖和非传递 R。
- 显示旧文案：核对实际安装包、变体、当前配置及是否缓存了解析值。
- XML 特殊字符：例如 & 要写成 &amp;，不要把 XML 转义与 Kotlin 字符串转义混用。
- 默认资源缺失：不要假设所有设备一定匹配某个语言目录。
