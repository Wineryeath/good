package com.jianghu.social;

/** 玩家与单个 NPC 的长期关系；数值限制在 -100 到 100。 */
public final class NpcRelationship {
    private int favor;
    private int trust;
    private int conversations;

    public int favor() { return favor; }
    public int trust() { return trust; }
    public int conversations() { return conversations; }

    public void talk(boolean meaningful) {
        conversations++;
        favor = clamp(favor + (meaningful ? 2 : 1));
        if (meaningful) trust = clamp(trust + 1);
    }
    public void changeFavor(int amount) { favor = clamp(favor + amount); }
    public void changeTrust(int amount) { trust = clamp(trust + amount); }
    private static int clamp(int value) { return Math.max(-100, Math.min(100, value)); }
}
