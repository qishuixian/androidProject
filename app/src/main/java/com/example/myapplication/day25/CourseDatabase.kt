package com.example.myapplication.day25

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "courses")
internal data class CourseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val completed: Boolean = false
)

@Dao
internal interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY id")
    fun observeAll(): Flow<List<CourseEntity>>

    // 已有主键保留，重新进入页面不会覆盖用户的完成情况。
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitial(courses: List<CourseEntity>)

    @Query("UPDATE courses SET completed = NOT completed WHERE id = :id")
    suspend fun toggle(id: String)

    @Query("DELETE FROM courses")
    suspend fun clear()

    // Day26 在独立数据库中使用事务整体替换缓存，读者不会看到半份数据。
    @Transaction
    suspend fun replaceCache(courses: List<CourseEntity>) {
        clear()
        insertInitial(courses)
    }
}

@Database(entities = [CourseEntity::class], version = 1, exportSchema = false)
internal abstract class CourseDatabase : RoomDatabase() {
    abstract fun courses(): CourseDao
}

internal object CourseDatabaseProvider {
    @Volatile private var instance: CourseDatabase? = null
    fun get(context: Context): CourseDatabase = instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(
            context.applicationContext, CourseDatabase::class.java, "day25-courses.db"
        ).build().also { instance = it }
    }
}
