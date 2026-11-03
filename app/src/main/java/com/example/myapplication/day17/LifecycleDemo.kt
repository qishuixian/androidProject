package com.example.myapplication.day17

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp

@Composable
internal fun LifecycleDemo() {
    var count by remember { mutableIntStateOf(0) }
    // 组合的进入和离开与 Activity 生命周期是不同层次的事件。
    DisposableEffect(Unit) {
        Log.d("Day17", "Day17 composition entered")
        onDispose { Log.d("Day17", "Day17 composition disposed") }
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Day 17 · 页面生命周期", style = MaterialTheme.typography.headlineSmall)
        Text("在 Logcat 搜索 Day17，观察 Activity 和组件的不同事件。")
        Button(onClick = { count++ }) { Text("普通 remember 计数：$count") }
        Text("1. 返回首页再进入：观察组件离开和进入，计数重置。")
        Text("2. 按系统 Home 再返回：观察 Activity 暂停和恢复。")
        Text("3. 旋转屏幕：观察旧 Activity 销毁、新 Activity 创建，计数重置。")
        Text("Activity 重建时 Day16 的 ViewModel 通常保留；这里的 remember 属于当前组合。")
    }
}
