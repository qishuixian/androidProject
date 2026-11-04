package com.example.myapplication.day20

import com.example.myapplication.day9.Course

/** 课程本身不保存搜索词、选中样式等页面信息。 */
internal data class CoursePageState(
    val courses: List<Course> = listOf(
        Course("text-view", "TextView", "展示文本内容"),
        Course("button", "Button", "响应用户点击"),
        Course("linear-layout", "LinearLayout", "按方向排列子 View")
    ),
    val query: String = "",
    val selectedId: String? = null,
    val onlyIncomplete: Boolean = false
) {
    // 派生数据按当前状态计算，避免再保存一份导致不同步。
    val visibleCourses: List<Course>
        get() = courses.filter {
            it.title.contains(query, ignoreCase = true) && (!onlyIncomplete || !it.completed)
        }
    val selectedCourse: Course?
        get() = courses.firstOrNull { it.id == selectedId }
}
