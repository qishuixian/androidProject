package com.example.myapplication.day25

import com.example.myapplication.day9.Course
import kotlinx.coroutines.flow.map

internal class StoredCourseRepository(private val dao: CourseDao) {
    val courses = dao.observeAll().map { rows ->
        rows.map { Course(it.id, it.title, "本地数据库课程", it.completed) }
    }
    suspend fun initialize() = dao.insertInitial(listOf(
        CourseEntity("1", "TextView"),
        CourseEntity("2", "Button"),
        CourseEntity("3", "LinearLayout")
    ))
    suspend fun toggle(id: String) = dao.toggle(id)
}
