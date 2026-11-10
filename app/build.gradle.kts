plugins {
    id("com.google.devtools.ksp") version "2.3.4"
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.myapplication"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Flavor 表示产品/环境差异，维度用于将不同类别的 Flavor 组合。
    flavorDimensions += "environment"
    // 两种环境与 debug/release 组合，产生四种 app 构建变体。
    productFlavors {
        create("demo") {
            // 指定此 Flavor 属于哪个维度。
            dimension = "environment"
            // 演示包独立安装，避免覆盖原包的数据。
            applicationIdSuffix = ".demo"
            // BuildConfig 字符串值需要保留 Java 字符串的双引号。
            buildConfigField("String", "ENVIRONMENT", "\"本地演示\"")
        }
        create("prod") {
            // prod 保持原 applicationId，仍只展示环境，不发起生产请求。
            dimension = "environment"
            buildConfigField("String", "ENVIRONMENT", "\"正式配置示例\"")
        }
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        // 为每个变体生成独立的 BuildConfig，页面读取编译时配置。
        buildConfig = true
        compose = true
        viewBinding = true
    }
}

dependencies {
    // app 依赖 Library 模块；Library 不反向依赖 app。
    implementation(project(":course-core"))
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.fragment:fragment-ktx:1.8.9")
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
