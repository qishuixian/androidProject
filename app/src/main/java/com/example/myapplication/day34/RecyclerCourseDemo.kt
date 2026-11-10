// Day34 的入口、页面外壳、列表适配器和行数据都放在此包。
package com.example.myapplication.day34

// Intent 用于从 Compose 入口打开传统 Activity。
import android.content.Intent
// Bundle 用于恢复 Activity 的少量界面状态。
import android.os.Bundle
// Log 用于观察行控件的创建和数据绑定。
import android.util.Log
// LayoutInflater 将行 XML 转为真实 View。
import android.view.LayoutInflater
// ViewGroup 表示 RecyclerView 提供的行父容器。
import android.view.ViewGroup
// 以下控件用于创建 Activity 的纵向页面及操作按钮。
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
// ComponentActivity 提供生命周期和保存状态支持。
import androidx.activity.ComponentActivity
// Compose 按钮起别名，避免与传统 Button 重名。
import androidx.compose.material3.Button as ComposeButton
// Text 描述 Compose 按钮的文字。
import androidx.compose.material3.Text
// Composable 标记 Compose 界面函数。
import androidx.compose.runtime.Composable
// LocalContext 获取入口所在宿主的 Context。
import androidx.compose.ui.platform.LocalContext
// DiffUtil 比较新旧行数据，而不是手动刷新整个列表。
import androidx.recyclerview.widget.DiffUtil
// LinearLayoutManager 控制列表按纵向排列和滚动。
import androidx.recyclerview.widget.LinearLayoutManager
// ListAdapter 提供异步差异计算和 submitList。
import androidx.recyclerview.widget.ListAdapter
// RecyclerView 负责可见行布局和 ViewHolder 复用。
import androidx.recyclerview.widget.RecyclerView
// 由 day34_course_item.xml 自动生成，不需要手写此类。
import com.example.myapplication.databinding.Day34CourseItemBinding

// val 属性不可直接改写；更新完成状态时使用 copy 创建新对象。
internal data class RecyclerCourse(
    // 稳定且唯一的业务 id，不使用列表位置充当身份。
    val id: Long,
    // 课程标题属于当前行需要显示的数据。
    val title: String,
    // 完成情况属于数据，不存放在被复用的 ViewHolder 中。
    val completed: Boolean = false
)

// 首页通过此 Compose 组件进入独立的传统列表页面。
@Composable
internal fun RecyclerCourseDemo() {
    // 当前 Context 来自 Compose 宿主，而不是即将打开的 Activity。
    val context = LocalContext.current
    // 点击时才创建 Intent 并请求 Android 打开页面。
    ComposeButton(onClick = {
        // ::class.java 获取目标 Activity 的 Java 类对象。
        context.startActivity(Intent(context, RecyclerCourseActivity::class.java))
    }) {
        // 定义按钮文字。
        Text("打开 Day34：RecyclerView 课程列表")
    }
}

// Activity 管理课程状态和按钮事件，Adapter 只负责把数据绑定到行。
class RecyclerCourseActivity : ComponentActivity() {
    // 创建足够多的数据，滚动时可观察 ViewHolder 复用。
    private var courses = List(60) { index ->
        // index 从 0 开始；id 从 1 开始，排序和删除时仍保留原 id。
        RecyclerCourse((index + 1).toLong(), "课程 ${index + 1}")
    }

    // Activity 创建时组装界面，包括旋转后的重新创建。
    override fun onCreate(savedInstanceState: Bundle?) {
        // 先完成父类初始化。
        super.onCreate(savedInstanceState)
        // 恢复课程顺序；这里只保存 id 和完成 id，不保存整批 View。
        val restoredIds = savedInstanceState?.getLongArray("ids")
        // 完成 id 转为集合，便于按 id 判断完成状态。
        val completedIds = savedInstanceState?.getLongArray("completed")?.toSet().orEmpty()
        // 初次进入保留默认课程；重建时按保存的 id 恢复顺序和删除结果。
        if (restoredIds != null) {
            // 每个 id 重新构造业务数据，保持身份不变。
            courses = restoredIds.map { id -> RecyclerCourse(id, "课程 $id", id in completedIds) }
        }
        // 创建纵向根布局，this 是当前 Activity 的 Context。
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        // 创建说明文字，提醒通过日志观察复用。
        root.addView(TextView(this).apply { text = "Day34 · 滚动、反转、删除，Logcat 搜索 Day34" })
        // 点击回调稍后才执行，先声明局部引用，随后立即完成初始化。
        lateinit var courseAdapter: CourseAdapter
        // 创建 Adapter；行只回传业务 id，由 Activity 统一更新状态。
        courseAdapter = CourseAdapter { id ->
            // map 生成新列表，copy 生成变更行的新对象，不修改旧数据快照。
            courses = courses.map { course ->
                // 只切换指定课程，其他课程继续使用原对象。
                if (course.id == id) course.copy(completed = !course.completed) else course
            }
            // 点击发生时 Adapter 已完成初始化，将新列表交给它计算差异。
            courseAdapter.submitList(courses)
        }
        // 添加反转按钮，数据顺序改变但每条课程的 id 和完成情况不变。
        root.addView(Button(this).apply {
            // 设置按钮文案。
            text = "反转顺序"
            // 点击后提交新列表，DiffUtil 可以识别课程移动。
            setOnClickListener {
                // reversed 返回新列表，不原地修改当前列表。
                courses = courses.reversed()
                // 触发异步差异计算并更新可见行。
                courseAdapter.submitList(courses)
            }
        })
        // 添加删除按钮，让用户观察删除后的行身份和列表位置区别。
        root.addView(Button(this).apply {
            // 删除的是当前顺序的第一条，不一定是 id 为 1 的课程。
            text = "删除第一条"
            // 空列表时 drop(1) 仍安全返回空列表。
            setOnClickListener {
                // 创建移除首项后的新列表。
                courses = courses.drop(1)
                // 提交删除结果，不调用 notifyDataSetChanged。
                courseAdapter.submitList(courses)
            }
        })
        // 创建 RecyclerView；它拥有自己的滚动区域，不嵌入首页 LazyColumn。
        val list = RecyclerView(this).apply {
            // 使用默认纵向线性布局管理器。
            layoutManager = LinearLayoutManager(this@RecyclerCourseActivity)
            // 将数据与行控件之间的适配工作交给 Adapter。
            adapter = courseAdapter
            // 为此动态创建的 View 设置固定 id，以参与 View 状态保存。
            id = android.R.id.list
        }
        // 高度 0 加权重 1，让列表使用操作区下方剩余高度。
        root.addView(list, LinearLayout.LayoutParams(-1, 0, 1f))
        // 将完整根布局设置为 Activity 内容。
        setContentView(root)
        // 初次提交数据，ListAdapter 随后为可见行创建并绑定 ViewHolder。
        courseAdapter.submitList(courses)
    }

    // Android 保存重建所需的界面状态时调用，不等于长期持久化。
    override fun onSaveInstanceState(outState: Bundle) {
        // 保存当前课程顺序，兼顾反转和删除。
        outState.putLongArray("ids", courses.map { it.id }.toLongArray())
        // 只保存已完成的 id，重建时再生成课程对象。
        outState.putLongArray("completed", courses.filter { it.completed }.map { it.id }.toLongArray())
        // 交给父类保存 RecyclerView 等 View 的状态。
        super.onSaveInstanceState(outState)
    }
}

// Adapter 接收点击回调；ListAdapter 的两个泛型分别是行数据和行 ViewHolder。
internal class CourseAdapter(private val onToggle: (Long) -> Unit) :
    ListAdapter<RecyclerCourse, CourseAdapter.CourseHolder>(CourseDiff) {

    // 初始化时声明每行有稳定 id，必须配合 getItemId 返回真正唯一的值。
    init {
        // 稳定 id 帮助 RecyclerView 跟踪行，但不能代替 DiffUtil 内容比较。
        setHasStableIds(true)
        // 首次异步列表仍为空时先不恢复滚动状态，等数据就绪再恢复。
        stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }

    // position 只是本次查询的位置；返回的业务 id 才是身份。
    override fun getItemId(position: Int): Long = getItem(position).id

    // 需要新行控件时调用；复用已有 ViewHolder 时不必再次创建。
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseHolder {
        // 加载行 XML，传 parent 生成正确布局参数，false 表示暂不手动挂到父布局。
        val binding = Day34CourseItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // 观察创建次数，通常少于总课程数，具体数量由布局和缓存决定。
        Log.d("Day34", "创建 ViewHolder")
        // 将控件引用交给 ViewHolder 保存。
        return CourseHolder(binding)
    }

    // 一行需要显示某条课程时调用；同一个 holder 可以先后绑定不同课程。
    override fun onBindViewHolder(holder: CourseHolder, position: Int) {
        // 读取 ListAdapter 当前快照的数据，不把 position 保存在点击回调里。
        holder.bind(getItem(position))
    }

    // ViewHolder 保存一行的 ViewBinding，减少重复查找控件。
    inner class CourseHolder(private val binding: Day34CourseItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        // 每次绑定完整覆盖本例所有可变显示状态。
        fun bind(course: RecyclerCourse) {
            // 日志显示本次绑定的数据 id，便于和创建日志对照。
            Log.d("Day34", "绑定课程 id=${course.id}")
            // 重新设置标题及完成状态，避免复用残留。
            binding.courseTitle.text = "${course.title} · ${if (course.completed) "已完成" else "未完成"}"
            // 同时覆盖按钮文字，不能只更新标题。
            binding.toggleButton.text = if (course.completed) "标记未完成" else "标记完成"
            // 绑定当前课程 id 的点击事件，不使用创建时的位置查找课程。
            binding.toggleButton.setOnClickListener { onToggle(course.id) }
        }
    }
}

// 单例差异规则：分别判断是否同一条课程，以及显示内容是否变化。
private object CourseDiff : DiffUtil.ItemCallback<RecyclerCourse>() {
    // 相同 id 表示同一条业务数据，排序后仍然成立。
    override fun areItemsTheSame(oldItem: RecyclerCourse, newItem: RecyclerCourse): Boolean =
        oldItem.id == newItem.id

    // data class 的 == 比较构造属性；标题或完成状态变化会使结果为 false。
    override fun areContentsTheSame(oldItem: RecyclerCourse, newItem: RecyclerCourse): Boolean =
        oldItem == newItem
}
