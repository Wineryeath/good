package com.jianghu.item;

import java.util.Map;
import java.util.Optional;

public final class EquipmentCatalog {
    private static final Map<String, EquipmentItem> ITEMS = Map.of(
        "wooden_sword", new EquipmentItem("wooden_sword", "桃木剑", EquipmentItem.Slot.WEAPON, 4, 0, "轻便的练习用木剑。"),
        "iron_sword", new EquipmentItem("iron_sword", "精钢长剑", EquipmentItem.Slot.WEAPON, 10, 0, "锋利耐用的江湖制式长剑。"),
        "cloth_armor", new EquipmentItem("cloth_armor", "粗布护衣", EquipmentItem.Slot.ARMOR, 0, 2, "能挡风，也能略微缓冲伤害。"),
        "leather_armor", new EquipmentItem("leather_armor", "皮制护甲", EquipmentItem.Slot.ARMOR, 0, 7, "以熟皮缝制的轻型护甲。")
    );
    private EquipmentCatalog() {}
    public static Optional<EquipmentItem> find(String id) { return Optional.ofNullable(ITEMS.get(id)); }
    public static Map<String, EquipmentItem> all() { return ITEMS; }
}