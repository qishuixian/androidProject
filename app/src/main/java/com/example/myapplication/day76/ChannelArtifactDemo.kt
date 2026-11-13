// Day76 对照 Flavor、渠道参数和 APK 产物检查，不引入 Walle 插件。
package com.example.myapplication.day76

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.BuildConfig

// 首页入口展示当前变体和渠道验证边界。
@Composable
internal fun ChannelArtifactDemo() {
    // 选择渠道只是本地草稿，不修改 APK 元数据。
    var channel by rememberSaveable { mutableStateOf("official") }
    // 记录人工验证动作。
    var message by rememberSaveable { mutableStateOf("尚未检查产物") }
    val context = LocalContext.current
    val info = remember(context) { context.packageManager.getPackageInfo(context.packageName, 0) }
    val channels = listOf("official", "xiaomi", "huawei", "demo")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day76 · 渠道包与产物检查")
        Text("Flavor=${BuildConfig.FLAVOR}；BuildType=${BuildConfig.BUILD_TYPE}")
        Text("applicationId=${BuildConfig.APPLICATION_ID}")
        Text("安装包版本=${info.versionName} (${info.longVersionCode})")
        Text("渠道草稿=$channel；这是页面状态，不是 Walle 已写入 APK 的证明")
        channels.forEach { item ->
            Button(onClick = { channel = item }) { Text("选择渠道：$item") }
        }
        Button(onClick = {
            message = "产物检查清单：APK 路径、包名、versionCode、签名、ABI、资源和渠道元数据"
        }) { Text("记录 APK 检查清单") }
        Button(onClick = {
            message = "渠道方案说明：Flavor 重新构建；Walle 类方案写入/读取 APK 元数据；两者不是运行时切换"
        }) { Text("查看渠道方案区别") }
        Text(message)
        Text("本课未接入 Walle 或真实渠道服务；最终渠道值必须从实际 APK/安装包验证，不能只看 BuildConfig 或按钮。")
    }
}
