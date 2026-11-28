package com.jianghu.npc;

import java.util.List;
import java.util.Map;

/** NPC 人生状态快照，覆盖年龄、需求、职业、财富、疾病、死亡与近期记忆。 */
public record NpcLifeSnapshot(int ageYears, int daysLivedThisYear, boolean ageInitialized,
    int hunger, int energy, int mood, int education, String profession,
    Map<String, Integer> vocationalTraining, long moneyInWen, int health, boolean alive,
    boolean estateTransferred, String illness, int illnessSeverity, List<String> memories) {
    public NpcLifeSnapshot {
        if (ageYears < 0 || ageYears > 120 || daysLivedThisYear < 0 || daysLivedThisYear >= 360
                || hunger < 0 || hunger > 100 || energy < 0 || energy > 100 || mood < 0 || mood > 100
                || education < 0 || education > 100 || moneyInWen < 0 || health < 0 || health > 100
                || illnessSeverity < 0 || illnessSeverity > 3 || (alive && health == 0) || (!alive && health != 0))
            throw new IllegalArgumentException("NPC 生活存档属性无效");
        vocationalTraining = Map.copyOf(vocationalTraining); memories = List.copyOf(memories);
    }
    public static NpcLifeSnapshot capture(NpcLifeState state) {
        return new NpcLifeSnapshot(state.ageYears(), state.daysLivedThisYear(), state.ageInitialized(),
            state.hunger(), state.energy(), state.mood(), state.education(), state.profession().orElse(null),
            state.vocationalTraining(), state.moneyInWen(), state.health(), state.alive(), state.estateTransferred(),
            state.illness(), state.illnessSeverity(), state.memories());
    }
}