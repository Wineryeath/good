package com.jianghu.map;

import java.util.EnumMap;
import java.util.Map;

/** Deterministic handcrafted macro-regions for the world map. */
public final class OverworldRegionCatalog {
    private static final int WIDTH = 64, HEIGHT = 48;
    private OverworldRegionCatalog() {}
    public static Map<OverworldRegion.RegionId, OverworldRegion> all() {
        Map<OverworldRegion.RegionId, OverworldRegion> regions = new EnumMap<>(OverworldRegion.RegionId.class);
        for (OverworldRegion.RegionId id : OverworldRegion.RegionId.values()) regions.put(id, build(id));
        return Map.copyOf(regions);
    }
    public static OverworldRegion find(OverworldRegion.RegionId id) { return build(id); }
    private static OverworldRegion build(OverworldRegion.RegionId id) {
        TerrainKind[][] map = new TerrainKind[HEIGHT][WIDTH];
        for (int y = 0; y < HEIGHT; y++) for (int x = 0; x < WIDTH; x++) map[y][x] = TerrainKind.GRASS;
        for (int x = 0; x < WIDTH; x++) { map[0][x] = TerrainKind.CLIFF; map[HEIGHT - 1][x] = TerrainKind.CLIFF; }
        for (int y = 0; y < HEIGHT; y++) { map[y][0] = TerrainKind.CLIFF; map[y][WIDTH - 1] = TerrainKind.CLIFF; }
        switch (id) {
            case JIANGNAN -> {
                for (int y = 4; y < 43; y++) for (int x = 27 + (int)(5 * Math.sin(y / 5.0)); x < 34 + (int)(5 * Math.sin(y / 5.0)); x++) map[y][x] = TerrainKind.SHALLOW_WATER;
                for (int x = 27; x < 34; x++) map[23][x] = TerrainKind.BRIDGE;
                patch(map, 7, 7, 10, 8, TerrainKind.TOWN_GROUND); patch(map, 45, 31, 7, 6, TerrainKind.FOREST);
                patch(map, 9, 34, 6, 6, TerrainKind.DENSE_FOREST);
            }
            case NORTHERN_RIDGE -> {
                ridge(map, 8, 5, 16, 38); ridge(map, 38, 2, 15, 40);
                patch(map, 27, 17, 9, 13, TerrainKind.STONE_PATH); patch(map, 31, 8, 3, 3, TerrainKind.CAVE_FLOOR);
            }
            case WESTERN_SHU -> {
                ridge(map, 5, 3, 13, 41); ridge(map, 25, 2, 15, 42); ridge(map, 45, 4, 13, 40);
                for (int x = 15; x < 50; x++) map[24][x] = TerrainKind.STONE_PATH;
                patch(map, 29, 17, 5, 6, TerrainKind.TEMPLE_GROUND);
            }
            case EASTERN_SEA -> {
                for (int y = 5; y < 44; y++) for (int x = 42; x < 63; x++) map[y][x] = TerrainKind.DEEP_WATER;
                for (int x = 10; x < 44; x++) map[25 + (int)(3 * Math.sin(x / 5.0))][x] = TerrainKind.STONE_PATH;
                patch(map, 18, 18, 10, 8, TerrainKind.TOWN_GROUND);
            }
            case CAPITAL -> {
                patch(map, 8, 8, 48, 32, TerrainKind.TOWN_GROUND);
                for (int x = 8; x < 56; x++) { map[23][x] = TerrainKind.STONE_PATH; map[24][x] = TerrainKind.STONE_PATH; }
                for (int y = 8; y < 40; y++) { map[y][31] = TerrainKind.STONE_PATH; map[y][32] = TerrainKind.STONE_PATH; }
                patch(map, 27, 18, 10, 10, TerrainKind.TEMPLE_GROUND);
            }
            case WULIN_MOUNTAINS -> {
                ridge(map, 3, 3, 18, 41); ridge(map, 23, 1, 18, 44); ridge(map, 44, 4, 17, 40);
                patch(map, 27, 18, 9, 9, TerrainKind.TEMPLE_GROUND);
                for (int x = 8; x < 56; x++) map[34 - (x % 7)][x] = TerrainKind.STONE_PATH;
                patch(map, 30, 30, 4, 4, TerrainKind.CAVE_FLOOR);
            }
        }
        return new OverworldRegion(id, map);
    }
    private static void ridge(TerrainKind[][] map, int x, int y, int width, int height) {
        patch(map, x, y, width, height, TerrainKind.MOUNTAIN);
        if (width > 4 && height > 4) patch(map, x + 2, y + 2, width - 4, height - 4, TerrainKind.HILL);
        for (int yy = y + 1; yy < y + height - 1; yy += 5) for (int xx = x + 1; xx < x + width - 1; xx += 6) map[yy][xx] = TerrainKind.CLIFF;
    }
    private static void patch(TerrainKind[][] map, int x, int y, int width, int height, TerrainKind kind) {
        for (int yy = Math.max(1, y); yy < Math.min(map.length - 1, y + height); yy++)
            for (int xx = Math.max(1, x); xx < Math.min(map[0].length - 1, x + width); xx++) map[yy][xx] = kind;
    }
}