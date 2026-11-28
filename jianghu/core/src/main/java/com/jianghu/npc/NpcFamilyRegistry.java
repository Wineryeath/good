package com.jianghu.npc;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** NPC 家庭谱系：维护配偶与亲子关系，并提供亲属查询。 */
public final class NpcFamilyRegistry {
    public enum EventType { MARRIAGE, BIRTH, CONFLICT, RECONCILIATION, INHERITANCE, DEATH }
    public record FamilyEvent(EventType type, List<String> participants, long worldDay, String description) {}
    private final Set<String> knownNpcIds;
    private final Map<String, String> spouses = new LinkedHashMap<>();
    private final Map<String, Set<String>> parents = new LinkedHashMap<>();
    private final Map<String, Set<String>> children = new LinkedHashMap<>();
    private final List<FamilyEvent> events = new java.util.ArrayList<>();

    public NpcFamilyRegistry(Set<String> knownNpcIds) {
        if (knownNpcIds == null) throw new IllegalArgumentException("NPC 名册不能为空");
        this.knownNpcIds = Set.copyOf(knownNpcIds);
    }

    public void marry(String firstNpcId, String secondNpcId) {
        validatePair(firstNpcId, secondNpcId);
        if (spouses.containsKey(firstNpcId) || spouses.containsKey(secondNpcId))
            throw new IllegalStateException("NPC 已有配偶");
        if (isAncestor(firstNpcId, secondNpcId) || isAncestor(secondNpcId, firstNpcId))
            throw new IllegalArgumentException("直系亲属不能结为配偶");
        spouses.put(firstNpcId, secondNpcId);
        spouses.put(secondNpcId, firstNpcId);
    }

    public void addChild(String parentNpcId, String childNpcId) {
        validatePair(parentNpcId, childNpcId);
        if (isAncestor(childNpcId, parentNpcId))
            throw new IllegalArgumentException("不能建立循环亲子关系");
        Set<String> childParents = parents.computeIfAbsent(childNpcId, ignored -> new LinkedHashSet<>());
        if (childParents.contains(parentNpcId)) return;
        if (childParents.size() >= 2) throw new IllegalStateException("一个 NPC 最多登记两位父母");
        childParents.add(parentNpcId);
        children.computeIfAbsent(parentNpcId, ignored -> new LinkedHashSet<>()).add(childNpcId);
    }

    public void recordEvent(EventType type, List<String> participants, long worldDay, String description) {
        if (type == null || participants == null || participants.isEmpty() || worldDay < 1 || description == null || description.isBlank())
            throw new IllegalArgumentException("家庭事件资料不完整");
        List<String> unique = participants.stream().distinct().toList();
        unique.forEach(this::requireKnown);
        events.add(new FamilyEvent(type, unique, worldDay, description));
    }
    public List<FamilyEvent> events() { return List.copyOf(events); }

    public Optional<String> spouseOf(String npcId) {
        requireKnown(npcId);
        return Optional.ofNullable(spouses.get(npcId));
    }
    public List<String> parentsOf(String npcId) {
        requireKnown(npcId);
        return List.copyOf(parents.getOrDefault(npcId, Set.of()));
    }
    public List<String> childrenOf(String npcId) {
        requireKnown(npcId);
        return List.copyOf(children.getOrDefault(npcId, Set.of()));
    }
    public List<String> siblingsOf(String npcId) {
        requireKnown(npcId);
        Set<String> result = new LinkedHashSet<>();
        for (String parent : parentsOf(npcId)) result.addAll(childrenOf(parent));
        result.remove(npcId);
        return List.copyOf(result);
    }
    public List<String> familyOf(String npcId) {
        requireKnown(npcId);
        Set<String> result = new LinkedHashSet<>();
        spouseOf(npcId).ifPresent(result::add);
        result.addAll(parentsOf(npcId));
        result.addAll(childrenOf(npcId));
        result.addAll(siblingsOf(npcId));
        return List.copyOf(result);
    }

    public NpcFamilySnapshot snapshot() {
        Map<String, Set<String>> parentCopy = new LinkedHashMap<>();
        parents.forEach((id, values) -> parentCopy.put(id, Set.copyOf(values)));
        return new NpcFamilySnapshot(spouses, parentCopy, events);
    }
    public void restoreSnapshot(NpcFamilySnapshot snapshot) {
        if (snapshot == null) throw new IllegalArgumentException("家庭谱系快照不能为空");
        spouses.clear(); parents.clear(); children.clear(); events.clear();
        for (var entry : snapshot.parents().entrySet()) for (String parent : entry.getValue()) addChild(parent, entry.getKey());
        for (var entry : snapshot.spouses().entrySet())
            if (entry.getKey().compareTo(entry.getValue()) < 0) marry(entry.getKey(), entry.getValue());
        for (FamilyEvent event : snapshot.events()) recordEvent(event.type(), event.participants(), event.worldDay(), event.description());
    }
    private boolean isAncestor(String possibleAncestor, String npcId) {
        for (String parent : parents.getOrDefault(npcId, Set.of())) {
            if (parent.equals(possibleAncestor) || isAncestor(possibleAncestor, parent)) return true;
        }
        return false;
    }
    private void validatePair(String first, String second) {
        requireKnown(first);
        requireKnown(second);
        if (first.equals(second)) throw new IllegalArgumentException("NPC 不能与自己建立家庭关系");
    }
    private void requireKnown(String npcId) {
        if (npcId == null || !knownNpcIds.contains(npcId))
            throw new IllegalArgumentException("家庭关系中包含未知 NPC：" + npcId);
    }
}
