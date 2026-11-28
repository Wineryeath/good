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
}
