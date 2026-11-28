package com.jianghu.combat;

import com.jianghu.map.OverworldRegion;
import com.jianghu.map.TerrainKind;
import com.jianghu.map.OverworldRegionCatalog;
import com.jianghu.save.GameSaveStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExpandedSystemsTest {
    @Test void regionMapKeepsTerrainAndCollisionData() {
        TerrainKind[][] grid = new TerrainKind[8][8];
        for (int y = 0; y < 8; y++) for (int x = 0; x < 8; x++) grid[y][x] = TerrainKind.GRASS;
        grid[2][3] = TerrainKind.DEEP_WATER; grid[4][4] = TerrainKind.CAVE_FLOOR;
        OverworldRegion region = new OverworldRegion(OverworldRegion.RegionId.JIANGNAN, grid);
        assertFalse(region.isWalkable(3, 2)); assertTrue(region.isWalkable(4, 4));
        grid[4][4] = TerrainKind.CLIFF; assertEquals(TerrainKind.CAVE_FLOOR, region.terrainAt(4, 4));
    }
    @Test void animationStateTransitionsAndDeathIsPersistentUntilRevive() {
        CharacterAnimationController animation = new CharacterAnimationController();
        animation.walk(-1); assertEquals(CharacterAnimationState.WALK, animation.state()); assertEquals(-1, animation.facing());
        animation.attack(); animation.update(0.4f); assertEquals(CharacterAnimationState.IDLE, animation.state());
        animation.die(); animation.walk(1); assertEquals(CharacterAnimationState.DEATH, animation.state());
        animation.revive(); assertEquals(CharacterAnimationState.IDLE, animation.state());
    }
    @Test void realTimeCombatSupportsComboDodgeAndBlock() {
        RealtimeCombatController combat = new RealtimeCombatController();
        assertTrue(combat.perform(CombatAction.LIGHT_ATTACK, 100).accepted());
        combat.update(0.4f); assertTrue(combat.perform(CombatAction.LIGHT_ATTACK, 100).accepted());
        assertEquals(2, combat.comboCount());
        combat.update(0.8f); assertTrue(combat.perform(CombatAction.DODGE, 100).accepted());
        assertTrue(combat.avoidsIncomingDamage());
        combat.update(0.6f); assertTrue(combat.perform(CombatAction.BLOCK, 100).accepted());
        assertTrue(combat.blocksIncomingDamage());
    }
    @Test void catalogBuildsSixLargeDistinctRegions() {
        var regions = OverworldRegionCatalog.all();
        assertEquals(6, regions.size());
        assertEquals(64, regions.get(OverworldRegion.RegionId.JIANGNAN).width());
        assertTrue(regions.get(OverworldRegion.RegionId.NORTHERN_RIDGE).terrainAt(10, 10) != null);
    }
    @Test void overworldExplorerUsesWalkableGatesToConnectRegions() {
        var explorer = new com.jianghu.map.OverworldExplorer();
        assertEquals(OverworldRegion.RegionId.JIANGNAN, explorer.regionId());
        assertTrue(explorer.move(1, 0));
        assertTrue(explorer.move(-1, 0));
        assertFalse(explorer.move(0, -1) && explorer.y() == 0);
        var gate = explorer.gates().stream().filter(g -> g.to() == OverworldRegion.RegionId.NORTHERN_RIDGE).findFirst().orElseThrow();
        while (explorer.x() < gate.x()) assertTrue(explorer.move(1, 0));
        while (explorer.y() < gate.y()) assertTrue(explorer.move(0, 1));
        assertTrue(explorer.gateAtPlayer().isPresent());
        assertTrue(explorer.travelThroughGate().orElseThrow().contains("北岭"));
        assertEquals(OverworldRegion.RegionId.NORTHERN_RIDGE, explorer.regionId());
        assertTrue(explorer.region().isWalkable(explorer.x(), explorer.y()));
    }
    @Test void saveStoreRoundTripsVersionedStateAtomically() throws Exception {
        Path file = Files.createTempDirectory("jianghu-save-test").resolve("slot-1.sav");
        GameSaveStore store = new GameSaveStore();
        store.save(file, Map.of("player.name", "行走江湖", "world.day", "12", "market.jiangnan.herb", "87"));
        assertEquals("行走江湖", store.load(file).get("player.name"));
        assertEquals("12", store.load(file).get("world.day"));
        Files.deleteIfExists(file); Files.deleteIfExists(file.getParent());
    }
    @Test void martialSchoolsAreDistinctAndDiscoverable() {
        assertEquals(4, MartialSchool.all().size());
        assertTrue(MartialSchool.find("wudang-zixiao").isPresent());
        assertNotEquals(MartialSchool.find("shaolin-jingang").orElseThrow().style(),
            MartialSchool.find("huashan-cangjian").orElseThrow().style());
    }
}