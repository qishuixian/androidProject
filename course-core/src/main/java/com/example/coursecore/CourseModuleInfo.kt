package com.example.coursecore

// 这是 Library 模块暴露给 app 的最小公共 API。
public object CourseModuleInfo {
    // 用于 Day36 页面验证：这个值来自 course-core，而不是 app 模块。
    public const val MODULE_NAME: String = "course-core"
    public const val RESPONSIBILITY: String = "课程业务模型与纯 Kotlin 工具"
}
