//# 告诉 Gradle：这是 Android Library 模块。
plugins {
    id("com.android.library")
}

android {
//    指定这个模块生成的 R 等类所在的命名空间。例如有资源时，可以通过： com.example.coursecore.R
    namespace = "com.example.coursecore"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
    }
}
