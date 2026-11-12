# Day66 · 多语言 fallback

入口：首页 → Day66 · 多语言 fallback。
源码：[LocaleFallbackDemo.kt](../app/src/main/java/com/example/myapplication/day66/LocaleFallbackDemo.kt)。

接 Day65：先理解资源定义、Gradle 合并与 R，本课再观察最终资源集合怎样按语言配置匹配。Day67 会区分 App 语言、业务报告语言及 Native/Laya 文案的边界。

## 实验范围

按钮创建独立的配置 Context，只影响下方预览区，不修改系统语言、不设置整个 App 的持久化语言、不重启 Activity。资源匹配由真实 Android Resources 完成，文案中的目录名只是手写的教学来源标记，不是我们自行实现的 fallback 算法。

实验只提供一个请求 Locale，避免设备系统多语言列表掩盖现象。生产可能配置多个首选语言，匹配还会受列表和系统版本影响。

## 从前端 i18n 理解

类似在一个组件子树中提供局部 i18n 实例，其他组件仍用原语言。不同的是 Android 根据编译资源表和配置做匹配，不是我们在代码里写 if(tag == ...) 返回中文字符串。

不能简单等同于“zh-HK 字典没有整个文件，所以整页改为另一个字典”：Android 按每个资源 id 选择可用条目。同一页面的一条文案可以来自地区资源，另一条来自字形资源，第三条来自默认 values。

## 目录、语言标签和内容是三件事

| 本课目录 | 表达什么 | 本课内容 |
|---|---|---|
| values | 无语言限定的默认资源 | 英文；它本身不等于英文语言限定符 |
| values-zh | 泛中文语言资源 | 本课使用简体，目录名不强制简体字形 |
| values-b+zh+Hant | 中文 + 繁体字形 | 繁体通用文案 |
| values-zh-rHK | 中文 + 香港地区 | 只提供 greeting 的香港文案 |

Hant 是繁体字形；Hans 是简体字形。地区并不等于字形：zh-TW、zh-HK 常与 Hant 关联，zh-CN 常与 Hans 关联，但不能用应用代码任意截取 tag 后两个字符来替代系统匹配。

资源目录可以用旧式 zh-rHK 或 BCP47 目录语法 b+zh+Hant 等，程序里的标准语言标签则用 zh-HK、zh-Hant、zh-Hant-HK。不要把 values-zh-rHK 或 zh-rHK 原样传给 Locale.forLanguageTag。

本课 minSdk=24，运行环境支持 BCP47 资源和 LocaleList。更旧 Android 的匹配算法与多语言能力可能不同，不能把本课设备结果直接当作所有历史版本的规则。

## 真正的读取链

```text
app 依赖 course-core
 → Gradle 按变体选 source set
 → 合并 app、Flavor、Library 各配置资源
 → 编译链接进 APK
 → 按钮修改 tag
 → 复制宿主 Configuration，设置单一 LocaleList
 → createConfigurationContext 创建局部 Context
 → Compose 子树提供局部 Context + Configuration
 → stringResource / Context.getString / XML @string / CoreR
 → 当前 Resources 针对每个 id 选择匹配值
 → 屏幕和 Toast 显示
```

import app.R 或 CoreR 只决定引用哪个模块生成的资源符号，不决定语言。库独有条目也合并到应用最终资源集合，并按查询它的 Resources 配置选择版本。

## 为什么复制 Configuration

```kotlin
// 从宿主配置复制，保留屏幕密度、方向等信息。
val config = Configuration(baseConfiguration).apply {
    // 用标准语言标签创建 Locale，并明确只设置一个首选语言。
    setLocales(LocaleList(Locale.forLanguageTag(tag)))
}
// 创建独立 Context，不修改全局资源。
val previewContext = baseContext.createConfigurationContext(config)
```

不能直接修改 LocalConfiguration.current 的对象，也不使用 Resources.updateConfiguration 去污染宿主全局资源。本课记忆的是 Context，依赖宿主 Context、配置和 tag；已解析出的字符串不通过无 key 的 remember 缓存。

## 为什么提供两个 CompositionLocal

LocalContext 供普通 Kotlin 查询、布局解析和事件回调使用；LocalConfiguration 让 Compose 资源查询感知局部配置变化。两者应指向一致的预览环境，不能 Context 是香港而 Configuration 仍是宿主旧值。

stringResource 在子组合中重新读取。AndroidView 的 factory 只在创建 View 时执行，因此 key(previewContext) 在语言变化时重建传统 TextView，避免它一直持有旧 Context 和旧解析文本。计数用 remember 放在这个 key 下，语言切换时会重置，这是实验设计。

Toast 在普通 onClick 中调用局部 context.getString，不调用 Composable stringResource，也不跳回 applicationContext。若误用全局 Context，可能出现页面是预览语言、Toast 却仍是宿主语言的情况。

这不是完整 App 语言设置方案。正式应用需依据宿主框架和 Android 版本选择受支持的应用语言 API，处理 Activity、菜单、通知、WebView、业务字段和持久化；不要把局部实验 Context 保存在全局单例或 ViewModel 中长期持有。

## 逐资源回退怎么观察

本课定义四种 app 条目：

1. greeting：默认、泛中文、繁体字形、香港地区都提供。
2. region：默认、泛中文、繁体提供，香港目录故意缺少。
3. default_only：只在 values 中提供。
4. click：默认、泛中文、繁体提供，Toast 点击时使用。

库额外提供 day66_library_message：默认和繁体字形版本，没有 app 覆盖。

选择 zh-HK 后观察：greeting 的来源标记与 region 是否不同？default_only 是否始终来自 values？Library 是否按同一局部配置选择自己的条目？同一页可以出现不同来源，这不一定是错误。

### 预期观察方向

| 请求 | 本课主要观察点 |
|---|---|
| zh-CN | 泛中文的简体文案与默认兜底条目 |
| zh-TW | 繁体字形文案，库也有繁体资源 |
| zh-HK | 香港 greeting；其余键继续按可用候选独立匹配 |
| en-US | 没有 values-en，使用默认英文条目 |
| fr-FR | 没有法文资源，观察默认兜底，并非自动翻译成法文 |

这是当前候选集合下的观察方向，实际选择以设备系统资源结果和来源标记为准。不要把本课总结成永远“地区 → 泛中文 → 默认”的固定三步链；系统还考虑字形、地区、首选语言列表及其他限定符。

## fallback 不是什么

- 不会自动翻译缺失的法文。
- 不保证同一页所有 id 来自一个目录。
- 不因 R 来自 Library 就只查库的默认值。
- 不保证某语言不存在时一定尝试开发者想象的另一个地区目录。
- 不把 values 当作固定英文语言；本课只是选择用英文作为默认内容。

必要资源应提供默认条目，不能假设任何设备总会命中 values-zh。默认缺失可能在未覆盖配置下造成资源查找失败，具体还要看编译配置及资源类型。

## 构建覆盖与配置匹配再区分一次

Day65 的 Flavor 覆盖针对同类型、同名、同限定配置的构建冲突；Day66 的 values、values-zh、values-b+zh+Hant 等是可以共存的候选配置。不能因为 app/main 中有默认英文，就认为它把库里所有语言版本都覆盖掉；同样也不能把地区目录看成更高优先级 Flavor。

如果 app 只覆盖库默认值，却遗漏库里已有的某些翻译，最终可能在不同语言看到不同来源。要先核对合并后的各配置条目，再查当前 Locale 和 Context。

## 查错顺序

1. 确认安装包和 BuildConfig，排除装错 demo/prod。
2. 检查 Gradle 模块依赖、source set、资源是否实际打包。
3. 确认使用的是哪个 Context，读取其 configuration.locales。
4. 确认标准 tag、字形、地区和资源目录语法。
5. 按具体资源 id 检查所有配置条目，不只看一个 XML 文件。
6. 检查是否 remember 缓存旧字符串，或旧 View 未按配置重建。
7. 核对设备 Android 版本、应用语言配置、系统首选语言列表与实际资源选择。

不要先手写“如果香港就把所有文案替换为某字符串”的补丁掩盖配置问题。也不要通过截取 resourceName 推断实际目录；本课依赖手写来源标记进行教学观察。

## 验收

1. 依次切换五个 tag，记录 Compose greeting、region、默认项、库资源、Context 与 XML 的结果。
2. 同一配置下 Compose、Context 与 XML 的 greeting 应一致。
3. 香港 greeting 与缺失翻译条目可来自不同配置；能解释按资源逐项匹配。
4. 用每种预览语言显示 Toast，确认其来源配置与预览区一致。
5. 宿主课程入口、系统语言与 APK Flavor 不因按钮而改变。
6. 旋转恢复所选 tag；局部 View 重建，Toast 次数无需保留。
7. 能区分默认资源、泛中文、繁体字形、香港地区、构建变体与当前语言配置。

设备未验证的结果必须标为待观察，不能只凭源码把所有语言候选选择宣称为已经实测。
