# Day 27：协程并发

目标：在 Day7 协程基础上，用 `coroutineScope` 和 `async/await` 并行加载相互独立的数据，并理解失败传播与取消。

## 1. 顺序与并行

顺序执行：

```kotlin
val courses = loadCourses() // 900ms
val details = loadDetails() // 600ms
```

总等待时间接近 1500ms。并行执行：

```kotlin
coroutineScope {
    val courses = async { loadCourses() }
    val details = async { loadDetails() }
    courses.await() to details.await()
}
```

两个任务同时开始，总等待时间接近较慢的 900ms。`async` 返回 Deferred，`await()` 读取结果；`await()` 是挂起等待，不阻塞线程。

## 2. Java 对照

Java 常见写法使用 `ExecutorService` 和 `CompletableFuture`：

```java
CompletableFuture<String> courses = CompletableFuture.supplyAsync(this::loadCourses);
CompletableFuture<String> details = CompletableFuture.supplyAsync(this::loadDetails);
CompletableFuture.allOf(courses, details).join();
```

Kotlin 协程把结构化生命周期、异常和取消放在协程作用域中处理。不要因为用了 `async` 就默认获得更快速度；只有任务互相独立且存在等待成本时才适合并发。

## 3. 结构化并发

`coroutineScope` 会等待子协程完成。一个子任务失败时，默认取消同一作用域中的兄弟任务，整体进入失败；外层 catch 更新 Error。ViewModel 被清理时，viewModelScope 取消整个树，正在等待的请求也应停止。

这就是为什么示例继续抛出 `CancellationException`：取消不是业务失败，不能显示成错误。

如果业务要求一个详情失败但列表仍成功，需要显式选择 `supervisorScope` 或对单个 `async` 使用 `runCatching`，并定义部分成功状态；不能无意中吞掉异常。

## 4. 当前 Demo

源码：[ConcurrentLoadingDemo.kt](../app/src/main/java/com/example/myapplication/day27/ConcurrentLoadingDemo.kt)。两个本地挂起函数用 `delay` 模拟独立请求，不访问网络或数据库。点击“并行加载”，观察状态从加载中到完成。

## 5. 完成标准

- 能区分顺序调用和 `async/await` 并行调用。
- 能解释 `coroutineScope` 的等待、失败传播与取消。
- 能估算两个独立任务的并行等待时间。
- 能说明何时需要 `supervisorScope`，而不是默认吞掉异常。
