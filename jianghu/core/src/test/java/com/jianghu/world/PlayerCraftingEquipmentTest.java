package com.jianghu.world;

import com.jianghu.character.Player;
import com.jianghu.item.EquipmentItem;
import com.jianghu.npc.Npc;
import com.jianghu.time.WorldClock;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PlayerCraftingEquipmentTest {
    private GameWorld world(Player player) { return new GameWorld(player, new WorldClock(1, 8 * 60), List.of()); }

    @Test void herbalMedicineCraftingConsumesOnlyRequiredMaterials() {
        Player player = new Player("行旅人");
        player.addItem("herb", 3);
        GameWorld world = world(player);
        assertTrue(world.craftMedicine().contains("金疮药"));
        assertEquals(1, player.inventory().get("herb"));
        assertEquals(1, player.inventory().get("medicine"));
        assertEquals(8 * 60 + 30, world.clock().minuteOfDay());
    }

    @Test void mealCookingAcceptsVegetableOrFishAndProtectsMaterialsOnFailure() {
        Player player = new Player("行旅人");
        player.addItem("grain", 1);
        GameWorld world = world(player);
        assertThrows(IllegalStateException.class, world::cookMeal);
        assertEquals(1, player.inventory().get("grain"));
        player.addItem("fish", 1);
        assertTrue(world.cookMeal().contains("熟食"));
        assertFalse(player.inventory().containsKey("grain"));
        assertFalse(player.inventory().containsKey("fish"));
        assertEquals(1, player.inventory().get("meal"));
    }

    @Test void equipmentReplacesPreviousItemAndUpdatesCombatStats() {
        Player player = new Player("行旅人");
        player.addItem("wooden_sword", 1);
        player.addItem("iron_sword", 1);
        player.addItem("leather_armor", 1);
        GameWorld world = world(player);
        world.equipPlayerItem("wooden_sword");
        assertEquals(4, player.attackPower());
        world.equipPlayerItem("iron_sword");
        assertEquals(10, player.attackPower());
        assertEquals(1, player.inventory().get("wooden_sword"));
        world.equipPlayerItem("leather_armor");
        assertEquals(7, player.defensePower());
        world.unequipPlayerItem(EquipmentItem.Slot.WEAPON);
        assertEquals(0, player.attackPower());
        assertEquals(1, player.inventory().get("iron_sword"));
    }

    @Test void cannotEquipMissingOrNonEquipmentItems() {
        Player player = new Player("行旅人");
        player.addItem("herb", 1);
        GameWorld world = world(player);
        assertThrows(IllegalStateException.class, () -> world.equipPlayerItem("iron_sword"));
        assertThrows(IllegalArgumentException.class, () -> world.equipPlayerItem("herb"));
        assertEquals(1, player.inventory().get("herb"));
    }
}