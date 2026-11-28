package com.jianghu.npc;

import java.util.ArrayDeque;
import java.util.List;

/** NPC 的持续生活状态：需求、心情、生计与近期记忆。 */
public final class NpcLifeState {
    public enum LifeStage { CHILD, YOUTH, ADULT, ELDER }
    private static final int DAYS_PER_YEAR = 360;
    private int ageYears = 20;
    private int daysLivedThisYear;
    private boolean ageInitialized;
    private static final int MEMORY_LIMIT = 20;
    private int hunger = 25;
    private int energy = 85;
    private int mood = 60;
    private long moneyInWen = 500;
    private final ArrayDeque<String> memories = new ArrayDeque<>();

    public int hunger() { return hunger; }
    public int ageYears() { return ageYears; }
    public int daysLivedThisYear() { return daysLivedThisYear; }
    public LifeStage lifeStage() {
        if (ageYears < 12) return LifeStage.CHILD;
        if (ageYears < 18) return LifeStage.YOUTH;
        if (ageYears < 60) return LifeStage.ADULT;
        return LifeStage.ELDER;
    }
    public void initializeAge(int years) {
        if (ageInitialized || years < 0 || years > 120) throw new IllegalArgumentException("初始年龄无效或已设置");
        ageYears = years;
        ageInitialized = true;
    }
    /** 推进一个游戏日；每 360 天增长一岁。 */
    public boolean passDay() {
        daysLivedThisYear++;
        if (daysLivedThisYear < DAYS_PER_YEAR) return false;
        daysLivedThisYear = 0;
        ageYears++;
        return true;
    }
    public int energy() { return energy; }
    public int mood() { return mood; }
    public long moneyInWen() { return moneyInWen; }
    public List<String> memories() { return List.copyOf(memories); }

    /** 每经过一个游戏小时结算一次需求与生计。 */
    public void passHour(String role, String location, int hour) {
        hunger = clamp(hunger + 4);
        energy = clamp(energy + ("home".equals(location) ? 3 : -2));
        if (hour >= 6 && hour < 19 && !"home".equals(location)) {
            long wage = switch (role) {
                case "茶摊老板" -> 18;
                case "渔夫" -> 12;
                default -> 5;
            };
            moneyInWen += wage;
        }
        if (hunger >= 80) mood = clamp(mood - 2);
        else if (energy >= 60) mood = clamp(mood + 1);
    }

    public void remember(String event) {
        if (event == null || event.isBlank()) throw new IllegalArgumentException("记忆内容不能为空");
        if (memories.size() == MEMORY_LIMIT) memories.removeFirst();
        memories.addLast(event);
    }
    public boolean remembers(String event) { return memories.contains(event); }
    public void changeMood(int amount) { mood = clamp(mood + amount); }
    private static int clamp(int value) { return Math.max(0, Math.min(100, value)); }
}
