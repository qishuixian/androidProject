# Day54 · RTM 聊天消息

## 前端流程类比

类似聊天组件先乐观插入一条 sending 消息，再按异步确认更新原行；收到消息时统一合并，而不是每次事件都 push。Day53 解决连接，今天解决连接之上的消息状态，连接成功不等于消息送达。

入口：首页 → Day54。源码：[ChatDemo.kt](../app/src/main/java/com/example/myapplication/day54/ChatDemo.kt)。本例不接真实 RTM SDK，没有账号登录、真实网络、持久化或已读回执。

```text
输入 → 生成 clientId → SENDING → 模拟服务端确认
                             ├─ 成功：带 serverSeq 的 SENT
                             └─ 失败：FAILED → 相同 clientId 重试
模拟接收 → 类型消息 → mergeMessage → 按 serverSeq 排序 → UI
```

## 身份、顺序和状态

| 字段 | 作用 |
|---|---|
| clientId | 一次逻辑发送的身份，重试沿用，确认更新同一行 |
| serverSeq | 本例单会话内的权威顺序，不是设备时间 |
| delivery | sending/sent/failed 状态 |

示例用递增字符串生成本地 id、固定序号模拟服务端；生产需全局/会话唯一 id 和后端协议。本例单会话只按 clientId 去重，真实多会话应包含 conversationId，可能还需要 serverMessageId 对齐发送确认与服务器回显。

重复 seq=2 不应新增第二行，先收到 seq=2 再收到 seq=1 应显示正确顺序。仅仅排序不等于补齐缺失消息：真实断线恢复可能要用游标补拉历史、检测缺口，再与实时消息合并。

## SENT 不等于已读

SDK 的 send 成功可能只代表入队或服务接收，要查具体 SDK 契约。服务器确认、对方设备送达、用户已读是不同阶段，不能统一显示“已读”。失败重试复用 clientId 能辅助去重，但服务端也必须支持幂等，否则 UI 不重复不代表业务不会重复。

本例防止 SENT 被旧失败事件降级；完整系统还需事件版本、请求代次和合法状态转换，不能无限信任所有迟到回调。时间戳可展示，但不同设备时钟不可靠，不能单独承担权威排序。

## RTM SDK 和协议映射

RTM 是实时消息能力的概念，不代表一个固定 Android API。真实接入通常：初始化 SDK → 用户鉴权 → 订阅会话/频道 → 注册监听 → 发送并接收回调 → 映射业务消息 → 合并状态。

Java Listener 的 onMessage/onSuccess/onFailure 与 Kotlin 回调做同样工作，具体名称依 SDK 为准。线程也依 SDK 约定，统一切回 Main 或串行状态管理器后更新 UI；不要在多个回调中并发修改普通 MutableList。

## 生命周期与数据

本例 rememberCoroutineScope 随页面取消任务，消息是内存实验数据，离开页面重置。真实聊天通常需要 Repository、数据库与应用级会话管理，让跨页面消息不丢；监听要明确注册/解除，避免旋转后重复订阅。退出登录还应清理会话归属并过滤旧账号回调，不能只关闭当前页面。

## 验收

1. 发送后先看到 SENDING，随后同一行变成 SENT。
2. 打开下次失败再发送，出现 FAILED；重试后更新原行，不增加重复消息。
3. 接收乱序/重复样例，remote-1 排在 remote-2 前，remote-2 只出现一次。
4. 多次点击接收样例，行数不增加。
5. 能区分发送入队、服务器确认、送达、已读。
6. 能解释重试 id、服务端排序、补历史和持久化分别解决什么问题。

构建任务：`:app:assembleDemoDebug`、`:app:assembleProdDebug`。未验证真实 RTM 或 WebSocket 消息。
