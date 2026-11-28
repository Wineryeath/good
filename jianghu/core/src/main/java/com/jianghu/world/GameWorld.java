package com.jianghu.world;

import com.jianghu.character.Player;
import com.jianghu.map.GameMap;
import com.jianghu.map.MapPoint;
import com.jianghu.npc.Npc;
import com.jianghu.npc.NpcLifeState;
import com.jianghu.npc.NpcFamilyRegistry;
import com.jianghu.time.WorldClock;
import com.jianghu.social.NpcRelationship;
import com.jianghu.social.DialogueService;
import com.jianghu.social.NpcSocialGraph;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;

public final class GameWorld {
    public record NpcPosition(String npcId, String name, String role, int x, int y, String schedulePlace) {}
    private final Player player;
    private final WorldClock clock;
    private final List<Npc> npcs;
    private String weather = "晴";
    private final Map<String, NpcRelationship> relationships = new LinkedHashMap<>();
    private final Map<String, NpcLifeState> npcLives = new LinkedHashMap<>();
    private final NpcSocialGraph socialGraph;
    private final NpcFamilyRegistry familyRegistry;
    private final Map<String, Long> lastSocialDay = new LinkedHashMap<>();
    private long lastAgedDay;
    private final java.util.Set<String> deathRecorded = new java.util.LinkedHashSet<>();
    private final Map<String, String> independentHouseholds = new LinkedHashMap<>();
    private final Map<String, Integer> courtshipDays = new LinkedHashMap<>();
    public GameWorld(Player player, WorldClock clock, List<Npc> npcs) {
        this.player = player; this.clock = clock; this.npcs = List.copyOf(npcs);
        this.socialGraph = new NpcSocialGraph(this.npcs.stream().map(Npc::id).collect(Collectors.toSet()));
        this.familyRegistry = new NpcFamilyRegistry(this.npcs.stream().map(Npc::id).collect(Collectors.toSet()));
        this.lastAgedDay = clock.day();
    }
    public Player player() { return player; }
    public WorldClock clock() { return clock; }
    public List<Npc> npcs() { return npcs; }
    public NpcSocialGraph socialGraph() { return socialGraph; }
    public NpcFamilyRegistry familyRegistry() { return familyRegistry; }

    /** 登记婚姻，并同步社会关系、双方心情与人生记忆。 */
    public void marryNpcs(String firstNpcId, String secondNpcId) {
        Npc first = requireNpc(firstNpcId);
        Npc second = requireNpc(secondNpcId);
        familyRegistry.marry(firstNpcId, secondNpcId);
        String sharedHome = independentHouseholds.get(firstNpcId);
        if (sharedHome == null) sharedHome = independentHouseholds.get(secondNpcId);
        if (sharedHome != null) {
            independentHouseholds.put(firstNpcId, sharedHome);
            independentHouseholds.put(secondNpcId, sharedHome);
        }
        familyRegistry.recordEvent(NpcFamilyRegistry.EventType.MARRIAGE, List.of(firstNpcId, secondNpcId), clock.day(), first.name() + " 与 " + second.name() + " 结为夫妻");
        socialGraph.connect(firstNpcId, secondNpcId, NpcSocialGraph.RelationType.FAMILY, 80);
        socialGraph.connect(secondNpcId, firstNpcId, NpcSocialGraph.RelationType.FAMILY, 80);
        life(firstNpcId).changeMood(8);
        life(secondNpcId).changeMood(8);
        life(firstNpcId).remember("与" + second.name() + "结为夫妻");
        life(secondNpcId).remember("与" + first.name() + "结为夫妻");
    }

    /** 登记亲子关系；若父母已婚，自动将另一方也登记为孩子的父母。 */
    public void addNpcChild(String parentNpcId, String childNpcId) {
        Npc parent = requireNpc(parentNpcId);
        Npc child = requireNpc(childNpcId);
        if (familyRegistry.parentsOf(childNpcId).contains(parentNpcId)) return;
        life(childNpcId).initializeAge(0);
        List<String> parentIds = new java.util.ArrayList<>();
        parentIds.add(parentNpcId);
        familyRegistry.spouseOf(parentNpcId).ifPresent(parentIds::add);
        for (String id : parentIds) familyRegistry.addChild(id, childNpcId);
        String familyHome = parentIds.stream().map(independentHouseholds::get).filter(java.util.Objects::nonNull).findFirst().orElse(null);
        if (familyHome != null) independentHouseholds.put(childNpcId, familyHome);
        familyRegistry.recordEvent(NpcFamilyRegistry.EventType.BIRTH, java.util.stream.Stream.concat(parentIds.stream(), java.util.stream.Stream.of(childNpcId)).toList(), clock.day(), "家中添了孩子" + child.name());
        for (String id : parentIds) {
            Npc familyParent = requireNpc(id);
            socialGraph.connect(id, childNpcId, NpcSocialGraph.RelationType.FAMILY, 85);
            socialGraph.connect(childNpcId, id, NpcSocialGraph.RelationType.FAMILY, 85);
            life(id).changeMood(10);
            life(id).remember("家中添了孩子" + child.name());
            life(childNpcId).remember(familyParent.name() + "成为自己的父母");
        }
    }
    public String weather() { return weather; }
    public void setWeather(String weather) {
        if (weather == null || weather.isBlank()) throw new IllegalArgumentException("天气不能为空");
        this.weather = weather;
    }
    public void waitMinutes(int minutes) {
        advanceMinutes(minutes);
        player.recoverStamina(Math.max(0, minutes / 10));
    }
    /** 推进世界时间，并在每个整点结算所有 NPC 的生活需求与工作收入。 */
    public void advanceMinutes(int minutes) {
        if (minutes < 0) throw new IllegalArgumentException("不能倒退时间");
        for (int i = 0; i < minutes; i++) {
            long previousDay = clock.day();
            clock.advance(1);
            if (clock.day() != previousDay) { settleNpcAging(); settleNpcRomance(); }
            if (clock.minuteOfDay() % 60 == 0) {
                for (Npc npc : npcs) {
                    if (life(npc.id()).alive()) life(npc.id()).passHour(npc.role(), locationFor(npc, clock.hour()), clock.hour());
                }
                settleNpcSocialLife();
            }
        }
    }

    /** 每跨过一个游戏日，NPC 增长一天；每 360 天增长一岁并记录成长节点。 */
    private void settleNpcAging() {
        while (lastAgedDay < clock.day()) {
            lastAgedDay++;
            for (Npc npc : npcs) {
                NpcLifeState state = life(npc.id());
                if (!state.alive()) continue;
                if (state.passDay() && state.alive()) {
                    String milestone = npc.name() + " 年满 " + state.ageYears() + " 岁";
                    state.remember(milestone);
                    if (state.ageYears() == 12 || state.ageYears() == 18 || state.ageYears() == 60) {
                        state.remember(switch (state.ageYears()) {
                            case 12 -> npc.name() + "进入少年时期";
                            case 18 -> npc.name() + "成年了";
                            default -> npc.name() + "步入晚年";
                        });
                    }
                }
                if (!state.alive() && !deathRecorded.contains(npc.id())) {
                    recordNpcDeath(npc.id(), "疾病恶化，未能救治");
                }
            }
        }
    }



    /** 每个游戏日结算自主婚恋：成年单身 NPC 的互相信任关系在共同生活中逐渐发展。 */
    private void settleNpcRomance() {
        for (int i = 0; i < npcs.size(); i++) {
            Npc first = npcs.get(i);
            NpcLifeState firstLife = life(first.id());
            if (!firstLife.alive() || firstLife.ageYears() < 18
                    || familyRegistry.spouseOf(first.id()).isPresent()) continue;
            for (int j = i + 1; j < npcs.size(); j++) {
                Npc second = npcs.get(j);
                NpcLifeState secondLife = life(second.id());
                if (!secondLife.alive() || secondLife.ageYears() < 18
                        || familyRegistry.spouseOf(second.id()).isPresent()) continue;
                if (familyRegistry.familyOf(first.id()).contains(second.id())
                        || familyRegistry.familyOf(second.id()).contains(first.id())) continue;
                String key = first.id() + "|" + second.id();
                var forward = socialGraph.relation(first.id(), second.id());
                var reverse = socialGraph.relation(second.id(), first.id());
                boolean mutualTrust = forward.isPresent() && reverse.isPresent()
                    && forward.get().type() == NpcSocialGraph.RelationType.FRIEND
                    && reverse.get().type() == NpcSocialGraph.RelationType.FRIEND
                    && forward.get().strength() >= 60 && reverse.get().strength() >= 60;
                boolean together = locationFor(first, 12).equals(locationFor(second, 12));
                if (!mutualTrust || !together) {
                    courtshipDays.remove(key);
                    continue;
                }
                int days = courtshipDays.merge(key, 1, Integer::sum);
                firstLife.remember("与" + second.name() + "相处融洽（第 " + days + " 日）");
                secondLife.remember("与" + first.name() + "相处融洽（第 " + days + " 日）");
                if (days >= 30) {
                    marryNpcs(first.id(), second.id());
                    courtshipDays.remove(key);
                }
            }
        }
    }

    public int courtshipDays(String firstNpcId, String secondNpcId) {
        requireNpc(firstNpcId);
        requireNpc(secondNpcId);
        String key = npcs.indexOf(requireNpc(firstNpcId)) < npcs.indexOf(requireNpc(secondNpcId))
            ? firstNpcId + "|" + secondNpcId : secondNpcId + "|" + firstNpcId;
        return courtshipDays.getOrDefault(key, 0);
    }

    /** 成年 NPC 可建立独立居所；仅在原日程回家时改为返回新居。 */
    public void establishIndependentHousehold(String npcId, String householdPlace) {
        Npc npc = requireNpc(npcId);
        if (householdPlace == null || householdPlace.isBlank())
            throw new IllegalArgumentException("独立居所不能为空");
        NpcLifeState state = life(npcId);
        if (!state.alive()) throw new IllegalStateException("已故 NPC 无法建立居所");
        if (state.ageYears() < 18) throw new IllegalStateException("未成年 NPC 不能独立建立居所");
        if (independentHouseholds.containsKey(npcId)) throw new IllegalStateException("NPC 已有独立居所");
        independentHouseholds.put(npcId, householdPlace);
        state.remember("成年后建立独立居所：" + householdPlace);
        state.changeMood(5);
    }

    /** 查询 NPC 当前所属的家庭住所；未建立独立住所时返回空。 */
    public Optional<String> familyHouseholdOf(String npcId) {
        requireNpc(npcId);
        return Optional.ofNullable(independentHouseholds.get(npcId));
    }

    public Optional<String> independentHouseholdOf(String npcId) {
        requireNpc(npcId);
        return Optional.ofNullable(independentHouseholds.get(npcId));
    }

    public String npcLocationAt(String npcId, int hour) {
        if (hour < 0 || hour > 23) throw new IllegalArgumentException("时辰无效");
        return locationFor(requireNpc(npcId), hour);
    }

    private String locationFor(Npc npc, int hour) {
        String scheduled = npc.locationAt(hour);
        if ("home".equals(scheduled)) return independentHouseholds.getOrDefault(npc.id(), scheduled);
        return scheduled;
    }

    public NpcLifeState life(String npcId) {
        if (npcs.stream().noneMatch(n -> n.id().equals(npcId))) throw new IllegalArgumentException("未知 NPC：" + npcId);
        return npcLives.computeIfAbsent(npcId, ignored -> new NpcLifeState());
    }
    /** 根据当前时辰把 NPC 的日程地点解析为地图坐标。未配置地点的 NPC 暂不显示。 */
    public List<NpcPosition> npcPositions(GameMap map) {
        return npcs.stream().filter(npc -> life(npc.id()).alive()).map(npc -> {
            String place = locationFor(npc, clock.hour());
            Optional<MapPoint> point = map.points().stream().filter(p -> p.id().equals(place)).findFirst();
            return point.map(p -> new NpcPosition(npc.id(), npc.name(), npc.role(), p.x(), p.y(), place)).orElse(null);
        }).filter(java.util.Objects::nonNull).toList();
    }
    public NpcRelationship relationship(String npcId) {
        if (npcs.stream().noneMatch(n -> n.id().equals(npcId))) throw new IllegalArgumentException("未知 NPC：" + npcId);
        return relationships.computeIfAbsent(npcId, ignored -> new NpcRelationship());
    }
    /** 送礼：礼物价值以文计，产生有限好感；贵重礼物也会略增信任。 */
    public void giveGiftToNpc(String npcId, String itemId, int valueInWen) {
        Npc npc = requireNpc(npcId);
        if (itemId == null || itemId.isBlank() || valueInWen <= 0)
            throw new IllegalArgumentException("礼物与正价值不能为空");
        int favorGain = Math.max(1, Math.min(15, valueInWen / 10));
        NpcRelationship bond = relationship(npcId);
        bond.changeFavor(favorGain);
        if (valueInWen >= 100) bond.changeTrust(2);
        life(npcId).changeMood(3);
        life(npcId).remember("玩家赠予" + itemId + "（价值 " + valueInWen + " 文）");
    }

    /** 帮助 NPC：同时改善好感、信任与心情，并留下可追溯记忆。 */
    public void helpNpc(String npcId, String description) {
        Npc npc = requireNpc(npcId);
        requireDescription(description);
        relationship(npcId).changeFavor(8);
        relationship(npcId).changeTrust(6);
        life(npcId).changeMood(5);
        life(npcId).remember("玩家帮助了自己：" + description);
    }

    /** 欺骗 NPC 会显著损害信任；即使玩家暂时获利，关系也会留下后果。 */
    public void deceiveNpc(String npcId, String description) {
        requireNpc(npcId);
        requireDescription(description);
        relationship(npcId).changeFavor(-12);
        relationship(npcId).changeTrust(-25);
        life(npcId).changeMood(-8);
        life(npcId).remember("玩家曾欺骗自己：" + description);
    }




    /** NPC 患病；疾病会在每日结算时持续损害健康。 */
    public void giveNpcIllness(String npcId, String illness, int severity) {
        Npc npc = requireNpc(npcId);
        life(npcId).contractIllness(illness, severity);
        life(npcId).remember("染上疾病：" + illness + "（病情 " + severity + "）");
    }

    /** 为患病 NPC 治疗；治愈后停止每日疾病伤害。 */
    public void treatNpc(String npcId, String treatment, int severityReduction, int healthRestored) {
        Npc npc = requireNpc(npcId);
        requireDescription(treatment);
        life(npcId).treatIllness(severityReduction, healthRestored);
        life(npcId).remember("接受治疗：" + treatment);
        life(npcId).changeMood(3);
    }

    /** 记录 NPC 去世；死亡不会自动抹去其家庭谱系或社会关系历史。 */
    public void recordNpcDeath(String npcId, String cause) {
        Npc npc = requireNpc(npcId);
        requireDescription(cause);
        NpcLifeState state = life(npcId);
        if (deathRecorded.contains(npcId)) throw new IllegalStateException("NPC 去世事件已登记");
        if (state.alive()) state.die();
        if (state.health() != 0) throw new IllegalStateException("NPC 健康状态异常");
        deathRecorded.add(npcId);
        state.remember("去世：" + cause);
        familyRegistry.recordEvent(NpcFamilyRegistry.EventType.DEATH, List.of(npcId), clock.day(), npc.name() + " 去世：" + cause);
    }


    /** NPC 遭受伤害；健康降至零时自动登记死亡事件。 */
    public void injureNpc(String npcId, int damage, String cause) {
        requireNpc(npcId);
        if (damage <= 0) throw new IllegalArgumentException("伤害必须为正数");
        requireDescription(cause);
        NpcLifeState state = life(npcId);
        state.changeHealth(-damage);
        if (!state.alive()) recordNpcDeath(npcId, cause);
    }

    /** 家族成员继承逝者全部遗产；转移与事件登记作为一次操作完成。 */
    public long inheritNpcEstate(String deceasedNpcId, String heirNpcId) {
        Npc deceased = requireNpc(deceasedNpcId);
        Npc heir = requireNpc(heirNpcId);
        if (life(deceasedNpcId).alive()) throw new IllegalStateException("遗产所有者尚在人世");
        if (!familyRegistry.familyOf(deceasedNpcId).contains(heirNpcId))
            throw new IllegalArgumentException("继承人必须属于逝者的直系或登记家庭");
        if (!life(heirNpcId).alive()) throw new IllegalStateException("继承人已故");
        long amount = life(deceasedNpcId).transferWealthTo(life(heirNpcId));
        familyRegistry.recordEvent(NpcFamilyRegistry.EventType.INHERITANCE,
            List.of(deceasedNpcId, heirNpcId), clock.day(),
            heir.name() + " 继承 " + deceased.name() + " 的遗产 " + amount + " 文");
        life(heirNpcId).remember("继承" + deceased.name() + "的遗产（" + amount + "文）");
        return amount;
    }

    /** 家庭成员发生冲突：谱系保留，社会关系暂时转为仇敌。 */
    public void familyConflict(String firstNpcId, String secondNpcId, String description) {
        Npc first = requireNpc(firstNpcId);
        Npc second = requireNpc(secondNpcId);
        requireDescription(description);
        if (!familyRegistry.familyOf(firstNpcId).contains(secondNpcId)
                && !familyRegistry.familyOf(secondNpcId).contains(firstNpcId))
            throw new IllegalArgumentException("双方不是已登记的家庭成员");
        socialGraph.connect(firstNpcId, secondNpcId, NpcSocialGraph.RelationType.ENEMY, -30);
        socialGraph.connect(secondNpcId, firstNpcId, NpcSocialGraph.RelationType.ENEMY, -30);
        life(firstNpcId).changeMood(-6);
        life(secondNpcId).changeMood(-6);
        life(firstNpcId).remember("与家人" + second.name() + "发生争执：" + description);
        life(secondNpcId).remember("与家人" + first.name() + "发生争执：" + description);
        familyRegistry.recordEvent(NpcFamilyRegistry.EventType.CONFLICT, List.of(firstNpcId, secondNpcId), clock.day(), description);
    }

    /** 和解恢复家庭关系并记录事件。 */
    public void reconcileFamily(String firstNpcId, String secondNpcId, String description) {
        Npc first = requireNpc(firstNpcId);
        Npc second = requireNpc(secondNpcId);
        requireDescription(description);
        if (!familyRegistry.familyOf(firstNpcId).contains(secondNpcId)
                && !familyRegistry.familyOf(secondNpcId).contains(firstNpcId))
            throw new IllegalArgumentException("双方不是已登记的家庭成员");
        socialGraph.connect(firstNpcId, secondNpcId, NpcSocialGraph.RelationType.FAMILY, 70);
        socialGraph.connect(secondNpcId, firstNpcId, NpcSocialGraph.RelationType.FAMILY, 70);
        life(firstNpcId).changeMood(8);
        life(secondNpcId).changeMood(8);
        life(firstNpcId).remember("与家人" + second.name() + "和解：" + description);
        life(secondNpcId).remember("与家人" + first.name() + "和解：" + description);
        familyRegistry.recordEvent(NpcFamilyRegistry.EventType.RECONCILIATION, List.of(firstNpcId, secondNpcId), clock.day(), description);
    }

    private Npc requireNpc(String npcId) {
        return npcs.stream().filter(n -> n.id().equals(npcId)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("未知 NPC：" + npcId));
    }
    private static void requireDescription(String description) {
        if (description == null || description.isBlank())
            throw new IllegalArgumentException("行为描述不能为空");
    }

    /** 每个游戏日，同处一地的 NPC 会按既有关系发生一次日常互动。 */
    private void settleNpcSocialLife() {
        for (NpcSocialGraph.Bond bond : socialGraph.allRelations()) {
            if (!life(bond.fromNpcId()).alive() || !life(bond.toNpcId()).alive()) continue;
            String pairKey = bond.fromNpcId() + "->" + bond.toNpcId();
            if (lastSocialDay.getOrDefault(pairKey, 0L) == clock.day()) continue;
            Npc from = requireNpc(bond.fromNpcId());
            Npc to = requireNpc(bond.toNpcId());
            String place = locationFor(from, clock.hour());
            if (!place.equals(locationFor(to, clock.hour()))) continue;
            boolean warm = switch (bond.type()) {
                case FAMILY, FRIEND, MENTOR, DISCIPLE -> true;
                case RIVAL, ENEMY -> false;
            };
            socialGraph.changeStrength(from.id(), to.id(), warm ? 1 : -1);
            life(from.id()).changeMood(warm ? 2 : -1);
            life(to.id()).changeMood(warm ? 2 : -1);
            String event = warm
                ? "今日在" + place + "与" + to.name() + "相聚"
                : "今日在" + place + "与" + to.name() + "发生争执";
            life(from.id()).remember(event);
            life(to.id()).remember(warm
                ? "今日在" + place + "与" + from.name() + "相聚"
                : "今日在" + place + "与" + from.name() + "发生争执");
            lastSocialDay.put(pairKey, clock.day());
        }
    }

    public Optional<String> talkToNearby(GameMap map) {
        return nearbyNpc(map).flatMap(position -> npcs.stream()
            .filter(n -> n.id().equals(position.npcId())).findFirst())
            .map(npc -> {
                NpcRelationship bond = relationship(npc.id());
                bond.talk(true);
                life(npc.id()).remember("与玩家交谈（第 " + bond.conversations() + " 次）");
                return DialogueService.greeting(npc, clock.hour(), bond);
            });
    }
    public Optional<NpcPosition> nearbyNpc(GameMap map) {
        return npcPositions(map).stream()
            .filter(n -> Math.abs(player.x()-n.x()) + Math.abs(player.y()-n.y()) <= 1)
            .min((a,b) -> Integer.compare(Math.abs(player.x()-a.x())+Math.abs(player.y()-a.y()),
                                          Math.abs(player.x()-b.x())+Math.abs(player.y()-b.y())));
    }
}
