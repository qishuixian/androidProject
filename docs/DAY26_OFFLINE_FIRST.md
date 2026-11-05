# Day 26：离线优先与缓存

目标：把 Day23 网络请求、Day24 错误处理和 Day25 Room 串起来，保证刷新时旧内容仍可见，网络失败不清空缓存。

## 1. 前端类比

类似页面先显示持久化缓存，再重新验证远端数据。这里不是同时让 UI 管理网络列表与本地列表，而是让 Room 成为列表的唯一读取来源。

```text
Room → Flow → ViewModel → UI
网络 → DTO → Entity → Room 事务写入 → Flow 通知 → UI
```

启动时本地观察与刷新并行启动，不保证二者完成顺序，但 UI 的课程始终来自 Room。离线优先不代表离线时凭空拥有数据，首次安装没有缓存且断网时只能展示空态和错误。

## 2. Repository 的两项职责

```kotlin
val courses = dao.observeAll()

suspend fun refresh() {
    val rows = api.load(3).map {
        CourseEntity(it.id.toString(), it.title ?: "无标题", it.completed)
    }
    dao.replaceCache(rows)
}
```

courses 持续提供本地查询结果；refresh 更新数据库，不直接向 UI 返回另一份列表。网络请求或转换失败时，代码根本不会执行 replaceCache，所以旧数据还在。

## 3. 为什么需要事务

```kotlin
@Transaction
suspend fun replaceCache(courses: List<CourseEntity>) {
    clear()
    insertInitial(courses)
}
```

清空与插入属于一次事务：插入失败会回滚，不留下半份缓存。正常网络空列表视为权威空结果，会清空旧缓存；它与请求失败不同。

Java 对照（概念示意，需在后台执行同步 DAO）：

```java
database.runInTransaction(() -> {
    dao.clearBlocking();
    dao.insertBlocking(rows);
});
```

Java 的事务回调与 Kotlin 的 @Transaction 都表达原子操作。本例使用 Room 的挂起 DAO，由 Room 管理异步执行；不在事务内等待 HTTP 请求，避免长时间占用数据库事务。

## 4. 页面状态与生命周期

reading 表示本地首次读取，refreshing 表示远端刷新，两者可以同时发生。刷新失败仅更新 refreshMessage，不覆盖 courses。读取数据库失败使用独立 readError，不能把“读库失败”误称为“断网”。

ViewModel 创建时自动刷新一次。当前作用域为 Activity，返回首页再进入可能复用实例，不保证再次执行 init，可点击刷新按钮。配置变化通常保留 ViewModel；进程重启重新读取磁盘并刷新。收集停止不等于网络任务取消，ViewModel 清理时 viewModelScope 才取消。

重复刷新被禁用并有方法内保护；CancellationException 继续抛出。其他错误复用 Day24 分类函数，缓存写入异常也会显示刷新失败，不能由此推断一定是网络故障。

## 5. 本例的数据策略

- 使用公共 JSONPlaceholder `/todos?_limit=3`，返回待办用于教学，不是真实课程接口。
- 新数据库 `day26-cache.db`，不修改 Day25 的课程完成记录。
- 此缓存按远端整份替换，远端删除的条目会消失；它是限定三条请求的缓存，不是完整服务器数据。
- Day26 不提供本地编辑或上传，因此不解决离线写入冲突；双向同步需要待上传队列、版本或时间戳及冲突策略。
- 没有过期时间、后台自动刷新、自动重试；启动与手动刷新是当前明确的刷新时机。
- 没有改表结构，只给原 DAO 增加方法，所以不需要提升数据库版本。

## 6. 代码入口

- [OfflineRepository.kt](../app/src/main/java/com/example/myapplication/day26/OfflineRepository.kt)：本地观察、网络刷新和 DTO 转换。
- [OfflineDemo.kt](../app/src/main/java/com/example/myapplication/day26/OfflineDemo.kt)：依赖组装、状态管理、页面。
- [CourseDatabase.kt](../app/src/main/java/com/example/myapplication/day25/CourseDatabase.kt)：复用表定义，新增事务缓存替换。

本课为了聚焦缓存，UI 直接展示只读 Entity；复杂项目可像 Day25 一样在 Repository 中转换成业务模型，避免存储字段变化影响 UI。

## 7. 验证步骤

1. 联网打开 Day26，等待网络数据写入缓存并显示标题。
2. 关闭网络，点击刷新：应保留课程并提示失败。
3. 保持断网，强制停止后重新打开 App：Day26 应读出磁盘缓存，随后网络刷新失败但内容仍在。
4. 恢复网络，手动刷新：成功后错误提示被成功消息替代。
5. 返回 Day25，确认原有完成情况不受 Day26 刷新影响。

这些是设备验收步骤，不代表已实机验证。不要通过清除整个应用数据来模拟普通断网，那会同时删除所有本地数据库。

## 8. 完成标准

- 能说明为什么 UI 只观察数据库，而不是在两份列表之间切换。
- 能解释网络失败保留缓存与网络成功返回空列表的区别。
- 能说明 clear + insert 为什么要处于同一个事务。
- 能区分刷新状态、缓存读取状态和已有列表内容。
- 能追踪网络结果写入 Room 再触发 Compose 更新的完整链路。
