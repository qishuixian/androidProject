// Day78 讲解崩溃、Logcat、脱敏和现场信息，不主动制造闪退。
package com.example.myapplication.day78

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.dp

// 首页入口：用固定案例练习日志阅读和现场信息收集。
@Composable
internal fun CrashLogDemo() {
    // 保存当前选择，旋转后仍能继续实验。
    var caseIndex by rememberSaveable { mutableIntStateOf(0) }
    // 保存用户点击后的分析结果。
    var result by rememberSaveable { mutableStateOf("请选择一个案例") }
    // 案例只模拟日志文本，不会真的抛异常或让 App 闪退。
    val cases = listOf(
        "Java/Kotlin 异常：IllegalStateException，优先看 FATAL EXCEPTION 和业务首个栈帧",
        "Native 崩溃：SIGSEGV，优先看 signal、线程、so 名称和 tombstone",
        "安装后无响应：先确认进程、Activity、ANR traces，再区分启动卡顿和崩溃"
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day78 · 崩溃与日志")
        Text("Logcat 是现场记录；先找错误级别和首个属于自己代码的堆栈位置。")
        Text("当前案例：${cases[caseIndex]}")
        Button(onClick = {
            // 使用固定标签输出一条可在 Logcat 中搜索的练习日志。
            Log.e("Day78", "case=$caseIndex；token=***REDACTED***；请结合堆栈定位")
            result = "已写入 Logcat：搜索标签 Day78，并检查敏感字段是否已脱敏"
        }) { Text("记录脱敏日志") }
        Button(onClick = {
            // 循环切换案例，观察不同故障需要的现场信息不同。
            caseIndex = (caseIndex + 1) % cases.size
            result = "已切换案例，先记录复现步骤、设备、版本、变体和最近改动"
        }) { Text("切换案例") }
        Text(result)
        Text("不要把 Token、密码、完整用户信息写进日志；本课不调用 Crashlytics/Bugly，也不主动制造崩溃。")
    }
}
