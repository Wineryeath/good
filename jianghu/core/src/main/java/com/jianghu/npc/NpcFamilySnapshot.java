package com.jianghu.npc;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** 配偶、亲子与家庭事件谱系快照。 */
public record NpcFamilySnapshot(Map<String, String> spouses, Map<String, Set<String>> parents,
                                List<NpcFamilyRegistry.FamilyEvent> events) {
    public NpcFamilySnapshot {
        spouses = Map.copyOf(spouses);
        Map<String, Set<String>> copy = new java.util.LinkedHashMap<>();
        parents.forEach((id, values) -> copy.put(id, Set.copyOf(values)));
        parents = Map.copyOf(copy); events = List.copyOf(events);
    }
}