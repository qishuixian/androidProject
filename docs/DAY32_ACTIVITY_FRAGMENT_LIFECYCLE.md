# Day 32 · Activity 与 Fragment 生命周期

## 学习目标

- 区分 Activity、Fragment、Fragment View 和 Compose 组合的生命周期。
- 用 `LifecycleEventObserver` 观察宿主生命周期事件。
- 理解为什么 `onDestroyView` 后不能继续持有 Fragment 的 View 引用。
- 区分普通返回、切到后台和配置变化导致的生命周期差异。

## 当前 Demo

首页进入 **Day 32 · Activity 与 Fragment 生命周期**。页面通过 `LocalLifecycleOwner` 获取当前宿主，使用 `DisposableEffect` 注册和移除观察者，并在 Logcat 搜索 `Day32`。

## 观察步骤

1. 进入 Day32，观察页面显示的生命周期状态。
2. 按系统 Home 再返回，观察 `ON_STOP`、`ON_START` 等事件。
3. 返回首页再进入，观察 Compose 组合离开和新的观察者注册。
4. 旋转屏幕，观察 Activity 重建；对照 Day15、Day16 理解状态保存与 ViewModel。

## 调用链

```text
LayoutLearningScreen
  -> ActivityFragmentLifecycleDemo
  -> LocalLifecycleOwner.current
  -> lifecycle.addObserver
  -> LifecycleEventObserver.onStateChanged
  -> Logcat / 页面计数更新
```

Fragment 的典型顺序还包括 `onCreate`、`onCreateView`、`onStart`、`onResume`、`onPause`、`onStop`、`onDestroyView` 和 `onDestroy`。其中 View 生命周期可能先于 Fragment 本身结束。

## 验证

```powershell
$env:JAVA_HOME='D:/android/Android Studio/jbr'
.\gradlew.bat :app:compileDebugKotlin --no-configuration-cache
```
