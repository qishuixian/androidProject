package com.example.myapplication.day19

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp

@Composable
internal fun RecompositionDemo() {
    var count by remember { mutableIntStateOf(0) }
    var title by remember { mutableStateOf("TextView") }
    // 在父组件作用域读取状态，便于观察父组件与两个子组件的执行差异。
    val currentCount = count
    val currentTitle = title
    SideEffect {
        Log.d("Day19", "Parent committed: count=$currentCount title=$currentTitle")
    }
    DisposableEffect(Unit) {
        Log.d("Day19", "Page entered")
        onDispose { Log.d("Day19", "Page disposed") }
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Day 19 · Compose 重组", style = MaterialTheme.typography.headlineSmall)
        Text("Logcat 搜索 Day19，比较 Parent、CountPanel 和 TitlePanel 日志。")
        CountPanel(currentCount)
        TitlePanel(currentTitle)
        Button(onClick = { count++ }) { Text("计数 +1") }
        Button(onClick = { title = if (title == "TextView") "Button" else "TextView" }) {
            Text("切换课程标题")
        }
        Button(onClick = {
            Log.d("Day19", "Click: assign the same count")
            count = currentCount
        }) { Text("赋相同计数") }
        Text("计数变化不代表 Activity 重建。旋转屏幕可对照 Day17 日志，此页普通 remember 状态会在重建后初始化。")
    }
}

@Composable
private fun CountPanel(count: Int) {
    SideEffect { Log.d("Day19", "CountPanel committed: count=$count") }
    Text("计数：$count")
}

@Composable
private fun TitlePanel(title: String) {
    SideEffect { Log.d("Day19", "TitlePanel committed: title=$title") }
    Text("课程：$title")
}
