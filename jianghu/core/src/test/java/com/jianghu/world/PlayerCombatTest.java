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
    @Test void fiveMartialStylesCanBeLearnedAndChangeCombatAttributes() {
        Player player = new Player("行旅人");
        GameWorld world = world(player);
        assertEquals(5, com.jianghu.combat.MartialArtStyle.values().length);
        assertThrows(IllegalStateException.class, () -> player.selectStyle(com.jianghu.combat.MartialArtStyle.SWORD));
        world.learnMartialStyle(com.jianghu.combat.MartialArtStyle.SWORD, 300);
        player.selectStyle(com.jianghu.combat.MartialArtStyle.SWORD);
        assertEquals(3, player.attackPower());
        assertEquals(1_700, player.moneyInWen());
    }

    @Test void techniquesRequireTheirStyleAndSpendBothResources() {
        Player player = new Player("行旅人");
        GameWorld world = world(player);
        assertThrows(IllegalStateException.class, () -> world.learnMartialTechnique("cloud_sword", 100));
        world.learnMartialStyle(com.jianghu.combat.MartialArtStyle.SWORD, 0);
        world.learnMartialTechnique("cloud_sword", 0);
        player.selectStyle(com.jianghu.combat.MartialArtStyle.SWORD);
        CombatEncounter fight = world.startBanditEncounter();
        int beforeEnergy = player.innerEnergy();
        int beforeStamina = player.stamina();
        fight.useTechnique("cloud_sword");
        assertEquals(42 - (3 + 3 + 2 + 10 - 2), fight.enemy().health());
        assertEquals(beforeEnergy - 20, player.innerEnergy());
        assertEquals(beforeStamina - 12, player.stamina());
    }

    @Test void insufficientInnerEnergyDoesNotConsumeStaminaOrAdvanceFight() {
        Player player = new Player("行旅人");
        player.learnStyle(com.jianghu.combat.MartialArtStyle.SWORD);
        player.learnTechnique("cloud_sword");
        player.selectStyle(com.jianghu.combat.MartialArtStyle.SWORD);
        player.spendInnerEnergy(60);
        CombatEncounter fight = world(player).startBanditEncounter();
        assertThrows(IllegalStateException.class, () -> fight.useTechnique("cloud_sword"));
        assertEquals(100, player.stamina());
        assertEquals(42, fight.enemy().health());
        assertEquals(0, fight.rounds());
    }

    @Test void meditationRestoresInnerEnergyButCannotBeUsedDuringCombat() {
        Player player = new Player("行旅人");
        GameWorld world = world(player);
        player.spendInnerEnergy(50);
        assertTrue(world.meditate().contains("内力恢复"));
        assertEquals(35, player.innerEnergy());
        world.startBanditEncounter();
        assertThrows(IllegalStateException.class, world::meditate);
    }
}