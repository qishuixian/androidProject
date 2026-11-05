# vipThinkStudent2 对比后的十项学习优先级

对比 `E:\code\app\vipThinkStudent2` 后，当前 MyApplication 已覆盖 Java/Kotlin 基础、Compose、ViewModel、StateFlow、Repository、Retrofit、Room、缓存、分页、协程和测试，但还需要补充以下内容。

## 第一优先级：传统 Android 页面体系

`vipThinkStudent2` 是 Java、Kotlin、XML、Fragment、Compose 混合项目，而当前项目主要是单 Activity + Compose。

1. Activity 与 Fragment 的关系
2. Fragment 生命周期
3. Fragment 与 Activity 通信
4. Fragment 返回栈
5. Dialog、BottomSheet、PopupWindow
6. XML 布局与 ViewBinding
7. RecyclerView、Adapter、ViewHolder
8. View 的测量、布局、绘制
9. `Context`、`Activity`、`Application` 的区别
10. 配置变化下 Fragment 的恢复

对应代码：`BaseActivity`、`BaseFragment`、`FragmentViewBinding`、`ActivityViewBinding`、`RecyclerView.Adapter`、`Dialog`、`PopupWindow`。

## 第二优先级：大型项目与 Gradle

1. `settings.gradle`
2. 多模块 Gradle 结构
3. Android Library 模块
4. `api` 与 `implementation`
5. 公共模块依赖关系
6. Build Variant
7. Product Flavor
8. `officialDebug`、`officialRelease`
9. 资源和代码 source set
10. Gradle convention plugin、本地 Maven 仓库和依赖冲突排查

对应模块：`Module_App`、`Module_Lib`、`resource`、`plugin`、`yosemite`、`review`、`preview`、`playback`、`librtc`、`librtm`、`libcore`、`viewbinding`。

## 第三优先级：WebView、H5 与 JavaScript Bridge

1. WebView 初始化
2. `loadUrl`
3. WebView 生命周期
4. JavaScript 开启与安全边界
5. `addJavascriptInterface`
6. JavaScript 调 Native
7. Native 调 JavaScript
8. 页面加载回调
9. Cookie、Token、User-Agent
10. WebView 返回键、文件选择、状态同步、白屏排查

对应代码：`GlobalWebActivity`、`GlobalWebFragment`、`FragmentWebView`、`MEJSInterface`、`WebActivityViewModel`、`H5ViewModel`、`H5ConnectViewModel`。

## 第四优先级：Laya 与 Native Bridge

1. Laya 页面和原生页面共存
2. Native 调 Laya
3. Laya 调 Native
4. 协议号设计
5. JSON 参数传递
6. RxBus 事件分发
7. 页面生命周期和 Laya 引擎生命周期
8. AssetManager
9. Laya Native 崩溃定位
10. Jugg 热更新、完整 APK、资源边界以及 Native/H5/Laya 页面判断

对应代码：`Laya`、`LayaInstance`、`BaseLayaActivity`、`JSCommonAPI`、`RxBus`、`OnCourseCallNativeListener`、`LiveClassFragment`。

## 第五优先级：网络和业务接口体系

1. Retrofit 公共 API 封装
2. 请求基类
3. Token 注入
4. 公共请求头
5. 登录态失效
6. HTTP 错误码与业务错误码
7. LiveData 和 StateFlow 混用
8. 分页接口
9. 文件上传、OSS 上传
10. WebSocket、RTM、请求取消、重复请求、防抖和网络日志脱敏

对应代码：`ApiService`、`WebSocketController`、`VipthinkWebsocketHelper`、`ChatManager`、`RTMListener`、`UploadLogViewModel`。

## 第六优先级：实时音视频

1. 音视频 SDK 初始化
2. 加入频道
3. 退出频道
4. 本地视频和远端视频
5. 音频状态
6. 麦克风和摄像头权限
7. 前后台切换
8. 网络质量
9. 远端用户加入/离开
10. SDK 回调线程、Surface、TextureView、旋转、资源释放和 Native 崩溃

对应代码：`AgoraRtc`、`RtcVideo`、`WorkerThread`、`MyEngineEventHandler`、`TXCloudRtc`、`PrivateChatAudioPolicy`、`TutoringVideoPolicy`。

## 第七优先级：输入法、焦点和复杂交互

1. EditText
2. Compose TextField
3. FocusRequester
4. InputMethodManager
5. `InputConnection`
6. `commitText`
7. `setComposingText`
8. `ACTION_DOWN`
9. IME_ACTION
10. 键盘显示隐藏、Laya EditBox 与 Native EditText 冲突、焦点丢失和输入事件日志

## 第八优先级：多语言和资源系统

1. `strings.xml`
2. `@string` 查找
3. `values-zh-rCN`
4. `values-zh-rHK`
5. `values-en`
6. 语言匹配与 fallback
7. Gradle 资源合并
8. App 语言与报告语言分离
9. Java/Kotlin 代码中的资源读取
10. Laya 文案、Native 文案、RTL、布局适配、日期数字本地化

对应内容：`eva_language_type`、`TopicReportLanguage`、`values-zh-rHK`。

## 第九优先级：权限、通知和系统能力

1. 相机权限
2. 麦克风权限
3. 存储权限
4. 通知权限
5. 悬浮窗
6. 屏幕录制
7. 前台 Service
8. 后台限制
9. Android 版本权限差异
10. 权限拒绝、永久拒绝和系统设置页跳转

对应代码：`MmearsPermission`、`PermissionConstants`、`MediaProjectionService`、`ScreenRecordHelper`、`PushService`、`AliPushManager`。

## 第十优先级：启动、热修复、推送和发布

1. Application 生命周期
2. 启动 Activity
3. Splash 页面
4. 登录态恢复
5. Sophix 热修复
6. 推送初始化
7. 渠道包
8. Walle
9. Release 签名
10. APK/AAB、版本升级、崩溃上报、Bugly、设备信息和渠道信息

典型启动链：

```text
Manifest
  → SophixStubApplication
  → VipThinkApplication
  → SplashActivity
  → 登录
  → 主页面
```
