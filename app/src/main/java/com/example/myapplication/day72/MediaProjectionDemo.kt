// Day72 练习 MediaProjection 授权与 VirtualDisplay 释放，不保存录屏文件。
package com.example.myapplication.day72

// MediaProjection、VirtualDisplay 和 Surface 绘制目标。
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.hardware.display.DisplayManager
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.view.Surface
import android.widget.Toast
// Compose 申请授权和观察状态。
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
// 兼容前台 Service 启动与通知。
import androidx.core.app.ServiceCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// 通知点击返回课程首页。
import com.example.myapplication.MainActivity
// 单进程实验状态。
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// 固定状态供页面看到 Service 的真实资源阶段。
private object ProjectionObservation {
    val mutable = MutableStateFlow("尚未请求 MediaProjection")
    val state = mutable.asStateFlow()
}

// 首页入口：授权由用户点击触发，页面不自动弹授权框。
@Composable
internal fun MediaProjectionDemo() {
    // 当前页面 Context 用于拿系统投影服务。
    val context = LocalContext.current
    // 系统控制器负责发起屏幕捕获授权。
    val manager = remember { context.getSystemService(MediaProjectionManager::class.java) }
    // 授权结果由 Activity Result API 交付。
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // 用户取消或系统拒绝时不启动 Service。
        if (result.resultCode != Activity.RESULT_OK || result.data == null) {
            ProjectionObservation.mutable.value = "用户取消或拒绝 MediaProjection 授权"
        } else {
            // 授权 Intent 只短暂交给 Service；不持久化到 Bundle 或数据库。
            val intent = Intent(context, ProjectionService::class.java).apply {
                action = ProjectionService.START
                putExtra(ProjectionService.RESULT_CODE, result.resultCode)
                putExtra(ProjectionService.RESULT_DATA, result.data)
            }
            // Android8+ 后台启动前台服务需走兼容入口；启动仍可能被系统约束拒绝。
            try {
                ContextCompat.startForegroundService(context, intent)
                ProjectionObservation.mutable.value = "授权成功，已请求 Projection Service"
            } catch (error: RuntimeException) {
                ProjectionObservation.mutable.value = "Service 启动失败：${error.javaClass.simpleName}"
            }
        }
    }
    // 服务状态是事实来源，页面不自行设置 running=true。
    val status by ProjectionObservation.state.collectAsStateWithLifecycle()
    // 外层课程页面滚动。
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 明确本课不保存录像。
        Text("Day72 · 屏幕录制授权（仅占位画面，不保存视频）")
        // 显示当前资源阶段。
        Text(status)
        // MediaProjection 必须由可见用户操作启动授权。
        Button(onClick = {
            // 每次重新获取当前系统管理器。
            val current = context.getSystemService(MediaProjectionManager::class.java)
            // 创建系统授权 Intent，不自行绘制授权弹窗。
            launcher.launch(current.createScreenCaptureIntent())
        }) { Text("请求屏幕录制授权") }
        // 发送停止命令，触发 Service 资源释放。
        Button(onClick = {
            context.stopService(Intent(context, ProjectionService::class.java))
            ProjectionObservation.mutable.value = "已发送停止请求，等待资源释放"
        }) { Text("停止 Projection Service") }
        // 防止把授权结果误认为录制已开始。
        Text("授权只允许创建投影；本课不编码 H.264、不写文件、不采集麦克风。")
        // 说明生命周期验证重点。
        Text("观察 Logcat 搜索 Day72：Service、VirtualDisplay、Surface 与 MediaProjection.onStop。")
    }
}

// 只承载授权结果并创建一个极短的投影实验。
class ProjectionService : android.app.Service() {
    companion object {
        const val START = "day72.START"
        const val STOP = "day72.STOP"
        const val RESULT_CODE = "result_code"
        const val RESULT_DATA = "result_data"
        private const val CHANNEL = "day72_projection"
        private const val ID = 72
    }
    // MediaProjection 与 VirtualDisplay 都是运行期资源，不能持久化。
    private var projection: MediaProjection? = null
    private var display: android.hardware.display.VirtualDisplay? = null
    private var surface: Surface? = null
    private var texture: SurfaceTexture? = null
    private var stopped = false

    // 记录资源阶段并同步到页面。
    private fun report(value: String) {
        android.util.Log.d("Day72", value)
        ProjectionObservation.mutable.value = value
    }
    // 创建通知渠道和资源销毁监听。
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(CHANNEL, "Day72 屏幕投影", NotificationManager.IMPORTANCE_LOW))
        }
        report("ProjectionService.onCreate")
    }
    // 处理授权结果或停止命令。
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == STOP) {
            releaseAll("收到停止命令")
            stopSelf()
            return START_NOT_STICKY
        }
        val code = intent?.getIntExtra(RESULT_CODE, Activity.RESULT_CANCELED) ?: Activity.RESULT_CANCELED
        val data = intent?.getParcelableExtra<Intent>(RESULT_DATA)
        if (code != Activity.RESULT_OK || data == null) {
            report("授权结果无效，未创建投影")
            stopSelf()
            return START_NOT_STICKY
        }
        // Service 启动后立即提升前台，不能先等待投影初始化。
        try {
            val type = if (Build.VERSION.SDK_INT >= 29) ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION else 0
            ServiceCompat.startForeground(this, ID, notification(), type)
        } catch (error: RuntimeException) {
            report("前台提升失败：${error.javaClass.simpleName}")
            stopSelf()
            return START_NOT_STICKY
        }
        // 重复 start 不重复创建第二个 VirtualDisplay。
        if (projection != null) {
            report("已存在投影实例，忽略重复启动")
            return START_NOT_STICKY
        }
        try {
            val manager = getSystemService(MediaProjectionManager::class.java)
            projection = manager.getMediaProjection(code, data)
            // 系统主动停止时必须释放 VirtualDisplay、Surface 和投影对象。
            projection?.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    report("MediaProjection.onStop：系统/用户停止投影")
                    releaseDisplayOnly()
                    stopSelf()
                }
            }, null)
            // 仅创建 1x1 低成本占位显示，不保存或读取帧。
            texture = SurfaceTexture(0)
            surface = Surface(texture)
            display = projection?.createVirtualDisplay("Day72Placeholder", 1, 1, 1, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, surface, null, null)
            report("VirtualDisplay 已创建：仅1x1占位，不读取视频帧")
            // 立即释放，演示“创建成功后也必须清理”，避免持续采集。
            releaseAll("占位创建后立即释放")
            stopSelf()
        } catch (error: RuntimeException) {
            report("投影创建失败：${error.javaClass.simpleName}")
            releaseAll("创建异常清理")
            stopSelf()
        }
        return START_NOT_STICKY
    }
    // 构造点击通知，用户可回到首页或停止服务。
    private fun notification(): android.app.Notification {
        val open = PendingIntent.getActivity(this, 72, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stop = PendingIntent.getService(this, 73, Intent(this, ProjectionService::class.java).setAction(STOP), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Day72 投影实验")
            .setContentText("仅创建1x1占位目标后立即释放")
            .setContentIntent(open)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "停止", stop)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    // 释放 VirtualDisplay、SurfaceTexture 和 Surface，但保留 projection 给总清理。
    private fun releaseDisplayOnly() {
        display?.release()
        display = null
        surface?.release()
        surface = null
        texture?.release()
        texture = null
    }
    // 总清理必须先停止显示目标，再停止投影，最后移除前台通知。
    private fun releaseAll(reason: String) {
        if (stopped) return
        stopped = true
        report("释放开始：$reason")
        releaseDisplayOnly()
        projection?.stop()
        projection = null
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        report("释放完成：VirtualDisplay/Surface/Projection/通知均已清理")
    }
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() {
        releaseAll("Service.onDestroy")
        super.onDestroy()
        report("ProjectionService.onDestroy")
    }
}

// Activity 仅用于满足投影授权由可见 Activity 发起的系统流程。
class ProjectionPermissionActivity : ComponentActivity()



