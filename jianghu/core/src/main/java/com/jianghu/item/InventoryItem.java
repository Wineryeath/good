package com.jianghu.item;

/** 背包物品的静态定义；效果数值由游戏规则统一解释。 */
public record InventoryItem(String id, String name, Category category, String description,
                            int health, int stamina, int hunger, int thirst, int morale) {
    public enum Category { FOOD, MEDICINE, MATERIAL, QUEST }
}