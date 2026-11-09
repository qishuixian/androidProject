// 声明当前文件所属的包，组织 Day33 的页面和入口。
package com.example.myapplication.day33

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.compose.material3.Button as ComposeButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import com.example.myapplication.R

// 标记此函数用于描述 Compose 界面。
@Composable
internal fun FragmentNavigationDemo() {
    // 获取当前 Compose 宿主环境的 Context，此处通常来自 MainActivity。
    val context = LocalContext.current
    // ComposeButton 是 Compose 按钮的别名，避免与传统 Button 同名；点击后才执行回调。
    ComposeButton(onClick = {
        // Intent 指定目标 Activity；::class.java 获取类对象，startActivity 请求打开页面。
        context.startActivity(Intent(context, FragmentNavigationActivity::class.java))
    // Text 定义按钮显示的文字。
    }) { Text("打开 Day33：课程列表 → 详情 → 返回结果") }
}

// 继承 FragmentActivity，让这个 Activity 可以通过 supportFragmentManager 管理 Fragment。
class FragmentNavigationActivity : FragmentActivity() {
    // Activity 创建时调用；Bundle? 表示恢复数据可以为空，首次启动通常为 null。
    override fun onCreate(savedInstanceState: Bundle?) {
        // 先执行父类初始化，让 FragmentManager 有机会恢复之前保存的 Fragment 和返回栈。
        super.onCreate(savedInstanceState)
        // this 是当前 Activity，作为 Context；apply 配置新容器后返回容器本身。
        // apply 内的 id 属于这个 FragmentContainerView，供后续事务定位容器。
        val container = FragmentContainerView(this).apply { id = R.id.day33_container }
        // 将容器设为 Activity 的界面，Fragment 的 View 随后会显示在这个容器里。
        setContentView(container)
        // 仅在没有恢复状态时添加初始列表，避免旋转等重建后覆盖已恢复的详情页。
        if (savedInstanceState == null) {
            // 获取当前 Activity 的 FragmentManager，开启一次描述页面变更的事务。
            supportFragmentManager.beginTransaction()
                // 允许优化事务中的生命周期状态变更，不是给课程列表排序。
                .setReorderingAllowed(true)
                // 三个参数：容器 id、要创建的 Fragment 类、传给 Fragment 的 Bundle（这里不传）。
                // ::class.java 获取 Java Class 对象，供 FragmentManager 实例化列表 Fragment。
                .add(R.id.day33_container, CourseListFragment::class.java, null)
                // 异步提交；这里没有 addToBackStack，所以初始列表事务不加入返回栈。
                .commit()
        }
        // 注册返回键回调；this 将注册绑定到 Activity 生命周期。
        // object : 创建匿名子类对象，构造参数 true 表示回调初始启用。
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            // 返回键交给本回调处理时执行这里，不是在注册时立即执行。
            override fun handleOnBackPressed() {
                // 判断可撤销事务是否存在；返回栈条目数不是 Fragment 实例数。
                if (supportFragmentManager.backStackEntryCount > 0) {
                    // 异步撤销最近一次入栈事务，例如移除详情并恢复列表界面。
                    supportFragmentManager.popBackStack()
                } else {
                    // 栈为空时结束整个示例 Activity，回到之前的 Compose Day33 页面。
                    finish()
                }
            }
        })
    }
}

// 列表 Fragment 是控制器，它的实例可能比本次创建的 View 存活更久。
class CourseListFragment : Fragment() {
    // inflater 是布局加载器，container 是父容器，state 是恢复数据；返回本次根 View。
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        // 在 Logcat 搜索 Day33，观察首次创建和返回后重新创建界面的时机。
        Log.d("Day33", "列表 onCreateView：创建新界面")
        // requireContext 获取关联的 Context；apply 配置新布局并返回布局本身。
        return LinearLayout(requireContext()).apply {
            // 子控件从上到下排列。
            orientation = LinearLayout.VERTICAL
            // 左、上、右、下内边距；这些数值的单位是像素 px，并不是 dp。
            setPadding(32, 64, 32, 32)
            // 创建结果文字；context 来自外层 LinearLayout，apply 设置初始文案。
            val resultText = TextView(context).apply { text = "尚未收到详情结果" }
            // 创建标题文字，并添加到根布局。
            addView(TextView(context).apply { text = "Day33 · 课程列表" })
            // 将结果文字添加到根布局。
            addView(resultText)
            // 创建传统 Android 按钮，配置后添加到布局。
            addView(Button(context).apply {
                // 设置进入详情按钮的文案。
                text = "打开课程 kotlin-33"
                // 注册点击事件；回调内部代码在点击时执行，而不是创建按钮时执行。
                setOnClickListener {
                    // 获取管理当前 Fragment 的管理器，开启一次页面变更事务。
                    parentFragmentManager.beginTransaction()
                        // 允许优化事务中的生命周期状态变更。
                        .setReorderingAllowed(true)
                        // 将指定容器中的列表替换为详情；传入类对象和下一行的参数 Bundle。
                        .replace(R.id.day33_container, CourseDetailFragment::class.java,
                            // to 创建键值对，bundleOf 把课程 id 放入 Bundle，供详情读取。
                            bundleOf("course_id" to "kotlin-33"))
                        // 将替换事务加入返回栈；名称用于标识事务，返回时可以撤销。
                        .addToBackStack("course-detail")
                        // 异步提交事务，由 FragmentManager 安排页面切换。
                        .commit()
                }
            })
            // 给结果控件设置 id，便于在 onViewCreated 中从本次 View 树查找它。
            resultText.id = android.R.id.text1
        }
    }

    // 本次根 View 创建后调用，适合注册绑定到界面生命周期的结果监听。
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 通过 id 找到本次界面的结果 TextView，引用不存入 Fragment 成员。
        val resultText = view.findViewById<TextView>(android.R.id.text1)
        // 请求键必须与发送方一致；绑定 View 生命周期，至少 STARTED 时交付结果。
        // _ 忽略回调中的请求键；data 是发送方传来的 Bundle。
        parentFragmentManager.setFragmentResultListener("course_result", viewLifecycleOwner) { _, data ->
            // 读取课程 id 并更新当前 View；这里只显示结果，没有持久保存完成状态。
            resultText.text = "收到结果：${data.getString("course_id")} 已完成"
            // 记录结果实际交付时机，区别于详情点击发送的时机。
            Log.d("Day33", "列表收到 Fragment Result")
        }
    }

    // 本次列表界面结束时执行；此时 Fragment 自身不一定销毁。
    override fun onDestroyView() {
        // 输出 View 销毁日志，与返回后的 onCreateView 日志对照。
        Log.d("Day33", "列表 onDestroyView：Fragment 可能仍在返回栈")
        // 执行父类销毁处理；本例没有成员级 binding 需要清空。
        super.onDestroyView()
    }
}

// 详情 Fragment 接收课程 id，并通过 Fragment Result 向列表返回结果。
class CourseDetailFragment : Fragment() {
    // 创建详情根 View；本例直接用 Kotlin 创建控件，因此没有使用 inflater 加载 XML。
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        // 读取必传参数；?: 表示为空时执行右边，这里抛错暴露调用方遗漏的参数。
        val courseId = requireArguments().getString("course_id") ?: error("缺少 course_id")
        // 使用当前关联的 Context 创建根布局，apply 配置后作为返回值。
        return LinearLayout(requireContext()).apply {
            // 让标题和两个按钮垂直排列。
            orientation = LinearLayout.VERTICAL
            // 设置四个方向的内边距，单位为 px。
            setPadding(32, 64, 32, 32)
            // 创建详情标题；$courseId 将课程 id 插入字符串，然后把文字添加到布局。
            addView(TextView(context).apply { text = "课程详情：$courseId" })
            // 创建发送结果按钮，并添加到根布局。
            addView(Button(context).apply {
                // 设置按钮显示的文字。
                text = "标记完成并返回结果"
                // 用户点击后，先发送结果，再撤销详情事务。
                setOnClickListener {
                    // 使用相同管理器和请求键发送 Bundle；列表未 STARTED 时结果暂存。
                    parentFragmentManager.setFragmentResult("course_result", bundleOf("course_id" to courseId))
                    // 异步撤销打开详情的事务，恢复列表，随后列表接收暂存的结果。
                    parentFragmentManager.popBackStack()
                }
            })
            // 创建第二个按钮，用于对比不发送结果的返回操作。
            addView(Button(context).apply {
                // 设置仅返回按钮的文字。
                text = "直接返回，不发送结果"
                // 只撤销事务，不调用 setFragmentResult，因此不产生新的结果。
                setOnClickListener { parentFragmentManager.popBackStack() }
            })
        }
    }
}
