// 公开模型属于独立模块，不反向依赖 app 或 course-core。
package com.example.coursemodel

// data class 根据构造属性生成 equals、copy 等方法。
data class CourseLabel(
    // 调用方可以读取标题，但不能直接重写 val 属性。
    val title: String
)
