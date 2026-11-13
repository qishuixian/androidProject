// Day79 综合发布练习：把自动检查、真机回归、发布记录和回滚串起来。
package com.example.myapplication.day79

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.dp

// 首页入口：用本地勾选状态模拟发布前检查，不执行真实发布和回滚。
@Composable
internal fun ReleaseChecklistDemo() {
    // 保存已完成的检查项，旋转后继续保留。
    var checked by rememberSaveable { mutableStateOf(setOf<Int>()) }
    // 根据完成度计算当前发布阶段。
    val steps = listOf("自动检查", "真机回归", "发布记录", "回滚方案")
    val done = checked.size
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day79 · 综合发布")
        Text("完成度：$done/${steps.size}；发布是可追踪、可验证、可回退的流程")
        steps.forEachIndexed { index, step ->
            Button(onClick = {
                // 点击只改变本地清单，不会上传 APK 或修改线上环境。
                checked = if (index in checked) checked - index else checked + index
            }) { Text("${if (index in checked) "✓" else "○"} $step") }
        }
        Text(if (done == steps.size) "可以进入发布评审：确认签名、版本、监控和回滚负责人" else "先完成所有检查，再进入发布评审")
        Text("本课不上传商店、不安装设备、不执行真实回滚；请把每项结果写入发布记录。")
    }
}
