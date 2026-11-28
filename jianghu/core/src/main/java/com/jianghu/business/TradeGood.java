package com.jianghu.business;

/** 江湖市集流通的基础商品，价格单位为文。 */
public enum TradeGood {
    MEAL("熟食", 24), TEA("茶叶", 36),
    HERB("草药", 42), MEDICINE("成药", 96),
    GRAIN("粮食", 18), VEGETABLE("时蔬", 12), FISH("鲜鱼", 30);
    private final String displayName;
    private final long basePriceInWen;
    TradeGood(String displayName, long basePriceInWen) {
        this.displayName = displayName; this.basePriceInWen = basePriceInWen;
    }
    public String displayName() { return displayName; }
    public long basePriceInWen() { return basePriceInWen; }
}
