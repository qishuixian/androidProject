// Day71 演示真实、限时的前台服务，不执行录音、录屏或网络传输。
package com.example.myapplication.day71

// Service、通知、显式 Intent 和系统版本。
import android.app.Service
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
// 前台服务兼容启动与通知构造。
import androidx.core.app.ServiceCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
// 页面展示真实服务在本进程中发布的状态。
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
// 生命周期感知地收集状态，不让页面负责服务任务的生命周期。
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// Service 自己持有可取消的协程作用域。
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
// 点击通知回到应用主入口，不改变已有课程路由契约。
import com.example.myapplication.MainActivity

// 本进程观察器，不是跨进程服务查询，也不是持久化运行凭证。
private object ServiceObservation {
    // 外部只能读取状态，Service 负责更新。
    val mutable = MutableStateFlow("当前进程尚未收到 Service 生命周期事件")
    // 只读流供 Compose 展示。
    val state = mutable.asStateFlow()
}

// 首页入口仅发送开始/停止命令，不在 Composable 中运行后台任务。
@Composable
internal fun ForegroundServiceDemo() {
    // 当前 UI Context 用于前台可见时发起服务启动。
    val context = LocalContext.current
    // 这是 Service 回传的观察信息，不靠按钮本地布尔值假装已启动。
    val status by ServiceObservation.state.collectAsStateWithLifecycle()
    // 启动失败单独反馈，不能当成服务已经创建。
    var message by remember { mutableStateOf("请在当前页面可见时启动；任务最多运行30秒") }
    // 页面仍由外层课程列表滚动。
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 明确实验实际做什么。
        Text("Day71 · 前台 Service（真实限时实验）")
        // 显示服务回调而非启动请求的猜测结果。
        Text(status)
        // 启动必须由当前可见页面的用户动作触发。
        Button(onClick = {
            // 系统可能因后台启动限制或权限配置拒绝请求。
            try {
                // Android8+ 走 startForegroundService，旧系统兼容普通启动。
                ContextCompat.startForegroundService(context, Intent(context, LessonForegroundService::class.java))
                // 发起请求不等于 onCreate/onStartCommand 已完成。
                message = "已请求启动，观察状态、通知和 Logcat Day71"
            } catch (error: RuntimeException) {
                // 记录真实失败类型，未成功时不设置 running=true。
                message = "启动被拒绝：${error.javaClass.simpleName}"
            }
        }) { Text("启动30秒前台短任务") }
        // stopService 在未运行时也不会创建一个新服务。
        Button(onClick = {
            // 显式停止当前应用的内部服务。
            val stopped = context.stopService(Intent(context, LessonForegroundService::class.java))
            // 等待 onDestroy 的真实生命周期反馈。
            message = "停止请求结果=$stopped；等待生命周期日志"
        }) { Text("停止服务") }
        // 页面返回不自动停止，方便观察任务与页面生命周期不同。
        Text(message)
        // 新系统授权拒绝时仍有前台服务通知责任。
        Text("通知权限可在Day70申请。Android13+ 拒绝后不代表无需前台通知：通知栏可能不显示，但系统任务管理仍可能列出服务。")
        // 明确模拟业务及后台时限。
        Text("任务仅每秒更新本地计数，30秒自动停止；Android14+声明shortService，不是无限后台保活。返回首页或按Home不会由页面主动停止。")
        // Service 不是新线程。
        Text("Service回调默认在主线程；delay不阻塞线程。耗时计算/IO应选择合适调度器，不能直接阻塞onStartCommand。")
    }
}

// 显式启动的内部 Service；没有绑定客户端，因此 onBind 返回 null。
class LessonForegroundService : Service() {
    // 稳定渠道 id，尊重用户对渠道的设置。
    private val channelId = "day71_short_task"
    // 固定 id 用于前台通知更新和清理，与 Day70 分开。
    private val notificationId = 71
    // Service 持有自己的作用域，不依赖页面组合。
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    // 重复启动不创建多份计时任务。
    private var task: Job? = null
    // 观察日志在生命周期实际发生时更新。
    private fun report(event: String) {
        // 日志包含服务实例与线程，帮助判断是否重复创建。
        Log.d("Day71", "Service@${System.identityHashCode(this)} $event thread=${Thread.currentThread().name}")
        // 单进程中发布给页面，不用于跨进程控制。
        ServiceObservation.mutable.value = event
    }
    // 首次创建服务实例时调用一次。
    override fun onCreate() {
        // 保留系统初始化。
        super.onCreate()
        // Android8+ 必须先有渠道。
        if (Build.VERSION.SDK_INT >= 26) {
            // 渠道创建幂等，重要级别低适合持续任务状态。
            getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(channelId, "Day71 短任务", NotificationManager.IMPORTANCE_LOW))
        }
        // 此时还不能把创建当作已提升前台。
        report("onCreate：服务已创建，尚待提升前台")
    }
    // 普通启动请求进入此处，重复启动也会调用。
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 通知停止动作只停止现有任务，不重新开始计时。
        if (intent?.action == "day71.STOP") {
            // 先取消运行任务。
            task?.cancel()
            // 清除前台通知绑定。
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            // 停止服务，后续 onDestroy 清理作用域。
            stopSelf()
            // 不要求系统自动重启已停止服务。
            return START_NOT_STICKY
        }
        // 前台启动后必须及时调用，不能先等待异步业务或权限弹窗。
        try {
            // 新系统使用与 Manifest 一致的短服务类型，旧系统传0。
            val type = if (Build.VERSION.SDK_INT >= 34) ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE else 0
            // 立即绑定有效通知并提升为前台服务。
            ServiceCompat.startForeground(this, notificationId, notification(30), type)
        } catch (error: RuntimeException) {
            // 类型或启动约束失败时停止，不假装已运行。
            report("提升前台失败：${error.javaClass.simpleName}")
            // 终止本实例。
            stopSelf()
            // 不自动重启。
            return START_NOT_STICKY
        }
        // 重复点击不延长本课的30秒任务。
        if (task?.isActive == true) {
            // 此时只观察到重复命令。
            report("onStartCommand startId=$startId：任务已在运行，不重启计时")
            // 不创建新协程。
            return START_NOT_STICKY
        }
        // 前台绑定成功后才开始有限任务。
        report("onStartCommand startId=$startId：已提升前台")
        // 任务属于服务生命周期，页面移除不会取消。
        task = scope.launch {
            // 30秒小于 shortService 系统时限，本课不会主动挑战超时。
            for (remaining in 30 downTo 1) {
                // 观察实际计数阶段，不声称进行了文件同步等真实业务。
                report("前台短任务：剩余 $remaining 秒")
                // 更新同一个前台通知，不创建多个通知。
                getSystemService(NotificationManager::class.java).notify(notificationId, notification(remaining))
                // 挂起等待，不阻塞 UI 主线程。
                delay(1000)
            }
            // 正常完成时先移除前台通知。
            ServiceCompat.stopForeground(this@LessonForegroundService, ServiceCompat.STOP_FOREGROUND_REMOVE)
            // 请求结束当前服务。
            stopSelf()
        }
        // 进程被杀后不自动重启演示计时。
        return START_NOT_STICKY
    }
    // 构造真实通知，点击正文返回App，停止按钮发显式服务命令。
    private fun notification(remaining: Int): android.app.Notification {
        // 使用固定身份，避免每秒创建新的点击令牌。
        val open = PendingIntent.getActivity(this, 71, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        // 停止动作由用户点击通知触发。
        val stop = PendingIntent.getService(this, 72, Intent(this, LessonForegroundService::class.java).setAction("day71.STOP"), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        // 图标、标题和持续标志满足前台通知的基本展示要求。
        return NotificationCompat.Builder(this, channelId)
            // 使用系统教学图标，生产需设计适合的smallIcon。
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            // 告知正在进行的真实本地任务。
            .setContentTitle("Day71 前台短任务")
            // 明确任务剩余时间。
            .setContentText("剩余 $remaining 秒；无录音、录屏或联网")
            // 点击正文返回应用，不创建网页跳转。
            .setContentIntent(open)
            // 标记持续任务，不把它当不可被系统停止的保证。
            .setOngoing(true)
            // 更新时不要反复提醒。
            .setOnlyAlertOnce(true)
            // 旧系统的低优先级，现代系统主要依据渠道。
            .setPriority(NotificationCompat.PRIORITY_LOW)
            // 提供用户可见的停止入口。
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "停止", stop)
            // 返回有效通知对象。
            .build()
    }
    // Android14+ shortService 超时必须尽快停止，不能继续赖在后台。
    override fun onTimeout(startId: Int) {
        // 系统回调是兜底，正常情况下任务30秒已完成。
        report("onTimeout startId=$startId：停止短任务")
        // 取消协程工作。
        task?.cancel()
        // 移除前台状态。
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        // 必须真正停止服务，单独stopForeground不够。
        stopSelf()
    }
    // 本课没有bindService，不返回Binder。
    override fun onBind(intent: Intent?): IBinder? = null
    // 正常停止时清理，系统强杀不保证调用这里。
    override fun onDestroy() {
        // 取消所有服务作用域任务，避免旧实例更新通知。
        scope.cancel()
        // 幂等移除通知绑定。
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        // 发布真实销毁事件。
        report("onDestroy：任务取消，服务结束")
        // 保留父类销毁逻辑。
        super.onDestroy()
    }
}
