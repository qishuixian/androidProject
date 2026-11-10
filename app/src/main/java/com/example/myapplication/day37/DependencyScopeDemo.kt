// Day37 页面属于 app 模块。
package com.example.myapplication.day37

// 以下 Compose API 负责纵向排列、文字和按钮展示。
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
// 状态变化触发重组，用按钮演示跨模块调用。
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
// app 直接依赖 course-core，因此能访问业务入口。
import com.example.coursecore.CourseDependencyDemo
// app 没直接声明 course-model，仍能通过 course-core 的 api 依赖使用模型类型。
import com.example.coursemodel.CourseLabel

// 首页进入此组件，查看依赖链和调用结果。
@Composable
internal fun DependencyScopeDemo() {
    // 显式声明类型，验证 course-model 确实位于 app 编译类路径上。
    val label: CourseLabel = CourseDependencyDemo.label()
    // 这是演示结果状态，离开页面后允许重置。
    var result by remember { mutableStateOf("点击按钮，调用库内部工具") }
    // 纵向排列说明和操作按钮。
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 展示来自公开模型模块的数据。
        Text("Day37 · api 与 implementation")
        Text("api 暴露模型：${label.title}")
        Text("app → course-core → api(course-model)")
        Text("course-core → implementation(course-internal)")
        // app 不导入内部格式化器，而是调用 course-core 的公开方法。
        Button(onClick = { result = CourseDependencyDemo.formattedTitle() }) {
            // 仅按钮点击后才执行上面的业务方法。
            Text("调用封装后的格式化方法")
        }
        // 显示普通 String 结果，证明实现依赖仍参与运行。
        Text(result)
        Text("implementation 隐藏传递编译依赖，不代表不打包，也不等于访问权限控制。")
    }
}
