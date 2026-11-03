# Day 9～Day 30：Java、Kotlin 与 Android 提升路线

这份路线承接 Day7 的协程与异步加载、Day8 的列表渲染与状态提升。你已有十年前端经验，因此不重复 HTML、CSS、JavaScript 基础，而是用前端已有经验类比 Android 概念，重点补齐 Java、Kotlin 和 Android 实战能力。

## 提升规则

每一天只增加一个核心概念，并完成一个可运行的小改动：

1. 先用已有前端经验理解概念。
2. 对照 Java 写法，再学习 Kotlin 写法。
3. 接入当前 Android App，保留可点击、可观察、可验证的结果。
4. 在前一天代码上增加能力，不推翻已有结构。

| 天数 | 核心提升 | Java / Kotlin | Android 实践 |
|---|---|---|---|
| Day 9 | 列表数据建模 | Java 类、Kotlin `data class` | 将课程字符串改为课程对象 |
| Day 10 | 稳定身份与列表 key | `equals`、`hashCode`、Kotlin key | 排序、删除后保持项身份 |
| Day 11 | 用户事件与回调 | 接口、函数类型 | 列表项点击、删除、重试 |
| Day 12 | 空值安全 | Java 判空、Kotlin `?.` | 可选课程详情与空状态 |
| Day 13 | 集合转换 | Java Stream、Kotlin `map/filter` | 搜索和筛选课程 |
| Day 14 | sealed 状态 | Java 枚举/类、Kotlin sealed class | Idle、Loading、Success、Error |
| Day 15 | ViewModel 状态 | Java 生命周期思路、Kotlin StateFlow | 列表状态从 UI 提升到 ViewModel |
| Day 16 | 单向数据流 | 接口与不可变数据 | State、Event、Effect 分离 |
| Day 17 | 页面生命周期 | Activity/Fragment Java API | 观察创建、销毁与重建 |
| Day 18 | 配置变化与状态恢复 | Bundle、保存状态 | 旋转屏幕保留列表和选中项 |
| Day 19 | Compose 重组 | Java 对比、Kotlin 稳定性 | 观察哪些组件重新组合 |
| Day 20 | UI 状态与业务状态 | DTO、Entity 基础 | 区分页面状态和课程数据 |
| Day 21 | Repository 分层 | Java 接口实现 | UI、ViewModel、Repository 分层 |
| Day 22 | 依赖注入深化 | 构造方法、接口 | Koin 注入真实 Repository |
| Day 23 | Retrofit 请求 | Java 回调对比、Kotlin suspend | 获取课程列表 |
| Day 24 | 网络错误处理 | 异常类型、Result | 网络失败、空数据、重试 |
| Day 25 | Room 本地数据库 | Java 注解模型、Kotlin Entity | 保存和读取课程 |
| Day 26 | 缓存与离线优先 | 数据层接口 | 本地数据优先、网络刷新 |
| Day 27 | 协程并发 | Thread、Executor、Coroutine | 并行加载课程与详情 |
| Day 28 | Paging 与大列表 | Java 分页模型、Kotlin Flow | 分页加载与加载更多 |
| Day 29 | 测试与调试 | JUnit、Mock 思路 | ViewModel、Repository、Compose 测试 |
| Day 30 | 综合项目 | Java/Kotlin 代码边界 | 完成课程列表、详情、刷新、缓存 |

## 与 Day7、Day8 的直接衔接

- **Day7 已学**：Repository、ViewModel、协程、Loading/Success/Error、StateFlow。
- **Day8 已学**：列表渲染、父组件持有选中状态、子组件通过回调上报事件。
- **Day9 开始**：把 Day8 的字符串列表提升为类型明确的课程对象。
- **后续提升**：把 UI 状态逐步提升到 ViewModel，再接入 Repository、网络和本地数据库。

不再安排 HTML、CSS、JavaScript 入门课程；前端经验只用于帮助理解状态、组件、事件和异步之间的对应关系。
