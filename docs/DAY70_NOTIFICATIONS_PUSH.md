# Day70 · 通知与推送

入口：首页 → Day70 · 通知与推送。
源码：[NotificationDemo.kt](../app/src/main/java/com/example/myapplication/day70/NotificationDemo.kt)。

## 先用前端概念理解

本地通知类似应用主动向系统提交一张提示卡片；远程推送则还要经过服务端和推送通道，把消息送到设备。SDK 初始化、消息到达、通知展示、用户点击是不同阶段，不应合并成一个“推送成功”。

本课发送真实本地通知，并实现真实点击跳转；“模拟推送初始化”只切换本地状态，不包含厂商 SDK、token、服务端注册、网络投递或厂商回调。初始化开关不控制本地通知，也不伪造 token。

## 四个条件分开看

| 条件 | 职责 | 不代表什么 |
|---|---|---|
| Manifest POST_NOTIFICATIONS | 声明通知权限 | 用户已经同意 |
| Android13+ 运行时授权 | 用户允许应用发布普通通知 | 应用总开关、渠道一定开启 |
| 应用通知总开关 | 系统是否允许该应用通知 | 每个渠道都有声音或横幅 |
| Android8+ 通知渠道 | 分类和重要级别，由系统与用户管理 | SDK 已初始化或消息已到达 |

minSdk24，因此渠道 API 走 API26+ 分支，POST_NOTIFICATIONS 请求走 API33+ 分支。旧系统没有该运行时权限，但仍可能禁用应用通知。权限拒绝不阻塞其他课程，不自动循环请求或跳设置。

## 通知渠道为什么要有稳定 id

本课渠道 id 是 day70_lessons，重要级别 DEFAULT，描述为课程提醒。重复 createNotificationChannel 是幂等初始化，不重置用户修改过的声音/重要级别等行为。已有渠道的重要行为不能指望靠重新 new 一个同 id 对象改回；应该尊重用户设置。

不要不断更换渠道 id 来绕过用户禁用。删除重建也不能当作产品正常权限流程。API25 以下兼容通知用 priority，API26+ 主要由渠道重要级别控制。

DEFAULT 不保证横幅提示。系统勿扰、静音、锁屏、OEM 策略和用户渠道设置还会影响声音、横幅与展示位置。notify 返回没有“用户看到了”的确认，必须实际拉下通知栏观察。

## 完整发送链

```text
进入 Day70
 → LaunchedEffect 创建渠道
 → 页面查询权限、总开关、渠道重要级别
 → 用户按需申请 Android13+ 权限
 → 用户点击发送
 → 再次创建/确认渠道
 → 再查实际授权、总开关、渠道是否禁用
 → 构造显式 Activity Intent 与 immutable PendingIntent
 → NotificationCompat.Builder 设置 smallIcon、标题、正文、点击目标
 → notify(70, notification)
 → 系统决定实际展示
```

必须有有效 smallIcon。页面使用系统图标作为教学占位，生产应使用符合通知规范的单色透明图标。再次发送相同通知 id=70 会更新同一条，取消也只取消本课通知，不清除其他通知。

执行前查询不能消除所有竞争，因此 notify 还捕获 SecurityException，给出实际失败反馈。返回设置触发 ON_RESUME 后刷新，不自动发通知；授权回调也不等于用户已开启所有渠道。

## 点击跳转的完整链

```text
用户点击系统通知
 → 系统执行该应用创建的 PendingIntent
 → 显式打开 NotificationLessonActivity
 → Activity 从 Intent 读取 lesson_id
 → 白名单校验 day70
 → 显示详情或参数缺失兜底
```

PendingIntent.getActivity 直接指向 Activity，不经过 BroadcastReceiver/Service 再拉 Activity 的 notification trampoline。FLAG_IMMUTABLE 限制令牌被接收方修改，FLAG_UPDATE_CURRENT 允许创建方更新匹配 PendingIntent 的 extras。

本课 requestCode 固定为70、目标固定为一页，适合演示一条更新通知。多业务通知要设计独立身份；extras 本身通常不参与 Intent.filterEquals 的身份比较，不能只换 lesson_id 就假定得到不同 PendingIntent。可结合独立 requestCode/data 等策略，避免点击旧通知打开错误对象。

详情 Activity 在 Manifest 注册 exported=false；系统可通过本应用创建的 PendingIntent 代表应用执行内部目标，不必为此开放外部启动入口。参数不是登录或访问权限证明，真实详情仍需校验账号、数据归属和有效状态，不能任意执行 URL/路由。

本课默认 Activity 启动模式，不实现 onNewIntent 复用或合成任务栈。App 存活、后台、进程被系统回收等条件下返回栈可能不同；关闭详情 finish 返回已有任务或系统界面。生产需要明确导航栈策略，可根据路由架构使用 TaskStackBuilder/导航深链等，并验证冷启动、热启动与缺失参数。

## 推送初始化真实工程还缺什么

```text
隐私与产品时机确认
 → 按厂商 SDK 契约初始化
 → 获得/刷新 token
 → 按认证契约注册到服务端
 → 服务端确定目标并发送
 → 推送通道交付
 → 应用/SDK 到达回调
 → 校验 payload、去重与账号边界
 → 按系统通知条件展示
 → 点击路由与业务校验
```

不同推送平台的数据消息/通知消息、前后台处理、进程限制和自动展示行为不同，不能把上述示意当某厂商固定 API 顺序。POST_NOTIFICATIONS 管展示能力，不等于每种 SDK token 注册都必须依赖同一个权限；要按具体 SDK 与产品策略判断。

token 可能刷新，与用户身份也不是同一回事；退出登录、切换账号、失效 token 和服务端解绑均需设计。不要把 token、完整 payload 或账号凭证放在通知正文和调试日志。模拟初始化=true 不能证明任何远程步骤完成。

## 和 Day69 的关系

Day69 学会了“声明、按需申请、反馈、返回重查”，本课复用到通知权限。区别是通知还受渠道与应用总开关控制，授权不是唯一条件。Day71 再学习前台 Service 的通知绑定与后台限制，不能把普通本地通知当作前台服务已经运行。

## 设备验收

1. Android13+ 首次点击申请，分别允许/拒绝；拒绝时其他课程可用。
2. Android12及以下不申请 POST_NOTIFICATIONS，提示检查系统设置。
3. 开启允许条件后发送，拉下通知栏观察真实通知；没有横幅不直接判失败。
4. 再发一次，同 id 更新；点击打开 Day70 详情，通知自动移除。
5. 返回课程发送后取消，确认只移除本课通知。
6. 系统关闭应用总开关或本课渠道，返回时状态刷新，发送给出阻止反馈。
7. 用户将渠道静音后重新创建渠道，不应借此重置用户设置。
8. App 在前台、后台及普通进程回收后的点击路径分别记录；强行停止后的通道行为不能与普通进程回收混为一谈。
9. 分别安装 demo/prod，设置、通知和 PendingIntent 都属于各自包，不误看另一个安装包。

验收要记录设备/系统、权限历史、渠道设置、勿扰状态、安装变体和实际点击结果。构建成功不等于设备收到通知，通知到达也不等于远程推送已验证。
