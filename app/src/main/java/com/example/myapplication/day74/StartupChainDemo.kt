// Day74 对照 Application、Activity 启动、Splash 和本地登录态。
package com.example.myapplication.day74

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Application 每个进程通常创建一次，不能当 Activity 生命周期使用。
class LearningApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 只记录进程级初始化，不在这里启动页面或读取 Activity View。
        Log.d("Day74", "Application.onCreate process=${android.os.Process.myPid()}")
    }
}

// Day74 独立 Activity 演示启动链，Manifest 中不作为 Launcher。
class StartupChainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Activity 创建后先显示本地 Splash，再决定登录态页面。
        setContent { MyApplicationTheme { StartupChainDemo() } }
    }
}

// 首页进入的启动链说明页；不触发真实登录请求。
@Composable
internal fun StartupChainDemo() {
    // 模拟 Splash 的短暂展示状态。
    var splash by rememberSaveable { mutableStateOf(true) }
    // 登录态来自本地教学存储，不把它当服务端认证。
    var loggedIn by rememberSaveable { mutableStateOf(false) }
    // 记录当前阶段，便于对照执行链。
    var message by rememberSaveable { mutableStateOf("Application 已创建，准备读取本地登录态") }
    // 只在本次页面首次进入时读取演示流程。
    LaunchedEffect(Unit) {
        // Application 已先于 Activity 初始化；这里模拟启动页的短等待。
        delay(800)
        // 教学示例固定从未登录开始，按钮可切换阶段。
        splash = false
        message = if (loggedIn) "已读取登录态，进入主页面" else "未登录，进入登录页面"
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (splash) {
            // Splash 只负责短暂启动占位，不承载业务页面。
            Text("Day74 · Splash：正在准备应用…")
            Text(message)
        } else if (!loggedIn) {
            // 登录页面只模拟本地成功，不发送账号密码。
            Text("Day74 · 登录页面")
            Text(message)
            Button(onClick = {
                // 业务成功后写入教学登录态。
                loggedIn = true
                message = "本地登录成功，进入主页面"
            }) { Text("模拟登录") }
            Text("本课没有真实账号、Token 或服务端认证")
        } else {
            // 主页面代表登录态通过后的业务入口。
            Text("Day74 · 主页面")
            Text(message)
            Button(onClick = {
                // 登出清除教学状态，下一次回到登录页。
                loggedIn = false
                message = "已退出本地教学登录态"
            }) { Text("模拟退出登录") }
            Text("主页面初始化应依赖登录结果，不应在 Application 中持有 Activity")
        }
    }
}
