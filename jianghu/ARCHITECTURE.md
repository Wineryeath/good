# 《一人一江湖》系统建模基线

## 设计原则
1. 数据驱动：地图、NPC、物品、任务、天气以 JSON 定义；规则逻辑与显示层分离。
2. 可验证：数值规则有边界、输入校验与确定的状态迁移；随机结果使用可注入种子。
3. 可扩展：地图通过出口连接；职业、门派、官职、势力是可组合身份，不将玩家锁死在单一路线。
4. 可存档：角色状态、世界状态、任务状态分离；存档带版本号与迁移策略。
5. 不伪装完成度：六大区域当前是世界规划数据，尚未全部制作成可游玩地图；联机暂未实现。

## 核心实体与关系
- Player 1—1 CharacterProfile；Player 1—1 Attributes；Player 1—N InventoryStack。
- World 1—N Region 1—N Map；Map 1—N TileLayer / PointOfInterest / Exit。
- NPC 1—1 FactionMember；NPC 1—N ScheduleEntry / DialogueNode；Player—NPC 关系由 disposition 数值记录。
- Quest 1—N Stage 1—N Objective；事件总线将 gameplay event 转为 objective progress。
- Item 定义静态规则；InventoryStack 只保存 itemId、数量、耐久与到期时间，避免复制物品定义。

## 时间与生态
- 游戏内时间以 day + minuteOfDay(0..1439) 表示；现实时间不直接决定游戏时间。
- 一日 24 小时；时段：破晓 05:00–08:00、白昼 08:00–17:00、黄昏 17:00–20:00、夜晚 20:00–05:00。
- 一年 24 节气，四季各六节气；节气影响作物、采集资源、鱼群与节庆事件。
- 天气采用区域天气状态，不让全世界同一时刻必然同雨；天气改变移动、钓鱼、能见度和 NPC 行为。

## 角色属性
| 属性 | 范围 | 用途 |
|---|---:|---|
| 气血 | 0–100 | 战斗生存；归零触发濒危/救治流程 |
| 体力 | 0–100 | 采集、劳作、战斗与赶路消耗 |
| 饥饿 | 0–100 | 越高越饥饿；影响体力恢复 |
| 口渴 | 0–100 | 越高越口渴；影响状态与行动 |
| 心境 | 0–100 | 影响修行效率、社交与部分事件 |
| 声望 | -100–100 | 地区与阵营口碑，后续拆分为多维声望 |

## 经济与物品
- 货币统一用整数“文”结算，100 文 = 1 两；UI 可格式化显示两/文。
- 物品由稳定 ID 标识，显示名可本地化；类别包括食物、材料、工具、装备、任务物品、药品。
- 商店买入价 = basePrice × buyMultiplier；卖出价 = basePrice × sellMultiplier；四舍五入到整数文。
- 市场库存每日刷新；价格波动限制在配置区间，声望折扣有上下限。
- 易腐品记录 acquiredAt / expiresAt；过期后变质，不允许静默消失。

## NPC 与社会关系
- NPC 具有身份、所属势力、性格标签、日程、关系值、记忆事件和可用服务。
- NPC 日程使用本地游戏小时区间；跨午夜时拆成两个区间，地点缺失时回退到最近安全点。
- 关系值 -100 至 100；态度分段：敌对(-100..-41)、冷淡(-40..-11)、中立(-10..10)、友善(11..40)、亲近(41..100)。
- NPC 不应全天固定站桩；任务、天气、节日和关系事件可覆盖常规日程。

## 任务状态机
locked → available → active → ready-to-turn-in → completed
失败分支：active → failed。已完成/失败为终态，不重复发奖。
Objective 由事件触发，如 item.gathered、npc.talked、fish.caught、item.delivered；目标可有 target、count、地点与时间限制。

## 操作与碰撞约定
- 世界坐标使用整数 tile；屏幕坐标由 camera transform 得出。
- 移动先校验目标 tile 的 collision，再更新角色位置；对角移动需归一化速度，不能穿墙。
- 交互优先级：可交互 NPC → 服务设施 → 可采集资源 → 地图出口；同距离时按稳定 ID 排序。
- 输入按键需处理 keydown/keyup、窗口失焦清键，避免角色持续移动。

## 存档契约
存档根对象：{version,savedAt,player,world,quests,settings}。
- player：身份、属性、货币、背包、装备、技能、位置、关系、旗标。
- world：日期时间、季节节气、各地图资源刷新、NPC 状态、天气与事件队列。
- 每次结构变更提升 SAVE_VERSION，并提供迁移函数；禁止静默丢弃未知字段。

## 本阶段落地文件
- data/world.json：区域、地图、地标、出口与 NPC 日程数据。
- data/economy.json：属性边界、物品目录、市场规则、采集与垂钓参数。
- src/core.js：可单独测试的时间、背包、属性、任务状态与存档规则。
- 当前 index.html 仍是早期演示入口；下一轮将把 UI 接入这些数据与核心模块。
