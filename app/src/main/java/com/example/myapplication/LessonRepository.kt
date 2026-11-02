package com.example.myapplication

/** Day 6 示例：Repository 负责提供课程数据，UI 不直接读取数据源。 */
internal class LessonRepository {
    fun getLessons(): List<LayoutLesson> = layoutLessons
}
