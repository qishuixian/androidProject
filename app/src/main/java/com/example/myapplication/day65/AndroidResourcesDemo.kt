// Day65 对照 XML、Kotlin、Compose 和 Library 的资源读取。
package com.example.myapplication.day65

// 传统 XML 布局通过当前 UI Context 创建。
import android.view.LayoutInflater
import android.widget.Toast
// 控件布局由外层课程页面滚动。
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
// 按钮与文字均使用新课资源。
import androidx.compose.material3.Button
import androidx.compose.material3.Text
// 次数参与重组与重建恢复。
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
// UI Context 用于点击时查询资源与显示 Toast。
import androidx.compose.ui.platform.LocalContext
// Compose 的资源函数按当前配置读取资源。
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
// AndroidView 嵌入真实 XML TextView。
import androidx.compose.ui.viewinterop.AndroidView
// app 资源和编译变体来自不同生成类。
import com.example.myapplication.R
import com.example.myapplication.BuildConfig
// Library 使用独立命名空间的 R，别名避免和 app.R 混淆。
import com.example.coursecore.R as CoreR

// 首页直接显示资源引用、格式化与合并结果。
@Composable
internal fun AndroidResourcesDemo() {
    // 次数是业务交互状态，不是资源定义的一部分。
    var count by rememberSaveable { mutableIntStateOf(0) }
    // 点击回调中不能调用 Composable 资源函数，使用 Context.getString。
    val context = LocalContext.current
    // 不用 remember 缓存已解析文案，避免配置变化后继续使用旧值。
    val title = stringResource(R.string.day65_course_title)
    // 同一资源的名称与数字 id 是不同概念。
    val resourceName = context.resources.getResourceName(R.string.day65_course_title)
    // 数字仅用于观察，不作为存储或网络传输的稳定标识。
    val resourceId = "0x${R.string.day65_course_title.toString(16)}"
    // 顺序展示资源定义到显示的完整结果。
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 页面标题也来自资源，而非硬编码业务文案。
        Text(stringResource(R.string.day65_title))
        // BuildConfig 说明安装的是哪个变体。
        Text(stringResource(R.string.day65_variant, BuildConfig.FLAVOR, BuildConfig.BUILD_TYPE))
        // 提示下方传统 View 的资源来源。
        Text(stringResource(R.string.day65_xml_label))
        // 创建传统 TextView 时由布局解析器处理 @string。
        AndroidView(
            // 使用当前 UI Context 的配置和主题解析布局。
            factory = { uiContext ->
                // 根节点就是 TextView；不手动再设置标题。
                LayoutInflater.from(uiContext).inflate(R.layout.day65_resource_panel, null, false)
            },
            // 外层 Compose 决定嵌入 View 的宽度。
            modifier = Modifier.fillMaxWidth()
        )
        // Compose 与上方 XML 读取相同资源。
        Text(stringResource(R.string.day65_compose_label, title))
        // %1$s 对应 title，%2$d 对应 count，不是字符串拼接。
        Text(stringResource(R.string.day65_progress, title, count))
        // 第二个参数选择数量类别，第三个参数才用于替换 %d。
        Text(pluralStringResource(R.plurals.day65_clicks, count, count))
        // 按钮事件修改状态，再从 Context 读取 Toast 文案。
        Button(onClick = {
            // 交互状态变化触发相关 UI 更新。
            count++
            // getString 是普通函数，可以用于事件回调。
            val message = context.getString(R.string.day65_toast, count)
            // 展示真实 Toast，不发网络请求。
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }) {
            // Composable 内容中可以调用 stringResource。
            Text(stringResource(R.string.day65_add))
        }
        // 重置只操作计数，不会改变 APK 内的资源集合。
        Button(onClick = { count = 0 }) { Text(stringResource(R.string.day65_reset)) }
        // 显示资源稳定名称与本次构建数字 id。
        Text(stringResource(R.string.day65_identity, resourceName, resourceId))
        // 这个值由构建变体决定，不由当前点击次数决定。
        Text(stringResource(R.string.day65_source_label, stringResource(R.string.day65_source)))
        // Library 独有条目通过 CoreR 访问，不假定 app.R 重新导出依赖资源。
        Text(stringResource(CoreR.string.day65_library_only))
        // 即使入口来自库 R，最终同名同配置资源也受 app 覆盖。
        Text(stringResource(CoreR.string.day65_shared_label))
        // 区分构建期合并与运行期配置匹配。
        Text(stringResource(R.string.day65_note))
    }
}
