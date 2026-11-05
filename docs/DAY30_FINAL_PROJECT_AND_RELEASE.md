# Day 30：综合项目、构建与发布

目标：把 Day7～Day29 的核心知识串成一个可运行课程页面，并建立发布前的检查顺序。

## 1. 综合链路

```text
用户点击
  → Composable 发送事件
  → ViewModel 启动协程
  → Repository/数据源获取数据
  → StateFlow 发布状态
  → Compose 收集并重组
  → UI 展示列表、加载状态和结束状态
```

Day30 Demo 复用 Day28 的分页 ViewModel，集中展示课程列表和加载更多。完整项目中可以继续把数据源替换为 Day25 Room 或 Day26 离线优先 Repository。

## 2. Java 与 Kotlin 边界

Java、Kotlin 可以在同一个 Android 模块中互相调用。模型、Repository、ViewModel 的边界应按职责划分，而不是按语言强行拆分。Kotlin 的协程、StateFlow 和 Compose 写法是当前项目主线；Java 代码可作为接口、旧模块和 SDK 的兼容层。

## 3. 发布前检查

### 自动检查

```powershell
.\gradlew.bat :app:testDebugUnitTest --no-configuration-cache
.\gradlew.bat :app:assembleDebug --no-configuration-cache
.\gradlew.bat :app:lintDebug --no-configuration-cache
```

如果环境使用 Android Studio JBR，可先设置：

```powershell
$env:JAVA_HOME = "D:/android/Android Studio/jbr"
```

### 手动检查

- 首页能进入所有 Day 页面，返回键和系统返回正常。
- Day7 加载、失败和重试正常。
- Day18 旋转后状态表现符合文档。
- Day25 完成状态写入数据库并可重新读取。
- Day26 网络失败时保留缓存。
- Day28 分页追加、加载中和没有更多正常。
- Day29 测试命令可重复运行。
- 至少在一台真机或模拟器安装并启动 APK。

### 发布构建

Debug 构建成功不等于可以发布。正式发布还需要：

1. 配置 release 签名，不把密钥提交到仓库。
2. 检查 applicationId、版本号、隐私和网络配置。
3. 生成 release APK 或 AAB。
4. 在干净设备安装并验证启动、升级和数据迁移。
5. 保存构建产物、版本信息和测试记录。

## 4. 当前项目边界

本项目的 Day30 是教学收尾 Demo，不代表已经完成 release 签名、应用商店发布或完整真机回归。公共网络接口、Room 缓存和示例数据都需要根据真实产品要求替换和补充。

## 5. 完成标准

- 能画出从点击到 UI 更新的完整数据流。
- 能说明 Debug 编译、测试、安装和 Release 发布的区别。
- 能按顺序执行自动检查与手动验收。
- 能识别示例代码与生产代码之间的配置、错误处理和安全差距。
