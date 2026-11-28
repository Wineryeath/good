package com.jianghu.map;

/** Detailed visual and traversal terrain used by the expanded overworld renderer. */
public enum TerrainKind {
    DEEP_WATER(false), SHALLOW_WATER(false), GRASS(true), FOREST(true),
    DENSE_FOREST(false), PATH(true), STONE_PATH(true), BRIDGE(true),
    HILL(true), MOUNTAIN(false), CAVE_FLOOR(true), CLIFF(false),
    TOWN_GROUND(true), BUILDING(false), TEMPLE_GROUND(true), TEMPLE_BUILDING(false);

    private final boolean walkable;
    TerrainKind(boolean walkable) { this.walkable = walkable; }
    public boolean walkable() { return walkable; }
}