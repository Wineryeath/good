package com.jianghu.map;

import java.util.List;
import java.util.Optional;

/** 六大区域间的可行走世界地图与固定关隘连接。 */
public final class OverworldExplorer {
    public record Gate(OverworldRegion.RegionId from, int x, int y,
                       OverworldRegion.RegionId to, int arrivalX, int arrivalY, String name) {}
    private static final List<Gate> GATES = List.of(
        new Gate(OverworldRegion.RegionId.JIANGNAN, 60, 24, OverworldRegion.RegionId.NORTHERN_RIDGE, 3, 24, "北岭关道"),
        new Gate(OverworldRegion.RegionId.NORTHERN_RIDGE, 2, 24, OverworldRegion.RegionId.JIANGNAN, 59, 24, "江南驿路"),
        new Gate(OverworldRegion.RegionId.NORTHERN_RIDGE, 32, 2, OverworldRegion.RegionId.WULIN_MOUNTAINS, 32, 45, "武林天梯"),
        new Gate(OverworldRegion.RegionId.WULIN_MOUNTAINS, 32, 45, OverworldRegion.RegionId.NORTHERN_RIDGE, 32, 3, "北岭山门"),
        new Gate(OverworldRegion.RegionId.WULIN_MOUNTAINS, 2, 24, OverworldRegion.RegionId.WESTERN_SHU, 60, 24, "蜀道栈桥"),
        new Gate(OverworldRegion.RegionId.WESTERN_SHU, 61, 24, OverworldRegion.RegionId.WULIN_MOUNTAINS, 3, 24, "入山古道"),
        new Gate(OverworldRegion.RegionId.WULIN_MOUNTAINS, 61, 24, OverworldRegion.RegionId.CAPITAL, 3, 24, "京畿官道"),
        new Gate(OverworldRegion.RegionId.CAPITAL, 2, 24, OverworldRegion.RegionId.WULIN_MOUNTAINS, 60, 24, "出京古道"),
        new Gate(OverworldRegion.RegionId.CAPITAL, 61, 24, OverworldRegion.RegionId.EASTERN_SEA, 3, 24, "东海渡口"),
        new Gate(OverworldRegion.RegionId.EASTERN_SEA, 2, 24, OverworldRegion.RegionId.CAPITAL, 60, 24, "归京海路")
    );
    private OverworldRegion.RegionId regionId = OverworldRegion.RegionId.JIANGNAN;
    private int x = 12, y = 12;
    private OverworldRegion region = OverworldRegionCatalog.find(regionId);

    public OverworldRegion.RegionId regionId() { return regionId; }
    public int x() { return x; }
    public int y() { return y; }
    public OverworldRegion region() { return region; }
    public List<Gate> gates() { return GATES.stream().filter(g -> g.from() == regionId).toList(); }
    public Optional<Gate> gateAtPlayer() { return gates().stream().filter(g -> g.x() == x && g.y() == y).findFirst(); }
    public boolean move(int dx, int dy) {
        if (Math.abs(dx) > 1 || Math.abs(dy) > 1 || (dx == 0 && dy == 0))
            throw new IllegalArgumentException("每次只能向相邻地块移动");
        int nx = x + dx, ny = y + dy;
        if (!region.isWalkable(nx, ny)) return false;
        x = nx; y = ny; return true;
    }
    /** 只有站在已开通关隘上才能跨区，抵达点也必须是可通行地形。 */
    public Optional<String> travelThroughGate() {
        Optional<Gate> gate = gateAtPlayer();
        if (gate.isEmpty()) return Optional.empty();
        Gate destination = gate.get();
        OverworldRegion next = OverworldRegionCatalog.find(destination.to());
        if (!next.isWalkable(destination.arrivalX(), destination.arrivalY()))
            throw new IllegalStateException("关隘出口被地形阻断：" + destination.name());
        regionId = destination.to(); region = next;
        x = destination.arrivalX(); y = destination.arrivalY();
        return Optional.of("你经由" + destination.name() + "抵达" + regionName(regionId) + "。");
    }
    public static String regionName(OverworldRegion.RegionId id) {
        return switch (id) {
            case JIANGNAN -> "江南水乡"; case NORTHERN_RIDGE -> "北岭雪山";
            case WESTERN_SHU -> "西蜀栈道"; case EASTERN_SEA -> "东海群岛";
            case CAPITAL -> "京城"; case WULIN_MOUNTAINS -> "武林群山";
        };
    }
}