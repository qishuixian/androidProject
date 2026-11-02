package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MyApplicationTheme { LayoutLearningApp() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LayoutLearningApp() {
    // 保存页面位置，旋转屏幕后仍停留在当前学习页面。
    var page by rememberSaveable { mutableStateOf("home") }
    val lesson = layoutLessons.firstOrNull { it.name == page }
    val back = { page = if (lesson != null) "layouts" else "home" }
    BackHandler(enabled = page != "home", onBack = back)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(lesson?.title ?: if (page == "home") "Android 学习" else "布局") },
                navigationIcon = {
                    if (page != "home") TextButton(onClick = back) { Text("返回") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                page == "home" -> {
                    item { Text("从一个页面开始学习 Android", style = MaterialTheme.typography.headlineSmall) }
                    item { Text("选择学习主题，查看概念、实际布局效果和 XML 写法。") }
                    item {
                        LearningCard("布局", "认识六种 View 布局：排列、相对定位、表格、层叠、坐标与网格。") {
                            page = "layouts"
                        }
                    }
                }
                lesson == null -> {
                    item { Text("六种常见布局", style = MaterialTheme.typography.headlineSmall) }
                    item { Text("布局就像网页中的容器，负责决定子元素的位置和大小。点击下面的布局开始学习。") }
                    layoutLessons.forEach { entry ->
                        item(key = entry.name) {
                            LearningCard(entry.name + " · " + entry.title, entry.summary) { page = entry.name }
                        }
                    }
                }
                else -> {
                    item { Text(lesson.name, style = MaterialTheme.typography.headlineMedium) }
                    item { Text(lesson.summary) }
                    item { Section("前端类比", lesson.analogy) }
                    item { Section("怎么使用", lesson.explanation) }
                    item { Text("实际布局效果", style = MaterialTheme.typography.titleLarge) }
                    item {
                        // 加载真实 XML 布局，演示的是 View 布局规则，而不是 Compose 模拟效果。
                        AndroidView(
                            factory = { context -> LayoutInflater.from(context).inflate(lesson.resource, null, false) },
                            modifier = Modifier.fillMaxWidth().height(240.dp)
                        )
                    }
                    item { Section("关键 XML", lesson.code, code = true) }
                    item { Section("动手试试", lesson.exercise) }
                    item { Text("完整示例：app/src/main/res/layout/${lesson.file}.xml", style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
    }
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
