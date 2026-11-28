package com.jianghu.combat;

import java.util.Optional;

/** Simulation-side combat controller. A renderer can drive it with frame delta and input actions. */
public final class RealtimeCombatController {
    public enum Phase { READY, ATTACKING, DODGING, BLOCKING, RECOVERING }
    public record ActionResult(boolean accepted, String message, int staminaSpent, int comboCount) {}
    private Phase phase = Phase.READY;
    private CombatAction lastAction;
    private float cooldown;
    private float actionTime;
    private int comboCount;
    private float dodgeInvulnerability;
    private boolean alive = true;
    public Phase phase() { return phase; }
    public int comboCount() { return comboCount; }
    public float dodgeInvulnerability() { return dodgeInvulnerability; }
    public Optional<CombatAction> lastAction() { return Optional.ofNullable(lastAction); }
    public ActionResult perform(CombatAction action, int availableStamina) {
        if (!alive) return new ActionResult(false, "角色已倒下", 0, comboCount);
        if (action == null) throw new IllegalArgumentException("战斗动作不能为空");
        if (cooldown > 0 || phase == Phase.ATTACKING || phase == Phase.DODGING || phase == Phase.RECOVERING)
            return new ActionResult(false, "动作尚未恢复", 0, comboCount);
        if (availableStamina < action.staminaCost()) return new ActionResult(false, "体力不足", 0, comboCount);
        if (lastAction == CombatAction.LIGHT_ATTACK && action == CombatAction.LIGHT_ATTACK && actionTime <= 0.7f) comboCount = Math.min(5, comboCount + 1);
        else if (action == CombatAction.LIGHT_ATTACK) comboCount = 1;
        else if (action != CombatAction.BLOCK) comboCount = 0;
        lastAction = action; actionTime = 0; cooldown = action.cooldownSeconds();
        phase = switch (action) {
            case LIGHT_ATTACK, HEAVY_ATTACK, SKILL -> Phase.ATTACKING;
            case DODGE, LIGHT_STEP -> Phase.DODGING;
            case BLOCK -> Phase.BLOCKING;
        };
        if (phase == Phase.DODGING) dodgeInvulnerability = 0.22f;
        return new ActionResult(true, action == CombatAction.DODGE ? "侧身闪避" : action == CombatAction.BLOCK ? "架势格挡" : "招式发动", action.staminaCost(), comboCount);
    }
    public void update(float deltaSeconds) {
        if (deltaSeconds < 0) throw new IllegalArgumentException("战斗时间不能倒退");
        cooldown = Math.max(0, cooldown - deltaSeconds);
        dodgeInvulnerability = Math.max(0, dodgeInvulnerability - deltaSeconds);
        actionTime += deltaSeconds;
        if (phase == Phase.RECOVERING) {
            if (actionTime >= 0.12f) phase = Phase.READY;
        } else if (phase == Phase.BLOCKING && cooldown == 0) {
            phase = Phase.READY;
        } else if (phase != Phase.READY && cooldown == 0) {
            phase = actionTime >= 0.12f ? Phase.READY : Phase.RECOVERING;
        }
    }
    public boolean blocksIncomingDamage() { return phase == Phase.BLOCKING; }
    public boolean avoidsIncomingDamage() { return dodgeInvulnerability > 0; }
    public void setAlive(boolean value) { alive = value; if (!value) { phase = Phase.READY; comboCount = 0; } }
}