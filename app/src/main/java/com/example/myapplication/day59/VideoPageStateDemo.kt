// Day59 使用真实 Surface 生命周期，但不接入真实音视频 SDK。
package com.example.myapplication.day59

// SurfaceHolder 通知底层绘制目标的创建、尺寸变化和销毁。
// Canvas 占位图帮助区分正常画布与整页黑屏。
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
// Compose 布局和操作按钮用于控制实验。
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
// 普通状态管理临时资源，Saveable 状态保存可恢复的小字段。
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
// 观察宿主前后台变化，离开组合时移除观察者。
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

// Surface 尺寸就绪后绘制静态占位图；没有视频线程与它争用画布。
private fun drawSurfacePlaceholder(holder: SurfaceHolder, density: Float) {
    // 只在底层 Surface 有效时获取绘制画布。
    if (!holder.surface.isValid) return
    // 获取当前 Surface 的 Canvas，未获得时不继续绘制。
    val canvas = holder.lockCanvas() ?: return
    // 无论绘制是否完成，都必须配对释放 Canvas。
    try {
        // 使用蓝灰色背景，避免未提交画面时的纯黑区域。
        canvas.drawColor(Color.rgb(32, 55, 77))
        // 创建带抗锯齿的白色画笔，字号随屏幕密度变化。
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            // 提高文字与背景的对比度。
            color = Color.WHITE
            // Canvas 字号单位是像素，这里转换为近似 dp 大小。
            textSize = 16f * density
        }
        // 第一行明确真实 Surface 已提供绘制目标。
        canvas.drawText("Surface 已就绪", 16f * density, 56f * density, paint)
        // 第二行避免把占位图误认为真实远端视频。
        canvas.drawText("本地占位画面 · 无真实视频", 16f * density, 88f * density, paint)
    } finally {
        // 提交这一帧并释放绘制锁。
        holder.unlockCanvasAndPost(canvas)
    }
}

// 模拟 SDK 的渲染绑定器，不持有或绘制真实视频帧。
private class PreviewBinding {
    // 只保存日志标签，不把 Surface 存入可恢复状态。
    private var target: String? = null
    // 每次重新绑定前先清理旧绑定，避免重复挂载。
    fun bind(uid: String, holder: SurfaceHolder) {
        // Surface 无效时不能模拟绑定成功。
        if (!holder.surface.isValid) return
        // 相同目标无需重复绑定。
        if (target == uid) return
        // 用户切换时解绑旧目标。
        unbind()
        // 记录当前模拟目标。
        target = uid
        // 真实 SDK 会在此设置对应用户的渲染目标。
        Log.d("Day59", "模拟绑定 uid=$uid holder=${System.identityHashCode(holder)}")
    }
    // 解绑允许重复调用，满足多个清理入口。
    fun unbind() {
        // 只有曾经绑定时才输出解绑日志。
        target?.let { Log.d("Day59", "模拟解绑 uid=$it") }
        // 清除临时绑定记录。
        target = null
    }
}

// 从首页进入的 Surface、前后台和旋转恢复实验。
@Composable
internal fun VideoPageStateDemo() {
    // 用户身份可以恢复，但并不表示真实频道已重新连接。
    var uid by rememberSaveable { mutableStateOf("1001") }
    // 是否展示画布是可恢复的页面操作状态。
    var showSurface by rememberSaveable { mutableStateOf(true) }
    // Holder 是当前 View 的临时引用，不能 rememberSaveable。
    var holder by remember { mutableStateOf<SurfaceHolder?>(null) }
    // 保存当前宿主生命周期拥有者。
    val owner = LocalLifecycleOwner.current
    // 初次显示时读取真实前台状态，之后通过观察者更新。
    var resumed by remember(owner) { mutableStateOf(owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    // 每次进入组合创建模拟绑定器，旋转后重新创建。
    val binding = remember { PreviewBinding() }
    // 只有画布存在且宿主处于交互前台，才满足本例绑定条件。
    val ready = holder != null && resumed && showSurface
    // 观察 Activity 生命周期，并在组合退出时清理观察者。
    DisposableEffect(owner, binding) {
        // 生命周期回调在主线程更新前台标志。
        val observer = LifecycleEventObserver { _, event ->
            // 日志与 Surface 日志对照，不假定二者严格固定顺序。
            Log.d("Day59", "宿主事件=$event")
            // onPause 后直接解绑，不等待 Surface 销毁。
            resumed = owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            // 本课采用暂停即解绑的策略，真实产品按 SDK 契约选择。
            if (!resumed) binding.unbind()
        }
        // 注册后接收宿主生命周期事件。
        owner.lifecycle.addObserver(observer)
        // 页面移除或拥有者替换时执行清理。
        onDispose {
            // 防止旧页面继续收到宿主事件。
            owner.lifecycle.removeObserver(observer)
            // 离开组合后不能继续绑定旧画布。
            binding.unbind()
        }
    }
    // 任一绑定条件或用户变化时，清理旧绑定并评估新绑定。
    DisposableEffect(holder, resumed, showSurface, uid) {
        // 把可空引用复制为当前这一轮使用的局部变量。
        val currentHolder = holder
        // 仅向有效且处于前台的画布模拟绑定用户。
        if (ready && currentHolder != null) binding.bind(uid, currentHolder)
        // 条件改变时解绑旧目标。
        onDispose { binding.unbind() }
    }
    // 页面展示状态与实验按钮。
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 明确本例范围，下面的占位图不是真实视频。
        Text("Day59 · 音视频页面状态（模拟绑定，无视频帧）")
        // 显示可恢复身份与不可保存的运行期条件。
        Text("uid=$uid；前台=$resumed；Surface 就绪=${holder != null}")
        // 切换用户会触发解绑旧目标、绑定新目标。
        Button(onClick = { uid = if (uid == "1001") "1002" else "1001" }) { Text("切换用户") }
        // 移除 View 观察销毁，再显示观察重新创建。
        Button(onClick = { showSurface = !showSurface }) { Text(if (showSurface) "移除画布" else "重建画布") }
        // 条件渲染决定 View 是否存在于当前组合。
        if (showSurface) {
            // AndroidView 将传统 SurfaceView 嵌入 Compose。
            AndroidView(
                // factory 在创建 View 时执行，不是每次重组都执行。
                factory = { context ->
                    // 使用当前 UI Context 创建 View。
                    SurfaceView(context).apply {
                        // 必须等待回调，创建 View 不等于 Surface 已就绪。
                        this.holder.addCallback(object : SurfaceHolder.Callback {
                            // 系统创建底层 Surface 后发布当前 Holder。
                            override fun surfaceCreated(created: SurfaceHolder) {
                                // 打印真实资源创建事件。
                                Log.d("Day59", "surfaceCreated")
                                // 通知 Compose 重新评估绑定条件。
                                holder = created
                            }
                            // 尺寸变化通常需要真实渲染器调整画面比例。
                            override fun surfaceChanged(changed: SurfaceHolder, format: Int, width: Int, height: Int) {
                                // 尺寸确定或变化后重绘静态占位图，不绘制真实视频。
                                drawSurfacePlaceholder(changed, context.resources.displayMetrics.density)
                                Log.d("Day59", "surfaceChanged ${width}x$height")
                            }
                            // 返回此回调前必须停止使用即将失效的绘制目标。
                            override fun surfaceDestroyed(destroyed: SurfaceHolder) {
                                // 立即解绑，不能只依赖后续重组清理。
                                binding.unbind()
                                // 清除旧资源引用。
                                if (holder === destroyed) holder = null
                                // 记录真实销毁事件。
                                Log.d("Day59", "surfaceDestroyed")
                            }
                        })
                    }
                },
                // 给嵌入的 View 明确尺寸，适应首页滚动容器。
                modifier = Modifier.fillMaxWidth().height(180.dp),
                // Compose 释放 View 时也进行幂等清理。
                onRelease = { view ->
                    // 防止旧绑定继续引用已经移除的画布。
                    binding.unbind()
                    // 仅清除当前 View 对应的 Holder。
                    if (holder === view.holder) holder = null
                }
            )
        }
        // 提供可以在真机执行的观察步骤。
        Text("切换到用户1002后旋转：身份可恢复，Surface 必须重建。Logcat 搜索 Day59。")
        // 提醒运行期连接与保存字段之间的边界。
        Text("蓝灰色区域是静态占位图；恢复 uid 不等于恢复 SDK 连接或收到首帧。")
    }
}
