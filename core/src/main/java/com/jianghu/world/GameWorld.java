package com.jianghu.world;

import com.jianghu.character.Player;
import com.jianghu.map.GameMap;
import com.jianghu.map.MapPoint;
import com.jianghu.npc.Npc;
import com.jianghu.time.WorldClock;
import com.jianghu.social.NpcRelationship;
import com.jianghu.social.DialogueService;
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
    public GameWorld(Player player, WorldClock clock, List<Npc> npcs) {
        this.player = player; this.clock = clock; this.npcs = List.copyOf(npcs);
    }
    public Player player() { return player; }
    public WorldClock clock() { return clock; }
    public List<Npc> npcs() { return npcs; }
    public String weather() { return weather; }
    public void setWeather(String weather) {
        if (weather == null || weather.isBlank()) throw new IllegalArgumentException("天气不能为空");
        this.weather = weather;
    }
    public void waitMinutes(int minutes) {
        clock.advance(minutes);
        player.recoverStamina(Math.max(0, minutes / 10));
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
    public Optional<String> talkToNearby(GameMap map) {
        return nearbyNpc(map).flatMap(position -> npcs.stream()
            .filter(n -> n.id().equals(position.npcId())).findFirst())
            .map(npc -> {
                NpcRelationship bond = relationship(npc.id());
                bond.talk(true);
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
