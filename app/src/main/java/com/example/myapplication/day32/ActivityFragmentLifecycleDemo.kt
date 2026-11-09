package com.example.myapplication.day32

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
internal fun ActivityFragmentLifecycleDemo() {
    val owner = LocalLifecycleOwner.current
    var events by remember { mutableIntStateOf(0) }
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            events++
            Log.d("Day32", "lifecycle event=$event count=$events")
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day 32 · Activity 与 Fragment 生命周期", style = MaterialTheme.typography.headlineSmall)
        Text("当前宿主：${owner.javaClass.simpleName}；已观察事件：$events")
        Text("返回首页再进入、按 Home 再返回、旋转屏幕，并在 Logcat 搜索 Day32。")
        Text("Compose 内容属于 Activity 的 View 树；Fragment 还有自己的 onCreateView/onDestroyView。")
        Text("配置变化通常会销毁并重建 Activity，ViewModel 可跨越重建，View 不能直接复用。")
        Text("当前生命周期状态：${owner.lifecycle.currentState}")
    }
}
