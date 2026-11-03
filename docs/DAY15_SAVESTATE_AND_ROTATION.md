# Day 15：rememberSaveable 与状态恢复

目标：理解普通 `remember`、`rememberSaveable` 和 Activity 配置变化之间的关系。

## 1. 为什么需要保存状态

旋转屏幕、切换深色模式等配置变化可能导致 Activity 重建。普通 `remember` 只在当前组合存在期间保存值，重建后会重新初始化；`rememberSaveable` 会把可保存值交给保存状态机制，在重建后恢复。

```kotlin
var name by rememberSaveable { mutableStateOf("") }
```

它适合文本、数字、布尔值等基础状态。复杂对象需要使用 `Saver`，业务数据则应放进 ViewModel。

## 2. Java 对照

传统 Activity 通常通过 `onSaveInstanceState(Bundle)` 保存状态，在 `onCreate` 中读取。`rememberSaveable` 是 Compose 对这类保存与恢复流程的封装。

## 3. 当前 Demo

源码位于 `app/src/main/java/com/example/myapplication/day15/SaveableStateDemo.kt`。输入备注并点击计数，再旋转屏幕，观察文字和次数是否恢复。

## 4. 完成标准

- 能区分 `remember` 和 `rememberSaveable`。
- 能说明配置变化为什么可能重建 Activity。
- 能判断哪些状态适合保存，哪些状态应该放进 ViewModel。
