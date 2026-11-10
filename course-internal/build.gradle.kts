// 这是独立的 Android Library 模块，不生成可安装 APK。
plugins { id("com.android.library") }

// 配置自己的资源命名空间和编译环境。
android {
    namespace = "com.example.courseinternal"
    compileSdk = 37
    defaultConfig { minSdk = 24 }
}
