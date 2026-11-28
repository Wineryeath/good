package com.jianghu.combat;

import com.jianghu.character.Player;
import java.util.ArrayList;
import java.util.List;

/** 确定性回合制遭遇战；每次出招后敌人若仍存活便立即反击。 */
public final class CombatEncounter {
    public enum Status { ONGOING, WON, LOST, FLED }
    private final Player player;
    private final CombatEnemy enemy;
    private final List<String> log = new ArrayList<>();
    private Status status = Status.ONGOING;
    private int rounds;
    public CombatEncounter(Player player, CombatEnemy enemy) {
        if (player == null || enemy == null) throw new IllegalArgumentException("战斗双方不能为空");
        if (player.health() <= 0) throw new IllegalStateException("重伤昏迷时无法进入战斗");
        this.player=player; this.enemy=enemy;
        log.add(enemy.name()+"拦住去路："+enemy.description());
    }
    public Player player(){return player;} public CombatEnemy enemy(){return enemy;} public Status status(){return status;}
    public int rounds(){return rounds;} public List<String> log(){return List.copyOf(log);}
    public String playerAttack() {
        requireOngoing();
        if (!player.spendStamina(8)) throw new IllegalStateException("体力不足，无法出招");
        rounds++;
        int damage=Math.max(1, 3+player.attackPower()+player.martialPower()-enemy.defense());
        enemy.takeDamage(damage);
        log.add("你施展招式，对"+enemy.name()+"造成"+damage+"点伤害。");
        if (enemy.defeated()) {
            status=Status.WON; player.addMoney(enemy.rewardWen());
            if (enemy.dropItem()!=null && !enemy.dropItem().isBlank() && enemy.dropCount()>0) player.addItem(enemy.dropItem(),enemy.dropCount());
            int levels=player.gainMartialExperience(enemy.experienceReward());
            player.changeReputation(1);
            log.add("战胜"+enemy.name()+"，获得"+enemy.rewardWen()+"文、武学经验"+enemy.experienceReward()+"点"
                +(enemy.dropCount()>0?"与战利品"+enemy.dropItem()+"×"+enemy.dropCount():"")+"。");
            if(levels>0) log.add("武学境界提升至第"+player.martialLevel()+"重！");
            return log.get(log.size()-1);
        }
        int received=Math.max(1,enemy.attack()-player.defensePower());
        player.changeHealth(-received);
        log.add(enemy.name()+"反击，你受到"+received+"点伤害。");
        if(player.health()==0){status=Status.LOST;log.add("你力竭倒地，战斗失败。");}
        return log.get(log.size()-1);
    }
    /** 使用已掌握且与当前流派匹配的招式；资源不足时不会消耗任一资源。 */
    public String useTechnique(String techniqueId) {
        requireOngoing();
        MartialTechnique technique = MartialTechnique.find(techniqueId)
            .orElseThrow(() -> new IllegalArgumentException("未知招式：" + techniqueId));
        if (!player.knowsTechnique(techniqueId)) throw new IllegalStateException("尚未学会这招");
        if (player.martialStyle() != technique.style()) throw new IllegalStateException("当前武学路数与招式不合");
        if (player.stamina() < technique.staminaCost() || player.innerEnergy() < technique.innerCost())
            throw new IllegalStateException("体力或内力不足，无法施展" + technique.displayName());
        player.spendStamina(technique.staminaCost());
        player.spendInnerEnergy(technique.innerCost());
        rounds++;
        int damage = Math.max(1, 3 + player.attackPower() + player.martialPower() + technique.power() - enemy.defense());
        enemy.takeDamage(damage);
        log.add("你施展" + technique.displayName() + "，对" + enemy.name() + "造成" + damage + "点伤害。");
        if (enemy.defeated()) {
            status = Status.WON; player.addMoney(enemy.rewardWen());
            if (enemy.dropItem()!=null && !enemy.dropItem().isBlank() && enemy.dropCount()>0) player.addItem(enemy.dropItem(),enemy.dropCount());
            int levels=player.gainMartialExperience(enemy.experienceReward()); player.changeReputation(1);
            log.add("战胜"+enemy.name()+"，获得"+enemy.rewardWen()+"文、武学经验"+enemy.experienceReward()+"点");
            if(levels>0) log.add("武学境界提升至第"+player.martialLevel()+"重！");
            return log.get(log.size()-1);
        }
        int received=Math.max(1,enemy.attack()-player.defensePower()); player.changeHealth(-received);
        log.add(enemy.name()+"反击，你受到"+received+"点伤害。");
        if(player.health()==0){status=Status.LOST;log.add("你力竭倒地，战斗失败。");}
        return log.get(log.size()-1);
    }
    public String flee() {
        requireOngoing(); status=Status.FLED; log.add("你趁对方换招之际脱身。"); return log.get(log.size()-1);
    }
    private void requireOngoing(){if(status!=Status.ONGOING)throw new IllegalStateException("这场战斗已经结束");}
}