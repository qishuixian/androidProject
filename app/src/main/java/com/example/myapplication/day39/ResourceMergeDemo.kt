// Day39 对比构建期合并后的实际资源值。
package com.example.myapplication.day39

// Column 纵向组织页面，Arrangement 设置间距。
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
// Text 展示最终的字符串资源。
import androidx.compose.material3.Text
// Composable 标记界面组件。
import androidx.compose.runtime.Composable
// stringResource 根据资源 id 获取当前配置下的字符串。
import androidx.compose.ui.res.stringResource
// dp 是 Compose 的尺寸单位。
import androidx.compose.ui.unit.dp
// BuildConfig 显示当前编译环境；R 引用 app 自己定义的资源。
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
// 库有独立的 R，使用别名避免与 app.R 同名。
import com.example.coursecore.R as CoreR

// 首页 Day39 的页面入口，没有运行时切换 source set 的按钮。
@Composable
internal fun ResourceMergeDemo() {
    // 相同资源 id 在不同 APK 中可以对应不同的合并结果。
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 当前变体决定构建时选择哪些目录。
        Text("Day39 · 资源合并与 source set")
        Text("当前：${BuildConfig.FLAVOR} / ${BuildConfig.BUILD_TYPE}")
        // 读取 app 同名资源覆盖链的最终值。
        Text("最终来源：${stringResource(R.string.day39_origin)}")
        // app 对依赖库同名资源的覆盖也会作用于最终打包结果。
        Text(stringResource(R.string.day39_module_origin))
        // 库独有资源通过库的 R 引用，而不是假定 app.R 暴露全部依赖资源。
        Text(stringResource(CoreR.string.day39_library_only))
        // 这个优先级用于同类型、同名称、同配置资源的构建合并。
        Text("同配置：完整变体 > BuildType > Flavor > main > Library")
        // values-zh 等限定符属于配置匹配，不能简单套用上面的覆盖顺序。
        Text("先构建合并，再按设备语言、方向等配置匹配资源。")
    }
}
