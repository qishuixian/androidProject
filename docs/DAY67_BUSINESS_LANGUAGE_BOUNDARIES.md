# Day67 · 业务语言边界

入口：首页 → Day67 · 业务语言边界。
源码：[BusinessLanguageDemo.kt](../app/src/main/java/com/example/myapplication/day67/BusinessLanguageDemo.kt)。
网页：[language.html](../app/src/main/assets/day67/language.html)。

## 从前端经验理解

界面语言类似前端全站 UI 的 i18n.locale；报告语言类似生成报告请求里的一个独立参数。两者可以相同，但职责不同。用户把按钮、菜单改成英文，不代表过去生成的中文报告要被重译；选择下次生成英文报告，也不代表当前导航按钮应切成英文。

本课用两份独立状态和两份独立 Context 验证这件事。课程外壳保持中文，所谓 appLanguage 仅表示本实验的界面语言选择，不是持久化全 App 语言开关。

## 实验边界

- Native 文案由真实 Android Resources 匹配。
- 网页由真实本地 WebView 渲染，但只模拟 Laya 独立语言字典，没有真实引擎或生产桥接。
- 报告由本地资源生成固定内容，没有请求后端。
- JSON 展示的是教学契约，未发送网络，不声称是生产接口字段。
- 三种语言选择是 zh-CN、zh-HK、en-US，不处理所有可能的标签归一化或服务端枚举。

## 先分清语言归属

| 对象 | 本课来源 | 谁应该改变它 |
|---|---|---|
| Native 标题、动作文案 | appLanguage 对应的局部 UI Context | 界面语言按钮 |
| 模拟 Laya 网页文案 | 显式 appLanguage 消息 + 网页字典 | Native 主动同步界面选择 |
| 下一份报告语言 | reportLanguage 状态 | 报告语言按钮 |
| 已生成报告内容 | 生成时的语言和内容快照 | 显式重新生成，而不是 UI 重组 |
| 课程外壳、系统语言 | 原宿主配置 | 本课不修改 |

不能因为两个变量的当前值刚好相同，就把它们合并成一个全局 language。也不能因为都叫中文，就忽略简繁、地区和不同业务枚举的映射。

## 进入页面后的完整链路

```text
恢复 appLanguage=zh-CN、reportLanguage=en-US 等小字段
 → 从宿主配置分别复制 UI 与报告 Context
 → Native 子组合使用 UI 配置解析 R.string
 → 创建本地网页
 → 网页尚未就绪时暂存最新 appLanguage
 → onPageFinished 后调用固定 JS 函数
 → 网页按自己的字典更新 DOM
 → 用户显式生成报告时，按 reportLanguage 读取本地报告资源
 → 记录生成语言与内容快照
```

默认状态故意把两种语言设为不同值，让边界从一开始就可见。rememberSaveable 保存选择与生成结果，WebView 不参与保存，旋转后重建并重新发送最新界面语言。

## 改界面语言时发生什么

```text
点击“界面语言 → en-US”
 → appLanguage=en-US
 → 新 UI Context
 → Native stringResource 读取英文
 → AndroidView.update 调用 setAppLanguage(en-US)
 → 若网页加载完成且语言未发送过，发出 JS 请求
 → 网页英文 DOM 更新
```

reportLanguage 与已有 snapshotText 都不变。Android 的 R 和 LocalConfiguration 不会自动翻译网页中的 textContent；网页要有自己的文案资源，并接收明确的同步消息。

本课传参使用 JSONObject.quote 生成 JS 字符串字面量，不把未经转义的值直接拼进脚本。网页只修改固定 DOM 的 textContent，不把消息当 HTML。没有 JavaScriptInterface，也没有远程资源，CSP 禁止子资源联网。

## 为什么网页要等待 ready

AndroidView.update 可能在网页脚本定义之前运行。如果此时直接调用 window.setAppLanguage，会出现函数不存在或更新丢失。

LanguageWebView 用 pending 保存最新语言，loaded 标记本地文档加载状态，sent 避免同一文档重复发送相同语言。页面加载完成时发送最新 pending，而不是 factory 初次创建时捕获的旧值。重新加载则清空 sent，再等待新文档就绪。

本课的 ready 使用静态本地页面的 onPageFinished，不能泛化成所有复杂 Laya 页面都已完成引擎初始化。真实 SDK 应定义引擎 ready、消息版本、确认与重试契约；sent 仅表示请求已发出，不代表引擎确认成功。

## 改报告语言时发生什么

```text
点击“报告语言 → zh-HK”
 → reportLanguage=zh-HK
 → 更新报告请求草稿
 → 新 reportContext
 → Native UI 和网页字典不变
 → 已生成报告仍保留旧内容
```

只有再点击生成时：

```text
显式生成
 → 捕获 reportLanguage
 → reportContext.getString(day67_report_summary)
 → 保存 snapshotLanguage 与 snapshotText
 → 展示本次生成结果
```

实际网络业务应在发请求时固定语言快照，响应也关联请求身份，避免“请求发出后语言切换”让旧内容被错误标记成新语言。这里没有异步请求，因此不伪装网络验证；代次过滤的思路可以复用 Day58/Day64。

## 为什么已生成报告不是 stringResource 实时显示

报告是业务生成结果，不是当前导航文案。本课故意保存 snapshotText：切换 UI 语言或下一次报告选择，不会把旧结果悄悄替换掉。

真实业务通常保存报告 id、生成语言、服务端内容/版本；需要翻译或重生成时是另一个明确操作。本地固定摘要只是帮助理解，不能把课程里的资源字符串方案直接当成完整报告系统。

## 两个 Context 不要混用

UI Context 依据 appLanguage 构建；reportContext 依据 reportLanguage 构建。都复制宿主配置后 createConfigurationContext，不修改全局 Resources。Native 预览同时提供 LocalContext 与 LocalConfiguration。

如果生成报告误用 LocalContext.current 或全局 applicationContext，得到的可能是界面语言或宿主语言，报告选择就形同虚设。反过来，若用报告 Context 创建所有 UI，选择报告语言可能污染按钮和弹窗。

本课只记忆 Context 到当前组合生命周期，不放到全局或 ViewModel。保存的是字符串 tag，不是旧 Activity、Resources、WebView 或引擎实例。

## Android fallback 和网页 fallback 独立

Native 使用 values、values-zh、values-b+zh+Hant 的真实资源匹配。网页使用三条固定字典项，未知标签回退 en-US，这是本课网页显式规定的策略，不等于 Android 的算法。

生产中还可能有后端报告语言枚举。各端应明确支持集合、标准 tag 与业务值映射，以及未知值策略；不能靠 substring、字符串相等或“简繁大概相同”随意替换。Day66 的 Android 资源目录不能自动配置 Laya 或后端。

## 交叉组合验收

| 界面语言 | 报告语言 | 点击生成后应观察 |
|---|---|---|
| zh-CN | en-US | Native/网页简体，报告英文 |
| en-US | zh-CN | Native/网页英文，报告简体 |
| zh-HK | en-US | Native/网页繁体，报告英文 |
| en-US | zh-HK | Native/网页英文，报告繁体 |

另外验证：

1. 生成英文报告后，只改界面为简体：旧报告仍英文。
2. 只改下次报告选择为繁体：Native/网页不变，旧报告仍英文。
3. 再生成：报告快照变为繁体，显示对应 snapshotLanguage。
4. 网页加载期间快速切换界面语言：就绪后应使用最后一次选择，不是初始值。
5. 旋转后两组选择和报告快照恢复；网页新建后重新同步。
6. 返回首页再进入，按实际组合生命周期建立新实验，不把 rememberSaveable 等同于长期数据库。

这些是待设备验证的操作清单，源码和构建成功不能替代网页/设备上实际点击结果。

## 学完应能说明

- 界面语言、报告参数与历史报告生成语言各自的职责。
- 为什么 Android 资源不会自动翻译网页或真实 Laya 文案。
- Native → 显式语言消息 → 网页字典 → DOM 的执行链。
- 为什么网页就绪与语言请求发送需要分别管理。
- 如何避免 UI Context 污染报告语言，以及如何保留生成时的语言快照。
