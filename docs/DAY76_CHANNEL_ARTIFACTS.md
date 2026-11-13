# Day76 · 渠道包与产物检查

入口：首页 → Day76 · 渠道包。
源码：[ChannelArtifactDemo.kt](../app/src/main/java/com/example/myapplication/day76/ChannelArtifactDemo.kt)。

## Flavor 和渠道不是一回事

Flavor 是 Gradle 构建模型的一部分。本项目的 demo/prod Flavor 会参与合并资源、生成 BuildConfig，并让 demoDebug 与 prodDebug 使用不同的 applicationId。选择 Flavor 后要重新构建 APK。

渠道参数是发布分发维度，例如 official、xiaomi、huawei。它可以通过额外 Flavor 生成独立产物，也可以由某些 APK 渠道工具在产物中写入和读取元数据。渠道值不能靠页面按钮在运行时把已经安装的 APK 变成另一渠道。

页面的“渠道草稿”只是一份 Compose 状态，用来演示选择过程；它没有写入 Manifest、APK 签名区或真实 Walle 元数据。源码和 BuildConfig 也不能单独证明安装包已经带上渠道。

## Walle 类方案放在哪里

Walle 是一种常见的 APK 渠道信息处理方案。具体插件、版本、签名和 AGP 兼容性必须按项目文档配置；本课不下载或引入它。理解层次即可：

```text
构建基础 APK
 → 渠道工具对 APK 写入/封装渠道元数据
 → 重新校验产物与签名
 → 安装该渠道 APK
 → 运行时由对应读取方案得到渠道
```

写入元数据不等于改变 BuildConfig.FLAVOR，也不等于重新执行 Gradle source set 合并。AAB、Play 多渠道分发、厂商商店和 APK 直分发的渠道实现可能不同，不能把一个工具的 API 当所有项目通用接口。

## 产物检查顺序

```text
确认任务与变体
 → 确认 APK 路径
 → 检查 applicationId / versionCode / versionName
 → 检查签名与证书
 → 检查 ABI、资源和 Manifest
 → 读取渠道元数据
 → 安装对应 APK
 → 运行时再次显示包名、版本、渠道
```

本项目 Debug 产物示例：

- `app/build/outputs/apk/demo/debug/app-demo-debug.apk`
- `app/build/outputs/apk/prod/debug/app-prod-debug.apk`

这两个 APK 可以并行安装，因为 demo 有 `.demo` applicationId 后缀；prod 使用原始 applicationId。路径和文件是否最新要以本次 Gradle 任务结果为准。

## 为什么签名和渠道检查不能省略

任何修改 APK 内容的渠道处理都必须重新检查签名。安装失败、更新失败或运行时渠道读取为空，可能来自产物被替换、签名变化、版本不匹配、工具不兼容或读取库不支持当前 APK 类型。不要只看到文件存在就称为渠道包成功。

versionCode 决定升级比较，versionName 用于展示；渠道不应混入业务账号或 Token。安装检查还要区分 demo/prod 包名，避免从另一份安装包读取日志。

## Flavor 与 Walle 的对照

| 方式 | 发生时间 | 影响 | 验证方式 |
|---|---|---|---|
| Flavor | Gradle 构建期 | source set、BuildConfig、包名、资源 | 构建任务、APK、BuildConfig |
| Walle 类元数据 | APK 产物处理期 | 渠道标记 | 工具读取、签名和安装检查 |
| 页面按钮 | 运行时 | 仅当前页面状态 | 不能证明产物渠道改变 |

多维 Flavor 还会影响变体组合和优先级。渠道工具和 Flavor 可以同时存在，但必须定义哪个字段是环境、哪个字段是分发渠道，避免一个字符串承担两种职责。

## 验收清单

1. 构建 demoDebug/prodDebug，记录任务、APK 路径、包名和版本。
2. 进入页面切换渠道草稿，说明为什么不会改变 BuildConfig 或 APK。
3. 使用真实渠道工具时，先记录写入前后 SHA、签名和读取结果；本课未执行。
4. 分别安装 APK，确认 demo/prod 不混淆，运行时读取包名和版本。
5. 修改资源或 Manifest 后重新构建完整 APK，不把热部署当渠道产物。
6. 能说明 Flavor、渠道元数据、Walle 和运行时状态的边界。

本课没有真实 Walle、厂商渠道 SDK 或远程发布，设备安装与渠道读取仍需单独验证。
