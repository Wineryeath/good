package com.jianghu.map;
public enum TileType {
    GRASS(true), PATH(true), WATER(false), WALL(false), BRIDGE(true), BUILDING(false);
    private final boolean walkable;
    TileType(boolean walkable) { this.walkable = walkable; }
    public boolean walkable() { return walkable; }
}
