// Day77 讲解 Release 签名、APK/AAB、版本和升级，不生成真实密钥。
package com.example.myapplication.day77

import android.content.Context
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

// 首页入口展示已安装产物与升级判断。
@Composable
internal fun ReleaseSigningDemo() {
    // 仅模拟待升级版本，不修改 Gradle 或 APK。
    var candidateCode by rememberSaveable { mutableIntStateOf(2) }
    var message by rememberSaveable { mutableStateOf("尚未执行升级判断") }
    val context = LocalContext.current
    val info = remember(context) { context.packageManager.getPackageInfo(context.packageName, 0) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day77 · Release 签名与升级")
        Text("当前包=${BuildConfig.APPLICATION_ID}；变体=${BuildConfig.FLAVOR}/${BuildConfig.BUILD_TYPE}")
        Text("已安装 versionCode=${info.longVersionCode}；versionName=${info.versionName}")
        Text("签名决定能否覆盖升级；versionCode 决定版本比较；versionName 主要用于展示")
        Button(onClick = {
            message = if (candidateCode > info.longVersionCode) "可进入升级候选：还需检查同包名、同签名、ABI、数据迁移和回滚" else "不可按普通升级处理：versionCode 必须递增"
        }) { Text("判断候选版本 code=$candidateCode") }
        Button(onClick = { candidateCode++ }) { Text("模拟提高候选 versionCode") }
        Button(onClick = { message = "APK：设备直装；AAB：交给商店拆分生成 APK，不能把两者路径混看" }) { Text("查看 APK / AAB 区别") }
        Text(message)
        Text("本课不创建 keystore、不签名发布、不上传商店；真实签名文件必须放在安全存储并通过 Gradle 机密配置读取。")
    }
}
