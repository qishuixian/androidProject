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
                    onDay7Click = { page = "Day7" }
                    , onDay8Click = { page = "Day8" }
                )
                page == "ViewModel" -> item { ViewModelDemo() }
                page == "Day7" -> item { AsyncLessonDemo() }
                page == "Day8" -> item { ListStateDemo() }
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
) {
    item { Text("从一个页面开始学习 Android", style = MaterialTheme.typography.headlineSmall) }
    item { Text("选择学习主题，查看概念、实际布局效果和 XML 写法。") }
    item { LearningCard("布局", "认识六种 View 布局：排列、相对定位、表格、层叠、坐标与网格。", onLayoutsClick) }
    item { LearningCard("ViewModel Demo", "观察状态如何保存在 ViewModel 中，并驱动 Compose 页面更新。", onViewModelClick) }
    item { LearningCard("Day 7 · 异步加载", "体验加载中、模拟失败、重试和成功状态。", onDay7Click) }
    item { LearningCard("Day 8 · 列表渲染", "使用 LazyColumn 展示课程，并通过状态提升处理选中项。", onDay8Click) }
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
