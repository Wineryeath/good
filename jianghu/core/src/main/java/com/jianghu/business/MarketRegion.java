package com.jianghu.business;

/** 江湖五大商贸区域；区域特产会改变本地初始供给。 */
public enum MarketRegion {
    JIANGNAN("江南", TradeGood.TEA, TradeGood.VEGETABLE),
    BEILING("北岭", TradeGood.GRAIN, TradeGood.MEAL),
    XISHU("西蜀", TradeGood.HERB, TradeGood.MEDICINE),
    DONGHAI("东海", TradeGood.FISH, TradeGood.TEA),
    JINGCHENG("京城", TradeGood.MEAL, TradeGood.MEDICINE);
    private final String displayName;
    private final TradeGood specialty;
    private final TradeGood secondary;
    MarketRegion(String displayName, TradeGood specialty, TradeGood secondary) {
        this.displayName = displayName; this.specialty = specialty; this.secondary = secondary;
    }
    public String displayName() { return displayName; }
    public int startingSupplyBonus(TradeGood good) {
        if (good == specialty) return 100;
        if (good == secondary) return 50;
        return 0;
    }
}
