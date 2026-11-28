package com.jianghu.time;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WorldClockTest {
    @Test void advancesAcrossMidnight() {
        WorldClock clock = new WorldClock(1, 23 * 60);
        clock.advance(120);
        assertEquals(2, clock.day());
        assertEquals(60, clock.minuteOfDay());
        assertEquals("丑时", clock.traditionalHour());
    }
    @Test void rejectsNegativeTime() {
        assertThrows(IllegalArgumentException.class, () -> new WorldClock(1, 0).advance(-1));
    }
}
