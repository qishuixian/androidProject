# Day56 · 音视频 SDK 生命周期

## 前端类比

把音视频 SDK 看成一个需要显式初始化和销毁的播放器/RTC 客户端实例：创建对象不等于初始化完成，加入频道不等于拥有权限，退出频道也不一定等于释放 SDK。

入口：首页 → Day56。源码：[VideoSdkDemo.kt](../app/src/main/java/com/example/myapplication/day56/VideoSdkDemo.kt)。当前工程没有接入真实音视频 SDK，本课用本地状态机学习调用顺序。

```text
NOT_INITIALIZED
 → INITIALIZING
 → READY
 → JOINED
 → LEAVING
 → READY
 → RELEASED
```

## 四个概念分开

- **初始化**：读取配置、创建引擎、注册回调，可能异步失败。
- **加入频道**：使用身份和频道信息建立会话，通常需要权限与网络。
- **退出频道**：停止发布、停止订阅、取消业务监听并离开会话。
- **释放资源**：销毁引擎、线程、音频设备和渲染资源；释放后不能继续调用。

真实 SDK 的方法名各不相同，不能把 `leaveChannel` 自动当成 `release`。有的 SDK 支持重复加入，有的要求先退出；以版本文档和实际回调为准。

## 为什么要用状态机

如果只写 `isJoined`、`isLoading`、`isReleased` 多个 Boolean，可能出现“已释放但仍加入”“正在退出又点击加入”的矛盾组合。本例用互斥状态限制按钮：初始化完成才可加入，加入后才可退出，释放后必须重新初始化。

## 生命周期和页面

Activity/Compose 页面离开时，真实实现需要决定：立即退出并释放，还是由 ViewModel/应用级会话继续持有。不要把 SDK 实例随意放进 Composable 重组，也不要让 SDK 回调持有销毁的 Activity。回调应带实例 id/会话代次，过滤旧引擎迟到事件。

旋转可能只重建界面，也可能触发业务退出，取决于架构。摄像头/麦克风权限、前后台切换、Surface 渲染和网络质量将在 Day57～59 分开学习。本课不申请权限、不创建真实渲染器。

## Java 对照

```java
engine.initialize(config, callback);
engine.joinChannel(token, channel, uid);
engine.leaveChannel();
engine.release();
```

Java/Kotlin 语法不同，但仍需保证初始化、加入、退出、释放顺序和回调线程安全。

## 验收

1. 按初始化、加入、退出、释放顺序操作，观察状态变化。
2. 未初始化时不能加入；未加入时不能退出；释放后不能直接加入。
3. 能解释退出频道和释放 SDK 的区别。
4. 能指出真实项目还需补充权限、Token、频道、回调线程和资源释放验证。

构建任务：`:app:assembleDemoDebug`、`:app:assembleProdDebug`。未接入真实音视频服务。
