# Day52 · 文件上传和 OSS

## 前端类比与完整流程

类似前端先向业务服务申请上传授权，再用 FormData 或对象存储 SDK 上传，最后通知业务服务保存文件引用。上传字节达到 100% 不一定表示服务端业务提交已成功。

```text
选择文件 → 校验元信息 → 向业务后端申请临时授权
 → 上传文件并报告字节进度
 → 检查上传响应/对象 key
 → 业务服务确认关联 → UI 成功
```

入口：首页 → Day52。源码：[UploadDemo.kt](../app/src/main/java/com/example/myapplication/day52/UploadDemo.kt)。本例使用 1 MiB 虚拟 PDF 和假授权，仅模拟进度、成功、中断、取消、过期和从头重试，不读取真实文件，也没有 OSS SDK 或网络上传。

## 三种上传不要混淆

| 方式 | 数据经过哪里 | 凭证/协议 |
|---|---|---|
| 业务服务器 multipart/form-data | 手机 → 业务服务器 | 应用 Token、表单字段 |
| OSS 直传 | 手机 → 对象存储 | STS 临时凭证/签名 URL/表单策略，按具体服务 |
| OSS Multipart Upload | 手机 → 对象存储，多分片 | uploadId、partNumber、ETag、最终 Complete |

HTTP multipart/form-data 不等于 OSS 分片上传。Day52 示例的 20 次进度更新也不是真实 OSS 分片，不能当成实现了断点续传。

## 实际 Android 文件入口

通常用 Activity Result 的 OpenDocument/GetContent 获取 content URI，通过 ContentResolver.openInputStream 读数据流。URI 不是电脑文件路径，也不保证能转成 File；按需持久化 URI 权限，不盲目申请全部存储权限。文件名、MIME、大小应校验，大小可能未知，不能只靠扩展名判断内容。

真实 Retrofit 轮廓如下（说明 API，不是本课实际调用）：

```kotlin
@Multipart
@POST("files")
suspend fun upload(@Part file: MultipartBody.Part): UploadResponse
```

Java 对应 `Call<UploadResponse> upload(@Part MultipartBody.Part file)`。Part 的字段名、Content-Type 和响应结构必须匹配服务端。大文件应流式读取，避免一次 readBytes 把文件全部放进内存；RequestBody 写入字节可用于进度，但要节流，回调不保证在 UI 线程。

## Token 与临时授权

Day51 的应用 Token 用来向自己的业务服务证明身份，不应直接当作 OSS 密钥。业务服务返回限定对象范围、权限与有效期的临时授权。客户端不能内置长期 AccessKey Secret，不能把凭证、签名 URL 或完整鉴权头写入日志。

授权过期需按具体协议重新申请；不要无限刷新重试。同名对象 key 可能覆盖已有对象，生产应由业务规则分配唯一 key、限制上传范围，并验证完成后的对象，而不是客户端随意拼接任意路径。

## 失败、取消与重试

本例“中途失败”之后手动重试会重新申请假授权，并从零开始；不是断点续传。真实可重试请求要考虑幂等、文件流是否可重新打开、对象残留以及业务确认是否已发生。分片上传取消可能需要 Abort Multipart，普通对象删除需要单独权限；取消客户端协程不证明服务器一定没保存数据。

CancellationException 继续抛出。页面作用域离开即取消，真实大型后台上传若要求跨页面/进程存活，应选择明确的后台任务机制，而不是永久保存 Activity。上传进度代表发送字节，完成响应和业务确认另行检查。

## 验收

1. 成功上传：进度到 100%，显示模拟对象 key。
2. 中途失败：停在部分进度，显示错误，再点击重试从头开始。
3. 取消：显示取消，不当成网络失败；之后可重新发起。
4. 授权过期：上传前失败，进度为零。
5. 区分应用 Token、临时 OSS 授权、对象 key 和下载 URL。
6. 能解释 FormData/multipart、OSS 分片与进度回调的不同含义。

构建用 `:app:assembleDemoDebug` 和 `:app:assembleProdDebug`；未进行真实文件选择、网络或 OSS 上传验证。
