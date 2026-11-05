package com.example.myapplication.day26

import com.example.myapplication.day23.TodoApi
import com.example.myapplication.day25.CourseDao
import com.example.myapplication.day25.CourseEntity
import com.example.myapplication.day25.CourseDatabase
import androidx.room.withTransaction

internal class OfflineRepository(private val database: CourseDatabase, private val api: TodoApi) {
    private val dao = database.courses()
    val courses = dao.observeAll()
    suspend fun refresh() {
        // 只有请求和转换都完成才写库，网络失败不清空旧缓存。
        val rows = api.load(3).map { CourseEntity(it.id.toString(), it.title ?: "无标题", it.completed) }
        database.withTransaction {
            dao.clear()
            dao.insertInitial(rows)
        }
    }
}
