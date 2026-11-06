# Day31～Day55 学习计划

本计划承接 Day1～Day30，按照已确定的优先级学习 `vipThinkStudent2` 中最重要的 Android 工程知识。每一天都包含真实源码调用链、Java/Kotlin 对照、当前项目 Demo、中文文档和验证步骤。

## 第一阶段：传统 Android 页面体系（Day31～Day35）

1. **Day31：XML、ViewBinding、Context**
   - XML 布局、ViewBinding 生成、Context、Activity 和 Application。
2. **Day32：Activity 与 Fragment 生命周期**
   - Activity/Fragment 回调、View 生命周期和配置变化。
3. **Day33：Fragment 通信与返回栈**
   - 参数传递、Fragment Result、返回栈和页面导航。
4. **Day34：RecyclerView、Adapter、ViewHolder**
   - Adapter、ViewHolder、DiffUtil、稳定 id 和列表状态。
5. **Day35：Dialog、BottomSheet、PopupWindow**
   - 弹层生命周期、事件回调、窗口上下文和状态恢复。

## 第二阶段：大型项目与 Gradle（Day36～Day40）

1. **Day36：多模块工程结构**
   - `settings.gradle`、Application 模块、Library 模块和依赖方向。
2. **Day37：`api`、`implementation` 与依赖管理**
   - 依赖暴露、版本管理、依赖冲突和本地 Maven。
3. **Day38：Build Variant、Flavor、Debug/Release**
   - 变体、source set、环境配置和构建产物。
4. **Day39：资源合并与 source set**
   - 资源优先级、不同模块资源和 flavor 资源。
5. **Day40：Gradle、JDK、SDK、构建失败排查**
   - 构建日志、工具链版本、缓存和 APK 产物诊断。

## 第三阶段：WebView 与 H5 Bridge（Day41～Day45）

1. **Day41：WebView 加载和生命周期**
   - `loadUrl`、页面回调、返回键和销毁。
2. **Day42：Native 调 JavaScript**
   - `evaluateJavascript`、参数编码、回调和线程。
3. **Day43：JavaScript 调 Native**
   - `addJavascriptInterface`、桥接方法、线程切换和安全边界。
4. **Day44：Cookie、Token、登录态**
   - WebView 会话、请求头、登录恢复和退出清理。
5. **Day45：WebView 白屏、调试和安全**
   - Chrome 调试、页面错误、网络失败、文件选择和白屏诊断。

## 第四阶段：Laya 与 Native 通信（Day46～Day50）

1. **Day46：Native、H5、Laya 页面识别**
   - 从 Activity、WebView、引擎初始化和资源路径判断运行页面。
2. **Day47：Laya 调 Native**
   - JavaScript 事件、Native 接收和参数校验。
3. **Day48：Native 调 Laya**
   - JS 调用、协议号、生命周期和回调结果。
4. **Day49：协议号、JSON、RxBus**
   - 事件分发、协议设计、线程和重复消息。
5. **Day50：Bridge 日志和崩溃定位**
   - Java/Kotlin、引擎、资源和 Native 崩溃边界。

## 第五阶段：网络与实时通信（Day51～Day55）

1. **Day51：统一请求封装、Token、业务错误码**
   - 公共请求、登录失效、HTTP 错误和业务错误。
2. **Day52：文件上传和 OSS**
   - Multipart、上传进度、失败重试和临时凭证。
3. **Day53：WebSocket 连接与重连**
   - 建连、断开、心跳、重连和生命周期。
4. **Day54：RTM 聊天消息**
   - 消息发送、接收、顺序、重复和协议映射。
5. **Day55：断网、重复消息、取消和调试**
   - 网络切换、任务取消、日志脱敏和可复现验证。

## 每天固定学习流程

1. 阅读 `vipThinkStudent2` 中一个真实调用链。
2. 用 Java 写出核心概念。
3. 用 Kotlin 写出同样逻辑。
4. 在 MyApplication 中增加一个最小 Demo。
5. 编写中文文档和完成标准。
6. 编译验证，必要时再做设备验证。

## 开始顺序

优先从 **Day31：XML 与 ViewBinding** 开始。它是后续 Fragment、RecyclerView、Dialog 和阅读 `vipThinkStudent2` XML 页面的基础。
