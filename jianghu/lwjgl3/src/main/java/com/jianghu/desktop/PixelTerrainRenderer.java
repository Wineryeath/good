package com.jianghu.desktop;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.jianghu.map.TerrainKind;
import com.jianghu.map.TileType;

/** Deterministic pixel-art terrain pass shared by the town, overworld and sect interiors. */
public final class PixelTerrainRenderer {
    private static final Color WATER_DARK = new Color(0.10f, 0.28f, 0.39f, 1f);
    private static final Color WATER_LIGHT = new Color(0.55f, 0.76f, 0.72f, 1f);
    private static final Color LEAF_DARK = new Color(0.12f, 0.31f, 0.20f, 1f);
    private static final Color LEAF_LIGHT = new Color(0.46f, 0.62f, 0.34f, 1f);
    private static final Color STONE_LIGHT = new Color(0.64f, 0.62f, 0.55f, 1f);
    private static final Color ROOF = new Color(0.31f, 0.18f, 0.16f, 1f);
    private static final Color GOLD = new Color(0.91f, 0.73f, 0.36f, 1f);

    private PixelTerrainRenderer() {}

    public static void drawOverworldTile(ShapeRenderer shapes, TerrainKind kind, int x, int y, float ox, float oy, float tile) {
        Color base = switch (kind) {
            case DEEP_WATER -> new Color(0.12f, 0.30f, 0.48f, 1f);
            case SHALLOW_WATER -> new Color(0.25f, 0.52f, 0.65f, 1f);
            case GRASS -> new Color(0.39f, 0.57f, 0.34f, 1f);
            case FOREST -> new Color(0.20f, 0.43f, 0.25f, 1f);
            case DENSE_FOREST -> new Color(0.10f, 0.29f, 0.18f, 1f);
            case PATH -> new Color(0.68f, 0.58f, 0.40f, 1f);
            case STONE_PATH -> new Color(0.55f, 0.54f, 0.50f, 1f);
            case BRIDGE -> new Color(0.75f, 0.48f, 0.25f, 1f);
            case HILL -> new Color(0.48f, 0.47f, 0.32f, 1f);
            case MOUNTAIN, CLIFF -> new Color(0.34f, 0.34f, 0.36f, 1f);
            case CAVE_FLOOR -> new Color(0.27f, 0.23f, 0.20f, 1f);
            case TOWN_GROUND -> new Color(0.66f, 0.57f, 0.42f, 1f);
            case BUILDING -> new Color(0.46f, 0.28f, 0.22f, 1f);
            case TEMPLE_GROUND -> new Color(0.70f, 0.61f, 0.48f, 1f);
            case TEMPLE_BUILDING -> new Color(0.50f, 0.35f, 0.25f, 1f);
        };
        float px = ox + x * tile, py = oy + y * tile;
        shapes.setColor(base); shapes.rect(px, py, tile - 0.5f, tile - 0.5f);
        int variation = hash(x, y);
        if (kind == TerrainKind.DEEP_WATER || kind == TerrainKind.SHALLOW_WATER) {
            shapes.setColor(WATER_LIGHT);
            float ripple = 2f + Math.floorMod(variation, Math.max(2, (int) tile - 4));
            shapes.rect(px + ripple, py + tile * 0.28f, Math.max(2f, tile * 0.30f), Math.max(1f, tile * 0.07f));
            if (tile >= 10) { shapes.setColor(WATER_DARK); shapes.rect(px + tile * 0.55f, py + tile * 0.70f, tile * 0.22f, 1.2f); }
        } else if (kind == TerrainKind.FOREST || kind == TerrainKind.DENSE_FOREST) {
            float cx = px + tile * (0.28f + Math.floorMod(variation, 4) * 0.12f);
            float cy = py + tile * (0.30f + Math.floorMod(variation >>> 3, 4) * 0.11f);
            shapes.setColor(LEAF_DARK); shapes.rect(cx, cy, tile * 0.48f, tile * 0.48f);
            shapes.setColor(LEAF_LIGHT); shapes.rect(cx + tile * 0.10f, cy + tile * 0.25f, tile * 0.23f, tile * 0.16f);
            if (tile >= 10) { shapes.setColor(new Color(0.30f, 0.22f, 0.15f, 1f)); shapes.rect(cx + tile * 0.20f, py, tile * 0.10f, tile * 0.25f); }
        } else if (kind == TerrainKind.MOUNTAIN || kind == TerrainKind.CLIFF || kind == TerrainKind.HILL) {
            shapes.setColor(STONE_LIGHT); shapes.rect(px + tile * 0.18f, py + tile * 0.62f, tile * 0.36f, Math.max(1f, tile * 0.08f));
            shapes.setColor(new Color(0.23f, 0.24f, 0.26f, 1f)); shapes.rect(px + tile * 0.62f, py + tile * 0.18f, tile * 0.18f, tile * 0.12f);
        } else if (kind == TerrainKind.BRIDGE) {
            shapes.setColor(new Color(0.36f, 0.23f, 0.17f, 1f));
            shapes.rect(px + tile * 0.12f, py + tile * 0.18f, tile * 0.12f, tile * 0.66f);
            shapes.rect(px + tile * 0.76f, py + tile * 0.18f, tile * 0.12f, tile * 0.66f);
            shapes.setColor(new Color(0.90f, 0.68f, 0.40f, 1f)); shapes.rect(px + tile * 0.32f, py + tile * 0.48f, tile * 0.38f, Math.max(1f, tile * 0.07f));
        } else if (kind == TerrainKind.BUILDING || kind == TerrainKind.TEMPLE_BUILDING) {
            shapes.setColor(ROOF); shapes.rect(px + tile * 0.08f, py + tile * 0.60f, tile * 0.84f, tile * 0.24f);
            shapes.setColor(GOLD); shapes.rect(px + tile * 0.18f, py + tile * 0.55f, tile * 0.64f, Math.max(1f, tile * 0.05f));
            shapes.setColor(new Color(0.20f, 0.14f, 0.12f, 1f)); shapes.rect(px + tile * 0.42f, py, tile * 0.16f, tile * 0.34f);
        } else if (kind == TerrainKind.PATH || kind == TerrainKind.STONE_PATH) {
            shapes.setColor(new Color(0.83f, 0.74f, 0.57f, 0.75f));
            shapes.rect(px + tile * 0.18f, py + tile * 0.25f, tile * 0.18f, Math.max(1f, tile * 0.08f));
            shapes.rect(px + tile * 0.60f, py + tile * 0.68f, tile * 0.20f, Math.max(1f, tile * 0.08f));
        } else if (kind == TerrainKind.CAVE_FLOOR) {
            shapes.setColor(new Color(0.12f, 0.11f, 0.12f, 1f));
            shapes.rect(px + tile * 0.22f, py + tile * 0.22f, tile * 0.56f, tile * 0.56f);
        } else {
            shapes.setColor(new Color(0.66f, 0.74f, 0.42f, 0.75f));
            shapes.rect(px + tile * 0.25f, py + tile * 0.30f, Math.max(1f, tile * 0.08f), Math.max(1f, tile * 0.18f));
        }
    }

    public static void drawTownTile(ShapeRenderer shapes, TileType kind, int x, int y, float ox, float oy, float tile) {
        Color base = switch (kind) {
            case WATER -> new Color(0.30f, 0.58f, 0.62f, 1f);
            case WALL -> new Color(0.25f, 0.34f, 0.29f, 1f);
            case BRIDGE -> new Color(0.66f, 0.49f, 0.30f, 1f);
            case PATH -> new Color(0.68f, 0.58f, 0.40f, 1f);
            case BUILDING -> new Color(0.48f, 0.32f, 0.25f, 1f);
            case GRASS -> new Color(0.48f, 0.65f, 0.43f, 1f);
        };
        float px = ox + x * tile, py = oy + y * tile;
        shapes.setColor(base); shapes.rect(px, py, tile - 0.5f, tile - 0.5f);
        if (kind == TileType.WATER) {
            shapes.setColor(WATER_LIGHT); shapes.rect(px + tile * 0.18f, py + tile * 0.28f, tile * 0.35f, 2f);
            shapes.rect(px + tile * 0.55f, py + tile * 0.68f, tile * 0.24f, 1.5f);
        } else if (kind == TileType.GRASS) {
            shapes.setColor(LEAF_LIGHT); shapes.rect(px + tile * 0.22f, py + tile * 0.24f, 2f, tile * 0.20f);
            shapes.rect(px + tile * 0.70f, py + tile * 0.58f, 2f, tile * 0.16f);
        } else if (kind == TileType.BRIDGE) {
            shapes.setColor(ROOF); shapes.rect(px + 2f, py + tile * 0.20f, tile - 4f, 2f);
            shapes.rect(px + 2f, py + tile * 0.72f, tile - 4f, 2f);
        } else if (kind == TileType.BUILDING) {
            shapes.setColor(ROOF); shapes.rect(px + 1f, py + tile * 0.62f, tile - 2f, tile * 0.24f);
            shapes.setColor(GOLD); shapes.rect(px + tile * 0.18f, py + tile * 0.56f, tile * 0.64f, 2f);
            shapes.setColor(new Color(0.20f, 0.14f, 0.12f, 1f)); shapes.rect(px + tile * 0.42f, py, tile * 0.16f, tile * 0.30f);
        } else if (kind == TileType.PATH) {
            shapes.setColor(new Color(0.83f, 0.74f, 0.57f, 0.8f)); shapes.rect(px + tile * 0.18f, py + tile * 0.28f, tile * 0.18f, 2f);
        }
    }

    public static void drawInteriorTile(ShapeRenderer shapes, TileType kind, int x, int y, float ox, float oy, float tile) {
        drawTownTile(shapes, kind, x, y, ox, oy, tile);
        if (kind == TileType.WALL) {
            float px = ox + x * tile, py = oy + y * tile;
            shapes.setColor(new Color(0.48f, 0.52f, 0.46f, 1f));
            shapes.rect(px + 2f, py + tile * 0.78f, tile - 4f, 2f);
        }
    }

    private static int hash(int x, int y) {
        int value = x * 0x45d9f3b ^ y * 0x119de1f3;
        value = (value ^ (value >>> 16)) * 0x45d9f3b;
        return value ^ (value >>> 16);
    }
}