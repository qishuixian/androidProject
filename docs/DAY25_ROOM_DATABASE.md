# Day 25：Room 本地数据库

目标：把 Day20 只保存在内存中的课程完成情况写到磁盘，理解 Entity、DAO、Database、Repository 和 UI 的协作。

## 1. 前端类比

Room 是 SQLite 的抽象层，可以类比围绕浏览器 IndexedDB 的类型化数据访问封装，但运行在 Android 上。它不是网络请求库，也不是 StateFlow；StateFlow 通知页面，数据库保存数据。

## 2. 三个核心概念

| 概念 | 职责 | 本例 |
|---|---|---|
| Entity | 定义表与字段 | CourseEntity，主键 id、title、completed |
| DAO | 定义 SQL 查询和写入 | CourseDao |
| Database | 数据库配置及 DAO 入口 | CourseDatabase，版本 1 |

```kotlin
@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val completed: Boolean = false
)
```

@Entity 是注解，告诉 Room 这是表模型；@PrimaryKey 指定唯一身份。它不等于网络 DTO。Repository 把 CourseEntity 转成之前的业务模型 Course，UI 不需要处理 SQL。

## 3. Java 与 Kotlin DAO

Java 的同步查询示意：

```java
@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY id")
    List<CourseEntity> loadAll();
}
```

同步方法需要在后台执行，不能直接在主线程调用。Java 也可以选择 LiveData 等可观察返回类型。

Kotlin 本例：

```kotlin
@Query("SELECT * FROM courses ORDER BY id")
fun observeAll(): Flow<List<CourseEntity>>

@Query("UPDATE courses SET completed = NOT completed WHERE id = :id")
suspend fun toggle(id: String)
```

`:id` 是绑定参数，不是手动拼接 SQL。Flow 查询在收集时读取数据，相关表变更后可以重新查询并发出结果；它不是数据库逐行变更事件。Room 的 suspend/Flow API 管理异步数据库执行，不需要开启 allowMainThreadQueries。

## 4. KSP 与生成代码

Room compiler 使用 KSP 处理注解，编译时检查 SQL 和生成 Database/DAO 实现。你写接口，Room 生成执行 SQL 的实现；它和 Retrofit 根据 HTTP 注解提供实现的思路类似，但机制不同。

依赖在 app/build.gradle.kts 中注册 Room runtime、ktx、compiler 和 KSP。当前项目使用 AGP 内置 Kotlin，KSP 必须兼容该模式。

## 5. 本例初始化与保存策略

数据库文件名为 `day25-courses.db`，使用 applicationContext 构造并在进程内复用数据库实例。

每次 ViewModel 创建时尝试插入三门固定课程，但使用 `OnConflictStrategy.IGNORE`：已有主键不会被覆盖，所以不会把已完成状态重置。不要为了初始化方便每次删库重建。

点击按钮直接执行 SQL 翻转 completed，不依赖 UI 中可能过时的旧值。Room 查询流重新发出列表后，ViewModel 更新 StateFlow，界面显示数据库中的值。

```text
点击 → ViewModel.toggle(id) → Repository → DAO UPDATE → SQLite
    → Room Flow 查询 → Entity 转 Course → StateFlow → Compose 重组
```

示例启动读取与写入异常分别展示提示；协程取消继续抛出。写入期间禁用按钮，避免连续点击产生误解。没有网络请求，也没有把本地数据上传。

## 6. 代码入口

- [CourseDatabase.kt](../app/src/main/java/com/example/myapplication/day25/CourseDatabase.kt)：表、DAO、数据库与单例提供者。
- [StoredCourseRepository.kt](../app/src/main/java/com/example/myapplication/day25/StoredCourseRepository.kt)：初始化、查询映射、写操作。
- [RoomDemo.kt](../app/src/main/java/com/example/myapplication/day25/RoomDemo.kt)：ViewModel、Factory、页面。

本例新建数据库专用 Repository，因为“持续观察+写入”的契约与 Day21 的一次性 loadCourses 不同，不强行复用不合适的接口。

## 7. 持久化边界与迁移

Activity 重建或进程结束不会自动删除数据库。重新启动后重新查询，读取的是磁盘内容，不是恢复旧 ViewModel。

清除应用数据或卸载会删除本地文件；设备备份可能在重新安装后恢复数据，因此不应把卸载作为永久删除所有备份的保证。此 Demo 没有云同步或账号隔离。

改变表结构时需要提升数据库版本并提供 Migration。本课版本为 1，未配置破坏性迁移。教学示例关闭 schema 导出；正式维护数据库应导出并提交 schema、编写迁移测试，不能用删库掩盖升级问题。

## 8. 操作验证

1. 进入首页 Day25，首次应显示三门未完成课程。
2. 将 Button 标记完成，等待界面显示“已完成”。
3. 返回首页再进入，完成状态仍在。
4. 强制停止 App 后重新启动、进入 Day25，确认 Button 仍已完成；这一步才能区别于只在内存中保存。
5. 断网重复操作，数据库功能不依赖网络。
6. 如需清除数据，注意系统清除应用数据会影响全部课程示例状态，不是只清 Day25。

这些是设备验收步骤，不代表已完成设备验证。

## 9. 完成标准

- 能解释 Entity、DAO、Database 各自的职责。
- 能追踪 SQL 写入到 Flow 再到 UI 更新的链路。
- 能解释 IGNORE 为什么不会覆盖已有完成状态。
- 能区分 StateFlow 内存状态与 Room 磁盘持久化。
- 能说明为什么数据库结构变更需要迁移。
