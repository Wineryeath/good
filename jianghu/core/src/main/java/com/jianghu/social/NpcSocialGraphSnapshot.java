package com.jianghu.social;

import java.util.List;

/** NPC 师徒、友谊、敌对等社会关系网络快照。 */
public record NpcSocialGraphSnapshot(List<NpcSocialGraph.Bond> bonds) {
    public NpcSocialGraphSnapshot { bonds = List.copyOf(bonds); }
}