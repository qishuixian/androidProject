// Day70 发送真实本地通知；远程推送初始化仅作本地流程演示。
package com.example.myapplication.day70

// 系统通知、显式点击 Intent 与版本分支。
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
// Activity Result 与 Compose 宿主。
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
// 向后兼容的通知构建和授权查询。
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
// 页面布局与状态。
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
// 返回前台时重新检查通知设置。
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
// 沿用应用主题，不改变其他课程。
import com.example.myapplication.ui.theme.MyApplicationTheme

// 渠道 id 保持稳定，不能每次发通知创建新渠道绕过用户设置。
private const val CHANNEL = "day70_lessons"
// 固定通知 id 表示再次发送时更新同一条通知。
private const val NOTICE_ID = 70

// Android 8 及以上需要渠道；重复创建不会重置用户设置。
private fun createLessonChannel(context: Context) {
    // minSdk24，仍需保护渠道 API。
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // 系统服务负责实际存储渠道。
        val manager = context.getSystemService(NotificationManager::class.java)
        // 默认重要级别不保证横幅弹出。
        val channel = NotificationChannel(CHANNEL, "Day70 课程提醒", NotificationManager.IMPORTANCE_DEFAULT).apply {
            // 用途说明展示在系统通知设置中。
            description = "本地课程学习通知实验，不接远程推送"
        }
        // 已有渠道时保留系统管理的设置。
        manager.createNotificationChannel(channel)
    }
}

// 首页进入本地通知实验。
@Composable
internal fun NotificationDemo() {
    // 当前包名自动区分 demo 和 prod 安装包。
    val context = LocalContext.current
    // 用于返回设置时刷新状态。
    val owner = LocalLifecycleOwner.current
    // 只记本次组合的系统快照，不当永久授权状态。
    var revision by remember { mutableIntStateOf(0) }
    // 模拟初始化只是演示逻辑，不生成假 token。
    var initialized by rememberSaveable { mutableStateOf(false) }
    // 显示动作结果，不把 notify 返回当可见证明。
    var message by rememberSaveable { mutableStateOf("先检查状态，再按需申请和发送") }
    // 初始化当前版本所需渠道，不在重组正文中重复执行副作用。
    LaunchedEffect(context) { createLessonChannel(context); revision++ }
    // Android 13 以下不发起 POST_NOTIFICATIONS 运行时请求。
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed ->
        // 刷新系统查询。
        revision++
        // 授权不保证渠道或应用通知总开关已开启。
        message = if (allowed) "通知权限已授予，请继续检查总开关与渠道" else "未授予通知权限；其他课程仍可使用"
    }
    // 设置页可能改变总开关或渠道，而不触发权限 launcher。
    DisposableEffect(owner) {
        // 仅刷新，不自动申请权限或发送通知。
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) revision++ }
        // 注册当前生命周期。
        owner.lifecycle.addObserver(observer)
        // 离开时清理。
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    // revision 改变后重新查询真实设置。
    val status = remember(revision, context) {
        // Android 13 起还需要运行时权限。
        val allowed = Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        // 总开关可以被用户关闭。
        val enabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        // 渠道可能未创建或被用户禁用。
        val importance = if (Build.VERSION.SDK_INT >= 26) context.getSystemService(NotificationManager::class.java).getNotificationChannel(CHANNEL)?.importance else null
        // 三项单独展示，不合并成“推送可用”。
        Triple(allowed, enabled, importance)
    }
    // 课程外层提供滚动。
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 明确本课不验证真实远程消息。
        Text("Day70 · 通知与推送（真实本地通知）")
        // null 在 API24/25 表示无渠道机制，而不是渠道被禁用。
        Text("通知权限=${status.first}；应用总开关=${status.second}；渠道重要级别=${status.third ?: "未创建或当前系统无渠道"}")
        // 本地模拟与真实 SDK 注册分开。
        Button(onClick = { initialized = true; message = "模拟初始化完成：未接 SDK、未获取 token、未注册服务端" }) { Text("模拟推送初始化流程") }
        // 初始化与显示授权互不推导。
        Text("模拟初始化=$initialized；本地发送不依赖该开关")
        // 权限请求由点击触发，不自动弹框。
        Button(onClick = {
            // 旧系统没有此运行时权限。
            if (Build.VERSION.SDK_INT >= 33) permission.launch(Manifest.permission.POST_NOTIFICATIONS)
            // 提示用户检查设置而非伪造授权。
            else message = "当前系统不需要 POST_NOTIFICATIONS 运行时申请，请检查通知设置"
        }) { Text("按需申请通知权限（Android13+）") }
        // 发送真实系统通知，点击后打开专属内部 Activity。
        Button(onClick = {
            // 先确保渠道存在，不改变已有用户设置。
            createLessonChannel(context)
            // 操作发生时再次检查权限与总开关。
            val allowed = Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            // 渠道检查同样不能只信旧页面状态。
            val channelBlocked = Build.VERSION.SDK_INT >= 26 && context.getSystemService(NotificationManager::class.java).getNotificationChannel(CHANNEL)?.importance == NotificationManager.IMPORTANCE_NONE
            // 未授权或设置禁用时不继续发布。
            if (!allowed || !NotificationManagerCompat.from(context).areNotificationsEnabled() || channelBlocked) {
                // 不强制跳设置或循环请求。
                message = "未发送：通知权限、应用总开关或渠道未开启"
            } else {
                // 显式指定内部 Activity，不接收任意外部路由。
                val intent = Intent(context, NotificationLessonActivity::class.java).apply {
                    // 固定业务 id，不放账号凭证等敏感内容。
                    putExtra("lesson_id", "day70")
                }
                // 使用 immutable 限制通知点击令牌被修改；更新 extras 保持当前业务目标。
                val click = PendingIntent.getActivity(context, NOTICE_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                // 标题和正文仅使用固定教学内容。
                val notification = NotificationCompat.Builder(context, CHANNEL)
                    // 必须提供有效 smallIcon，使用系统教学图标。
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    // 真实通知栏显示的内容。
                    .setContentTitle("Day70 课程学习提醒")
                    // 不包含隐私信息。
                    .setContentText("点击查看 Day70 通知实验详情")
                    // 直接打开 Activity，避免通知 trampoline。
                    .setContentIntent(click)
                    // 点击后自动移除该通知。
                    .setAutoCancel(true)
                    // API25 以下使用该优先级；新系统主要由渠道控制。
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // 构造通知对象。
                    .build()
                // 授权可能在检查后被改变，处理调用时的真实失败。
                try {
                    // 相同 id 再发会更新，不无限累积通知。
                    NotificationManagerCompat.from(context).notify(NOTICE_ID, notification)
                    // notify 没有“用户看见了”的确认。
                    message = "已调用 notify，请拉下通知栏查看；横幅/声音不保证出现"
                } catch (_: SecurityException) {
                    // 权限竞争或系统策略导致拒绝时反馈。
                    message = "系统拒绝发布，请重新检查通知授权"
                }
            }
            // 操作后刷新页面状态。
            revision++
        }) { Text("发送 / 更新本地通知") }
        // 取消固定 id 只影响本课通知。
        Button(onClick = { NotificationManagerCompat.from(context).cancel(NOTICE_ID); message = "已取消 Day70 通知" }) { Text("取消本课通知") }
        // 当前系统支持时打开专门通知设置。
        Button(onClick = {
            // Android8 及以上支持应用通知设置入口。
            val intent = if (Build.VERSION.SDK_INT >= 26) Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            // 旧系统使用应用详情设置。
            else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.parse("package:${context.packageName}"))
            // 由用户主动查看和改变设置。
            context.startActivity(intent)
        }) { Text("打开通知设置") }
        // 最后显示当前流程结果。
        Text(message)
        // 权限、注册和消息投递是独立阶段。
        Text("真实推送还需 SDK 初始化、token 注册、服务端发送与到达回调；本课没有这些远程链路。")
    }
}

// 通知点击目标必须在 Manifest 注册，即使 exported=false 也可由本应用 PendingIntent 启动。
class NotificationLessonActivity : ComponentActivity() {
    // 点击会创建或恢复 Activity；本课默认启动模式不依赖 onNewIntent。
    override fun onCreate(savedInstanceState: Bundle?) {
        // 执行宿主初始化。
        super.onCreate(savedInstanceState)
        // 只允许固定课程目标，不拿 extras 任意打开 URL 或页面。
        val valid = intent.getStringExtra("lesson_id") == "day70"
        // 使用应用主题显示详情。
        setContent {
            MyApplicationTheme {
                // 使用安全区域避免状态栏遮挡。
                androidx.compose.material3.Scaffold { padding ->
                    // 显示收到的有效业务目标。
                    Column(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // 非法或缺失参数有明确兜底。
                        Text(if (valid) "Day70 · 通知点击详情" else "缺少有效课程参数")
                        // 入口由 PendingIntent 显式传递，不代表用户已登录或服务器验签。
                        Text("这是本地通知点击目标，不是远程推送到达证明。真实业务仍需检查登录和数据访问权限。")
                        // 结束详情页，回到原有任务或系统界面。
                        Button(onClick = { finish() }) { Text("关闭详情") }
                    }
                }
            }
        }
    }
}
