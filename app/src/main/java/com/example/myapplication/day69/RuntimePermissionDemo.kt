// Day69 使用真实权限与文件选择器，不实际拍摄、录音或读取文件正文。
package com.example.myapplication.day69

// 系统权限、Activity 查找与应用设置入口。
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
// Activity Result 将授权与文件选择结果绑定到生命周期。
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
// 页面布局、说明弹窗与操作按钮。
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
// 状态保存说明对象，但系统授权始终重新查询。
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
// 查询权限与是否建议说明原因。
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
// 从设置返回时刷新状态。
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

// UI Context 可能被主题或配置包装，不能直接强转 Activity。
private fun Context.permissionActivity(): Activity? {
    // 已经是 Activity 时直接返回。
    if (this is Activity) return this
    // 解包时避免异常自引用造成无限递归。
    if (this is ContextWrapper && baseContext !== this) return baseContext.permissionActivity()
    // 没有宿主时不猜测 rationale 结果。
    return null
}

// 首页进入权限与 URI 授权实验。
@Composable
internal fun RuntimePermissionDemo() {
    // 当前 UI Context 用于请求与设置跳转。
    val context = LocalContext.current
    // 查找 Activity 用于 rationale 查询。
    val activity = context.permissionActivity()
    // 当前宿主生命周期拥有者。
    val owner = LocalLifecycleOwner.current
    // Manifest 已在 Day57 声明两种权限，本课不重复新增。
    val permissions = listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    // 统一从系统读取，不把历史回调当永久授权凭证。
    fun readPermissions() = permissions.associateWith {
        // 每种权限独立判断，允许部分授权。
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
    // 当前授权快照不参与持久化。
    var grants by remember { mutableStateOf(readPermissions()) }
    // rationale 是建议展示说明的信号，不是“永久拒绝”检测器。
    fun readRationale() = permissions.associateWith {
        // 没有 Activity 时返回未知。
        activity?.let { host -> ActivityCompat.shouldShowRequestPermissionRationale(host, it) }
    }
    // 与授权快照一起在回调和返回前台时刷新。
    var rationales by remember { mutableStateOf(readRationale()) }
    // 正在展示哪种权限的申请前说明。
    var pending by rememberSaveable { mutableStateOf<String?>(null) }
    // 请求中的标志用于避免重复点击。
    var requesting by rememberSaveable { mutableStateOf(false) }
    // 记录流程反馈，不记录文件正文或敏感内容。
    var message by rememberSaveable { mutableStateOf("选择实际操作需要的权限，不会自动弹窗") }
    // 文件 URI 是授权入口，不能当成本地绝对路径。
    var selectedUri by rememberSaveable { mutableStateOf<String?>(null) }
    // 将系统权限常量转为易读名称。
    fun label(permission: String) = if (permission == Manifest.permission.CAMERA) "相机" else "麦克风"
    // 单项申请方便验证相机和麦克风互不替代。
    val request = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed ->
        // 允许再次发起明确的用户请求。
        requesting = false
        // 重新读取两种权限，保持显示与系统一致。
        grants = readPermissions()
        // 拒绝后查询是否应说明，不猜测永久拒绝。
        rationales = readRationale()
        // 授权不等于设备真正打开成功。
        message = if (allowed) "本次已授权；本课没有实际采集" else "本次未授权。功能保持关闭；可阅读说明后重试，或主动检查应用设置。"
    }
    // OpenDocument 授予所选文档的访问能力，不需要广泛存储权限。
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        // 取消不覆盖之前的选择，也不当作权限拒绝。
        if (uri == null) {
            // 这是文件选择取消，不是 CAMERA/RECORD_AUDIO 拒绝。
            message = "文件选择已取消，之前选择保持不变"
        } else {
            // 只展示 URI，不读取正文、不查询真实文件路径。
            selectedUri = uri.toString()
            // 本课不调用 takePersistableUriPermission，不承诺长期授权。
            message = "收到文档 URI；没有读取正文，也没有申请持久 URI 授权。"
        }
    }
    // 从系统设置回来后重新查询实际授权。
    DisposableEffect(owner, context, activity) {
        // 生命周期回调不自动重新弹系统权限框。
        val observer = LifecycleEventObserver { _, event ->
            // 一次性授权或设置撤销后，历史状态可能失效。
            if (event == Lifecycle.Event.ON_RESUME) {
                // 更新当前真实系统结果。
                grants = readPermissions()
                // 同时更新说明建议。
                rationales = readRationale()
            }
        }
        // 注册当前宿主观察者。
        owner.lifecycle.addObserver(observer)
        // 离开组合时移除旧观察者。
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    // 页面使用课程外层滚动。
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 标题说明本课内容。
        Text("Day69 · 运行时权限与存储访问")
        // 逐项展示真实状态与申请入口。
        permissions.forEach { permission ->
            // rationale=false 有多种原因，不标成永久拒绝。
            Text("${label(permission)}：已授权=${grants[permission]}；建议说明=${rationales[permission] ?: "未知"}")
            // 每次申请都由明确点击触发。
            Button(enabled = !requesting, onClick = {
                // 操作当下再次查询，不相信旧画面。
                grants = readPermissions()
                // 已授权时不再重复申请。
                if (grants[permission] == true) message = "${label(permission)}当前已授权；未开启真实设备"
                // 未授权时先展示本地用途说明。
                else pending = permission
            }) { Text("检查并申请${label(permission)}权限") }
        }
        // 只做能力校验，不打开硬件或伪装采集成功。
        Button(onClick = {
            // 业务执行前还要重新检查权限。
            val current = readPermissions()
            // 立即更新页面快照。
            grants = current
            // 部分授权不能当全部可用。
            message = "模拟音视频前置检查：${if (current.values.all { it }) "两项均授权（不等于采集成功）" else "至少一项未授权，未启动"}"
        }) { Text("重新校验模拟音视频前置条件") }
        // 设置页必须由用户主动打开，不因拒绝无限跳转。
        Button(onClick = {
            // 精确打开当前变体包名的详情页。
            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
        }) { Text("打开本应用设置") }
        // SAF 按所选文档授予 URI 能力。
        Button(onClick = { picker.launch(arrayOf("*/*")) }) { Text("系统选择一个文档（不读正文）") }
        // URI 可显示但不应当作可永久使用的文件路径。
        Text("本次文档：${selectedUri ?: "未选择"}")
        // 明确历史字符串不证明系统仍授予访问能力。
        Text("保存 URI 字符串不等于保存授权；本课没有持久化 URI 权限。")
        // 用户可读的流程反馈。
        Text(message)
        // 不申请老式 WRITE_EXTERNAL_STORAGE 来访问所有文件。
        Text("应用私有文件、SAF 文档、照片选择器和媒体库权限是不同方案；按用途和 Android 版本选择，详见文档。")
    }
    // 用局部变量固定本轮弹窗的权限，避免闭包引用变化。
    val explainedPermission = pending
    // 本地用途说明不是系统授权弹窗。
    if (explainedPermission != null) {
        // 无论系统是否建议 rationale，本课都先解释用途。
        AlertDialog(
            // 取消说明不发出系统请求。
            onDismissRequest = { pending = null },
            // 展示实际待申请权限。
            title = { Text("申请${label(explainedPermission)}前的说明") },
            // 告知范围和降级行为，不将未授权当错误崩溃。
            text = { Text("真实业务需在拍摄/录音前按需申请。本课只观察系统授权；拒绝后仍可学习其他功能，不实际采集。") },
            // 用户明确继续后才调用 launcher。
            confirmButton = {
                TextButton(onClick = {
                    // 收起本地说明。
                    pending = null
                    // 等待真实授权结果期间禁用重复请求。
                    requesting = true
                    // 发起当前单项权限请求。
                    request.launch(explainedPermission)
                }) { Text("继续申请") }
            },
            // 明确提供取消路径。
            dismissButton = { TextButton(onClick = { pending = null }) { Text("暂不申请") } }
        )
    }
}
