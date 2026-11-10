# Day35 · Dialog、BottomSheet、PopupWindow

## 前端角色对照

| Android | 前端类比 | 适合场景 |
|---|---|---|
| Dialog | Modal 确认框 | 确认、取消等短决策 |
| BottomSheet | 底部 Drawer / ActionSheet | 一组操作、筛选面板 |
| PopupWindow | 锚点 Dropdown / Popover | 紧贴某个按钮的小菜单 |

入口：首页 → Day35。源码：[OverlayDemo.kt](../app/src/main/java/com/example/myapplication/day35/OverlayDemo.kt)。

本例 Dialog 与 BottomSheet 使用 Compose Material3 的 `AlertDialog`、`ModalBottomSheet`；PopupWindow 使用真实的传统 Android 窗口，用 AndroidView 中的按钮做锚点。它们不是 Day33 的 Fragment 导航，也不会增加 Fragment 返回栈。

## 按用户操作串起来

```text
点击“确认完成”
  → overlay = "dialog"
  → Compose 条件渲染 AlertDialog
  → 点击确认
  → 更新 result
  → overlay = null
  → 弹窗离开组合
```

类似 Vue 的 `v-if="overlay === 'dialog'"`。创建弹窗时不会自动确认；只有确认按钮的回调执行才更新结果。取消、点击外部、返回只关闭，不应当作确认。

```text
点击底部面板按钮
  → overlay = "sheet"
  → ModalBottomSheet 显示操作
  → 点击加入计划
  → 更新 result 并关闭
```

```text
点击传统锚点按钮
  → 创建 PopupWindow 和内容 View
  → showAsDropDown(anchor)
  → 点击菜单项
  → 更新 result
  → dismiss()
```

PopupWindow 是命令式 API，类似 `popover.open(anchor)`；不能只创建对象就认为它已显示。

## 状态与生命周期

- `overlay` 保存的是弹层类型，`result` 保存操作结果，二者使用 rememberSaveable 支持配置恢复。
- 不保存 Dialog、PopupWindow、View、Context 对象。这些依赖当前窗口，重建后旧对象不可复用。
- PopupWindow 使用普通 remember，页面移除或配置重建时由 DisposableEffect 关闭；不会在重建后自动重新显示。
- Compose 弹层随条件离开组合而清理；`onDismissRequest` 是关闭请求，需要我们修改状态来移除弹层。
- 应用切到后台不等于离开组合，不必认为所有弹层一定销毁。进程被终止时也不能依赖清理回调一定执行。
- 本例只保留界面结果，不写数据库；关闭页面重新进入可重置，不是长期持久化。

## 传统 Java/Kotlin API 对照

传统确认框常使用 `AlertDialog.Builder(activity)`，通过 `setPositiveButton`、`setNegativeButton` 注册按钮，再调用 `show()`。Fragment 页面中通常考虑 DialogFragment，让 FragmentManager 管理展示与恢复，不持有旧 Activity 的 Dialog。

```java
new android.app.AlertDialog.Builder(activity)
    .setTitle("确认完成课程？")
    .setPositiveButton("确认", (dialog, which) -> {
        // 确认回调中执行操作。
    })
    .setNegativeButton("取消", null)
    .show();
```

传统 Material 底部面板常见 `BottomSheetDialog` / `BottomSheetDialogFragment`；它们与本例 Compose ModalBottomSheet 是不同 API，不要混用生命周期写法。

```java
PopupWindow popup = new PopupWindow(content, width, height, true);
popup.showAsDropDown(anchor);
// 用完后关闭，不能让窗口继续引用已销毁的页面。
popup.dismiss();
```

## Context 与窗口令牌

创建 View 需要 Context，显示窗口还需要有效的窗口环境或锚点。PopupWindow 在已显示的锚点上调用 showAsDropDown，利用其窗口令牌定位。Application Context 不等价于 Activity 窗口环境；页面已经结束或锚点尚未挂载时，不应强行显示窗口，否则可能发生 BadTokenException 等问题。

## 验收实验

1. 确认框点击取消：结果不变；点击确认：显示已完成。
2. 底部面板点击加入计划：更新结果并关闭；返回或遮罩关闭不执行操作。
3. 浮窗紧贴锚点按钮；点击收藏后关闭并更新结果；返回键可以关闭。
4. 打开浮窗后返回首页，确认没有遗留窗口。
5. 打开 Dialog 或 Sheet 再旋转，观察按保存的类型重新显示；浮窗不恢复。
6. 能解释“显示状态”“业务结果”“平台窗口对象”各自保存在哪里。
