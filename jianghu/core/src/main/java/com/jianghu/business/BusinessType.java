package com.jianghu.business;

import java.util.Set;

/** 可由 NPC 家族经营的基础产业类型及其专营商品。 */
public enum BusinessType {
    INN("客栈", Set.of(TradeGood.MEAL, TradeGood.TEA, TradeGood.FISH)),
    PHARMACY("药铺", Set.of(TradeGood.HERB, TradeGood.MEDICINE)),
    FARM("农庄", Set.of(TradeGood.GRAIN, TradeGood.VEGETABLE));
    private final String displayName;
    private final Set<TradeGood> goods;
    BusinessType(String displayName, Set<TradeGood> goods) { this.displayName = displayName; this.goods = goods; }
    public String displayName() { return displayName; }
    public Set<TradeGood> goods() { return goods; }
    public boolean supports(TradeGood good) { return goods.contains(good); }
}
