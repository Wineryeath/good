package com.jianghu.item;

public record EquipmentItem(String id, String name, Slot slot, int attack, int defense, String description) {
    public enum Slot { WEAPON, ARMOR }
}