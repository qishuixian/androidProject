package com.example.myapplication.day7

import java.io.IOException
import kotlinx.coroutines.delay

internal interface AsyncLessonRepository {
    suspend fun loadNames(): List<String>
}

/** 本地模拟：同一实例首次失败，后续成功，不访问网络。 */
internal class DemoAsyncLessonRepository : AsyncLessonRepository {
    private var attempts = 0

    override suspend fun loadNames(): List<String> {
        val attempt = ++attempts
        delay(1000)
        if (attempt == 1) throw IOException("首次加载模拟失败，请点击重试")
        return listOf("TextView", "Button", "LinearLayout")
    }
}
