package com.jianghu.business;

/** 可由 NPC 家族经营的基础产业类型。 */
public enum BusinessType {
    INN("客栈"), PHARMACY("药铺"), FARM("农庄");
    private final String displayName;
    BusinessType(String displayName) { this.displayName = displayName; }
    public String displayName() { return displayName; }
}
