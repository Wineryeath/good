package com.jianghu.npc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NpcLifeStateTest {
    @Test void dailyNeedsAndWorkIncomeAdvanceWithTime() {
        NpcLifeState life = new NpcLifeState();
        long before = life.moneyInWen();
        life.passHour("渔夫","river-hut",8);
        assertEquals(29,life.hunger());
        assertEquals(before + 12,life.moneyInWen());
    }
    @Test void memoriesKeepOnlyTheMostRecentTwentyEvents() {
        NpcLifeState life = new NpcLifeState();
        for (int i=0;i<21;i++) life.remember("event-" + i);
        assertEquals(20,life.memories().size());
        assertFalse(life.remembers("event-0"));
        assertTrue(life.remembers("event-20"));
    }
    @Test void ageAdvancesByGameDaysAndChangesLifeStages() {
        NpcLifeState life = new NpcLifeState();
        life.initializeAge(11);
        assertEquals(NpcLifeState.LifeStage.CHILD, life.lifeStage());
        for (int i = 0; i < 359; i++) assertFalse(life.passDay());
        assertEquals(11, life.ageYears());
        assertTrue(life.passDay());
        assertEquals(12, life.ageYears());
        assertEquals(NpcLifeState.LifeStage.YOUTH, life.lifeStage());
        assertEquals(0, life.daysLivedThisYear());
        assertThrows(IllegalArgumentException.class, () -> life.initializeAge(20));
    }

    @Test void adultAndElderStagesUseClearAgeThresholds() {
        NpcLifeState life = new NpcLifeState();
        life.initializeAge(18);
        assertEquals(NpcLifeState.LifeStage.ADULT, life.lifeStage());
        NpcLifeState elder = new NpcLifeState();
        elder.initializeAge(60);
        assertEquals(NpcLifeState.LifeStage.ELDER, elder.lifeStage());
    }

}
