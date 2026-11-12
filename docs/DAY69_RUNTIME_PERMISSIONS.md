# Day69 · 运行时权限

入口：首页 → Day69 · 运行时权限。
源码：[RuntimePermissionDemo.kt](../app/src/main/java/com/example/myapplication/day69/RuntimePermissionDemo.kt)。

接 Day57：此前观察真实相机、麦克风授权，本课补齐按需申请、用途说明、拒绝反馈、设置返回重查，以及存储访问方案的边界。Day70 再学习通知权限和通知渠道。

## 用前端概念理解

类似浏览器的摄像头/麦克风授权：声明能力、弹窗申请、用户选择、实际使用资源是不同阶段。Android 还需要 Manifest 声明和版本相关的运行时授权机制。授权成功不等于摄像头初始化成功，更不等于已经采集或上传。

本课真实调用系统权限申请和文件选择器，但不打开摄像头、不录音、不读取文件正文、不发送网络。模拟音视频按钮只检查前置授权。相机和麦克风沿用 Day57 已有的 Manifest 声明，不新增广泛存储权限。

## 完整请求链

```text
用户点击相机/麦克风按钮
 → 重新 checkSelfPermission
 → 已授权：反馈当前状态，不重复申请
 → 未授权：显示本地用途说明
 → 用户继续：RequestPermission.launch
 → 系统决定是否显示弹窗并返回结果
 → 回调重新查询两项权限及 rationale
 → 页面反馈，未授权时保持功能关闭
```

用途说明是应用自己的 AlertDialog，不是系统授权框。点取消不会调用 launcher。请求中的按钮禁用，避免重复排队。请求始终由用户事件触发，进入页面和 ON_RESUME 都不会自动申请。

RequestPermission 是 Activity Result Contract；rememberLauncherForActivityResult 将注册与宿主生命周期衔接。不要在重组正文里无条件 launch，也不要把 grant 回调保存为永久业务事实。

## granted 与 rationale 不能混用

| 查询 | 含义 | 不能推出 |
|---|---|---|
| checkSelfPermission = GRANTED | 当前系统授予了这项权限 | 实际硬件已打开或将永久有效 |
| shouldShowRequestPermissionRationale = true | 系统建议应用先说明用途 | 用户已经授权 |
| rationale = false | 不建议展示说明，原因不唯一 | 必然永久拒绝 |

首次请求前、某些拒绝/不再询问状态、系统策略或厂商差异，都可能得到 false。不能写成 `!granted && !rationale → 永久拒绝` 的绝对判断。

本课统一提供用途说明与用户主动的设置入口，不猜测永久拒绝，也不因拒绝强制跳设置或反复弹窗。是否展示更多解释是产品策略；真实拒绝行为要结合系统、权限历史和设备观察。

## 为什么从 Context 查 Activity

LocalContext.current 可能是 Activity，也可能是 ContextWrapper。直接 `context as Activity` 可能失败。示例递归解包并允许返回 null；没有宿主时 rationale 显示未知，而不是伪装为 false。Activity 引用只属于当前组合，不放全局单例或 ViewModel。

## 返回设置为什么还要重查

```text
打开当前包的应用详情设置
 → 用户可能授权/撤销，也可能什么都不改
 → 回到 Activity 的 ON_RESUME
 → 再查相机、麦克风与 rationale
 → 页面显示当前结果
```

系统设置页不是 RequestPermission 的结果回调，不能只靠旧 launcher 回调更新。一次性授权、后台行为、自动重置或用户撤销也可能让历史结果失效；执行敏感操作前仍要再查，真实 API 调用还需处理权限竞争、设备占用和 SecurityException 等实际失败。

本课没有采集资源，所以没有伪造资源释放流程；真实相机/录音要按生命周期关闭资源，不能只把页面布尔变量改成 false。

## 存储不是一个万能权限

| 用途 | 常见选择 | 是否需要广泛存储权限 |
|---|---|---|
| 应用内部 filesDir/cacheDir | 应用私有文件 API | 不需要 |
| 应用专属外部目录 | getExternalFilesDir 等 | 现代支持版本通常不需要；目录随卸载删除 |
| 用户挑选一个文档 | SAF OpenDocument | 获得所选 URI 的访问能力，不申请全部存储 |
| 用户挑选照片/视频 | 系统 Photo Picker 或兼容回退 | 按系统/库能力使用，通常无需广泛媒体权限 |
| 批量访问共享媒体库 | MediaStore 与版本相关权限 | 需按用途、OS、targetSdk 与访问范围判断 |

Android 13 起图片/视频/音频可能涉及 READ_MEDIA_IMAGES、READ_MEDIA_VIDEO、READ_MEDIA_AUDIO；Android 14 的部分照片访问还有不同授权范围，不能把“允许部分照片”显示成全部照片已授权。旧系统对应旧权限与 scoped storage 行为。现代 target 下 WRITE_EXTERNAL_STORAGE 不是访问所有文件的万能开关，MANAGE_EXTERNAL_STORAGE 也不是普通课程选择文件应申请的方案。

本课选择 OpenDocument，不引入上述媒体库申请，不等于实现了跨版本的完整媒体管理器。通知权限属于另一个用途，Day70 单独学习。

## OpenDocument 的真实执行链

```text
用户点击选择文档
 → picker.launch(arrayOf("*/*"))
 → 系统文档提供器让用户选择
 → 返回 Uri 或 null
 → 只保存/显示 Uri 字符串
 → 不打开输入流、不读取正文、不反查真实文件路径
```

null 是选择取消，不是相机/麦克风拒绝。本课取消时保留之前的选择。`content://...` 是提供器资源标识，不等于 `/storage/...` 文件路径；实际读取应通过 ContentResolver，并处理访问失败、文件变化和提供器状态。

URI 字符串可以保存，不意味着访问授权也保存了。OpenDocument 支持在合适条件下调用 takePersistableUriPermission 保留授予的能力，但本课故意不调用，不承诺进程结束或长期重启后仍能访问。即使持久化也不保证文件永远存在，需要按业务释放不再需要的 grant。

本课只展示 URI，不把文件名、正文或内容写入日志。真实读取应避免主线程长时间 I/O，按需求限制大小和类型；不能把 `*/*` 当作业务格式校验。

## 和 remember 家族的关系

- 当前授权快照用 remember，ON_RESUME 与操作时重新查询，不保存旧 grant 为权威结果。
- pending、requesting、反馈和 URI 字符串使用 rememberSaveable，用于当前流程恢复，不代表数据库或系统 grant。
- DisposableEffect 注册/移除生命周期观察者，不等于 Activity 每次暂停都会 onDispose。
- 请求回调由 Activity Result 管理；实际进程死亡、重建和请求恢复仍需设备覆盖，不以编译成功替代验证。

## 验收矩阵

1. 首次进入不弹系统框，点击后先出现本地说明；取消后不申请。
2. 分别申请相机与麦克风，只允许一个，前置检查不能显示全部可用。
3. 拒绝后其他课程和文件选择仍可使用，不循环强制弹窗。
4. 观察 rationale 首次与拒绝后的变化，不把 false 标记为永久拒绝。
5. 主动进入设置更改权限，返回后页面重新显示实际结果。
6. 若设备支持一次性授权，按系统实际行为离开/返回后重新检查。
7. 选择一个测试文档，看到 URI；取消下一次选择，旧选择不被覆盖。
8. 旋转、切后台与请求进行中的重建分别记录，核对结果回调与页面状态。

记录设备型号、Android 版本、targetSdk、安装包、权限历史及用户实际点选。已经授权的设备不一定再弹窗，不能据此认定请求无效。需要重置权限历史时先明确测试条件，不把卸载或清除数据偷偷混入同一轮实验。

## 学完应能说明

- Manifest 声明、运行时授权、资源使用与结果反馈的区别。
- 相机和麦克风独立授权，以及拒绝后的降级处理。
- rationale=false 为什么不能直接证明永久拒绝。
- 为什么从设置返回和执行操作前都应重新查询。
- 私有文件、SAF URI、照片选择器和媒体库权限的边界。
- 保存 URI 字符串与获得/持久化 URI 授权之间的区别。
