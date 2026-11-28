package com.jianghu.npc;

import java.util.ArrayDeque;
import java.util.List;

/** NPC 的持续生活状态：需求、心情、生计与近期记忆。 */
public final class NpcLifeState {
    private static final int MEMORY_LIMIT = 20;
    private int hunger = 25;
    private int energy = 85;
    private int mood = 60;
    private long moneyInWen = 500;
    private final ArrayDeque<String> memories = new ArrayDeque<>();

    public int hunger() { return hunger; }
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
