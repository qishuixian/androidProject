# Day68 · 多语言测试

入口：首页 → Day68 · 多语言测试。
源码：[LocalizationTestDemo.kt](../app/src/main/java/com/example/myapplication/day68/LocalizationTestDemo.kt)。

Day67 解决语言职责边界，本课解决“翻译后页面是否仍然可用”。页面用三种 Locale、固定日期和固定数值反复观察：手机宽度、字体放大、长文案溢出、日期顺序、数字分组和货币符号。

## 测试不是把中文换成英文

多语言验收至少包含两组变量：

- 内容变量：语言、字形、地区、长短文案、复数形式。
- 设备变量：屏幕宽度、方向、fontScale、系统栏、输入法和实际设备。

同一字符串在英文、简体和繁体下长度可能不同；相同数值在不同 Locale 下分组符号、小数分隔符、货币符号和日期顺序也可能不同。资源读取正确不代表布局正确。

本课切换的是格式化 Locale，不修改整个 App 的 Android Resources；系统显示大小/字体大小变化需要在设备设置中单独验证。示例会显示实际设备宽度、fontScale 和系统语言，方便记录复现条件。

## 长文案与溢出

标题区域不能只按中文宽度设计。常见策略有：

- 多行：允许高度增长，适合课程描述和卡片正文。
- 单行省略：`maxLines = 1` 与 `TextOverflow.Ellipsis`，适合固定高度标题栏。
- 缩小字号：只在设计明确允许时使用，避免可读性下降。
- 滚动或展开：适合必须完整显示的内容。

本课按钮在最多三行与单行省略之间切换，边框帮助观察实际占用区域，最大宽度限制为 320dp。局部窄布局不等于真实手机，仍需真机检查窗口、系统栏、字体和滚动。它没有假装一种策略适合所有产品页面；生产要依据设计、无障碍和可点击区域验收。

Compose 的 Text 默认可能因父布局约束而重新测量；不要只在宽屏模拟器上看一次。传统 XML 中同样要检查 `maxLines`、`ellipsize`、`wrap_content`、权重和最小高度。长字词、德语复合词、阿拉伯语方向与 CJK 字体还可能暴露额外问题，不能把三种语言实验当作完整覆盖。

## 日期和数字必须用 Locale 格式化

代码使用 `DateFormat.getDateInstance(DateFormat.LONG, locale)` 与 `NumberFormat`：

```kotlin
// 同一个瞬间由 Locale 决定展示顺序和月份文字。
val date = DateFormat.getDateInstance(DateFormat.LONG, locale).format(sampleDate)
// 不手写逗号、小数点和货币符号。
val number = NumberFormat.getNumberInstance(locale).format(1234567.89)
val money = NumberFormat.getCurrencyInstance(locale).format(1234567.89)
```

业务层应保存原始时间戳/Instant、整数或 Decimal 数值，传输也使用结构化字段；展示层根据目标语言、地区和时区格式化。不要把“2026年…”或“1,234.56”这样的展示字符串当接口值，也不要用字符串替换逗号来实现国际化。

本课固定时间戳与数值，避免每次重组产生新结果。实际日期还受时区影响；这里的 DateFormat 使用设备默认时区，正式报表应明确业务时区后再格式化。货币 Locale 表示展示习惯，不一定等于用户账户的结算币种；业务货币应有独立 currency code。

## 语言切换和资源测试的边界

本课的 `localeTag` 只用于 `DateFormat`、`NumberFormat` 和测试长文案。Day66 的局部资源 Context 才是观察 Android 资源 fallback 的入口，Day67 的 appLanguage/reportLanguage 才是业务语言边界。不要把本页的选择按钮当作完整 App 语言切换实现。

如果想测试资源 Context 与格式化同时变化，需要固定目标 Configuration，把它传给资源查询和格式化 API；不能让 Text 从一个 Locale 来，数字又偷偷使用系统 Locale，然后把差异归因给资源回退。

## 验收矩阵

| 条件 | 检查点 |
|---|---|
| zh-CN / zh-HK / en-US | 长文案是否换行/省略，日期和数字是否合理 |
| 手机窄宽度 | 按钮、标题、边框是否被裁剪，是否需要滚动 |
| fontScale 放大 | 文字不遮挡，按钮仍可点击，页面能滚动 |
| 横竖屏 | 固定高度区域是否仍可读，是否错误缓存尺寸 |
| 单行省略 / 三行 | 省略号是否可见，重要信息是否被截断 |
| DateFormat / NumberFormat | 不手写分隔符，记录 Locale 与时区 |

每次记录设备型号、系统版本、屏幕宽度 dp、fontScale、Locale、方向和构建变体。仅凭源码或宽屏截图不能宣称手机布局已通过。

## 常见错误

- 只测试中文：英文、繁体或长词把按钮推到屏外。
- 直接拼接日期数字：忽略地区、时区、货币和小数规则。
- 用 `String.format(Locale.getDefault(), ...)` 偷读系统 Locale：业务报告有明确语言时应传入目标 Locale。
- 用 `remember` 缓存已格式化字符串：配置或测试 Locale 改变后继续显示旧值。
- 把 `maxLines=1` 当成完整可读性策略：省略可能隐藏关键信息。
- 把设备截图当作所有手机结论：尺寸、字体、系统栏和字体渲染都不同。

## 学完应能说明

1. 为什么翻译资源通过仍可能在手机上溢出。
2. 为什么日期、数字、货币要用目标 Locale 格式化。
3. 为什么业务保存原始值，展示层再格式化。
4. 如何记录设备条件并复现多语言布局问题。
5. 如何区分 Day66 的资源 fallback、Day67 的业务语言和 Day68 的测试变量。
