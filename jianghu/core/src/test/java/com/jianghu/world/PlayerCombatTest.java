package com.jianghu.world;

import com.jianghu.character.Player;
import com.jianghu.combat.CombatEncounter;
import com.jianghu.combat.CombatEnemy;
import com.jianghu.item.EquipmentItem;
import com.jianghu.npc.Npc;
import com.jianghu.time.WorldClock;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PlayerCombatTest {
    private GameWorld world(Player player) { return new GameWorld(player, new WorldClock(1, 8 * 60), List.of()); }

    @Test void weaponAndArmorAffectCombatAndVictoryPaysLoot() {
        Player player = new Player("行旅人");
        player.addItem("iron_sword", 1); player.addItem("leather_armor", 1);
        player.equip("iron_sword"); player.equip("leather_armor");
        GameWorld world = world(player);
        CombatEncounter fight = world.startBanditEncounter();
        assertEquals(CombatEncounter.Status.ONGOING, fight.status());
        world.combatAttack();
        assertEquals(29, fight.enemy().health());
        assertEquals(96, player.health());
        world.combatAttack(); world.combatAttack(); world.combatAttack();
        assertEquals(CombatEncounter.Status.WON, fight.status());
        assertEquals(2_120, player.moneyInWen());
        assertEquals(1, player.inventory().get("herb"));
        assertEquals(35, player.martialExperience());
        assertEquals(1, player.reputation());
        assertTrue(fight.log().stream().anyMatch(line -> line.contains("战胜")));
    }

    @Test void insufficientStaminaDoesNotAdvanceCombatOrDamagePlayer() {
        Player player = new Player("行旅人");
        player.spendStamina(95);
        CombatEncounter fight = world(player).startBanditEncounter();
        assertThrows(IllegalStateException.class, fight::playerAttack);
        assertEquals(42, fight.enemy().health());
        assertEquals(100, player.health());
        assertEquals(0, fight.rounds());
    }

    @Test void enemyCanDefeatPlayerAndFinishedBattleRejectsFurtherActions() {
        Player player = new Player("行旅人");
        player.changeHealth(-90);
        CombatEncounter fight = new CombatEncounter(player, new CombatEnemy("brute", "恶徒", "凶狠挥拳", 100, 20, 0, 0, 0, null, 0));
        fight.playerAttack();
        assertEquals(CombatEncounter.Status.LOST, fight.status());
        assertEquals(0, player.health());
        assertThrows(IllegalStateException.class, fight::playerAttack);
    }

    @Test void fleeingEndsEncounterWithoutRewardsAndMartialArtsCanLevelUp() {
        Player player = new Player("行旅人");
        CombatEncounter fight = world(player).startBanditEncounter();
        assertTrue(fight.flee().contains("脱身"));
        assertEquals(CombatEncounter.Status.FLED, fight.status());
        assertEquals(2_000, player.moneyInWen());
        assertEquals(0, player.martialLevel()-1);
        assertEquals(1, player.gainMartialExperience(100));
        assertEquals(2, player.martialLevel());
        assertEquals(0, player.martialExperience());
    }

    @Test void cannotStartAnotherEncounterWhileOneIsOngoing() {
        GameWorld world = world(new Player("行旅人"));
        world.startBanditEncounter();
        assertThrows(IllegalStateException.class, world::startBanditEncounter);
    }
}