package com.jianghu.map;
public record MapPoint(String id, String name, Kind kind, int x, int y, int interactionRange) {
    public enum Kind { NPC, INN, MARKET, RESOURCE, EXIT, LANDMARK }
    public MapPoint {
        if (id == null || id.isBlank() || name == null || kind == null) throw new IllegalArgumentException("地图交互点信息不完整");
        if (x < 0 || y < 0 || interactionRange < 0) throw new IllegalArgumentException("地图坐标或交互范围无效");
    }
    public boolean canInteractFrom(int playerX, int playerY) {
        return Math.abs(playerX - x) + Math.abs(playerY - y) <= interactionRange + 1;
    }
}
