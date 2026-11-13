// Day73 诊断权限兼容性，不自动弹窗、不把状态猜成永久拒绝。
package com.example.myapplication.day73

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

// UI Context 可能被包装，解包失败时不猜测 Activity 状态。
private fun Context.findActivity(): android.app.Activity? = when {
    this is android.app.Activity -> this
    this is ContextWrapper && baseContext !== this -> baseContext.findActivity()
    else -> null
}

// 首页进入兼容性实验。
@Composable
internal fun PermissionCompatibilityDemo() {
    // 当前宿主用于查询权限和打开设置。
    val context = LocalContext.current
    val activity = context.findActivity()
    val owner = LocalLifecycleOwner.current
    // 复用 Day69 已声明的相机、麦克风权限。
    val permission = rememberSaveable { mutableStateOf(Manifest.permission.CAMERA) }
    var revision by remember { mutableIntStateOf(0) }
    var history by rememberSaveable { mutableStateOf("尚未执行检查") }
    // 读取系统真实状态，不保存旧快照作为事实。
    fun snapshot(name: String): String {
        val granted = ContextCompat.checkSelfPermission(context, name) == PackageManager.PERMISSION_GRANTED
        val rationale = activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, name) }
        return "${if (name == Manifest.permission.CAMERA) "相机" else "麦克风"}：granted=$granted；rationale=${rationale ?: "unknown"}"
    }
    val request = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed ->
        revision++
        history = "系统回调 allowed=$allowed\n${snapshot(permission.value)}"
    }
    DisposableEffect(owner, context, activity) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) revision++
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    val current = remember(revision, context, permission.value) { snapshot(permission.value) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day73 · 权限兼容性")
        Text("设备：${Build.MANUFACTURER} ${Build.MODEL}；API=${Build.VERSION.SDK_INT}；targetSdk=${context.applicationInfo.targetSdkVersion}")
        Text("当前：$current")
        Text("rationale=false 不能单独证明永久拒绝；可能是首次请求、系统策略或用户历史不同。")
        Button(onClick = { permission.value = Manifest.permission.CAMERA; revision++ }) { Text("检查相机") }
        Button(onClick = { permission.value = Manifest.permission.RECORD_AUDIO; revision++ }) { Text("检查麦克风") }
        Button(onClick = {
            val name = permission.value
            if (ContextCompat.checkSelfPermission(context, name) == PackageManager.PERMISSION_GRANTED) history = "当前已授权，不重复申请"
            else request.launch(name)
        }) { Text("按当前选择申请") }
        Button(onClick = {
            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
        }) { Text("打开应用设置检查权限") }
        Button(onClick = { revision++; history = "已重新读取：${snapshot(permission.value)}" }) { Text("返回设置后重新检查") }
        Text(history)
        Text("拒绝后的功能应可降级；不要循环弹窗，也不要仅凭一次 false 推断永久拒绝。")
    }
}
