# Day 28：分页与大列表

目标：理解分页数据源、加载更多、追加列表和“没有更多”状态，为后续接入 Android Paging 库建立基础。

## 1. 为什么需要分页

一次加载几万条数据会增加网络、内存和 UI 组合成本。分页把数据分成页面：首次加载第 1 页，用户需要时再加载下一页。

```text
第 1 页 → 追加第 2 页 → 追加第 3 页 → 没有更多
```

本例使用本地 `delay` 模拟数据源，保证演示不依赖网络。

## 2. 当前 Demo

源码位于 `app/src/main/java/com/example/myapplication/day28/PagingDemo.kt`。每页 5 条，最多 4 页；点击“加载下一页”后追加课程，加载中禁用按钮。

短列表为了适配本项目外层滚动容器使用普通 `Column`；真实长列表应使用 `LazyColumn`，并为每项提供稳定 key。使用 Paging 3 时通常是 `Pager`、`PagingSource`、`LazyPagingItems` 与 `items(key = ...)` 的组合。

## 3. Java 与 Kotlin 对照

Java 常把分页参数封装成 Page：

```java
record Page<T>(List<T> items, int page, boolean hasNext) {}
```

Kotlin 可以用数据类和不可变列表：

```kotlin
data class PagingState(
    val items: List<String> = emptyList(),
    val page: Int = 0,
    val loading: Boolean = false,
    val hasNext: Boolean = true
)
```

ViewModel 使用 `items + newItems` 创建新列表，不原地修改旧状态。真实接口还要处理页码、游标、重复数据、刷新和失败重试。

## 4. 与前几课的连接

Day27 的协程负责等待一页数据；Day20 的状态模型保存 items/loading/hasNext；Day19 的重组更新新增列表；Day21 的 Repository 应隐藏分页接口细节；Day25 的 Room 可以作为本地分页源。

## 5. 完成标准

- 能解释分页为什么减少首次加载成本。
- 能区分追加下一页、刷新和替换全部数据。
- 能处理 Loading、HasNext 和 End 状态。
- 能说明长列表为什么需要 LazyColumn 与稳定 key。
- 能描述 Android Paging 组件与当前手写 Demo 的对应关系。
