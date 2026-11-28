package com.jianghu.map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class GameMapTest {
    @Test void starterMapHasWaterAndWalkableBridge() {
        GameMap map=GameMap.starterTown();
        assertFalse(map.isWalkable(15,8));
        assertTrue(map.isWalkable(11,8));
        assertFalse(map.isWalkable(-1,8));
    }
    @Test void findsNearbyInteractionPoint() {
        assertEquals("tea-stall",GameMap.starterTown().nearestInteractable(10,5).orElseThrow().id());
    }
}
