package com.jianghu.social;

import com.jianghu.npc.Npc;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DialogueServiceTest {
    @Test void greetingReflectsNpcRoleAndTime() {
        Npc npc = new Npc("tea","沈掌柜","茶摊老板",List.of(),0);
        assertTrue(DialogueService.greeting(npc,8,new NpcRelationship()).contains("热茶"));
        assertTrue(DialogueService.greeting(npc,22,new NpcRelationship()).contains("打烊"));
    }
    @Test void relationshipValuesAreBounded() {
        NpcRelationship bond = new NpcRelationship();
        bond.changeFavor(500);
        bond.changeTrust(-500);
        assertEquals(100,bond.favor());
        assertEquals(-100,bond.trust());
    }
}
