package com.jianghu.world;

import com.jianghu.character.Player;
import com.jianghu.map.GameMap;
import com.jianghu.npc.Npc;
import com.jianghu.time.WorldClock;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PlayerWorldInteractionTest {
    @Test void playerCanGatherHerbsOncePerDayAndSpendStamina() {
        Player player = new Player("行旅人");
        player.moveTo(9, 11);
        GameWorld world = new GameWorld(player, new WorldClock(1, 8 * 60), List.of());
        String first = world.interactWithNearbyPoint(GameMap.starterTown()).orElseThrow();
        assertTrue(first.contains("采得草药"));
        assertEquals(1, player.inventory().get("herb"));
        assertEquals(95, player.stamina());
        assertEquals(8 * 60 + 10, world.clock().minuteOfDay());
        String second = world.interactWithNearbyPoint(GameMap.starterTown()).orElseThrow();
        assertTrue(second.contains("今日已经采过"));
        assertEquals(1, player.inventory().get("herb"));
    }

    @Test void playerCanPayForInnRestAndRecover() {
        Player player = new Player("行旅人");
        player.moveTo(7, 3);
        player.spendStamina(50);
        player.changeHealth(-10);
        GameWorld world = new GameWorld(player, new WorldClock(1, 8 * 60), List.of());
        String message = world.interactWithNearbyPoint(GameMap.starterTown()).orElseThrow();
        assertTrue(message.contains("花费 30 文"));
        assertEquals(1_970, player.moneyInWen());
        assertEquals(76, player.stamina());
        assertEquals(92, player.health());
        assertEquals(9 * 60, world.clock().minuteOfDay());
    }

    @Test void playerCannotHarvestWithoutEnoughStaminaOrInteractOutsideRange() {
        Player player = new Player("行旅人");
        player.moveTo(9, 11);
        player.spendStamina(98);
        GameWorld world = new GameWorld(player, new WorldClock(1, 8 * 60), List.of());
        assertTrue(world.interactWithNearbyPoint(GameMap.starterTown()).orElseThrow().contains("体力不足"));
        assertTrue(player.inventory().isEmpty());
        player.moveTo(1, 1);
        assertTrue(world.interactWithNearbyPoint(GameMap.starterTown()).isEmpty());
    }
}