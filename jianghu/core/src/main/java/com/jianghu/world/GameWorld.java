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
    public GameWorld(Player player, WorldClock clock, List<Npc> npcs) {
        this.player = player; this.clock = clock; this.npcs = List.copyOf(npcs);
        this.socialGraph = new NpcSocialGraph(this.npcs.stream().map(Npc::id).collect(Collectors.toSet()));
        this.familyRegistry = new NpcFamilyRegistry(this.npcs.stream().map(Npc::id).collect(Collectors.toSet()));
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
        List<String> parentIds = new java.util.ArrayList<>();
        parentIds.add(parentNpcId);
        familyRegistry.spouseOf(parentNpcId).ifPresent(parentIds::add);
        for (String id : parentIds) familyRegistry.addChild(id, childNpcId);
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
            clock.advance(1);
            if (clock.minuteOfDay() % 60 == 0) {
                for (Npc npc : npcs) {
                    life(npc.id()).passHour(npc.role(), npc.locationAt(clock.hour()), clock.hour());
                }
                settleNpcSocialLife();
            }
        }
    }
    public NpcLifeState life(String npcId) {
        if (npcs.stream().noneMatch(n -> n.id().equals(npcId))) throw new IllegalArgumentException("未知 NPC：" + npcId);
        return npcLives.computeIfAbsent(npcId, ignored -> new NpcLifeState());
    }
    /** 根据当前时辰把 NPC 的日程地点解析为地图坐标。未配置地点的 NPC 暂不显示。 */
    public List<NpcPosition> npcPositions(GameMap map) {
        return npcs.stream().map(npc -> {
            String place = npc.locationAt(clock.hour());
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
            String pairKey = bond.fromNpcId() + "->" + bond.toNpcId();
            if (lastSocialDay.getOrDefault(pairKey, 0L) == clock.day()) continue;
            Npc from = requireNpc(bond.fromNpcId());
            Npc to = requireNpc(bond.toNpcId());
            String place = from.locationAt(clock.hour());
            if (!place.equals(to.locationAt(clock.hour()))) continue;
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
