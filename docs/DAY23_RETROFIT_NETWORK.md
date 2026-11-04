# Day 23：Retrofit 网络请求

目标：接续 Day21 的接口和 Day22 的手动依赖注入，把本地数据源替换为 HTTP 数据源。前端可类比 axios service，但 Retrofit 使用注解声明请求，通过转换器解析 JSON。

## 1. 当前示例

首页 → Day23 → 点击请求数据，才发送 GET：

```text
https://jsonplaceholder.typicode.com/todos?_limit=3
```

这是公共测试服务，返回的是待办事项，不是真实课程接口。示例将待办映射为 Course，便于沿用前几课模型。服务可用性依赖当前网络；失败会显示错误并允许重试，没有自动用假数据替代失败。

## 2. Retrofit 声明

```kotlin
interface TodoApi {
    @GET("todos")
    suspend fun load(@Query("_limit") limit: Int): List<TodoDto>
}
```

- `@GET` 指定 HTTP 方法和相对路径。
- `@Query` 把参数写入 URL，调用 load(3) 得到 `_limit=3`。
- `suspend` 允许等待结果；Retrofit 的挂起适配器异步请求，不需要为这次请求额外套 Dispatchers.IO。
- suspend 本身不负责启动协程，也不保证任意函数都不阻塞。

## 3. Java 对照

Java 常见写法使用 Retrofit 的 Call 和回调（片段）：

```java
interface TodoApi {
    @GET("todos")
    Call<List<TodoDto>> load(@Query("_limit") int limit);
}

// 在已有 api 对象处调用
api.load(3).enqueue(new Callback<List<TodoDto>>() {
    @Override
    public void onResponse(Call<List<TodoDto>> call, Response<List<TodoDto>> response) {
        if (response.isSuccessful() && response.body() != null) {
            List<TodoDto> items = response.body();
        }
        // 非 2xx 仍进入 onResponse，必须另行处理。
    }
    @Override
    public void onFailure(Call<List<TodoDto>> call, Throwable error) {
        // 网络或转换失败；真实页面还需管理 call.cancel()。
    }
});
```

Kotlin 的挂起返回值写法更接近前端 await：

```kotlin
val items = api.load(3)
```

本例直接返回 List，因此非 2xx 会抛 HttpException；网络失败或解析失败也会抛异常。若返回 Response<List<TodoDto>>，则需要自己检查 isSuccessful/body。不要用阻塞的 execute() 在主线程请求。

## 4. 创建与转换

```kotlin
Retrofit.Builder()
    .baseUrl("https://jsonplaceholder.typicode.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(TodoApi::class.java)
```

baseUrl 以斜杠结束；create 生成接口实现。Gson 转换器把 JSON 字段转换成 DTO，Repository 再把 DTO 映射为 Course。真实业务还需验证必填字段，不能只依赖 Kotlin 非空声明验证外部 JSON。

依赖使用 Retrofit 与 converter-gson 2.11.0；Manifest 注册 INTERNET 权限，这是普通权限，不需要运行时弹窗申请。本例使用 HTTPS。

## 5. 完整运行链路

`点击 → NetworkViewModel.load → Loading → NetworkCourseRepository → TodoApi.load → HTTP/JSON → List<TodoDto> → List<Course> → Success → Compose 重组`。

代码：[NetworkDemo.kt](../app/src/main/java/com/example/myapplication/day23/NetworkDemo.kt)。NetworkDependencies 是手动依赖容器，通过 Factory 注入 Repository，没有接入 Koin。ViewModel 不创建 Retrofit，也不依赖 TodoApi。

请求期间禁用按钮并防重复调用；异常显示 Error，点击重试重新请求。CancellationException 必须重新抛出，不能把协程取消当作请求失败。更细的错误分类留给 Day24。

ViewModel 属于 Activity：旋转通常保留进行中的任务和结果，切回首页不必然取消任务；清理 ViewModel 时取消 viewModelScope。页面停止收集 StateFlow 不等于停止网络请求。

## 6. 动手验证与完成标准

1. 联网请求，观察 Loading 后显示三条标题；公共服务实际返回由服务决定。
2. 关闭网络后重新请求，观察错误；恢复网络再重试。
3. 请求中重复点击，确认按钮禁用。
4. 阅读代码，解释注解、DTO、Repository、Factory、ViewModel 的职责。
5. 能对照 Java enqueue 回调与 Kotlin suspend 写法。
6. 能区分请求成功、HTTP 错误、网络失败与协程取消。

以上是设备验证步骤，不代表已经完成设备网络验证。
