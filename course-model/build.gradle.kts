// 使用根工程已声明版本的 Android Library 插件。
plugins { id("com.android.library") }

// 模块拥有独立命名空间，SDK 版本保持与现有库一致。
android {
    namespace = "com.example.coursemodel"
    compileSdk = 37
    defaultConfig { minSdk = 24 }
}
