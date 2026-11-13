# Day77 · Release 签名、APK/AAB、版本与升级

入口：首页 → Day77 · Release 签名。
源码：[ReleaseSigningDemo.kt](../app/src/main/java/com/example/myapplication/day77/ReleaseSigningDemo.kt)。

## 本课目标

- 认识 keystore、alias 和签名文件的职责。
- 区分 Debug 签名与 Release 签名。
- 区分 APK 与 AAB 的交付路径。
- 理解 `versionCode`、`versionName` 与覆盖升级规则。
- 能根据包名、签名和版本定位安装或升级失败原因。

## keystore 与 alias

Android 安装包会带有签名证书。keystore 是保存密钥的容器，alias 是容器中某一组密钥的名字；密码和私钥属于发布凭据。发布项目应把 keystore 放在安全存储中，通过 `keystore.properties`、环境变量或 CI 密钥注入 Gradle，不能把真实密码写进源码，也不能把真实 keystore 提交到 Git。

本课只展示概念和检查顺序，不创建真实 keystore、不生成 Release 签名包、不上传应用商店。

## Debug 与 Release

Debug 通常使用开发环境自动生成的调试签名，便于本地安装；Release 应使用团队长期保管的发布签名。签名证书变化会影响覆盖升级：同一个 `applicationId` 想覆盖已有安装，必须使用兼容的签名证书。更换签名通常需要卸载重装、迁移包名，或按应用商店的密钥升级规则处理。

## APK 与 AAB

| 产物 | 典型用途 | 说明 |
|---|---|---|
| APK | 设备直装、内测分发 | 是可直接安装的包，签名后交给设备校验 |
| AAB | 上传 Google Play 等商店 | 是商店输入文件，商店根据设备生成一个或多个 APK |

AAB 不是可以直接双击安装的 APK。检查产物时要先确认构建任务和文件类型，再确认包名、版本和签名。

## 版本字段与升级

- `versionCode`：整数，系统用于比较新旧版本，升级版本通常必须递增。
- `versionName`：面向用户展示的字符串，例如 `1.4.0`，本身不负责系统升级比较。
- `applicationId`：安装身份。`demoDebug` 使用带 `.demo` 后缀的包名，`prodDebug` 使用正式包名，因此两者可以作为不同应用安装。

普通覆盖升级至少要满足：包名相同、签名兼容、`versionCode` 满足升级规则，并且新包能兼容已有数据。仅仅把 `versionName` 改大，不能保证系统接受升级。

## 安装操作的区别

- `adb install -r app.apk`：尝试保留数据覆盖安装；失败时查看包名、签名和版本错误。
- 卸载后重装：会删除应用数据，适合签名不兼容或需要干净环境的测试。
- 清除应用数据：保留安装包和签名，只删除数据库、偏好等运行数据。

不要把“卸载重装成功”当作“覆盖升级成功”，两者验证的是不同路径。

## 本项目的检查顺序

```text
选择变体（demoDebug / prodDebug / Release）
 → 执行对应 Gradle 任务
 → 找到最新 APK 或 AAB
 → 检查 applicationId、versionCode、versionName
 → 检查签名证书和 SHA-256
 → 再执行安装或覆盖升级
 → 启动应用并检查数据迁移、登录态和日志
```

Debug 产物示例路径如下，实际文件以本次构建结果为准：

- `app/build/outputs/apk/demo/debug/app-demo-debug.apk`
- `app/build/outputs/apk/prod/debug/app-prod-debug.apk`

Release 构建不同于“已经发布”。构建只代表本地生成了一个候选产物；签名保管、商店上传、审核、分发和线上升级仍是后续流程。

## 页面 Demo 的边界

页面会读取当前 `applicationId`、Flavor、BuildType、`versionCode` 和 `versionName`，并用一个候选整数模拟版本比较。按钮不会修改 Gradle 配置、APK 签名或已安装应用，也不会真的执行升级；它用于把“版本比较”和“签名检查”拆开观察。

## 验收清单

1. 进入首页 Day77，确认当前包名、变体和版本字段。
2. 点击候选版本判断，再提高候选 `versionCode` 重新判断。
3. 说明为什么同包名不同签名不能直接覆盖升级。
4. 说明 APK 直装与 AAB 商店分发的区别。
5. 分别构建 demoDebug 和 prodDebug，记录任务与 APK 路径。
6. 如做设备验证，分别记录 `install -r`、卸载重装、清除数据的结果。

本次课程未生成真实 keystore、未上传商店，也未执行真机覆盖升级验证；这些步骤需要在具备发布凭据和测试设备后单独完成。
