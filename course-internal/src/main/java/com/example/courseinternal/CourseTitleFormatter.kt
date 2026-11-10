// 包名只是代码组织方式，不决定 Gradle 的依赖暴露规则。
package com.example.courseinternal

// 必须是 public 才能被另一个模块 course-core 调用；implementation 不等于 Kotlin internal。
object CourseTitleFormatter {
    // 返回普通 String，不向调用方暴露实现模块的类型。
    fun format(title: String): String = "课程：${title.trim()}"
}
