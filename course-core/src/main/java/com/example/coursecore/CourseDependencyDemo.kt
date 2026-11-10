// 业务入口位于 app 已经依赖的 course-core 中。
package com.example.coursecore

// 这个类型出现在公开返回值中，因此其模块使用 api 依赖。
import com.example.coursemodel.CourseLabel
// 这个工具只在方法内部调用，因此其模块使用 implementation 依赖。
import com.example.courseinternal.CourseTitleFormatter

// object 创建单例入口，调用方无需手动 new。
object CourseDependencyDemo {
    // 公开返回模型，让 app 能使用 CourseLabel 类型。
    fun label(): CourseLabel = CourseLabel("Day37 依赖管理")

    // 内部调用格式化器，对外仅返回 String。
    fun formattedTitle(): String = CourseTitleFormatter.format(label().title)
}
