package com.jianghu.npc;

import java.util.ArrayDeque;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

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
    private int education;
    private String profession;
    private final Map<String, Integer> vocationalTraining = new LinkedHashMap<>();
    private long moneyInWen = 500;
    private int health = 100;
    private boolean alive = true;
    private boolean estateTransferred;
    private String illness;
    private int illnessSeverity;
    private final ArrayDeque<String> memories = new ArrayDeque<>();

    public int hunger() { return hunger; }
    public int ageYears() { return ageYears; }
    public boolean ageInitialized() { return ageInitialized; }
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
        if (illness != null) {
            health = clamp(health - illnessSeverity * 2);
            if (health == 0) alive = false;
        } else {
            health = clamp(health + 1);
        }
        daysLivedThisYear++;
        if (daysLivedThisYear < DAYS_PER_YEAR) return false;
        daysLivedThisYear = 0;
        ageYears++;
        return true;
    }
    public int energy() { return energy; }
    public int mood() { return mood; }
    public int education() { return education; }
    public java.util.Optional<String> profession() { return java.util.Optional.ofNullable(profession); }
    public int vocationalTraining(String professionName) { return vocationalTraining.getOrDefault(professionName, 0); }
    public Map<String, Integer> vocationalTraining() { return Map.copyOf(vocationalTraining); }
    public void trainProfession(String professionName, int points) {
        if (!alive) throw new IllegalStateException("已故 NPC 无法学习技艺");
        if (professionName == null || professionName.isBlank() || points < 1)
            throw new IllegalArgumentException("技艺名称或训练值无效");
        vocationalTraining.merge(professionName, points, (oldValue, added) -> Math.min(100, oldValue + added));
    }
    public void chooseProfession(String value) {
        if (!alive) throw new IllegalStateException("已故 NPC 无法选择职业");
        if (value == null || value.isBlank()) throw new IllegalArgumentException("职业名称不能为空");
        if (profession != null) throw new IllegalStateException("已经选择职业");
        profession = value;
    }
    public String educationStage() {
        if (education < 20) return "启蒙";
        if (education < 50) return "识字";
        if (education < 80) return "通晓";
        return "博学";
    }
    /** 教育成长值限制在 0..100。 */
    public void study(int points) {
        if (!alive) throw new IllegalStateException("已故 NPC 无法学习");
        if (points < 1) throw new IllegalArgumentException("学习成长值必须为正数");
        education = Math.min(100, education + points);
    }
    public long moneyInWen() { return moneyInWen; }
    public void creditMoney(long amount) {
        if (amount < 0) throw new IllegalArgumentException("入账金额不能为负数");
        moneyInWen = Math.addExact(moneyInWen, amount);
    }
    public void debitMoney(long amount) {
        if (amount < 0 || amount > moneyInWen) throw new IllegalArgumentException("支出金额无效或余额不足");
        moneyInWen -= amount;
    }
    public int health() { return health; }
    public boolean alive() { return alive; }
    public boolean sick() { return illness != null; }
    public String illness() { return illness; }
    public int illnessSeverity() { return illnessSeverity; }
    public void contractIllness(String name, int severity) {
        if (!alive) throw new IllegalStateException("已故 NPC 不会患病");
        if (name == null || name.isBlank() || severity < 1 || severity > 3)
            throw new IllegalArgumentException("疾病名称或严重程度无效");
        illness = name;
        illnessSeverity = Math.max(illnessSeverity, severity);
    }
    /** 治疗会降低病情并恢复健康；severityReduction 至少为 1。 */
    public void treatIllness(int severityReduction, int healthRestored) {
        if (!alive) throw new IllegalStateException("无法治疗已故 NPC");
        if (illness == null) throw new IllegalStateException("NPC 当前没有患病");
        if (severityReduction < 1 || healthRestored < 0)
            throw new IllegalArgumentException("治疗参数无效");
        illnessSeverity = Math.max(0, illnessSeverity - severityReduction);
        health = clamp(health + healthRestored);
        if (illnessSeverity == 0) illness = null;
    }
    public void changeHealth(int amount) {
        if (!alive) throw new IllegalStateException("已故 NPC 无法改变健康");
        health = clamp(health + amount);
        if (health == 0) alive = false;
    }
    public void die() {
        if (!alive) throw new IllegalStateException("NPC 已经去世");
        alive = false;
        health = 0;
    }
    /** 将全部现有银钱转交给继承人，返回实际转移金额。 */
    public long transferWealthTo(NpcLifeState heir) {
        if (heir == null || heir == this) throw new IllegalArgumentException("继承人无效");
        if (alive) throw new IllegalStateException("在世 NPC 不能进行遗产继承");
        if (estateTransferred) throw new IllegalStateException("遗产已经完成继承");
        if (!heir.alive) throw new IllegalStateException("继承人已故");
        long amount = moneyInWen;
        heir.moneyInWen = Math.addExact(heir.moneyInWen, amount);
        moneyInWen = 0;
        estateTransferred = true;
        return amount;
    }
    public List<String> memories() { return List.copyOf(memories); }

    /** 每经过一个游戏小时结算一次需求与生计。 */
    public void passHour(String role, String location, int hour) {
        hunger = clamp(hunger + 4);
        energy = clamp(energy + ("home".equals(location) ? 3 : -2));
        if (hour >= 6 && hour < 19 && !"home".equals(location)) {
            long wage = switch (role) {
                case "茶摊老板" -> 18;
                case "渔夫" -> 12;
                case "药师" -> 20;
                case "书生" -> 10;
                case "教书先生" -> 22;
                case "镖师" -> 24;
                case "农夫" -> 8;
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
