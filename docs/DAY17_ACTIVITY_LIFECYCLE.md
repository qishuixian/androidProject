# Day 17：Activity 生命周期

目标：承接 Day15 的状态恢复与 Day16 的 ViewModel，理解 Activity 何时创建、暂停、恢复和销毁。

## 1. 用前端经验理解

Compose 组件进入/离开组合，可以类比组件挂载/卸载。Activity 是承载这些组件的 Android 页面容器，有自己的生命周期；它和某个 Composable 不是同一个对象。

当前项目只有一个 MainActivity。点击课程或返回首页，只是切换 Compose 内容，不会因此新建或销毁 Activity。项目没有 Fragment，后续接触 Fragment 时还需要区分 Fragment 自身和其 View 的生命周期。

## 2. 生命周期回调

| 回调 | 主要含义 |
|---|---|
| onCreate | 初始化本次 Activity 实例，设置界面 |
| onStart | Activity 开始可见 |
| onResume | 进入前台可交互阶段 |
| onPause | 离开 resumed 状态，可能仍可见 |
| onStop | Activity 不再可见 |
| onRestart | 从 stopped 状态准备重新启动 |
| onDestroy | 本次 Activity 实例即将销毁 |

典型完整启动：`onCreate → onStart → onResume`。

按 Home：`onPause → onStop`；进程仍在时返回：`onRestart → onStart → onResume`。

通常旋转重建：旧实例 `onPause → onStop → onDestroy`，新实例 `onCreate → onStart → onResume`。设备配置和 Manifest 可以影响是否重建。系统终止进程不保证调用 onDestroy，不能把关键数据持久化只放在那里。

## 3. Java 和 Kotlin 对照

下面是传统 Java Activity 回调的写法示意：

```java
@Override
protected void onResume() {
    super.onResume();
    Log.d("Day17", "onResume");
}
```

同样的 Kotlin 回调：

```kotlin
override fun onResume() {
    super.onResume()
    Log.d("Day17", "onResume")
}
```

Java 的 `@Override` 与 Kotlin 的 `override` 都表达重写父类方法；Kotlin 必须显式写 override。`super.onResume()` 执行父类处理，随后记录自己的日志。回调由 Android 调用，不能通过手动调用它模拟真实生命周期。

## 4. 本项目实现与日志

- [MainActivity.kt](../app/src/main/java/com/example/myapplication/MainActivity.kt)：记录七个回调，附带 Activity 实例标识和 changingConfig。
- [LifecycleDemo.kt](../app/src/main/java/com/example/myapplication/day17/LifecycleDemo.kt)：用 DisposableEffect 记录组件进入/离开，并显示普通 remember 计数。
- [LayoutLearningScreen.kt](../app/src/main/java/com/example/myapplication/LayoutLearningScreen.kt)：首页 Day17 入口与返回处理。

打开 Android Studio Logcat，选择应用进程，搜索 `Day17`（支持新版查询语法时可用 `tag:Day17`）。例如：

```text
Activity@12345 onCreate restored=false changingConfig=false
Activity@12345 onStart changingConfig=false
Activity@12345 onResume changingConfig=false
Day17 composition entered
```

以上是示意，不是实机抓取。实例标识用于本次调试区分对象，不是业务 id。`restored=true` 只表示收到保存状态 Bundle，不保证所有业务数据都恢复。

`DisposableEffect(Unit)` 在当前组合位置进入时注册，普通重组不会重新执行；离开组合时执行 onDispose。它不是 Activity 的 onCreate/onDestroy，Activity 与组件日志也不要求严格交错成某个固定顺序。

## 5. 与 Day15、Day16 的联系

| 状态位置 | 普通重组 | Activity 配置重建 | 进程被终止后 |
|---|---|---|---|
| remember | 保留 | 重新初始化 | 丢失 |
| rememberSaveable | 保留 | 可保存值可恢复 | 满足系统状态恢复条件时可恢复 |
| ViewModel | 保留 | 同一作用域通常保留 | 实例丢失，需 SavedStateHandle 或持久化重建 |

Day16 使用当前 Activity 范围的 ViewModel，返回首页并不结束这个作用域；Activity 真正结束时会清理。ViewModel 不是数据库，也不会自动跨进程保存。

## 6. 动手验证

1. 启动 App，进入 Day17，查看组件 entered 日志。
2. 点击计数，再返回应用首页并重进：计数重置，记录 disposed/entered；不应仅因这次导航就销毁 Activity。
3. 点击计数，按系统 Home，再返回 App：进程保留时计数仍在；观察暂停、停止、恢复日志。
4. 旋转屏幕触发重建：比较 Activity 实例标识，普通 remember 计数重新初始化。
5. 分别在 Day15 输入备注、Day16 点击成功，再旋转，对比它们与 Day17 的状态表现。

这些是设备验收步骤，不代表已完成设备验证。

## 7. 完成标准

- 能说出典型启动、退后台、回前台的回调链。
- 能解释返回课程首页为什么不等于销毁 Activity。
- 能区分 Kotlin override、父类 super 调用和系统回调。
- 能通过日志判断 Activity 是否创建了新实例。
- 能解释 ViewModel 跨配置重建与跨进程恢复的区别。
