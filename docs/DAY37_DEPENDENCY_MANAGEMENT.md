# Day37 · api、implementation 与依赖管理

## 用前端流程理解

Day36 类似 Monorepo workspace，今天关注 package 之间的编译契约。Gradle 的 api/implementation 没有与 npm 完全等价的配置，不能简单当成 export 或 dependencies/devDependencies。

```text
app --implementation--> course-core --api--> course-model
                                   --implementation--> course-internal
```

app 能直接写 `CourseLabel`（模型类型）；不能仅凭这条依赖链直接引用 `CourseTitleFormatter`（内部实现类型）。但 course-core 调用内部格式化器返回 String 可以正常运行：implementation 依赖仍参与运行和打包。

## 实际入口与调用链

首页 → Day37 → 点击“调用封装后的格式化方法”。

```text
DependencyScopeDemo（app）
  → CourseDependencyDemo.formattedTitle（course-core）
  → CourseTitleFormatter.format（course-internal）
  → String 结果
  → Compose 更新显示
```

页面显式写 `val label: CourseLabel`，验证公开模型通过 api 进入 app 编译类路径。两个新模块是为比较两种依赖而拆分的小实验，不代表实际项目必须把每个工具都拆成模块。

## 为什么模型用 api，工具用 implementation？

```kotlin
dependencies {
    // public 方法的返回值用到了 CourseLabel，调用方也需要认识这个类型。
    api(project(":course-model"))
    // 格式化器只在方法内部使用，调用方不必知道具体实现。
    implementation(project(":course-internal"))
}
```

api：依赖类型传递给消费者的编译类路径。implementation：当前模块可编译使用，不自动作为消费者的传递编译依赖。两者都可以带来运行依赖。

implementation 不是权限控制：格式化器仍是 public，其他模块显式添加依赖后就能访问。Kotlin internal 则限制同一编译模块内访问，不能把它改成 internal 再期待 course-core 跨模块调用。

Java 也遵循同一规则：`CourseLabel label = CourseDependencyDemo.INSTANCE.label();` 能否编译取决于类路径，与调用方使用 Java 还是 Kotlin 无关。

## 受控实验

1. 保留默认配置，页面可编译并显示格式化结果。
2. 暂时把 course-core 的模型依赖从 api 改为 implementation，再编译 app：显式使用 CourseLabel 会失败。随后恢复 api。
3. 不修改依赖时，尝试在 app 导入 `com.example.courseinternal.CourseTitleFormatter` 应失败；不要将错误 import 留在代码里。
4. 观察内部工具的类仍可通过 course-core 被执行，说明“不能直接编译引用”不同于“未打包”。

## 版本管理

当前 `gradle/libs.versions.toml` 集中保存 Compose、Lifecycle 等版本，模块用 `libs.xxx` 引用，类似前端集中依赖目录；项目仍有部分直接声明的版本，本课不批量迁移。

外部依赖坐标例如 `androidx.recyclerview:recyclerview:1.4.0` 对应 group、artifact、version。版本应按兼容性统一，不是数字越新越好。api/implementation 不会自动解决所有版本兼容问题。

## 查找依赖冲突

```powershell
# 查看 app 的运行依赖树，包括传递依赖。
.\gradlew.bat :app:dependencies --configuration debugRuntimeClasspath
# 定位某个库被谁引入、最终选择哪个版本。
.\gradlew.bat :app:dependencyInsight --dependency lifecycle-runtime --configuration debugRuntimeClasspath
# 对比编译依赖；实现模块不应通过 course-core 传递到 app 这里。
.\gradlew.bat :app:dependencies --configuration debugCompileClasspath
```

同一库有多个版本请求时，Gradle 通常选择较高版本，但还会受约束、平台和解析规则影响。先用 dependencyInsight 查选择理由，再决定统一版本、使用平台或排除依赖，不要一上来全局强制版本。

## 本地 Maven 与 project 依赖

- `project(":course-core")`：同一工程直接依赖源码模块，Gradle 联动构建。
- `group:artifact:version`：从声明的仓库解析已发布产物。
- `mavenLocal()`：使用本机 Maven 仓库，通常位于用户目录 `.m2/repository`；不是 Gradle 缓存目录，也不同于随意放 AAR 文件。

本项目仓库统一在 settings.gradle.kts 配置，启用了 FAIL_ON_PROJECT_REPOS，因此不要在某个模块里随意增加 repositories。使用本地 Maven 还需要发布配置与真实产物；本课只介绍边界，没有自动启用 mavenLocal 或发布库。

## 验收

- 能解释两个新模块为什么分别使用 api、implementation。
- 能追踪点击 → 公开业务入口 → 内部工具 → UI 的完整链路。
- 能说明 implementation 不等于不打包，也不等于 Kotlin internal。
- 能使用依赖树和 dependencyInsight 查版本来源。
- 能区分源码项目依赖、远程 Maven、本地 Maven。
