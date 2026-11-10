package com.example.myapplication

import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LayoutLearningScreen() {
    var page by rememberSaveable { mutableStateOf("home") }
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
                else -> "布局"
            }) },
            navigationIcon = { if (page != "home") TextButton(onClick = back) { Text("返回") } }
        )
    }) { padding ->
        LazyColumn(
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
