// Day35 集中比较确认弹窗、底部面板与锚点浮窗。
package com.example.myapplication.day35

// ColorDrawable 给传统浮窗提供背景。
import android.graphics.drawable.ColorDrawable
// 以下传统 View 用于展示 PopupWindow 的锚点和内容。
import android.widget.Button as ViewButton
import android.widget.LinearLayout
import android.widget.PopupWindow
// Compose 布局和按钮用于组织课程页面。
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
// 组合状态决定当前显示哪一类弹层。
import androidx.compose.runtime.*
// 保存少量界面状态，支持配置重建。
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.dp
// AndroidView 将传统 View 放入 Compose，供 PopupWindow 定位。
import androidx.compose.ui.viewinterop.AndroidView

// ModalBottomSheet 当前版本需要显式选择使用实验 API。
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OverlayDemo() {
    // 类似前端 modalType；null 表示无 Compose 弹层，字符串只保存展示类型。
    var overlay by rememberSaveable { mutableStateOf<String?>(null) }
    // 业务操作结果与弹层显隐分开，关闭弹层并不自动修改结果。
    var result by rememberSaveable { mutableStateOf("尚未操作") }
    // PopupWindow 是平台对象，只在内存中持有，不放入 rememberSaveable。
    var popup by remember { mutableStateOf<PopupWindow?>(null) }
    // 记录组件离开时的资源清理，不是 Activity 的 onDestroy 回调。
    DisposableEffect(Unit) {
        // 离开 Day35 时关闭浮窗，释放对锚点 View 和 Context 的引用。
        onDispose { popup?.dismiss(); popup = null }
    }
    // 三个按钮按纵向排列，避免同时操作多个弹层。
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 展示学习主题和当前操作结果。
        Text("Day35 · Dialog、BottomSheet、PopupWindow")
        Text("操作结果：$result")
        // 点击只修改状态；Compose 根据状态创建弹窗。
        Button(onClick = { popup?.dismiss(); overlay = "dialog" }) {
            Text("Dialog：确认完成")
        }
        // 与 Dialog 共用一个类型状态，避免两者同时显示。
        Button(onClick = { popup?.dismiss(); overlay = "sheet" }) {
            Text("BottomSheet：课程操作")
        }
        // 创建传统按钮作为锚点；factory 负责创建 View，不是每次重组都重新创建。
        AndroidView(factory = { context ->
            // context 来自当前宿主，不能用 Application Context 代替窗口环境。
            ViewButton(context).apply {
                // 标明浮窗会定位到此按钮下方。
                text = "PopupWindow：锚点菜单"
            }
        }, update = { anchor ->
            // update 更新点击监听，让回调使用本次组合中的状态。
            anchor.setOnClickListener {
                // 打开前先关闭旧浮窗，防止连续点击叠加。
                popup?.dismiss()
                // 使用锚点 View 的 Context 创建菜单内容。
                val content = ViewButton(anchor.context).apply { text = "收藏课程" }
                // 宽高自适应；true 表示可获得焦点，支持返回键关闭。
                val window = PopupWindow(content, LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, true)
                // 白色背景和外部点击关闭行为在显示前配置。
                window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.WHITE))
                window.isOutsideTouchable = true
                // 点击菜单项才更新操作结果，然后关闭窗口。
                content.setOnClickListener { result = "已收藏课程"; window.dismiss() }
                // 清理当前引用；身份判断避免旧窗口关闭时清掉新窗口。
                window.setOnDismissListener { if (popup === window) popup = null }
                // 先保存窗口对象，便于页面移除时统一关闭。
                popup = window
                // 锚点已经在界面中，使用其窗口令牌，显示到按钮下方。
                window.showAsDropDown(anchor)
            }
        })
        // 说明本例可恢复状态和临时平台窗口的区别。
        Text("旋转时 Dialog/Sheet 按类型恢复；PopupWindow 关闭，不保存窗口对象。")
    }
    // 状态为 dialog 才让弹窗进入组合，类似前端条件渲染。
    if (overlay == "dialog") {
        AlertDialog(
            // 返回或点击外部时仅关闭，不执行确认业务。
            onDismissRequest = { overlay = null },
            title = { Text("确认完成课程？") },
            text = { Text("点击确认后才更新操作结果。") },
            confirmButton = {
                // 先执行确认操作，再移除弹窗。
                TextButton(onClick = { result = "已完成课程"; overlay = null }) { Text("确认") }
            },
            dismissButton = {
                // 取消按钮只改变显隐状态。
                TextButton(onClick = { overlay = null }) { Text("取消") }
            }
        )
    }
    // 底部面板用于展示一组操作，不承担页面导航职责。
    if (overlay == "sheet") {
        ModalBottomSheet(onDismissRequest = { overlay = null }) {
            // 面板内容仍然是普通 Compose 组件。
            Text("课程操作")
            TextButton(onClick = { result = "已加入学习计划"; overlay = null }) {
                Text("加入学习计划")
            }
            // 显式关闭与下拉、返回、点击遮罩最终都移除面板。
            TextButton(onClick = { overlay = null }) { Text("关闭") }
        }
    }
}
