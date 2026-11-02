package com.example.myapplication

internal data class LayoutLesson(
    val name: String,
    val title: String,
    val summary: String,
    val analogy: String,
    val explanation: String,
    val resource: Int,
    val file: String,
    val code: String,
    val exercise: String
)

internal val layoutLessons = listOf(
    LayoutLesson(
        "LinearLayout", "线性布局", "沿一个方向，把子控件按顺序排列。",
        "类似 display: flex，通过方向控制横排或竖排。",
        "orientation 决定 horizontal（水平）或 vertical（垂直）。layout_weight 用于按比例分配剩余空间；水平等分时通常把子控件宽度设为 0dp。示例为水平等分三块。",
        R.layout.demo_linear, "demo_linear",
        """<LinearLayout android:orientation="horizontal" ...>
    <TextView android:layout_width="0dp"
        android:layout_weight="1" ... />
    <!-- 其余两个子控件也设置 weight="1" -->
</LinearLayout>""",
        "把第一个控件的 layout_weight 改为 2，观察三个控件的宽度比例变为 2:1:1。"
    ),
    LayoutLesson(
        "RelativeLayout", "相对布局", "根据父容器或其他控件的位置来摆放。",
        "类似依据父容器定位，但还可以直接声明“放在另一个控件下方”。",
        "给被参照的控件设置 id，再通过 layout_below、layout_toEndOf 等属性引用。layout_alignParentEnd 表示对齐父容器末端。示例中 B 位于 A 下方，C 对齐右侧。",
        R.layout.demo_relative, "demo_relative",
        """<TextView android:id="@+id/anchor" ... />
<TextView android:layout_below="@id/anchor" ... />
<TextView android:layout_alignParentEnd="true" ... />""",
        "给 B 增加 android:layout_toEndOf=\"@id/anchor\"，观察它与 A 的相对位置。"
    ),
    LayoutLesson(
        "TableLayout", "表格布局", "使用 TableRow 组织行，让各行的列对齐。",
        "类似 HTML 的 table、tr 和 td。",
        "每个 TableRow 是一行，行内控件形成单元格。stretchColumns 指定可拉伸的列（从 0 开始）；layout_span 可跨列。表格不会自动绘制边框。",
        R.layout.demo_table, "demo_table",
        """<TableLayout android:stretchColumns="*" ...>
    <TableRow ...>
        <TextView android:text="姓名" ... />
        <TextView android:text="课程" ... />
    </TableRow>
</TableLayout>""",
        "新增一个 TableRow，并在里面放两个 TextView，观察列是否仍然对齐。"
    ),
    LayoutLesson(
        "FrameLayout", "帧布局", "把子控件层叠放置，适合图片上的文字或角标。",
        "类似多个元素叠放在同一个 position: relative 容器内。",
        "默认从左上角放置；在没有额外 Z 轴设置时，后添加的控件通常显示在上层。子控件的 layout_gravity 控制其位置。示例为背景、居中文字和右下角角标。",
        R.layout.demo_frame, "demo_frame",
        """<FrameLayout ...>
    <TextView android:layout_gravity="center" ... />
    <TextView android:layout_gravity="bottom|end" ... />
</FrameLayout>""",
        "把角标的 layout_gravity 改为 top|end，让它移到右上角。"
    ),
    LayoutLesson(
        "AbsoluteLayout", "绝对布局", "通过固定 x、y 坐标摆放控件（已废弃）。",
        "类似 position: absolute 配合固定 left、top。",
        "layout_x、layout_y 指定子控件坐标。AbsoluteLayout 自 API 3 起已废弃，固定坐标难以适配屏幕、字体和内容变化。仅用于认识旧代码，新页面应使用其他布局。",
        R.layout.demo_absolute, "demo_absolute",
        """<AbsoluteLayout ...>
    <TextView android:layout_x="16dp"
        android:layout_y="20dp" ... />
    <TextView android:layout_x="120dp"
        android:layout_y="100dp" ... />
</AbsoluteLayout>""",
        "调整第二个控件的 layout_x，观察水平移动；思考窄屏或文字变长时为何可能重叠。"
    ),
    LayoutLesson(
        "GridLayout", "网格布局", "按行列组织控件，也支持跨行、跨列。",
        "类似 CSS Grid，将容器划分成网格。",
        "columnCount 指定列数，子控件可通过 layout_row、layout_column 指定单元格。layout_columnSpan 表示跨列，layout_columnWeight 用于分配剩余宽度。示例是两列三行。",
        R.layout.demo_grid, "demo_grid",
        """<GridLayout android:columnCount="2" ...>
    <TextView android:layout_width="0dp"
        android:layout_columnWeight="1" ... />
    <!-- 其余单元格使用相同宽度和权重 -->
</GridLayout>""",
        "把 columnCount 从 2 改为 3，观察六个控件如何重新排列。"
    )
)
