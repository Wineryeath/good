package com.jianghu.quest;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class QuestTest {
    @Test void objectiveProgressAndTurnInAreSeparate() {
        Quest quest = new Quest("herb-delivery", Map.of("collect:wild-herb",1));
        quest.activate();
        quest.record("collect:wild-herb",1);
        assertEquals(Quest.Status.READY_TO_TURN_IN, quest.status());
        quest.turnIn();
        assertEquals(Quest.Status.COMPLETED, quest.status());
    }
}
