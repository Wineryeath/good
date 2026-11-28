package com.jianghu.combat;

import com.jianghu.character.Player;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RealtimeCombatEncounterTest {
    private RealtimeCombatEncounter encounter() {
        return new RealtimeCombatEncounter(new Player("测试侠客"),
            new CombatEnemy("test-bandit", "试剑匪徒", "试招", 100, 12, 0, 80, 20, "herb", 1));
    }

    @Test void lightAttacksConsumeStaminaAndBuildComboDamage() {
        RealtimeCombatEncounter fight = encounter();
        int initialStamina = fight.player().stamina();
        int initialHealth = fight.enemy().health();
        assertTrue(fight.perform(CombatAction.LIGHT_ATTACK).accepted());
        int firstDamage = initialHealth - fight.enemy().health();
        assertEquals(initialStamina - 8, fight.player().stamina());
        fight.update(0.30f);
        assertTrue(fight.perform(CombatAction.LIGHT_ATTACK).accepted());
        assertEquals(2, fight.controller().comboCount());
        assertTrue(initialHealth - fight.enemy().health() > firstDamage);
    }

    @Test void timedDodgeAvoidsTelegraphedEnemyStrike() {
        RealtimeCombatEncounter fight = encounter();
        fight.update(0.70f);
        assertTrue(fight.enemyTelegraphing());
        assertTrue(fight.perform(CombatAction.DODGE).accepted());
        fight.update(0.33f);
        assertEquals(100, fight.player().health());
        assertTrue(fight.log().stream().anyMatch(line -> line.contains("劈空")));
    }

    @Test void timedBlockReducesIncomingDamage() {
        RealtimeCombatEncounter fight = encounter();
        fight.update(0.70f);
        assertTrue(fight.enemyTelegraphing());
        assertTrue(fight.perform(CombatAction.BLOCK).accepted());
        fight.update(0.33f);
        assertEquals(94, fight.player().health());
        assertTrue(fight.log().stream().anyMatch(line -> line.contains("格挡")));
    }

    @Test void heavyAttackCanDefeatEnemyAndSettlesRewardOnce() {
        Player player = new Player("重剑客");
        RealtimeCombatEncounter fight = new RealtimeCombatEncounter(player,
            new CombatEnemy("weak-bandit", "小贼", "试招", 9, 1, 0, 80, 20, "herb", 1));
        assertTrue(fight.perform(CombatAction.HEAVY_ATTACK).accepted());
        assertEquals(RealtimeCombatEncounter.Status.WON, fight.status());
        assertEquals(2_080, player.moneyInWen());
        assertEquals(1, player.inventory().get("herb"));
    }
}