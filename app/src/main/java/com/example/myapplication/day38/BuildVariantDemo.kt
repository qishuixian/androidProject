// Day38 页面演示编译时选择的变体，而不是运行时切换环境。
package com.example.myapplication.day38

// 布局与文字用于展示当前构建信息。
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
// 标记 Compose 界面函数。
import androidx.compose.runtime.Composable
// dp 用于组件间距。
import androidx.compose.ui.unit.dp
// BuildConfig 由 Gradle 按当前变体生成，不要手动创建或修改。
import com.example.myapplication.BuildConfig

// 首页路由进入本组件，每个 APK 显示自身编译时的配置。
@Composable
internal fun BuildVariantDemo() {
    // 用纵向布局组织字段；页面没有真实网络请求。
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Flavor 与 BuildType 组合形成变体，如 demoDebug。
        Text("Day38 · Build Variant、Flavor、Debug/Release")
        // FLAVOR 是 demo 或 prod，不是 Kotlin 包名。
        Text("Flavor：${BuildConfig.FLAVOR}")
        // BUILD_TYPE 表示 debug/release，与环境维度相互独立。
        Text("BuildType：${BuildConfig.BUILD_TYPE}")
        // 自定义字段来自 app/build.gradle.kts 中的 buildConfigField。
        Text("环境：${BuildConfig.ENVIRONMENT}")
        // applicationId 决定设备上的应用身份；namespace 仍保持不变。
        Text("安装包名：${BuildConfig.APPLICATION_ID}")
        // DEBUG 反映可调试配置，不应单独用来判断是否连接生产服务。
        Text("DEBUG：${BuildConfig.DEBUG}")
        // 切换后必须重新构建安装，已有 APK 不会自行变成另一环境。
        Text("在 Android Studio 的 Build Variants 中切换 demoDebug / prodDebug，再重新运行。")
        // 明确演示边界，prod 只是配置名称，不表示实际发布或调用线上接口。
        Text("本例只展示编译配置，不请求生产接口。")
    }
}
