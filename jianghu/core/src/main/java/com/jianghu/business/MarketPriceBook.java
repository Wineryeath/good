package com.jianghu.business;

import java.util.LinkedHashMap;
import java.util.Map;

/** 由游戏日期与天气驱动的确定性市价表；相同世界状态总能得到相同价格。 */
public final class MarketPriceBook {
    public long priceOf(TradeGood good, long worldDay, String weather) {
        if (good == null || worldDay < 1 || weather == null || weather.isBlank())
            throw new IllegalArgumentException("市场查询参数无效");
        long fluctuation = Math.floorMod(worldDay * 7 + good.ordinal() * 11L, 21) - 10;
        long weatherPremium = switch (weather) {
            case "暴雨" -> switch (good) { case GRAIN, VEGETABLE -> 18; case HERB -> 10; default -> 0; };
            case "暴雪" -> switch (good) { case GRAIN, MEAL, HERB -> 15; default -> 0; };
            case "台风" -> switch (good) { case GRAIN, VEGETABLE -> 25; case HERB, TEA -> 12; default -> 0; };
            default -> 0;
        };
        long percent = 100 + fluctuation + weatherPremium;
        return Math.max(1, Math.multiplyExact(good.basePriceInWen(), percent) / 100);
    }
    public Map<TradeGood, Long> pricesFor(long worldDay, String weather) {
        Map<TradeGood, Long> prices = new LinkedHashMap<>();
        for (TradeGood good : TradeGood.values()) prices.put(good, priceOf(good, worldDay, weather));
        return Map.copyOf(prices);
    }
}
