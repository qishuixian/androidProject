// Day75 诊断初始化时机、版本和完整 APK，不执行真实热修复。
package com.example.myapplication.day75

import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.BuildConfig

// 只保存本地教学状态，不加载外部代码或资源补丁。
@Composable
internal fun HotfixResourceDemo() {
    // 模拟补丁检查结果，不能当真实服务器响应。
    var checked by rememberSaveable { mutableStateOf(false) }
    // 记录用户选择的验证方式。
    var method by rememberSaveable { mutableStateOf("尚未选择") }
    // 当前包信息由 Android PackageManager 读取。
    val context = LocalContext.current
    val packageInfo = remember(context) { context.packageManager.getPackageInfo(context.packageName, 0) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day75 · 热修复与资源验证（本地诊断）")
        Text("包名=${BuildConfig.APPLICATION_ID}")
        Text("变体=${BuildConfig.FLAVOR}/${BuildConfig.BUILD_TYPE}；环境=${BuildConfig.ENVIRONMENT}")
        Text("版本=${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})；系统 API=${Build.VERSION.SDK_INT}")
        Text("PackageManager 版本=${packageInfo.versionName} (${packageInfo.longVersionCode})")
        Text("初始化阶段：Application → Activity → Compose；BuildConfig 来自编译变体，不是运行时热切换")
        Button(onClick = {
            // 只记录一次本地检查，不访问网络或执行补丁。
            checked = true
            method = "已模拟校验：应比较包名、版本、变体、签名和资源版本"
        }) { Text("模拟检查补丁兼容性") }
        Button(onClick = { method = "完整 APK 验证：构建 → 安装 → 冷启动 → 读取 Logcat/产物" }) { Text("选择完整 APK 验证") }
        Button(onClick = { method = "热部署验证：只记录 IDE 实际动作；资源/Manifest/Native 改动需完整安装" }) { Text("选择热部署对照") }
        Text("检查状态=$checked；$method")
        Text("本课不下载、不加载、不执行远程代码；资源值应来自当前 APK，不能凭 IDE 页面显示推断已发布。")
    }
}
