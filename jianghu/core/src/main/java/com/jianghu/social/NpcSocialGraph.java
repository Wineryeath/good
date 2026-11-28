package com.jianghu.social;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** NPC 之间的社会关系网络；关系以稳定的 NPC ID 建立，强度限制在 -100..100。 */
public final class NpcSocialGraph {
    public enum RelationType { FAMILY, FRIEND, MENTOR, DISCIPLE, RIVAL, ENEMY }

    public record Bond(String fromNpcId, String toNpcId, RelationType type, int strength) {}

    private final Set<String> knownNpcIds;
    private final Map<String, Bond> bonds = new LinkedHashMap<>();

    public NpcSocialGraph(Set<String> knownNpcIds) {
        if (knownNpcIds == null)
            throw new IllegalArgumentException("NPC 名册不能为空");
        this.knownNpcIds = Set.copyOf(knownNpcIds);
    }

    public void connect(String fromNpcId, String toNpcId, RelationType type, int strength) {
        validatePair(fromNpcId, toNpcId);
        if (type == null) throw new IllegalArgumentException("关系类型不能为空");
        bonds.put(key(fromNpcId, toNpcId), new Bond(fromNpcId, toNpcId, type, clamp(strength)));
    }

    public void changeStrength(String fromNpcId, String toNpcId, int delta) {
        validatePair(fromNpcId, toNpcId);
        Bond old = bonds.get(key(fromNpcId, toNpcId));
        if (old == null) throw new IllegalArgumentException("NPC 之间尚无关系");
        bonds.put(key(fromNpcId, toNpcId),
            new Bond(old.fromNpcId(), old.toNpcId(), old.type(), clamp(old.strength() + delta)));
    }

    public java.util.Optional<Bond> relation(String fromNpcId, String toNpcId) {
        validatePair(fromNpcId, toNpcId);
        return java.util.Optional.ofNullable(bonds.get(key(fromNpcId, toNpcId)));
    }

    public java.util.List<Bond> allRelations() { return java.util.List.copyOf(bonds.values()); }

    public java.util.List<Bond> relationsOf(String npcId) {
        if (!knownNpcIds.contains(npcId)) throw new IllegalArgumentException("未知 NPC：" + npcId);
        return bonds.values().stream()
            .filter(b -> b.fromNpcId().equals(npcId) || b.toNpcId().equals(npcId)).toList();
    }

    private void validatePair(String from, String to) {
        if (from == null || to == null || !knownNpcIds.contains(from) || !knownNpcIds.contains(to))
            throw new IllegalArgumentException("关系中包含未知 NPC");
        if (from.equals(to)) throw new IllegalArgumentException("NPC 不能与自己建立关系");
    }
    private static String key(String from, String to) { return from.length() + ":" + from + to; }
    private static int clamp(int value) { return Math.max(-100, Math.min(100, value)); }
}
