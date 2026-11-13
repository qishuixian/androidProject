# Day73 · 权限兼容性

入口：首页 → Day73 · 权限兼容性。
源码：[PermissionCompatibilityDemo.kt](../app/src/main/java/com/example/myapplication/day73/PermissionCompatibilityDemo.kt)。

## 先分清三个版本

- 设备 API：系统实际版本，决定系统权限与行为能力。
- targetSdk：应用针对哪套行为变化编译和适配，影响系统兼容策略。
- compileSdk：编译时可引用的 API，不等于设备实际行为。

页面显示设备 API 和 targetSdk，帮助记录实验条件。不能因为 compileSdk 很新，就假定旧设备支持同样的运行时授权行为。

## `granted`、`rationale` 和永久拒绝

`checkSelfPermission` 只回答当前是否授予；`shouldShowRequestPermissionRationale` 只表示系统是否建议应用先说明用途。`rationale=false` 可能出现在首次请求、用户历史选择、系统策略或设备差异下，不能单独当作“永久拒绝”。

正确流程是：

```text
执行操作前重新检查 granted
 → 未授权时按产品策略说明用途
 → 用户明确触发 requestPermission
 → 回调读取新的 granted/rationale
 → 未授权则降级或提供设置入口
 → 从设置返回 ON_RESUME 再读取
```

不要用“第一次 false + 第二次 false”写成跨版本绝对规则。Android 版本、targetSdk、权限类型、一次性授权和厂商系统都可能影响可见行为。

## 为什么从设置返回要重查

设置页不是 Activity Result 的权限回调。用户可能在设置里授予、撤销、只允许使用期间、允许部分照片或关闭应用能力，回到应用后旧页面变量仍可能过时。Day73 用 `LifecycleEventObserver` 在 `ON_RESUME` 推进重新读取，页面按钮也提供手动刷新。

打开设置只是一种用户主动路径，不是拒绝后的强制跳转。未授权仍应能继续学习其他课程，不循环弹窗。

## 权限类型不能混为一谈

相机和麦克风在 Day69 中分别申请；它们互不替代。通知权限、MediaProjection、媒体库访问和精确位置又是不同能力，各有版本规则。得到相机授权不能推出摄像头硬件可用、没有其他应用占用或采集初始化成功。

Android 13+ 的通知权限与 Android 14 的部分照片访问范围，不能用本课相机的判断逻辑直接替换。实际项目按目标能力、Android 版本和官方契约分别设计。

## 设备实验矩阵

| 条件 | 记录 |
|---|---|
| API/targetSdk/变体 | 设备型号、Android API、targetSdk、demo/prod |
| 权限历史 | 首次、允许、拒绝、再次请求、设置修改 |
| 应用状态 | 前台/后台、旋转、进程重建、一次性授权 |
| 结果 | granted、rationale、系统弹窗、实际功能降级 |

建议每次只改变一个变量。已经授权的设备可能不再弹系统框，不能把“没看到弹窗”当成 API 没执行。需要重置权限时单独记录卸载、清除数据或系统设置操作。

## 常见错误

- 把 `ContextCompat.checkSelfPermission` 的一次结果保存成永久状态。
- 把 `rationale=false` 直接显示为永久拒绝。
- 在 Composable 重组正文里反复 `launch` 权限请求。
- 只在授权回调刷新，忽略从设置返回和执行前重查。
- 把 Manifest 声明、系统授权、硬件可用和业务操作成功当成同一件事。
- 用一个旧系统权限名覆盖新版本媒体访问范围。

## 验收

1. 首次进入不自动弹窗，查看设备 API、targetSdk 与当前状态。
2. 分别选择相机和麦克风，验证独立检查与申请。
3. 允许或拒绝后观察回调；记录 rationale，不把 false 直接标成永久拒绝。
4. 进入设置修改权限，返回应用后检查页面是否刷新。
5. 旋转、切后台、重新打开页面，区分组合重建与系统权限事实。
6. 在不同 API 或设备上重复，并记录权限历史；不要用单台设备结论覆盖所有版本。
