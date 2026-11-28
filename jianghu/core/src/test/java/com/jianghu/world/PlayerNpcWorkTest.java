package com.jianghu.world;

import com.jianghu.character.Player;
import com.jianghu.map.GameMap;
import com.jianghu.npc.Npc;
import com.jianghu.time.WorldClock;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PlayerNpcWorkTest {
    private Npc teaOwner() {
        return new Npc("tea-owner", "沈掌柜", "茶摊老板",
            List.of(new Npc.Schedule(6, 20, "tea-stall", 0)), 0);
    }

    @Test void playerCanWorkForNearbyNpcForWages() {
        Player player = new Player("行旅人");
        player.moveTo(9, 4);
        GameWorld world = new GameWorld(player, new WorldClock(1, 8 * 60), List.of(teaOwner()));
        long before = player.moneyInWen();
        assertEquals(45, world.workForNearbyNpc(GameMap.starterTown()));
        assertEquals(before + 45, player.moneyInWen());
        assertEquals(85, player.stamina());
        assertEquals(10 * 60, world.clock().minuteOfDay());
        assertEquals(2, world.relationship("tea-owner").favor());
    }

    @Test void playerCanGiveInventoryGiftToNearbyNpc() {
        Player player = new Player("行旅人");
        player.moveTo(9, 4);
        player.addItem("herb", 1);
        GameWorld world = new GameWorld(player, new WorldClock(1, 8 * 60), List.of(teaOwner()));
        world.giftToNearbyNpc(GameMap.starterTown(), "herb");
        assertFalse(player.inventory().containsKey("herb"));
        assertEquals(4, world.relationship("tea-owner").favor());
        assertEquals(0, world.relationship("tea-owner").conversations());
    }

    @Test void workAndGiftFailSafelyWhenNpcOrResourcesAreUnavailable() {
        Player player = new Player("行旅人");
        player.moveTo(9, 4);
        player.spendStamina(90);
        GameWorld world = new GameWorld(player, new WorldClock(1, 8 * 60), List.of(teaOwner()));
        assertThrows(IllegalStateException.class, () -> world.workForNearbyNpc(GameMap.starterTown()));
        assertEquals(10, player.stamina());
        assertThrows(IllegalStateException.class, () -> world.giftToNearbyNpc(GameMap.starterTown(), "herb"));
        assertTrue(player.inventory().isEmpty());
        player.moveTo(1, 1);
        assertThrows(IllegalStateException.class, () -> world.workForNearbyNpc(GameMap.starterTown()));
    }
}