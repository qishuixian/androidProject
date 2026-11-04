package com.example.myapplication.day21

import com.example.myapplication.day9.Course
import kotlinx.coroutines.delay

internal interface CourseRepository {
    suspend fun loadCourses(): List<Course>
}

internal class LocalCourseRepository : CourseRepository {
    override suspend fun loadCourses(): List<Course> {
        delay(500)
        return listOf(
            Course("text-view", "TextView", "展示文本内容"),
            Course("button", "Button", "响应用户点击"),
            Course("linear-layout", "LinearLayout", "按方向排列子 View")
        )
    }
}
