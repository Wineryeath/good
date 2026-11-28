package com.jianghu.character;

import com.jianghu.combat.MartialArtStyle;
import com.jianghu.item.EquipmentItem;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerSnapshotTest {
    @Test void snapshotRestoresAllPlayerProgressAndCollections() {
        Player original = new Player("云游客");
        original.changeHealth(-17); original.spendStamina(23);
        original.changeHunger(31); original.changeThirst(22); original.changeMorale(-9);
        original.changeReputation(14); original.addMoney(321); original.moveTo(14, 8);
        original.addItem("herb", 4); original.addItem("iron_sword", 1);
        assertTrue(original.equip("iron_sword"));
        original.learnStyle(MartialArtStyle.SWORD); original.selectStyle(MartialArtStyle.SWORD);
        original.learnTechnique("cloud_sword"); original.spendInnerEnergy(11);
        original.gainMartialExperience(100);

        Player restored = PlayerSnapshot.capture(original).restore();
        assertEquals(original.name(), restored.name());
        assertEquals(original.health(), restored.health());
        assertEquals(original.stamina(), restored.stamina());
        assertEquals(original.hunger(), restored.hunger());
        assertEquals(original.thirst(), restored.thirst());
        assertEquals(original.morale(), restored.morale());
        assertEquals(original.reputation(), restored.reputation());
        assertEquals(original.moneyInWen(), restored.moneyInWen());
        assertEquals(original.x(), restored.x()); assertEquals(original.y(), restored.y());
        assertEquals(original.inventory(), restored.inventory());
        assertEquals(original.equipment(), restored.equipment());
        assertEquals(original.martialExperience(), restored.martialExperience());
        assertEquals(original.martialLevel(), restored.martialLevel());
        assertEquals(original.innerEnergy(), restored.innerEnergy());
        assertEquals(original.martialStyle(), restored.martialStyle());
        assertEquals(original.learnedStyles(), restored.learnedStyles());
        assertEquals(original.learnedTechniques(), restored.learnedTechniques());
        assertTrue(restored.equipment().containsKey(EquipmentItem.Slot.WEAPON));
    }

    @Test void snapshotRejectsInvalidPlayerState() {
        assertThrows(IllegalArgumentException.class, () -> new PlayerSnapshot("", 100, 100, 0, 0, 0, 0,
            0, 0, 0, java.util.Map.of(), java.util.Map.of(), 0, 1, 100, MartialArtStyle.FIST,
            java.util.Set.of(MartialArtStyle.FIST), java.util.Set.of("basic_palm")));
    }
}