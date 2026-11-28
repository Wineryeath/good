package com.jianghu.social;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class NpcSocialGraphTest {
    @Test void storesTypedRelationshipsAndClampsStrength() {
        NpcSocialGraph graph = new NpcSocialGraph(Set.of("shopkeeper", "fisher", "child"));
        graph.connect("shopkeeper", "fisher", NpcSocialGraph.RelationType.FRIEND, 140);
        assertEquals(100, graph.relation("shopkeeper", "fisher").orElseThrow().strength());
        graph.changeStrength("shopkeeper", "fisher", -35);
        assertEquals(65, graph.relation("shopkeeper", "fisher").orElseThrow().strength());
        assertEquals(1, graph.relationsOf("fisher").size());
    }

    @Test void rejectsSelfLinksAndUnknownNpcIds() {
        NpcSocialGraph graph = new NpcSocialGraph(Set.of("shopkeeper"));
        assertThrows(IllegalArgumentException.class, () ->
            graph.connect("shopkeeper", "shopkeeper", NpcSocialGraph.RelationType.FAMILY, 50));
        assertThrows(IllegalArgumentException.class, () ->
            graph.connect("shopkeeper", "stranger", NpcSocialGraph.RelationType.FRIEND, 20));
    }
}
