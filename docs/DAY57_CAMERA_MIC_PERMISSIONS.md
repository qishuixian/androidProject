# Day57 · 摄像头与麦克风

## 前端类比与完整链

类似网页 getUserMedia：需要用户授权，但授权与拿到可用媒体流不是同一个阶段。Android 先在 Manifest 声明权限，再用运行时权限 API 请求；真正采集还需 CameraX、AudioRecord 或厂商音视频 SDK。

```text
进入 Day57 → 查询系统权限
 → 用户点击申请 → 系统权限对话框（系统决定是否展示）
 → Activity Result 回调 → 更新权限状态
 → 模拟开启视频/音频
 → 宿主暂停 → 关闭模拟开关
 → 返回前台 → 重查权限，不自动恢复采集
```

入口：首页 → Day57。源码：[MediaPermissionDemo.kt](../app/src/main/java/com/example/myapplication/day57/MediaPermissionDemo.kt)。本课真实请求 CAMERA/RECORD_AUDIO，但不开摄像头、不录音、不渲染真实视频帧。视频占位文字是模拟状态。

## 三个状态要分开

| 状态 | 说明 |
|---|---|
| 权限已授予 | 系统当前允许访问，不保证设备存在或能使用 |
| 用户启用意图 | 用户希望开视频/麦克风，属于业务状态 |
| SDK 实际采集/渲染 | 设备是否占用、隐私开关、SDK 回调、Surface 是否准备好 |

摄像头被其他应用占用、系统麦克风/摄像头隐私开关关闭、设备无硬件，都可能在授权后仍然失败。因此真正业务要观察 SDK 成功/失败与首帧，不凭 granted=true 就显示“正在拍摄”。本课未验证这些真实设备行为。

## 权限流程

Manifest 声明能力，uses-feature required=false 避免要求所有设备必须有硬件。运行时使用 RequestMultiplePermissions，示例每次按需申请一种；用户可以只授予摄像头而拒绝录音，不应把两者绑成一个权限 Boolean。

用户拒绝后不应无休止弹窗。系统可能不再展示对话框，或提供仅本次授权，权限也可能被回收。`shouldShowRequestPermissionRationale=false` 既可能是首次，也可能是不能再次提示，不能单独等同于“永久拒绝”。本例不猜测拒绝原因，提供应用设置入口，并在 ON_RESUME 重查。

Java/Kotlin 原理一致：ContextCompat.checkSelfPermission 查询，Activity Result Launcher 请求；Compose 的 rememberLauncherForActivityResult 自动关联注册生命周期，不在重组过程中主动 launch，只在用户事件中请求。

## 前后台与音视频 SDK

本例 ON_PAUSE 关闭模拟开关，包括按 Home、打开设置以及某些系统权限界面导致的暂停；回来不自动打开。不同系统回调细节可能有差异。真实项目需按通话需求明确：仅停预览、停止发布、静音，还是退出频道，不能把这几个操作混同。

Day56 的初始化/加入频道不代表已经授权，Day57 的授权也不代表已经加入频道。视频 Surface/TextureView 的创建与销毁独立于 SDK 引擎，后续继续学习远端用户和渲染生命周期。不要把旧 Activity 或 View 放进单例 SDK 回调。

## 验收

1. 首次申请观察系统弹窗；允许一种、拒绝另一种，界面独立显示。
2. 没授权不能打开对应模拟开关；授权后可切换。
3. 按 Home 再回来，模拟开关关闭。
4. 进入应用设置撤销权限再返回，状态重新读取；系统也可能重启进程。
5. 能解释为什么占位区不是摄像头预览，以及真实采集还需要什么。
6. 区分权限、用户意图、设备采集、频道发布与画面渲染。

构建任务：`:app:assembleDemoDebug`、`:app:assembleProdDebug`。设备弹窗与真实音视频采集需另行设备验证，本课不接真实 RTC 服务。
