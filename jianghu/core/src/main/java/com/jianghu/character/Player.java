package com.jianghu.character;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Player {
    private final String name;
    private int health = 100, stamina = 100, hunger = 20, thirst = 15, morale = 70, reputation;
    private long money = 2_000; // 以文为最小单位，100 文 = 1 两
    private int x = 9, y = 9;
    private final Map<String, Integer> inventory = new LinkedHashMap<>();
    private final Map<com.jianghu.item.EquipmentItem.Slot, String> equipment = new java.util.EnumMap<>(com.jianghu.item.EquipmentItem.Slot.class);

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
    public int attackPower() { return equipment.values().stream().map(com.jianghu.item.EquipmentCatalog::find).flatMap(java.util.Optional::stream).mapToInt(com.jianghu.item.EquipmentItem::attack).sum(); }
    public int defensePower() { return equipment.values().stream().map(com.jianghu.item.EquipmentCatalog::find).flatMap(java.util.Optional::stream).mapToInt(com.jianghu.item.EquipmentItem::defense).sum(); }
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
