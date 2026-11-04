// 当前文件所属的包，对应 Day25 的课程代码目录。
package com.example.myapplication.day25

import androidx.compose.foundation.layout.Arrangement // 控制布局中子元素的排列和间距。
import androidx.compose.foundation.layout.Column // 将子组件从上到下排列。
import androidx.compose.material3.Button // Material 按钮组件。
import androidx.compose.material3.Text // 文本展示组件。
import androidx.compose.runtime.Composable // 标记可以参与 Compose UI 组合的函数。
import androidx.compose.runtime.getValue // 支持通过 by 读取 Compose State 的值。
import androidx.compose.runtime.remember // 在当前组合位置记住对象，避免每次重组都重新创建。
import androidx.compose.ui.platform.LocalContext // 从当前组合环境获取 Android Context。
import androidx.compose.ui.unit.dp // 将数字转换为与屏幕密度无关的尺寸单位。
import androidx.lifecycle.ViewModel // 保存页面状态，并提供与界面重建分离的生命周期。
import androidx.lifecycle.ViewModelProvider // 提供创建和获取 ViewModel 的相关接口。
import androidx.lifecycle.compose.collectAsStateWithLifecycle // 按生命周期将 Flow 收集为 Compose State。
import androidx.lifecycle.viewModelScope // ViewModel 被清理时自动取消的协程作用域。
import androidx.lifecycle.viewmodel.compose.viewModel // 从当前 ViewModelStoreOwner 获取或创建 ViewModel。
import com.example.myapplication.day9.Course // Day9 定义的业务模型，包含 title、completed 等属性。
import kotlinx.coroutines.CancellationException // 协程取消信号，需要继续向上传递。
import kotlinx.coroutines.flow.MutableStateFlow // 保存最新值并通知收集者的可变状态流。
import kotlinx.coroutines.flow.asStateFlow // 向外提供同一个状态流的只读视图。
import kotlinx.coroutines.flow.update // 根据旧状态原子地计算并设置新状态。
import kotlinx.coroutines.launch // 启动一个协程，返回 Job。

// internal 表示模块内可见；data class 自动生成 copy、equals 等方法。
internal data class StoredCourseState(
    val courses: List<Course> = emptyList(), // 当前课程列表，默认是空的只读 List。
    val loading: Boolean = true, // 首次进入先显示读取中，收到数据库结果后改为 false。
    val saving: Boolean = false, // 是否正在保存，用于禁用按钮、防止重复写入。
    val error: String? = null // String? 允许为空；null 表示当前没有错误提示。
)

// 构造方法接收 Repository；private val 将依赖保存为私有属性；ViewModel() 调用父类构造方法。
internal class StoredCourseViewModel(private val repository: StoredCourseRepository) : ViewModel() {
    // 创建初始页面状态；val 固定容器引用，不妨碍修改容器内的 value。
    private val _state = MutableStateFlow(StoredCourseState())
    // 外部只能读取或收集，不能直接给 state.value 赋值；这里没有复制一份业务数据。
    val state = _state.asStateFlow()
    // init 在这个 ViewModel 实例构造时执行，不是每次 UI 重组都执行。
    init {
        // 启动初始化与持续观察的协程；launch 本身不代表切换到后台线程。
        viewModelScope.launch {
            // 捕获初始化或收集数据库查询流期间抛出的异常。
            try {
                // 插入初始课程；Repository 使用 IGNORE 保留已有记录，不重置完成情况。
                repository.initialize()
                // 持续收集课程列表；courses 是每次收到的新列表，Room 负责异步数据库查询。
                repository.courses.collect { courses ->
                    // it 是旧页面状态；copy 创建新状态，只替换课程和 loading，保留其他字段。
                    _state.update { it.copy(courses = courses, loading = false) }
                }
            } catch (cancelled: CancellationException) {
                // 取消表示任务应停止，不能转换成“读取失败”。
                throw cancelled
            } catch (error: Exception) {
                // 停止加载提示并显示异常类名；javaClass.simpleName 例如 SQLiteException。
                // 进入此分支后，本次收集协程结束，不会自动重新订阅查询流。
                _state.update { it.copy(loading = false, error = "数据库读取失败：${error.javaClass.simpleName}") }
            }
        }
    }
    // UI 传入课程 id，要求切换这条课程的完成状态。
    fun toggle(id: String) {
        // || 表示或：读取中或保存中就直接结束方法，避免重复操作。
        if (_state.value.loading || _state.value.saving) return
        // 在启动协程前设置保存标记，并清除上一次错误提示。
        _state.update { it.copy(saving = true, error = null) }
        // 将挂起的数据库写操作放入 ViewModel 管理的协程中。
        viewModelScope.launch {
            try {
                // Repository 调用 DAO 的 UPDATE；列表随后由 Room Flow 回传，不手动修改 UI 列表。
                repository.toggle(id)
            } catch (cancelled: CancellationException) {
                // 保持取消传播，不向用户报告业务错误。
                throw cancelled
            } catch (error: Exception) {
                // 保留当前列表，记录保存错误；it 是旧状态，error 是捕获的异常。
                _state.update { it.copy(error = "保存失败：${error.javaClass.simpleName}") }
            } finally {
                // 成功、失败或协程取消退出 try 时，都清除保存标记；这里的 update 不挂起。
                _state.update { it.copy(saving = false) }
            }
        }
    }
}

// 这个函数负责描述 Compose 页面，可从其他 Composable 中调用。
@Composable
internal fun RoomDemo() {
    // 使用应用级 Context 构造数据库，避免长生命周期对象持有 Activity。
    val context = LocalContext.current.applicationContext
    // 同一组合位置且 context 不变时复用 Factory；重新进入组合会创建新的 Factory。
    val factory = remember(context) {
        // object : 接口 表示创建实现该接口的匿名对象，类似 Java 的匿名内部类。
        object : ViewModelProvider.Factory {
            // 泛型转换无法被编译器完全验证，这里仅抑制该警告，不是取消运行时检查。
            @Suppress("UNCHECKED_CAST")
            // T 必须继承 ViewModel；modelClass 是请求创建的类型，返回值必须是 T。
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // 只支持本页面的 ViewModel；类型不匹配就抛出 IllegalArgumentException。
                require(modelClass == StoredCourseViewModel::class.java)
                // 从内到外：get(context) 获取数据库 → courses() 获取 DAO → 创建 Repository
                // → 注入 StoredCourseViewModel → as T 转换为调用方要求的类型。
                return StoredCourseViewModel(StoredCourseRepository(CourseDatabaseProvider.get(context).courses())) as T
            }
        }
    }
    // 优先取当前作用域已保存的实例，没有才调用 Factory；本项目默认是 Activity 作用域。
    val model: StoredCourseViewModel = viewModel(factory = factory)
    // 默认在生命周期达到 STARTED 时收集；by 委托读取 Compose State.value，读取处可随变化重组。
    val state by model.state.collectAsStateWithLifecycle()
    // 纵向排列组件，相邻子元素间距为 10dp；由外层 LazyColumn 负责滚动。
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Day 25 · Room 本地数据库") // 页面标题。
        Text("标记完成后写入本地数据库，重新启动 App 可以读回。无需网络。") // 操作说明。
        if (state.loading) Text("读取中…") // 只有 loading 为 true 时展示加载提示。
        state.error?.let { Text(it) } // error 非 null 才调用 let，it 就是错误字符串。
        state.courses.forEach { course -> // 遍历当前列表，course 表示这一项 Course 对象。
            // 字符串模板嵌入标题；Kotlin 的 if 是表达式，可以直接返回状态文字。
            Text("${course.title}：${if (course.completed) "已完成" else "未完成"}")
            // 点击时才调用 toggle；!saving 表示未保存时可点，保存期间禁用所有课程按钮。
            Button(onClick = { model.toggle(course.id) }, enabled = !state.saving) {
                Text(if (course.completed) "标记未完成" else "标记完成") // 按当前状态显示下一步操作名称。
            }
        }
        // && 表示且：读取结束、没有错误、列表为空，这三个条件同时满足才展示空态。
        if (!state.loading && state.error == null && state.courses.isEmpty()) Text("暂无课程")
        Text("卸载或清除应用数据会删除本地数据库；本例没有云同步。") // 本地存储范围说明。
    }
}
