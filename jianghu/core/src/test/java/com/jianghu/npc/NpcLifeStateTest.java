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
    @Test void restoringOversizedMemoryHistoryKeepsOnlyTheLatestTwenty() {
        NpcLifeState life = new NpcLifeState();
        var memories = java.util.stream.IntStream.range(0, 25).mapToObj(i -> "restored-" + i).toList();
        life.restoreSnapshot(new NpcLifeSnapshot(20, 0, false, 25, 85, 60, 0, null,
            java.util.Map.of(), 500, 100, true, false, null, 0, memories));
        assertEquals(20, life.memories().size());
        assertFalse(life.remembers("restored-0"));
        assertTrue(life.remembers("restored-24"));
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

    @Test void deathStopsHealthAndTransfersEstateExactlyOnce() {
        NpcLifeState owner = new NpcLifeState();
        NpcLifeState heir = new NpcLifeState();
        long estate = owner.moneyInWen();
        long before = heir.moneyInWen();
        owner.die();
        assertFalse(owner.alive());
        assertEquals(estate, owner.transferWealthTo(heir));
        assertEquals(before + estate, heir.moneyInWen());
        assertEquals(0, owner.moneyInWen());
        assertThrows(IllegalStateException.class, () -> owner.transferWealthTo(heir));
        assertThrows(IllegalStateException.class, () -> owner.changeHealth(-1));
    }

    @Test void healthCannotDropBelowZero() {
        NpcLifeState life = new NpcLifeState();
        life.changeHealth(-150);
        assertEquals(0, life.health());
        assertFalse(life.alive());
    }

    @Test void illnessCanBeTreatedAndSeverityIsReduced() {
        NpcLifeState life = new NpcLifeState();
        life.contractIllness("风寒",2);
        assertTrue(life.sick());
        assertEquals("风寒",life.illness());
        life.passDay();
        assertEquals(96,life.health());
        life.treatIllness(2,3);
        assertFalse(life.sick());
        assertEquals(99,life.health());
    }

}
