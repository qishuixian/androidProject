//# 告诉 Gradle：这是 Android Library 模块。
plugins {
    id("com.android.library")
}

dependencies {
    // 公开 API 返回 CourseLabel，调用方编译时也需要看到这个模型模块。
    api(project(":course-model"))
    // 格式化器只在库内部使用，不放到调用方的编译类路径。
    implementation(project(":course-internal"))
}

android {
//    指定这个模块生成的 R 等类所在的命名空间。例如有资源时，可以通过： com.example.coursecore.R
    namespace = "com.example.coursecore"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
    }
}
