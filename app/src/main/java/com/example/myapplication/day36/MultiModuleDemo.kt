package com.example.myapplication.day36

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.coursecore.CourseModuleInfo

@Composable
internal fun MultiModuleDemo() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day 36 · 多模块工程结构", style = MaterialTheme.typography.headlineSmall)
        Text("当前 app 已依赖模块：${CourseModuleInfo.MODULE_NAME}")
        Text("Library 职责：${CourseModuleInfo.RESPONSIBILITY}")
        Text("调用链：settings.gradle.kts 声明模块 → app implementation(project(\":course-core\")) → app 使用 Library 的公共 API")
        Text("app 是 Application 模块，负责启动和界面；course-core 是 Library 模块，负责可复用业务代码。")
        Text("依赖方向应保持单向：app → course-core，不能让 course-core 反向依赖 app。")
    }
}
