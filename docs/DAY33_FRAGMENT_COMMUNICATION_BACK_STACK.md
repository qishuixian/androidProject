# Day33 · Fragment 通信与返回栈

承接 Day32：今天实际创建 Fragment，观察“列表 View 销毁，但列表 Fragment 仍可返回”的过程。

## 入口和源码

首页 → Day33 → 打开课程示例。源码：`app/src/main/java/com/example/myapplication/day33/FragmentNavigationDemo.kt`。

`FragmentNavigationDemo` 启动独立的 `FragmentNavigationActivity`，Activity 使用 `FragmentContainerView` 承载列表与详情。此例控件用 Kotlin 创建，便于集中学习导航；同样可以在 `onCreateView` 中使用 Day31 的 XML/ViewBinding。

## 一、向详情传参数

```kotlin
bundleOf("course_id" to "kotlin-33")
```

`bundleOf` 创建 Bundle，相当于参数容器。`to` 创建键值对，不是赋值。详情通过 `requireArguments().getString("course_id")` 读取。

Java 对照：

```java
Bundle args = new Bundle();
args.putString("course_id", "kotlin-33");
CourseDetailFragment detail = new CourseDetailFragment();
detail.setArguments(args);
```

保存课程 id 即可，详情再按 id 获取数据，避免传递庞大对象。不要通过带业务参数的 Fragment 构造函数传值，系统重建需要重新实例化 Fragment。

## 二、事务与返回栈

```kotlin
parentFragmentManager.beginTransaction()
    .setReorderingAllowed(true)
    .replace(R.id.day33_container, CourseDetailFragment::class.java, args)
    .addToBackStack("course-detail")
    .commit()
```

- FragmentManager 管理 Fragment；事务描述一次页面变更。
- replace 将容器中的列表替换为详情。
- addToBackStack 将该事务加入返回栈，后面可以撤销；名字不是自动导航的路由。
- commit 异步提交，不代表下一行执行时页面已经切换完成。
- popBackStack 撤销最近一次入栈事务，恢复列表。

本例系统返回键：有事务则出栈，否则结束示例 Activity，回到 Compose 的 Day33 入口。

## 三、详情向列表返回结果

```kotlin
parentFragmentManager.setFragmentResult(
    "course_result", bundleOf("course_id" to courseId)
)
parentFragmentManager.popBackStack()
```

列表在 `onViewCreated` 中注册：

```kotlin
parentFragmentManager.setFragmentResultListener(
    "course_result", viewLifecycleOwner
) { _, data ->
    resultText.text = "收到结果：${data.getString("course_id")} 已完成"
}
```

双方必须使用同一个 FragmentManager 和相同的请求 key。列表 View 至少到 STARTED 后才接收结果；尚未就绪时，管理器暂存结果。相同 key 的多个待交付结果只保留最新一个，因此它不适合当消息队列或数据库。

`viewLifecycleOwner` 将监听绑定到这一次 View：View 销毁后移除监听，新 View 创建时重新注册。示例只显示一次返回结果，没有持久保存课程完成状态；再打开并直接返回或重建时，文字可能恢复初始值。业务状态应交给 ViewModel/Repository。

Java 注册写法：

```java
getParentFragmentManager().setFragmentResultListener(
    "course_result", getViewLifecycleOwner(), (key, data) -> {
        String id = data.getString("course_id");
    }
);
```

## 四、实验与验收

1. 打开详情，确认显示 `kotlin-33`，说明参数传入成功。
2. 点击“标记完成并返回结果”，列表显示收到结果。
3. 再进详情并直接返回：不发送新的结果，列表新建的 View 显示初始文字。
4. Logcat 搜索 Day33：进入详情时列表 onDestroyView，返回时列表 onCreateView，印证 Day32 的两个生命周期。
5. 在详情旋转屏幕，Activity 通过 savedInstanceState 判断避免重复添加列表，FragmentManager 恢复详情与返回栈。
6. 能解释参数负责向前传值、Result 负责返回一次结果、返回栈负责撤销事务，三者职责不同。

## 调用链

首页按钮 → Intent → FragmentNavigationActivity → 列表 Fragment → Bundle 参数 → replace + addToBackStack → 详情 Fragment → setFragmentResult → popBackStack → 列表新 View 接收结果。
