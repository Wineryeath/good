package com.jianghu.item;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** 游戏物品图鉴：集中维护名称、分类、说明与可使用效果。 */
public final class InventoryCatalog {
    private static final Map<String, InventoryItem> ITEMS = createItems();
    private InventoryCatalog() {}
    private static Map<String, InventoryItem> createItems() {
        Map<String, InventoryItem> items = new LinkedHashMap<>();
        add(items, new InventoryItem("meal", "江南熟食", InventoryItem.Category.FOOD, "饱腹的热食，缓解饥饿。", 0, 8, -35, -3, 2));
        add(items, new InventoryItem("tea", "清茶", InventoryItem.Category.FOOD, "一盏清茶，解渴醒神。", 0, 4, 0, -40, 4));
        add(items, new InventoryItem("medicine", "金疮药", InventoryItem.Category.MEDICINE, "常备疗伤药，可恢复健康。", 28, 0, 0, 0, 0));
        add(items, new InventoryItem("herb", "新鲜草药", InventoryItem.Category.MATERIAL, "可用于委托、赠礼与后续制药。", 0, 0, 0, 0, 0));
        add(items, new InventoryItem("grain", "南地稻米", InventoryItem.Category.FOOD, "可储存的粮食，暂不能直接食用。", 0, 0, 0, 0, 0));
        add(items, new InventoryItem("vegetable", "时令蔬菜", InventoryItem.Category.FOOD, "新鲜时蔬，可供烹饪。", 0, 0, 0, 0, 0));
        add(items, new InventoryItem("fish", "河鲜", InventoryItem.Category.FOOD, "鲜鱼尚需烹制后食用。", 0, 0, 0, 0, 0));
        add(items, new InventoryItem("wooden_sword", "桃木剑", InventoryItem.Category.MATERIAL, "轻便的练习用木剑，可装备为武器。", 0, 0, 0, 0, 0));
        add(items, new InventoryItem("iron_sword", "精钢长剑", InventoryItem.Category.MATERIAL, "锋利耐用的制式长剑，可装备为武器。", 0, 0, 0, 0, 0));
        add(items, new InventoryItem("cloth_armor", "粗布护衣", InventoryItem.Category.MATERIAL, "能略微缓冲伤害，可装备为护甲。", 0, 0, 0, 0, 0));
        add(items, new InventoryItem("leather_armor", "皮制护甲", InventoryItem.Category.MATERIAL, "轻型护甲，可装备为护甲。", 0, 0, 0, 0, 0));
        return Map.copyOf(items);
    }
    private static void add(Map<String, InventoryItem> items, InventoryItem item) { items.put(item.id(), item); }
    public static Optional<InventoryItem> find(String id) { return Optional.ofNullable(ITEMS.get(id)); }
    public static Map<String, InventoryItem> all() { return ITEMS; }
}