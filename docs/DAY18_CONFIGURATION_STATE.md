# Day 18：配置变化与状态恢复

目标：承接 Day17 生命周期日志，把 Day15 的简单状态保存用于真实的课程筛选和选择，并通过对照实验验证恢复边界。

## 1. 前端类比

可把 Activity 重建理解为界面实例重新创建，而系统额外提供了一份小型恢复快照。它不是 localStorage：rememberSaveable 不承诺应用被主动关闭后仍永久保留数据。

## 2. Java：Bundle 保存与读取

传统 Activity 可以这样保存选中 id（示意，放在 Activity 类内）：

```java
private String selectedId;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState != null) {
        selectedId = savedInstanceState.getString("selectedId");
    }
}

@Override
protected void onSaveInstanceState(Bundle outState) {
    outState.putString("selectedId", selectedId);
    super.onSaveInstanceState(outState);
}
```

Bundle 是 Android 的键值状态容器，支持有限的数据类型。保存回调不等于 onDestroy；不要等销毁才尝试保存，也不能依赖进程终止时一定执行回调。

## 3. Kotlin：rememberSaveable

```kotlin
var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
val selected = courses.firstOrNull { it.id == selectedId }
```

rememberSaveable 通过 Compose 保存状态机制接入宿主恢复流程，不需要为每个字段手写 Activity 回调。本例保存 String、Int 等支持的值；复杂类型可能需要 Saver。

只保存 selectedId，再从课程数据查出对象。列表是本地固定数据，可以重新构造；真实接口数据应由 Repository/ViewModel 重新获取或从缓存恢复。不要把完整网络列表塞进 Bundle，容量有限，大状态可能导致异常。

## 4. 实际事件链

`点击课程 → 保存 id 和计数 → 配置变化触发 Activity 重建 → 恢复小型状态 → 按 id 查找课程 → 显示标题`。

输入关键词通过 filter 派生可见列表，不需要额外保存筛选结果。筛选隐藏已选课程时，选中项仍保留；数据源真的不存在该 id 时，firstOrNull 返回 null，UI 显示“未选择”。

## 5. 当前 Demo 与验证

入口：首页 → Day 18 · 配置变化。

源码：[ConfigurationStateDemo.kt](../app/src/main/java/com/example/myapplication/day18/ConfigurationStateDemo.kt)。

1. 输入 `Button`，点击课程两次：两种计数都是 2。
2. 旋转屏幕，并在 Logcat 搜索 `Day17`。MainActivity 的日志仍用这个标签，与打开哪个 Day 无关。
3. 确认旧 Activity 销毁、新实例创建后：关键词、Button 选择、可恢复计数 2 应保留；普通 remember 计数变为 0。
4. 输入不存在的关键词，确认显示空态，选中课程仍保留。
5. 点击“重置实验”，所有输入、选择和计数归零。

如果设备锁定方向或配置被应用自行处理，旋转不一定导致重建，必须以实例日志确认。上述是验收步骤，不是已完成的设备验证。

## 6. 保存边界

| 方式 | 适用数据 | 边界 |
|---|---|---|
| remember | 临时界面值 | 当前组合离开或重建后丢失 |
| rememberSaveable | 关键词、选中 id、小型输入 | 配置重建可恢复；系统进程重建需满足任务状态恢复条件 |
| ViewModel | 页面业务状态、异步加载 | 通常跨配置重建，不自动跨进程保存 |
| SavedStateHandle | ViewModel 内用于恢复的少量参数 | 基于系统保存状态，不是长期存储 |
| Room/DataStore | 长期业务数据、偏好 | 持久化到磁盘 |

当前课程导航只是条件组合，没有配置跨页面状态保存容器；返回首页移除 Day18 后再进入，不应期待 rememberSaveable 保留上一次页面状态。强制停止、主动结束任务也不等同于系统恢复流程。

## 7. 完成标准

- 能对照 Java Bundle 与 Kotlin rememberSaveable 解释保存和读取过程。
- 能解释为何保存 id/关键词，而不保存整个派生列表。
- 能通过 Activity 日志与双计数实验判断是否发生重建和恢复。
- 能区分配置重建、页面移除、进程终止和长期持久化。
