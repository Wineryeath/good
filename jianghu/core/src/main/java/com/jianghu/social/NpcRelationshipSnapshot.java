package com.jianghu.social;

/** 玩家与 NPC 关系快照。 */
public record NpcRelationshipSnapshot(int favor, int trust, int conversations) {
    public NpcRelationshipSnapshot {
        if (favor < -100 || favor > 100 || trust < -100 || trust > 100 || conversations < 0)
            throw new IllegalArgumentException("NPC 关系存档属性无效");
    }
    public static NpcRelationshipSnapshot capture(NpcRelationship relationship) {
        return new NpcRelationshipSnapshot(relationship.favor(), relationship.trust(), relationship.conversations());
    }
}