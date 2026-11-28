package com.jianghu.world;

import com.jianghu.character.Player;
import com.jianghu.item.InventoryCatalog;
import com.jianghu.item.InventoryItem;
import com.jianghu.npc.Npc;
import com.jianghu.time.WorldClock;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PlayerInventoryUseTest {
    private GameWorld world(Player player) {
        return new GameWorld(player, new WorldClock(1, 8 * 60), List.of());
    }

    @Test void mealAndTeaRestoreNeedsAndConsumeOneStackItem() {
        Player player = new Player("行旅人");
        player.addItem("meal", 2);
        player.addItem("tea", 1);
        player.changeHunger(50);
        player.changeThirst(50);
        GameWorld world = world(player);
        assertTrue(world.useInventoryItem("meal").contains("江南熟食"));
        assertEquals(35, player.hunger());
        assertEquals(1, player.inventory().get("meal"));
        assertEquals(8 * 60 + 5, world.clock().minuteOfDay());
        world.useInventoryItem("tea");
        assertEquals(25, player.thirst());
        assertFalse(player.inventory().containsKey("tea"));
    }

    @Test void medicineRestoresHealthAndHerbsCannotBeConsumedDirectly() {
        Player player = new Player("行旅人");
        player.addItem("medicine", 1);
        player.addItem("herb", 1);
        player.changeHealth(-40);
        GameWorld world = world(player);
        world.useInventoryItem("medicine");
        assertEquals(88, player.health());
        assertFalse(player.inventory().containsKey("medicine"));
        assertThrows(IllegalStateException.class, () -> world.useInventoryItem("herb"));
        assertEquals(1, player.inventory().get("herb"));
    }

    @Test void inventoryIsGroupedByCatalogCategoryAndFailedUseDoesNotConsumeItems() {
        Player player = new Player("行旅人");
        player.addItem("meal", 1);
        player.addItem("herb", 2);
        player.addItem("unknown-relic", 1);
        GameWorld world = world(player);
        var grouped = world.inventoryByCategory();
        assertEquals(1, grouped.get(InventoryItem.Category.FOOD).get("meal"));
        assertEquals(2, grouped.get(InventoryItem.Category.MATERIAL).get("herb"));
        assertEquals(1, grouped.get(InventoryItem.Category.MATERIAL).get("unknown-relic"));
        assertThrows(IllegalStateException.class, () -> world.useInventoryItem("medicine"));
        assertEquals(1, player.inventory().get("meal"));
        assertTrue(InventoryCatalog.find("medicine").isPresent());
    }
}