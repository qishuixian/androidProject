// Day57 使用真实系统权限，但不实际打开摄像头或录音。
package com.example.myapplication.day57

// Android 权限常量、设置页和授权结果。
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
// Activity Result API 将权限回调绑定到界面生命周期。
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
// 布局和按钮展示权限与模拟采集状态。
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
// ContextCompat 查询当前真实授权状态。
import androidx.core.content.ContextCompat
// 使用现代包获取宿主生命周期，观察前后台变化。
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

// 首页进入权限实验；本例不创建音视频 SDK 或 Surface。
@Composable
internal fun MediaPermissionDemo() {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    // 该函数每次查询系统，不把一次授权回调当成永久凭证。
    fun granted(permission: String) = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    var cameraGranted by remember { mutableStateOf(granted(Manifest.permission.CAMERA)) }
    var micGranted by remember { mutableStateOf(granted(Manifest.permission.RECORD_AUDIO)) }
    // 下面两个开关仅代表模拟业务意图，不是真实设备采集。
    var video by remember { mutableStateOf(false) }
    var audio by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("按需申请权限，不实际拍摄或录音") }
    // 权限请求返回后重新查询两种权限，允许部分授权。
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        cameraGranted = granted(Manifest.permission.CAMERA)
        micGranted = granted(Manifest.permission.RECORD_AUDIO)
        message = "授权结果：摄像头=$cameraGranted，麦克风=$micGranted；未授权可重试或到设置页检查"
    }
    // 生命周期观察只注册一次，离开组合移除。
    DisposableEffect(owner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // 从设置页返回时权限可能改变，必须重新读取。
                cameraGranted = granted(Manifest.permission.CAMERA)
                micGranted = granted(Manifest.permission.RECORD_AUDIO)
                if (!cameraGranted) video = false
                if (!micGranted) audio = false
            }
            if (event == Lifecycle.Event.ON_PAUSE) {
                // 本例策略：离开前台就关闭模拟采集，回来不自动重启。
                video = false
                audio = false
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Day57 · 摄像头与麦克风权限")
        Text("系统授权：摄像头=$cameraGranted；麦克风=$micGranted")
        Button(onClick = { launcher.launch(arrayOf(Manifest.permission.CAMERA)) }) { Text("申请摄像头权限") }
        Button(onClick = { launcher.launch(arrayOf(Manifest.permission.RECORD_AUDIO)) }) { Text("申请麦克风权限") }
        // 即使按钮显示可用，操作时仍再次校验系统权限。
        Button(enabled = cameraGranted, onClick = {
            cameraGranted = granted(Manifest.permission.CAMERA)
            video = cameraGranted && !video
        }) { Text("模拟视频：${if (video) "开" else "关"}") }
        Button(enabled = micGranted, onClick = {
            micGranted = granted(Manifest.permission.RECORD_AUDIO)
            audio = micGranted && !audio
        }) { Text("模拟麦克风：${if (audio) "开" else "关"}") }
        // 不猜测“永久拒绝”，提供明确设置入口供用户检查。
        Button(onClick = {
            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:${context.packageName}")))
        }) { Text("打开应用设置") }
        Text(message)
        Text("视频占位区：${if (video) "模拟预览就绪" else "未启用"}；没有真实视频帧。")
        Text("授权不等于采集成功；按 Home、进设置或权限弹窗引起暂停时，本例关闭模拟开关。")
    }
}
