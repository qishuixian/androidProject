package com.example.myapplication

import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication.day7.AsyncLessonDemo
import com.example.myapplication.day8.ListStateDemo
import com.example.myapplication.day9.LazyColumnPerformanceDemo
import com.example.myapplication.day10.CourseIdentityDemo
import com.example.myapplication.day11.CourseEventsDemo
import com.example.myapplication.day12.NullSafetyDemo
import com.example.myapplication.day13.CollectionTransformDemo
import com.example.myapplication.day14.UiStateDemo
import com.example.myapplication.day15.SaveableStateDemo
import com.example.myapplication.day16.ViewModelStateDemo
import com.example.myapplication.day17.LifecycleDemo
import com.example.myapplication.day18.ConfigurationStateDemo
import com.example.myapplication.day19.RecompositionDemo
import com.example.myapplication.day20.CoursePageDemo
import com.example.myapplication.day21.RepositoryDemo
import com.example.myapplication.day22.DependencyInjectionDemo
import com.example.myapplication.day23.NetworkDemo
import com.example.myapplication.day24.ErrorHandlingDemo
import com.example.myapplication.day25.RoomDemo
import com.example.myapplication.day26.OfflineDemo
import com.example.myapplication.day27.ConcurrentLoadingDemo
import com.example.myapplication.day28.PagingDemo
import com.example.myapplication.day29.TestingDemo
import com.example.myapplication.day30.FinalCourseDemo
import com.example.myapplication.day31.XmlViewBindingDemo
import com.example.myapplication.day32.ActivityFragmentLifecycleDemo
import com.example.myapplication.day33.FragmentNavigationDemo
import com.example.myapplication.day34.RecyclerCourseDemo
import com.example.myapplication.day35.OverlayDemo
import com.example.myapplication.day36.MultiModuleDemo
// Day37 比较跨模块依赖的编译可见范围。
import com.example.myapplication.day37.DependencyScopeDemo
// Day38 展示当前 APK 的编译变体。
import com.example.myapplication.day38.BuildVariantDemo
// Day39 显示构建合并后的实际资源。
import com.example.myapplication.day39.ResourceMergeDemo
// Day40 提供本地构建排错练习。
import com.example.myapplication.day40.BuildDiagnosisDemo
// Day41 使用独立 Activity 演示内嵌网页。
import com.example.myapplication.day41.WebViewDemo
// Day42 展示 Native 主动调用网页函数。
import com.example.myapplication.day42.NativeJavaScriptDemo
// Day43 网页主动调用 Native。
import com.example.myapplication.day43.JavaScriptNativeDemo
// Day44 演示 WebView 的 Cookie 和 Token 会话清理。
import com.example.myapplication.day44.WebSessionDemo
// Day45 使用可控页面练习 WebView 白屏诊断。
import com.example.myapplication.day45.WebViewDiagnosticsDemo
// Day46 对比 Native、普通 H5 和模拟 Laya 资源。
import com.example.myapplication.day46.PageTypeDemo
// Day47 模拟 Laya 事件调 Native。
import com.example.myapplication.day47.LayaEventDemo
// Day48 模拟 Native 调 Laya。
import com.example.myapplication.day48.NativeLayaDemo
// Day49 本地模拟协议接收和事件路由。
import com.example.myapplication.day49.ProtocolDemo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LayoutLearningScreen() {
    var page by rememberSaveable { mutableStateOf("home") }
    // 保存首页列表的滚动状态，避免重组时重新创建滚动位置。
    val listState = rememberLazyListState()
    // 进入首页后等待列表完成一次布局，再自动滚到最后一条课程。
    LaunchedEffect(page) {
        if (page == "home") {
            withFrameNanos { }
            val lastIndex = listState.layoutInfo.totalItemsCount - 1
            if (lastIndex >= 0) listState.animateScrollToItem(lastIndex)
        }
    }
    val lesson = layoutLessons.firstOrNull { it.name == page }
    val back = { page = if (lesson != null) "layouts" else "home" }
    BackHandler(enabled = page != "home", onBack = back)
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(lesson?.title ?: when (page) {
                "home" -> "Android 学习"
                "ViewModel" -> "ViewModel Demo"
                "Day7" -> "Day 7 · 异步加载"
                "Day8" -> "Day 8 · 列表渲染"
                "Day9" -> "Day 9 · 列表性能"
                "Day10" -> "Day 10 · 稳定身份"
                "Day11" -> "Day 11 · 事件与回调"
                "Day12" -> "Day 12 · 空值安全"
                "Day13" -> "Day 13 · 集合转换"
                "Day14" -> "Day 14 · sealed 状态"
                "Day15" -> "Day 15 · 状态恢复"
                "Day16" -> "Day 16 · ViewModel 状态"
                "Day17" -> "Day 17 · 页面生命周期"
                "Day18" -> "Day 18 · 配置变化"
                "Day19" -> "Day 19 · Compose 重组"
                "Day20" -> "Day 20 · 数据与 UI 状态"
                "Day21" -> "Day 21 · Repository 分层"
                "Day22" -> "Day 22 · 依赖注入"
                "Day23" -> "Day 23 · Retrofit 网络请求"
                "Day24" -> "Day 24 · 网络错误处理"
                "Day25" -> "Day 25 · Room 数据库"
                "Day26" -> "Day 26 · 离线优先"
                "Day27" -> "Day 27 · 协程并发"
                "Day28" -> "Day 28 · 分页列表"
                "Day29" -> "Day 29 · 测试与调试"
                "Day30" -> "Day 30 · 综合项目"
                "Day31" -> "Day 31 · XML 与 ViewBinding"
                "Day32" -> "Day 32 · Activity 与 Fragment 生命周期"
                "Day33" -> "Day 33 · Fragment 通信与返回栈"
                "Day34" -> "Day 34 · RecyclerView 列表"
                "Day35" -> "Day 35 · 弹层与窗口"
                "Day36" -> "Day 36 · 多模块工程结构"
                "Day37" -> "Day 37 · 依赖管理"
                "Day38" -> "Day 38 · 构建变体"
                "Day39" -> "Day 39 · 资源合并"
                "Day40" -> "Day 40 · 构建排查"
                "Day41" -> "Day 41 · WebView 生命周期"
                "Day42" -> "Day 42 · Native 调 JavaScript"
                "Day43" -> "Day 43 · JavaScript 调 Native"
                "Day44" -> "Day 44 · WebView 登录态"
                "Day45" -> "Day 45 · WebView 白屏与调试"
                "Day46" -> "Day 46 · 页面类型识别"
                "Day47" -> "Day 47 · Laya 调 Native"
                "Day48" -> "Day 48 · Native 调 Laya"
                "Day49" -> "Day 49 · 协议与事件分发"
                else -> "布局"
            }) },
            navigationIcon = { if (page != "home") TextButton(onClick = back) { Text("返回") } }
        )
    }) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                page == "home" -> homeItems(
                    onLayoutsClick = { page = "layouts" },
                    onViewModelClick = { page = "ViewModel" },
                    onDay7Click = { page = "Day7" },
                    onDay8Click = { page = "Day8" },
                    onDay9Click = { page = "Day9" },
                    onDay10Click = { page = "Day10" },
                    onDay11Click = { page = "Day11" },
                    onDay12Click = { page = "Day12" },
                    onDay13Click = { page = "Day13" },
                    onDay14Click = { page = "Day14" },
                    onDay15Click = { page = "Day15" },
                    onDay16Click = { page = "Day16" },
                    onDay17Click = { page = "Day17" },
                    onDay18Click = { page = "Day18" },
                    onDay19Click = { page = "Day19" },
                    onDay20Click = { page = "Day20" },
                    onDay21Click = { page = "Day21" },
                    onDay22Click = { page = "Day22" },
                    onDay23Click = { page = "Day23" },
                    onDay24Click = { page = "Day24" },
                    onDay25Click = { page = "Day25" },
                    onDay26Click = { page = "Day26" },
                    onDay27Click = { page = "Day27" },
                    onDay28Click = { page = "Day28" },
                    onDay29Click = { page = "Day29" },
                    onDay30Click = { page = "Day30" },
                    onDay31Click = { page = "Day31" },
                    onDay32Click = { page = "Day32" },
                    onDay33Click = { page = "Day33" },
                    onDay34Click = { page = "Day34" },
                    onDay35Click = { page = "Day35" }
                    , onDay36Click = { page = "Day36" }
                    , onDay37Click = { page = "Day37" }
                    , onDay38Click = { page = "Day38" }
                    , onDay39Click = { page = "Day39" }
                    , onDay40Click = { page = "Day40" }
                    , onDay41Click = { page = "Day41" }
                    , onDay42Click = { page = "Day42" }
                    , onDay43Click = { page = "Day43" }
                    , onDay44Click = { page = "Day44" }
                    , onDay45Click = { page = "Day45" }
                    , onDay46Click = { page = "Day46" }
                    , onDay47Click = { page = "Day47" }
                    , onDay48Click = { page = "Day48" }
                    , onDay49Click = { page = "Day49" }
                )
                page == "ViewModel" -> item { ViewModelDemo() }
                page == "Day7" -> item { AsyncLessonDemo() }
                page == "Day8" -> item { ListStateDemo() }
                page == "Day9" -> item { LazyColumnPerformanceDemo() }
                page == "Day10" -> item { CourseIdentityDemo() }
                page == "Day11" -> item { CourseEventsDemo() }
                page == "Day12" -> item { NullSafetyDemo() }
                page == "Day13" -> item { CollectionTransformDemo() }
                page == "Day14" -> item { UiStateDemo() }
                page == "Day15" -> item { SaveableStateDemo() }
                page == "Day16" -> item { ViewModelStateDemo() }
                page == "Day17" -> item { LifecycleDemo() }
                page == "Day18" -> item { ConfigurationStateDemo() }
                page == "Day19" -> item { RecompositionDemo() }
                page == "Day20" -> item { CoursePageDemo() }
                page == "Day21" -> item { RepositoryDemo() }
                page == "Day22" -> item { DependencyInjectionDemo() }
                page == "Day23" -> item { NetworkDemo() }
                page == "Day24" -> item { ErrorHandlingDemo() }
                page == "Day25" -> item { RoomDemo() }
                page == "Day26" -> item { OfflineDemo() }
                page == "Day27" -> item { ConcurrentLoadingDemo() }
                page == "Day28" -> item { PagingDemo() }
                page == "Day29" -> item { TestingDemo() }
                page == "Day30" -> item { FinalCourseDemo() }
                page == "Day31" -> item { XmlViewBindingDemo() }
                page == "Day32" -> item { ActivityFragmentLifecycleDemo() }
                page == "Day33" -> item { FragmentNavigationDemo() }
                page == "Day34" -> item { RecyclerCourseDemo() }
                page == "Day35" -> item { OverlayDemo() }
                page == "Day36" -> item { MultiModuleDemo() }
                page == "Day37" -> item { DependencyScopeDemo() }
                page == "Day38" -> item { BuildVariantDemo() }
                page == "Day39" -> item { ResourceMergeDemo() }
                page == "Day40" -> item { BuildDiagnosisDemo() }
                page == "Day41" -> item { WebViewDemo() }
                page == "Day42" -> item { NativeJavaScriptDemo() }
                page == "Day43" -> item { JavaScriptNativeDemo() }
                page == "Day44" -> item { WebSessionDemo() }
                page == "Day45" -> item { WebViewDiagnosticsDemo() }
                page == "Day46" -> item { PageTypeDemo() }
                page == "Day47" -> item { LayaEventDemo() }
                page == "Day48" -> item { NativeLayaDemo() }
                page == "Day49" -> item { ProtocolDemo() }
                lesson == null -> layoutListItems { page = it }
                else -> lessonItems(lesson)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.homeItems(
    onLayoutsClick: () -> Unit,
    onViewModelClick: () -> Unit,
    onDay7Click: () -> Unit
    , onDay8Click: () -> Unit
    , onDay9Click: () -> Unit
    , onDay10Click: () -> Unit
    , onDay11Click: () -> Unit
    , onDay12Click: () -> Unit
    , onDay13Click: () -> Unit
    , onDay14Click: () -> Unit
    , onDay15Click: () -> Unit
    , onDay16Click: () -> Unit
    , onDay17Click: () -> Unit
    , onDay18Click: () -> Unit
    , onDay19Click: () -> Unit
    , onDay20Click: () -> Unit
    , onDay21Click: () -> Unit
    , onDay22Click: () -> Unit
    , onDay23Click: () -> Unit
    , onDay24Click: () -> Unit
    , onDay25Click: () -> Unit
    , onDay26Click: () -> Unit
    , onDay27Click: () -> Unit
    , onDay28Click: () -> Unit
    , onDay29Click: () -> Unit
    , onDay30Click: () -> Unit
    , onDay31Click: () -> Unit
    , onDay32Click: () -> Unit
    , onDay33Click: () -> Unit
    , onDay34Click: () -> Unit
    , onDay35Click: () -> Unit
    , onDay36Click: () -> Unit
    , onDay37Click: () -> Unit
    , onDay38Click: () -> Unit
    , onDay39Click: () -> Unit
    , onDay40Click: () -> Unit
    , onDay41Click: () -> Unit
    , onDay42Click: () -> Unit
    , onDay43Click: () -> Unit
    , onDay44Click: () -> Unit
    , onDay45Click: () -> Unit
    , onDay46Click: () -> Unit
    , onDay47Click: () -> Unit
    , onDay48Click: () -> Unit
    , onDay49Click: () -> Unit
) {
    item(key = "home-title") { Text("从一个页面开始学习 Android", style = MaterialTheme.typography.headlineSmall) }
    item(key = "home-description") { Text("选择学习主题，查看概念、实际布局效果和 XML 写法。") }
    item(key = "home-layouts") { LearningCard("布局", "认识六种 View 布局：排列、相对定位、表格、层叠、坐标与网格。", onLayoutsClick) }
    item(key = "home-view-model") { LearningCard("ViewModel Demo", "观察状态如何保存在 ViewModel 中，并驱动 Compose 页面更新。", onViewModelClick) }
    item(key = "home-day7") { LearningCard("Day 7 · 异步加载", "体验加载中、模拟失败、重试和成功状态。", onDay7Click) }
    item(key = "home-day8") { LearningCard("Day 8 · 列表渲染", "使用 LazyColumn 展示课程，并通过状态提升处理选中项。", onDay8Click) }
    item(key = "home-day9") { LearningCard("Day 9 · 列表性能", "理解稳定 key、列表身份和重新排序。", onDay9Click) }
    item(key = "home-day10") { LearningCard("Day 10 · 稳定身份", "理解列表位置与课程 id 的区别，练习排序和删除。", onDay10Click) }
    item(key = "home-day11") { LearningCard("Day 11 · 事件与回调", "学习子项上报点击和删除事件，父组件统一更新状态。", onDay11Click) }
    item(key = "home-day12") { LearningCard("Day 12 · 空值安全", "学习可空类型、安全调用和默认值，避免空指针崩溃。", onDay12Click) }
    item(key = "home-day13") { LearningCard("Day 13 · 集合转换", "使用 filter 和 map 搜索、筛选和转换课程数据。", onDay13Click) }
    item(key = "home-day14") { LearningCard("Day 14 · sealed 状态", "使用密封类型表达 Idle、Loading、Success、Empty 和 Error。", onDay14Click) }
    item(key = "home-day15") { LearningCard("Day 15 · 状态恢复", "使用 rememberSaveable 保存输入和计数状态。", onDay15Click) }
    item(key = "home-day16") { LearningCard("Day 16 · ViewModel 状态", "使用 ViewModel 和 StateFlow 保存页面业务状态。", onDay16Click) }
    item(key = "home-day17") { LearningCard("Day 17 · 页面生命周期", "观察 Activity 创建、暂停、恢复与销毁。", onDay17Click) }
    item(key = "home-day18") { LearningCard("Day 18 · 配置变化", "保存筛选和选中 id，对比旋转后的两种计数。", onDay18Click) }
    item(key = "home-day19") { LearningCard("Day 19 · Compose 重组", "通过父子组件日志观察状态读取、重组与跳过。", onDay19Click) }
    item(key = "home-day20") { LearningCard("Day 20 · 数据与 UI 状态", "区分课程完成情况、页面筛选与派生结果。", onDay20Click) }
    item(key = "home-day21") { LearningCard("Day 21 · Repository 分层", "把数据获取从 ViewModel 抽出，理解接口和职责边界。", onDay21Click) }
    item(key = "home-day22") { LearningCard("Day 22 · 依赖注入", "用依赖容器和 Factory 创建 Repository 与 ViewModel。", onDay22Click) }
    item(key = "home-day23") { LearningCard("Day 23 · Retrofit 网络请求", "请求公共测试接口，完成 DTO 到业务模型的转换。", onDay23Click) }
    item(key = "home-day24") { LearningCard("Day 24 · 网络错误处理", "区分错误类型、空数据与重试策略。", onDay24Click) }
    item(key = "home-day25") { LearningCard("Day 25 · Room 数据库", "用 Entity、DAO 与数据库保存课程完成情况。", onDay25Click) }
    item(key = "home-day26") { LearningCard("Day 26 · 离线优先", "先读缓存，网络刷新成功后写回数据库。", onDay26Click) }
    item(key = "home-day27") { LearningCard("Day 27 · 协程并发", "使用 async/await 并行加载独立数据。", onDay27Click) }
    item(key = "home-day28") { LearningCard("Day 28 · 分页列表", "分批加载并追加数据，理解分页状态。", onDay28Click) }
    item(key = "home-day29") { LearningCard("Day 29 · 测试与调试", "用单元测试验证状态转换，学习 Logcat 调试方法。", onDay29Click) }
    item(key = "home-day30") { LearningCard("Day 30 · 综合项目", "整合分页、状态、ViewModel 和发布前检查。", onDay30Click) }
    item(key = "home-day31") { LearningCard("Day 31 · XML 与 ViewBinding", "学习传统 XML 页面、ViewBinding 和 Context。", onDay31Click) }
    item(key = "home-day32") { LearningCard("Day 32 · Activity 与 Fragment 生命周期", "观察宿主生命周期，理解 Fragment View 与配置变化。", onDay32Click) }
    item(key = "home-day33") { LearningCard("Day 33 · Fragment 通信与返回栈", "通过参数、Fragment Result 与返回栈完成列表和详情通信。", onDay33Click) }
    // Day34 入口：点击后进入列表学习页，再打开传统 RecyclerView 示例。
    item(key = "home-day34") { LearningCard("Day 34 · RecyclerView 列表", "学习 Adapter、ViewHolder、DiffUtil、稳定 id 与列表状态。", onDay34Click) }
    // Day35 入口：比较三种弹层及其关闭和恢复行为。
    item(key = "home-day35") { LearningCard("Day 35 · 弹层与窗口", "学习 Dialog、BottomSheet、PopupWindow 和窗口生命周期。", onDay35Click) }
    item(key = "home-day36") { LearningCard("Day 36 · 多模块工程结构", "理解 settings.gradle、Application 模块、Library 模块和依赖方向。", onDay36Click) }
    // 新课程入口沿用现有首页导航方式。
    item(key = "home-day37") { LearningCard("Day 37 · 依赖管理", "理解 api、implementation、版本管理和依赖冲突。", onDay37Click) }
    // 打开构建变体学习页，显示当前安装产物的配置。
    item(key = "home-day38") { LearningCard("Day 38 · 构建变体", "理解 Flavor、Debug/Release、source set 和构建产物。", onDay38Click) }
    // Day39 入口沿用现有首页导航，资源选择在编译时完成。
    item(key = "home-day39") { LearningCard("Day 39 · 资源合并", "比较 main、Flavor、BuildType、变体及 Library 资源优先级。", onDay39Click) }
    // Day40 从典型错误日志学习定位问题阶段。
    item(key = "home-day40") { LearningCard("Day 40 · 构建排查", "区分 Gradle、JDK、SDK，练习定位编译、安装和运行错误。", onDay40Click) }
    // Day41 开始 WebView 阶段，使用本地 HTML 排除网络不确定性。
    item(key = "home-day41") { LearningCard("Day 41 · WebView 生命周期", "学习 loadUrl、加载回调、网页返回及销毁。", onDay41Click) }
    // Day42 使用本地网页，集中理解参数编码和异步返回。
    item(key = "home-day42") { LearningCard("Day 42 · Native 调 JavaScript", "学习 evaluateJavascript、JSON 参数、返回值和线程。", onDay42Click) }
    // Day43 通过受控本地内容演示双向通信闭环。
    item(key = "home-day43") { LearningCard("Day 43 · JavaScript 调 Native", "学习桥接对象、JavascriptInterface、线程切换和结果回传。", onDay43Click) }
    // Day44 使用本地页面练习会话写入、读取和清理。
    item(key = "home-day44") { LearningCard("Day 44 · WebView 登录态", "学习 Cookie、Token、登录恢复和退出清理。", onDay44Click) }
    // Day45 练习正常页、错误页和 Debug WebView 调试开关。
    item(key = "home-day45") { LearningCard("Day 45 · WebView 白屏与调试", "学习白屏定位、错误回调、Chrome 调试和安全白名单。", onDay45Click) }
    // Day46 先学习如何根据运行证据识别页面类型。
    item(key = "home-day46") { LearningCard("Day 46 · 页面类型识别", "区分 Native、普通 H5、WebView 和真正 Laya 引擎页面。", onDay46Click) }
    item(key = "home-day47") { LearningCard("Day 47 · Laya 调 Native", "学习事件名、JSON 参数、Native 接收和结果回传。", onDay47Click) }
    item(key = "home-day48") { LearningCard("Day 48 · Native 调 Laya", "学习 JS 调用、协议号、生命周期和回调结果。", onDay48Click) }
    // Day49 练习校验和去重，不加载真实 Laya 引擎。
    item(key = "home-day49") { LearningCard("Day 49 · 协议与事件分发", "理解协议号、JSON、RxBus 概念、线程和重复消息。", onDay49Click) }
}

private fun androidx.compose.foundation.lazy.LazyListScope.layoutListItems(onLessonClick: (String) -> Unit) {
    item { Text("常见控件与布局", style = MaterialTheme.typography.headlineSmall) }
    item { Text("布局和控件就像网页中的容器与元素，负责决定内容的位置、大小和交互。") }
    layoutLessons.forEach { entry ->
        item(key = entry.name) { LearningCard(entry.name + " · " + entry.title, entry.summary) { onLessonClick(entry.name) } }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.lessonItems(lesson: LayoutLesson) {
    item { Text(lesson.name, style = MaterialTheme.typography.headlineMedium) }
    item { Text(lesson.summary) }
    item { Section("前端类比", lesson.analogy) }
    item { Section("怎么使用", lesson.explanation) }
    item { Text("实际布局效果", style = MaterialTheme.typography.titleLarge) }
    item {
        AndroidView(
            factory = { context ->
                val view = LayoutInflater.from(context).inflate(lesson.resource, null, false)
                if (lesson.name == "Button") {
                    view.findViewById<Button>(R.id.start_learning_button)?.setOnClickListener {
                        Toast.makeText(context, "开始学习 Android", Toast.LENGTH_SHORT).show()
                    }
                    view.findViewById<Button>(R.id.text_view_button)?.setOnClickListener {
                        Toast.makeText(context, "打开 TextView 课程", Toast.LENGTH_SHORT).show()
                    }
                }
                view
            },
            modifier = Modifier.fillMaxWidth().height(240.dp)
        )
    }
    item { Section("关键 XML", lesson.code, code = true) }
    item { Section("动手试试", lesson.exercise) }
    item { Text("完整示例：app/src/main/res/layout/${lesson.file}.xml", style = MaterialTheme.typography.bodySmall) }
}

@Composable
private fun LearningCard(title: String, description: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(description)
            Text("点击学习 →", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun Section(title: String, body: String, code: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(body, fontFamily = if (code) FontFamily.Monospace else FontFamily.Default)
    }
}
