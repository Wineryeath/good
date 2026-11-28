package com.jianghu.combat;

import com.jianghu.character.Player;
import java.util.ArrayList;
import java.util.List;

/** 真正结算伤害、体力、连招、闪避与格挡的实时遭遇战。 */
public final class RealtimeCombatEncounter {
    public enum Status { ONGOING, WON, LOST, FLED }
    private final Player player;
    private final CombatEnemy enemy;
    private final RealtimeCombatController controller = new RealtimeCombatController();
    private final List<String> log = new ArrayList<>();
    private Status status = Status.ONGOING;
    private float enemyWindup;
    private float enemyRecovery;
    private boolean enemyTelegraph;
    private boolean rewardsSettled;

    public RealtimeCombatEncounter(Player player, CombatEnemy enemy) {
        if (player == null || enemy == null) throw new IllegalArgumentException("战斗双方不能为空");
        if (player.health() <= 0) throw new IllegalStateException("重伤昏迷时无法进入战斗");
        this.player = player; this.enemy = enemy;
        log.add(enemy.name() + "拦住去路：" + enemy.description());
        enemyWindup = 0.65f;
    }
    public Player player() { return player; }
    public CombatEnemy enemy() { return enemy; }
    public Status status() { return status; }
    public RealtimeCombatController controller() { return controller; }
    public boolean enemyTelegraphing() { return enemyTelegraph; }
    public List<String> log() { return List.copyOf(log); }

    public RealtimeCombatController.ActionResult perform(CombatAction action) {
        requireOngoing();
        RealtimeCombatController.ActionResult result = controller.perform(action, player.stamina());
        if (!result.accepted()) return result;
        if (!player.spendStamina(result.staminaSpent()))
            throw new IllegalStateException("体力状态发生变化，请重试");
        switch (action) {
            case LIGHT_ATTACK, HEAVY_ATTACK, SKILL -> {
                int base = 3 + player.attackPower() + player.martialPower();
                int multiplier = action == CombatAction.HEAVY_ATTACK ? 2 : action == CombatAction.SKILL ? 3 : 1;
                int comboBonus = action == CombatAction.LIGHT_ATTACK ? Math.max(0, result.comboCount() - 1) * 2 : 0;
                int damage = Math.max(1, base * multiplier + comboBonus - enemy.defense());
                enemy.takeDamage(damage);
                log.add(action == CombatAction.HEAVY_ATTACK ? "你沉肩蓄力，重击造成" + damage + "点伤害。"
                    : action == CombatAction.SKILL ? "你催动内劲，绝招造成" + damage + "点伤害。"
                    : "你迅疾出手（连击 " + result.comboCount() + ")，造成" + damage + "点伤害。");
                if (enemy.defeated()) win();
            }
            case DODGE -> log.add("你侧身闪避，短暂进入无敌身法。");
            case LIGHT_STEP -> log.add("你施展轻功掠步，避开锋芒。");
            case BLOCK -> log.add("你沉腕架势，准备格挡来袭攻击。");
        }
        return result;
    }

    public void update(float deltaSeconds) {
        if (deltaSeconds < 0) throw new IllegalArgumentException("战斗时间不能倒退");
        if (status != Status.ONGOING) return;
        controller.update(deltaSeconds);
        if (enemyTelegraph) {
            enemyRecovery -= deltaSeconds;
            if (enemyRecovery <= 0) resolveEnemyStrike();
        } else if (enemyRecovery > 0) {
            enemyRecovery = Math.max(0, enemyRecovery - deltaSeconds);
        } else {
            enemyWindup -= deltaSeconds;
            if (enemyWindup <= 0) { enemyTelegraph = true; enemyRecovery = 0.32f; }
        }
    }

    private void resolveEnemyStrike() {
        enemyTelegraph = false;
        if (controller.avoidsIncomingDamage()) {
            log.add(enemy.name() + "一刀劈空，你借势闪至侧后方。");
        } else {
            int damage = Math.max(1, enemy.attack() - player.defensePower());
            if (controller.blocksIncomingDamage()) damage = Math.max(1, (damage + 1) / 2);
            player.changeHealth(-damage);
            log.add(enemy.name() + (controller.blocksIncomingDamage() ? "的攻势被你格挡，仍造成" : "挥刀命中，你受到") + damage + "点伤害。");
            if (player.health() == 0) { status = Status.LOST; controller.setAlive(false); log.add("你力竭倒地，战斗失败。"); return; }
        }
        enemyWindup = 0.72f;
        enemyRecovery = 0;
    }

    public String flee() {
        requireOngoing(); status = Status.FLED; controller.setAlive(false);
        log.add("你趁对方换招之际施展轻功脱身。"); return log.get(log.size() - 1);
    }
    private void win() {
        status = Status.WON; controller.setAlive(false);
        if (!rewardsSettled) {
            rewardsSettled = true;
            player.addMoney(enemy.rewardWen());
            if (enemy.dropItem() != null && !enemy.dropItem().isBlank() && enemy.dropCount() > 0)
                player.addItem(enemy.dropItem(), enemy.dropCount());
            int levels = player.gainMartialExperience(enemy.experienceReward());
            player.changeReputation(1);
            log.add("战胜" + enemy.name() + "，获得" + enemy.rewardWen() + "文与武学经验" + enemy.experienceReward() + "点。"
                + (enemy.dropCount() > 0 ? "战利品：" + enemy.dropItem() + "×" + enemy.dropCount() + "。" : "")
                + (levels > 0 ? "境界提升至第" + player.martialLevel() + "重！" : ""));
        }
    }
    private void requireOngoing() { if (status != Status.ONGOING) throw new IllegalStateException("这场战斗已经结束"); }
}