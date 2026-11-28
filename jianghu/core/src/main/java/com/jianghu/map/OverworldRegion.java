package com.jianghu.map;

import java.util.Arrays;

/** A self-contained, deterministic region map. Coordinates use tile units. */
public final class OverworldRegion {
    public enum RegionId { JIANGNAN, NORTHERN_RIDGE, WESTERN_SHU, EASTERN_SEA, CAPITAL, WULIN_MOUNTAINS }
    private final RegionId id;
    private final int width, height;
    private final TerrainKind[][] terrain;

    public OverworldRegion(RegionId id, TerrainKind[][] source) {
        if (id == null || source == null || source.length < 8 || source[0] == null || source[0].length < 8)
            throw new IllegalArgumentException("区域地图至少需要 8×8 格");
        this.id = id; this.height = source.length; this.width = source[0].length;
        terrain = new TerrainKind[height][width];
        for (int y = 0; y < height; y++) {
            if (source[y] == null || source[y].length != width) throw new IllegalArgumentException("区域地图必须为矩形");
            for (int x = 0; x < width; x++) terrain[y][x] = source[y][x] == null ? TerrainKind.CLIFF : source[y][x];
        }
    }
    public RegionId id() { return id; }
    public int width() { return width; }
    public int height() { return height; }
    public boolean inside(int x, int y) { return x >= 0 && y >= 0 && x < width && y < height; }
    public TerrainKind terrainAt(int x, int y) {
        if (!inside(x, y)) throw new IndexOutOfBoundsException("区域坐标越界");
        return terrain[y][x];
    }
    public boolean isWalkable(int x, int y) { return inside(x, y) && terrain[y][x].walkable(); }
    public TerrainKind[][] copyTerrain() {
        TerrainKind[][] copy = new TerrainKind[height][width];
        for (int y = 0; y < height; y++) copy[y] = Arrays.copyOf(terrain[y], width);
        return copy;
    }
}