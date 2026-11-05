# Day 29：测试与调试

目标：用单元测试验证 ViewModel 的状态转换，用 Logcat 和断点定位运行时问题，并理解测试与真机验收的边界。

## 1. 测试什么

测试应验证有意义的行为，而不是重复实现细节。Day28 的行为包括：

- 点击加载后追加 5 条。
- 页码从 0 变成 1。
- 第 4 页后 `hasNext` 变成 false。
- 没有更多数据时不能继续追加。

## 2. Java 与 Kotlin 测试

Java JUnit：

```java
@Test
public void pageAppendsItems() {
    assertEquals(5, state.getItems().size());
}
```

Kotlin：

```kotlin
assertEquals(5, model.state.value.items.size)
```

ViewModel 使用协程时，测试不能真实等待 400ms；`StandardTestDispatcher` 和 `advanceUntilIdle()` 让测试手动推进虚拟时间，执行稳定且快速。

## 3. 当前测试

测试文件：[PagingDemoViewModelTest.kt](../app/src/test/java/com/example/myapplication/day28/PagingDemoViewModelTest.kt)。运行：

```powershell
.\gradlew.bat :app:testDebugUnitTest --no-configuration-cache
```

测试通过说明 ViewModel 的这两个状态行为在 JVM 单元测试中成立，不等于设备上的滚动、网络、数据库和视觉布局已经验证。Compose UI 测试属于另一层，需要语义节点和 Android 测试环境。

## 4. 调试方法

1. 先确定触发动作：例如点击“加载下一页”。
2. 在事件入口、ViewModel 更新、Repository 返回处分别观察。
3. Logcat 打印状态类型和数量，避免打印 Token、完整响应或个人数据。
4. 用断点查看 `current.page`、`newItems` 和 `hasNext`。
5. 区分编译错误、运行时崩溃、业务错误和设备/网络环境问题。

## 5. 完成标准

- 能写出验证状态转换的 JUnit 测试。
- 能解释虚拟调度器为什么不需要真实等待。
- 能区分单元测试、Compose UI 测试和设备手动验证。
- 能从点击事件追踪到 ViewModel 状态变化。
