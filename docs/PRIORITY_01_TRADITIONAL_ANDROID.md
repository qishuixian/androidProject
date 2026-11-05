# 第一优先级：传统 Android 页面体系

对比 `E:\code\app\vipThinkStudent2` 后，当前 MyApplication 还需要补充传统 Android 页面体系。

`vipThinkStudent2` 是 Java、Kotlin、XML、Fragment、Compose 混合项目，而当前项目主要是单 Activity + Compose。

需要补充：

1. Activity 与 Fragment 的关系
2. Fragment 生命周期
3. Fragment 与 Activity 通信
4. Fragment 返回栈
5. Dialog、BottomSheet、PopupWindow
6. XML 布局与 ViewBinding
7. RecyclerView、Adapter、ViewHolder
8. View 的测量、布局、绘制
9. `Context`、`Activity`、`Application` 的区别
10. 配置变化下 Fragment 的恢复

对应 `vipThinkStudent2` 中的大量代码：

```text
BaseActivity
BaseFragment
FragmentViewBinding
ActivityViewBinding
RecyclerView.Adapter
Dialog
PopupWindow
```
