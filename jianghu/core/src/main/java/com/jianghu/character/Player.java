package com.jianghu.character;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import com.jianghu.combat.MartialArtStyle;
import com.jianghu.combat.MartialTechnique;

public final class Player {
    private final String name;
    private int health = 100, stamina = 100, hunger = 20, thirst = 15, morale = 70, reputation;
    private long money = 2_000; // 以文为最小单位，100 文 = 1 两
    private int x = 9, y = 9;
    private final Map<String, Integer> inventory = new LinkedHashMap<>();
    private final Map<com.jianghu.item.EquipmentItem.Slot, String> equipment = new java.util.EnumMap<>(com.jianghu.item.EquipmentItem.Slot.class);
    private int martialExperience;
    private int martialLevel = 1;
    private int innerEnergy = 60;
    private MartialArtStyle martialStyle = MartialArtStyle.FIST;
    private final Set<MartialArtStyle> learnedStyles = EnumSet.of(MartialArtStyle.FIST);
    private final Set<String> learnedTechniques = new LinkedHashSet<>(Set.of("basic_palm"));

    public Player(String name) { this.name = name == null || name.isBlank() ? "无名旅人" : name; }
    public String name() { return name; }
    public int health() { return health; }
    public int stamina() { return stamina; }
    public int hunger() { return hunger; }
    public int thirst() { return thirst; }
    /** 饥饿与口渴均以 0..100 表示需求强度，数值越低越舒适。 */
    public void changeHunger(int amount) { hunger = clamp(hunger + amount); }
    public void changeThirst(int amount) { thirst = clamp(thirst + amount); }
    public void changeMorale(int amount) { morale = clamp(morale + amount); }
    public int morale() { return morale; }
    public int reputation() { return reputation; }
    public long moneyInWen() { return money; }
    public int x() { return x; }
    public int y() { return y; }
    public Map<String,Integer> inventory() { return Collections.unmodifiableMap(inventory); }
    public Map<com.jianghu.item.EquipmentItem.Slot, String> equipment() { return Collections.unmodifiableMap(equipment); }
    public int martialExperience() { return martialExperience; }
    public int martialLevel() { return martialLevel; }
    public int martialPower() { return martialLevel * 2; }
    public int innerEnergy() { return innerEnergy; }
    public MartialArtStyle martialStyle() { return martialStyle; }
    public Set<MartialArtStyle> learnedStyles() { return Set.copyOf(learnedStyles); }
    public Set<String> learnedTechniques() { return Set.copyOf(learnedTechniques); }
    public void learnStyle(MartialArtStyle style) {
        if (style == null) throw new IllegalArgumentException("武学流派不能为空");
        learnedStyles.add(style);
    }
    public void selectStyle(MartialArtStyle style) {
        if (style == null || !learnedStyles.contains(style)) throw new IllegalStateException("尚未学会这门武学");
        martialStyle = style;
    }
    public void learnTechnique(String techniqueId) {
        MartialTechnique technique = MartialTechnique.find(techniqueId)
            .orElseThrow(() -> new IllegalArgumentException("未知招式：" + techniqueId));
        if (!learnedStyles.contains(technique.style())) throw new IllegalStateException("尚未掌握该招式所属的武学流派");
        learnedTechniques.add(techniqueId);
    }
    public boolean knowsTechnique(String techniqueId) { return learnedTechniques.contains(techniqueId); }
    public boolean spendInnerEnergy(int amount) {
        if (amount < 0) throw new IllegalArgumentException("内力消耗不能为负");
        if (innerEnergy < amount) return false;
        innerEnergy -= amount; return true;
    }
    public void recoverInnerEnergy(int amount) {
        if (amount < 0) throw new IllegalArgumentException("内力恢复不能为负");
        innerEnergy = Math.min(100, innerEnergy + amount);
    }
    /** 武学经验每满百点提升一重境界，境界提升会增强基础攻击。 */
    public int gainMartialExperience(int amount) {
        if (amount < 0) throw new IllegalArgumentException("武学经验不能为负");
        martialExperience += amount;
        int levels = 0;
        while (martialExperience >= martialLevel * 100 && martialLevel < 10) {
            martialExperience -= martialLevel * 100;
            martialLevel++;
            levels++;
        }
        return levels;
    }
    public int attackPower() { return martialStyle.attackBonus() + equipment.values().stream().map(com.jianghu.item.EquipmentCatalog::find).flatMap(java.util.Optional::stream).mapToInt(com.jianghu.item.EquipmentItem::attack).sum(); }
    public int defensePower() { return Math.max(0, martialStyle.defenseBonus()) + equipment.values().stream().map(com.jianghu.item.EquipmentCatalog::find).flatMap(java.util.Optional::stream).mapToInt(com.jianghu.item.EquipmentItem::defense).sum(); }
    /** 装备时自动将旧装备放回背包；新装备只有在成功扣除库存后才替换。 */
    public boolean equip(String itemId) {
        var item = com.jianghu.item.EquipmentCatalog.find(itemId).orElseThrow(() -> new IllegalArgumentException("这件物品不能装备"));
        if (!removeItem(itemId, 1)) return false;
        String previous = equipment.put(item.slot(), itemId);
        if (previous != null) addItem(previous, 1);
        return true;
    }
    public boolean unequip(com.jianghu.item.EquipmentItem.Slot slot) {
        String previous = equipment.remove(slot);
        if (previous == null) return false;
        addItem(previous, 1);
        return true;
    }
    public void moveTo(int nx, int ny) { x = nx; y = ny; }
    public boolean spendStamina(int amount) {
        if (amount < 0) throw new IllegalArgumentException("体力消耗不能为负");
        if (stamina < amount) return false;
        stamina -= amount; return true;
    }
    public void recoverStamina(int amount) { stamina = clamp(stamina + amount); }
    /** 战斗伤害与疗伤统一作用于玩家健康值，范围限制在 0..100。 */
    public void changeHealth(int amount) { health = clamp(health + amount); }
    public void changeReputation(int amount) { reputation = Math.max(-100, Math.min(100, reputation + amount)); }
    public void addMoney(long wen) { if (money + wen < 0) throw new IllegalArgumentException("银钱不足"); money += wen; }
    public void addItem(String id, int count) {
        if (id == null || id.isBlank() || count <= 0) throw new IllegalArgumentException("物品参数无效");
        inventory.merge(id, count, Integer::sum);
    }
    public boolean removeItem(String id, int count) {
        int have = inventory.getOrDefault(id, 0);
        if (count <= 0 || have < count) return false;
        if (have == count) inventory.remove(id); else inventory.put(id, have - count);
        return true;
    }
    private static int clamp(int n) { return Math.max(0, Math.min(100, n)); }
}
