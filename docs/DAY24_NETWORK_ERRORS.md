# Day 24：网络错误处理与重试

目标：承接 Day23，将技术异常转换为用户可理解的状态，区分可重试错误、请求问题和成功空数据。

## 1. 前端对应

类似 axios 的错误处理层：HTTP 非 2xx、没有拿到响应、JSON 转换失败是不同情况。Repository 获取数据，ViewModel 分类错误并更新 StateFlow，UI 展示提示与重试入口。

## 2. 分类规则

| 类型 | 本例提示/策略 |
|---|---|
| HttpException 401/403 | 检查认证或权限，不盲目重试 |
| HttpException 404 | 检查资源或地址 |
| HttpException 429 | 稍后手动重试，真实项目应考虑 Retry-After |
| HttpException 5xx | 服务异常，可手动重试 |
| SocketTimeoutException | 超时，可重试 |
| UnknownHostException | 无法解析域名，可能是网络或域名配置问题，不能直接断言断网 |
| JsonParseException | 格式问题，需检查接口，不做无意义重试 |
| IOException | 其他连接/传输问题，可在修复条件后重试 |
| CancellationException | 继续抛出，不显示业务失败 |

具体异常要先于通用 IOException 判断。错误分类不是所有根因的诊断；例如转换器还可能抛其他 I/O 异常，实际项目要结合日志和接口契约细化。

## 3. Java 与 Kotlin 对照

Java Retrofit enqueue 的 onResponse 中检查 response.isSuccessful() 与 response.code()，非 2xx 不会自动进入 onFailure。onFailure 中可用 `error instanceof SocketTimeoutException` 分类。

Kotlin 使用直接返回业务对象的 suspend API 时，可以捕获：

```kotlin
try {
    val courses = repository.loadCourses()
    // 发布 Success，即使 courses 为空也是成功。
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (error: Exception) {
    val problem = classifyRequestError(error)
    // 发布 Failure(problem)，UI 根据 retryable 决定重试入口。
}
```

不要用无条件 runCatching/onFailure 把取消吞掉。协程取消用于停止工作，不是网络错误。StateFlow 是最新状态流，不保证 UI 展示每个瞬间状态；本例 delay 使 Loading 可以观察。

## 4. 当前 Demo

- [NetworkErrors.kt](../app/src/main/java/com/example/myapplication/day24/NetworkErrors.kt)：分类函数和 RequestProblem。
- [ErrorHandlingDemo.kt](../app/src/main/java/com/example/myapplication/day24/ErrorHandlingDemo.kt)：ViewModel、本地实验与 UI。

本页不发送 HTTP 请求，而是构造 Retrofit/Gson/Java 网络异常，保证每种场景可重复。Day23 仍是实际网络示例。模拟不能证明设备真实断网或服务端错误的处理已通过验证。

选择场景 → Loading → 等待 500ms → 首次抛对应异常 → 分类 → Failure。可重试场景点击重试后成功；重新选择场景会重置实验。404 与解析失败不显示重试按钮。空数据直接进入 Success(emptyList())。

ViewModel 负责事件和状态，UI 通过 collectAsStateWithLifecycle 观察。加载中禁用场景按钮并阻止重复启动。此实验使用 Activity 范围 ViewModel，返回首页不等同于取消请求。

## 5. 重试原则

本例只有用户手动重试，没有自动重试循环。真实 GET 可以按业务要求重试；支付或创建订单等写操作必须考虑幂等性，不能简单重复提交。自动重试需限制次数、退避并尊重取消；不要在 update lambda 中发送请求。

技术错误日志与用户提示应分开。示例 Logcat 标签 Day24 只记录场景、次数、异常类型，不记录响应正文、Token 或个人信息。

## 6. 验证与练习

1. 选择超时：加载后出现超时提示，点击重试出现两条课程。
2. 选择 HTTP 503：首次失败，重试成功；再次选择仍首次失败。
3. 选择 404 和解析失败：显示对应提示，没有重试按钮。
4. 选择空数据：显示“请求成功，但没有数据”，不是错误。
5. 阅读 classifyRequestError，解释为什么取消要抛出、子类型要先判断。
6. 将来把 Day23 的异常传给同一个分类函数时，UI 无需了解底层异常类。

以上为设备验收步骤，不代表完成实机验证。
