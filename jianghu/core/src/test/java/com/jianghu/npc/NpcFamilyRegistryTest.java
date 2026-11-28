package com.jianghu.npc;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class NpcFamilyRegistryTest {
    @Test void marriageIsMutualAndCannotBeRepeated() {
        NpcFamilyRegistry family = new NpcFamilyRegistry(Set.of("a", "b", "c"));
        family.marry("a", "b");
        assertEquals("b", family.spouseOf("a").orElseThrow());
        assertEquals("a", family.spouseOf("b").orElseThrow());
        assertThrows(IllegalStateException.class, () -> family.marry("a", "c"));
    }

    @Test void parentChildAndSiblingQueriesWork() {
        NpcFamilyRegistry family = new NpcFamilyRegistry(Set.of("mother", "father", "one", "two"));
        family.marry("mother", "father");
        family.addChild("mother", "one");
        family.addChild("father", "one");
        family.addChild("mother", "two");
        assertEquals(Set.of("mother", "father"), Set.copyOf(family.parentsOf("one")));
        assertEquals(Set.of("one", "two"), Set.copyOf(family.childrenOf("mother")));
        assertEquals(Set.of("two"), Set.copyOf(family.siblingsOf("one")));
        assertTrue(family.familyOf("one").containsAll(Set.of("mother", "father", "two")));
    }

    @Test void invalidAndCyclicFamilyLinksAreRejected() {
        NpcFamilyRegistry family = new NpcFamilyRegistry(Set.of("a", "b", "c"));
        assertThrows(IllegalArgumentException.class, () -> family.marry("a", "missing"));
        assertThrows(IllegalArgumentException.class, () -> family.addChild("a", "a"));
        family.addChild("a", "b");
        family.addChild("b", "c");
        assertThrows(IllegalArgumentException.class, () -> family.addChild("c", "a"));
        assertThrows(IllegalStateException.class, () -> family.addChild("c", "b"));
    }
}
