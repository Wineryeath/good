package com.jianghu.combat;

public enum CombatAction {
    LIGHT_ATTACK(8, 0.28f), HEAVY_ATTACK(16, 0.62f), DODGE(12, 0.48f),
    BLOCK(5, 0.22f), LIGHT_STEP(10, 0.55f), SKILL(20, 0.75f);
    private final int staminaCost;
    private final float cooldownSeconds;
    CombatAction(int staminaCost, float cooldownSeconds) { this.staminaCost = staminaCost; this.cooldownSeconds = cooldownSeconds; }
    public int staminaCost() { return staminaCost; }
    public float cooldownSeconds() { return cooldownSeconds; }
}