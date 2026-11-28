package com.jianghu.business;

import java.util.EnumMap;
import java.util.Map;

/** 单一区域的市场供需账本；采购消耗现货，销售补充现货。 */
public final class RegionalMarket {
    private final MarketRegion region;
    private final Map<TradeGood, Integer> stock = new EnumMap<>(TradeGood.class);
    private final Map<TradeGood, Integer> demand = new EnumMap<>(TradeGood.class);
    public RegionalMarket(MarketRegion region) {
        if (region == null) throw new IllegalArgumentException("市场区域不能为空");
        this.region = region;
        for (TradeGood good : TradeGood.values()) {
            stock.put(good, 100 + region.startingSupplyBonus(good));
            demand.put(good, 50);
        }
    }
    public MarketRegion region() { return region; }
    public int stockOf(TradeGood good) { requireGood(good); return stock.get(good); }
    public int demandOf(TradeGood good) { requireGood(good); return demand.get(good); }
    public Map<TradeGood, Integer> stock() { return Map.copyOf(stock); }
    public long quote(TradeGood good, long worldDay, String weather) {
        requireGood(good);
        long base = new MarketPriceBook().priceOf(good, worldDay, weather);
        int supply = stock.get(good);
        int buyers = demand.get(good);
        long supplyDemandPercent = 100L + buyers / 2L - Math.min(70, supply / 3L);
        return Math.max(1, Math.multiplyExact(base, supplyDemandPercent) / 100L);
    }
    /** 从本地市场采购，市场现货不足时拒绝交易。 */
    public void buy(int quantity, TradeGood good) {
        requireQuantity(quantity); requireGood(good);
        int available = stock.get(good);
        if (quantity > available) throw new IllegalStateException("当地" + good.displayName() + "现货不足");
        stock.put(good, available - quantity);
        demand.put(good, Math.addExact(demand.get(good), Math.max(1, quantity / 10)));
    }
    /** 将货物卖入本地市场，增加供给并缓和价格。 */
    public void sell(int quantity, TradeGood good) {
        requireQuantity(quantity); requireGood(good);
        stock.put(good, Math.addExact(stock.get(good), quantity));
        demand.put(good, Math.max(0, demand.get(good) - Math.max(1, quantity / 20)));
    }
    /** 本地居民每日消耗会减少库存，形成持续供需变化。 */
    public void consume(TradeGood good, int quantity) {
        requireQuantity(quantity); requireGood(good);
        stock.put(good, Math.max(0, stock.get(good) - quantity));
        demand.put(good, Math.addExact(demand.get(good), quantity));
    }
    private static void requireGood(TradeGood good) { if (good == null) throw new IllegalArgumentException("商品不能为空"); }
    private static void requireQuantity(int quantity) { if (quantity <= 0) throw new IllegalArgumentException("交易数量必须为正数"); }
}
