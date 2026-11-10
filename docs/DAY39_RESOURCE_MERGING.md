# Day39 · 资源合并与 source set

## 用前端理解整条流程

类似构建前把“默认配置 + 环境配置 + 特定构建配置”合并，最终组件只读取合并结果。资源优先级不是 CSS 选择器权重，也不是程序运行时依次查文件夹。

```text
选择 demoDebug
 → 收集 main、demo、debug、demoDebug 和依赖库资源
 → Gradle 处理同名同配置覆盖
 → 生成资源表并打入 APK
 → 页面用 R.string.xxx 找到资源
 → Android 按当前语言等设备配置选择值
```

入口：首页 → Day39。源码：[ResourceMergeDemo.kt](../app/src/main/java/com/example/myapplication/day39/ResourceMergeDemo.kt)。

## 实际新增目录

```text
app/src/main/res/values/day39.xml       默认
app/src/demo/res/values/day39.xml       Flavor
app/src/prod/res/values/day39.xml       Flavor
app/src/debug/res/values/day39.xml      BuildType
app/src/demoDebug/res/values/day39.xml  完整变体
course-core/src/main/res/values/day39.xml 依赖库
```

source set 不只是资源，也能包含 java/Kotlin、assets、Manifest。但规则不能混用：同名资源能覆盖，同一变体中的重复类通常会报错，不是高优先级 Kotlin 类自动替换低优先级类；Manifest 有专门合并规则。

## 同名资源到底认哪个？

单 Flavor 维度的本项目，同类型、同名称、同限定配置下：

**完整变体 > BuildType > Flavor > main > Library**

例如同为 `values` 下的 `string/day39_origin`：

| 变体 | 参与的 app source set | 最终值 |
|---|---|---|
| demoDebug | main、demo、debug、demoDebug | demoDebug 完整变体资源 |
| prodDebug | main、prod、debug | debug 构建类型资源 |
| demoRelease | main、demo | demo Flavor 资源 |
| prodRelease | main、prod | prod Flavor 资源 |

文件名不决定字符串身份；`day39.xml` 和 `strings.xml` 中相同 name 的 string 仍可能冲突。同一优先级内重复定义同配置资源通常报错，不靠文件先后顺序选一个。多 Flavor 维度还有维度优先级，本项目只有 environment 一个维度。

## app 与 Library 资源

库和 app 都定义 `day39_module_origin`，最终 app 的值优先。库独有的 `day39_library_only` 不冲突，会保留。

页面使用 `CoreR.string.day39_library_only` 访问库资源。独立 namespace 与非传递 R 下，不能认为 app.R 一定包含全部依赖库资源。库的 R 命名空间也不代表库资源运行时一定隔离、永不被 app 覆盖。实际项目应给库资源增加前缀，降低无意撞名风险。

Java 使用方式相同：`getString(R.string.day39_origin)`；Compose 使用 `stringResource(R.string.day39_origin)`。`R` 是编译生成的资源索引，不是文字内容本身。

## 构建合并与语言匹配是两件事

`values/day39.xml` 与 `values-zh/day39.xml` 是不同语言配置，可以同时保留。构建先处理同配置的覆盖，运行时再按语言等配置匹配。例如 app 的默认语言值不一定取代库中已有的更匹配语言值。缺少匹配项时通常回退到默认资源，因此应提供默认值。

这也解释了：只看 app/src/main 的文字，不能断言设备最终显示什么；还要看当前变体、依赖资源和设备配置。

## 操作实验

1. Sync，选择 demoDebug，重新运行 Day39，确认显示 demoDebug 来源。
2. 选择 prodDebug，重新运行，确认显示 debug 来源，说明 BuildType 高于 Flavor。
3. 两种都应显示 app 覆盖库的文字，以及库独有文字。
4. 如需实验 Flavor 回退，可暂时移走 debug 中对应声明并重新构建，完成后恢复；不要直接修改 build 里的生成文件。
5. 可使用 Android Studio 的 Analyze APK 查看最终 resources.arsc；本课不要求签名发布 Release。

## 构建命令与验收

```powershell
.\gradlew.bat :app:assembleDemoDebug :app:assembleProdDebug --no-configuration-cache --no-daemon
```

- 能从 Variant 推导参与构建的 source set。
- 能解释 demoDebug 和 prodDebug 为何显示不同来源。
- 能区分 app 覆盖库资源和引用库 R。
- 能区分构建合并与设备语言匹配。
- 知道源码改在 src，不能改 build 自动生成文件。
