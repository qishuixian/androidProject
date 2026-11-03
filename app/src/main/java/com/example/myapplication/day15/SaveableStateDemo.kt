package com.example.myapplication.day15

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp

@Composable
internal fun SaveableStateDemo() {
    var name by rememberSaveable { mutableStateOf("") }
    var count by rememberSaveable { mutableIntStateOf(0) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day 15 · 状态恢复", style = MaterialTheme.typography.headlineSmall)
        Text("旋转屏幕或发生配置变化后，rememberSaveable 会恢复可保存状态。")
        TextField(value = name, onValueChange = { name = it }, label = { Text("课程备注") })
        Button(onClick = { count++ }) { Text("点击次数：$count") }
        Text(if (name.isBlank()) "还没有备注" else "备注：$name")
    }
}
