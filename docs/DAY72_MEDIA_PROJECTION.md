# Day72 · 屏幕录制

入口：首页 → Day72 · 屏幕录制。
源码：[MediaProjectionDemo.kt](../app/src/main/java/com/example/myapplication/day72/MediaProjectionDemo.kt)。

## 实验范围

本课真实调用 MediaProjection 授权流程和前台 Service，但只创建一个 1×1 的占位 VirtualDisplay，随后立即释放。不会编码 H.264、写视频文件、采集麦克风、上传网络或保存屏幕帧。页面文字和 Logcat 重点观察授权、Service、VirtualDisplay、Surface 与 `MediaProjection.onStop` 的关系。

这不是“录屏功能已经完成”的证明，也不是把授权 Intent 当作长期 token。系统、用户或 Activity 结束投影后，资源必须释放。

## 用前端概念理解

MediaProjection 授权类似浏览器给页面授予屏幕捕获能力；VirtualDisplay 类似真正建立了一个输出管线；Surface 是接收绘制目标。授权成功只是允许建立管线，不能直接推出已经保存视频。

页面只是发起者，ProjectionService 承担运行期资源。页面离开组合不应持有或释放 Service 内部的 Surface；Service 自己在停止、系统回调和异常路径清理资源。

## 完整链路

```text
可见 Activity 点击授权
 → MediaProjectionManager.createScreenCaptureIntent
 → 系统授权页
 → Activity Result 返回 resultCode + data Intent
 → startForegroundService
 → Service.onCreate 创建通知渠道
 → onStartCommand 立即 startForeground
 → getMediaProjection
 → registerCallback(onStop)
 → createVirtualDisplay + Surface
 → 记录已创建
 → 立即释放占位资源
 → stopForeground + stopSelf
 → onDestroy 兜底清理
```

Activity Result 的 `data` 只短暂交给 Service，示例不放进 rememberSaveable、数据库、SharedPreferences 或全局单例。真实产品也要依据平台契约管理授权生命周期，不把它当可跨进程、跨重启永久凭证。

## 为什么必须是前台 Service

屏幕捕获属于敏感、持续的系统能力。启动服务后应及时调用 `startForeground`，并在 Manifest 声明 `android:foregroundServiceType="mediaProjection"`；API29+ 提升时传入相同类型。类型不匹配或系统限制可能导致启动失败，示例会记录失败类型并停止。

Android 版本、targetSdk、用户通知设置和后台启动条件都会影响结果。前台服务不是无限后台权限，也不替代用户授权。Day71 的 `shortService` 是另一种类型，不能复制来处理录屏。

## 资源对象与释放顺序

本课维护四类运行期资源：

| 对象 | 创建位置 | 清理位置 |
|---|---|---|
| `MediaProjection` | Service 获取授权结果后 | `projection.stop()` |
| `VirtualDisplay` | `createVirtualDisplay` | `display.release()` |
| `Surface` | 绑定 `SurfaceTexture` | `surface.release()` |
| `SurfaceTexture` | 创建占位绘制目标 | `texture.release()` |

总清理先释放 VirtualDisplay，再释放 Surface/Texture，最后停止 MediaProjection，并移除前台通知。实际编码器、输入 Surface、音视频线程等应按对应 SDK 的契约停止和等待；这里只演示最小对象链。

`MediaProjection.Callback.onStop` 可能来自用户停止、系统停止或投影会话结束。回调中释放显示目标并 `stopSelf`；`onDestroy` 再次调用幂等清理，避免不同入口造成资源泄漏。系统强杀不保证 `onDestroy`，真实重要状态不能只依赖它写入。

## 为什么用 1×1 占位

目的是让 `createVirtualDisplay` 的对象生命周期可观察，同时避免课程 App 持续捕获屏幕或生成大资源。占位 Surface 不读取任何帧，不能从创建成功推断“收到了屏幕图像”。接入真实录屏时，应替换成编码器 InputSurface，并加入停止、异常、文件关闭和磁盘空间处理。

## 授权结果与页面状态

页面从 `ProjectionObservation` 收集 Service 的事件文字，不在点击后直接设置 `running=true`。启动请求成功只说明 Intent 已提交；真正的“前台提升”“VirtualDisplay 已创建”“释放完成”来自服务阶段。

观察器是同进程 StateFlow，不是 bindService，也不是跨进程服务查询。进程重建后初始状态不能证明系统不存在投影服务。真实应用需要根据架构选择绑定、持久化任务状态或系统查询，不能把本地文字当平台事实。

## 和 Day71 的关系

Day71 练习普通短前台服务；Day72 增加敏感的 MediaProjection 授权和类型声明。两课都用通知，但通知只是服务可见性要求，不能替代授权。Day70 的 `POST_NOTIFICATIONS` 与录屏授权是两套不同的用户同意。

## 验收清单

1. 点击授权，确认系统弹出屏幕捕获确认页；取消后 Service 不应创建 VirtualDisplay。
2. 允许后观察 `onCreate`、前台提升、VirtualDisplay 创建、释放完成和 `onDestroy` 日志。
3. 允许通知/拒绝通知分别记录实际表现；不要用通知栏是否可见推断投影是否运行。
4. 在投影启动后立即点击停止，确认重复释放安全。
5. 按系统停止投影或切换用户，观察 `MediaProjection.onStop`。
6. 旋转或返回首页时，记录 Activity 与 Service 生命周期，不把页面重组当资源释放事件。
7. 确认没有生成视频文件、没有录音、没有网络上传；本课的 1×1 目标会立即释放。
8. API29+ 核对 Manifest 类型与运行 `startForeground` 类型一致。

这些是设备验收步骤；源码和构建成功不能替代系统授权页、通知设置和投影回调的实际观察。
