package com.jianghu.business;

import java.util.Map;

/** 区域市场库存与需求快照。 */
public record RegionalMarketSnapshot(MarketRegion region, Map<TradeGood, Integer> stock, Map<TradeGood, Integer> demand) {
    public RegionalMarketSnapshot {
        if (region == null) throw new IllegalArgumentException("市场区域不能为空");
        stock = Map.copyOf(stock); demand = Map.copyOf(demand);
        for (TradeGood good : TradeGood.values())
            if (!stock.containsKey(good) || !demand.containsKey(good) || stock.get(good) < 0 || demand.get(good) < 0)
                throw new IllegalArgumentException("市场库存或需求数据无效");
    }
    public static RegionalMarketSnapshot capture(RegionalMarket market) {
        return new RegionalMarketSnapshot(market.region(), market.stock(), market.demand());
    }
}