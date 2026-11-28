package com.jianghu.business;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegionalMarketTest {
    @Test void regionalSpecialtiesStartWithGreaterLocalSupply() {
        RegionalMarket jiangnan = new RegionalMarket(MarketRegion.JIANGNAN);
        RegionalMarket beiling = new RegionalMarket(MarketRegion.BEILING);
        assertTrue(jiangnan.stockOf(TradeGood.TEA) > beiling.stockOf(TradeGood.TEA));
        assertTrue(beiling.stockOf(TradeGood.GRAIN) > jiangnan.stockOf(TradeGood.GRAIN));
    }
    @Test void buyingReducesSupplyAndRaisesLocalPrice() {
        RegionalMarket market = new RegionalMarket(MarketRegion.JIANGNAN);
        long before = market.quote(TradeGood.TEA, 1, "晴");
        market.buy(80, TradeGood.TEA);
        assertEquals(120, market.stockOf(TradeGood.TEA));
        assertTrue(market.quote(TradeGood.TEA, 1, "晴") > before);
    }
    @Test void sellingIncreasesSupplyAndLowersLocalPrice() {
        RegionalMarket market = new RegionalMarket(MarketRegion.BEILING);
        long before = market.quote(TradeGood.GRAIN, 1, "晴");
        market.sell(120, TradeGood.GRAIN);
        assertEquals(320, market.stockOf(TradeGood.GRAIN));
        assertTrue(market.quote(TradeGood.GRAIN, 1, "晴") < before);
    }
    @Test void marketRejectsInvalidTradeAndInsufficientSupply() {
        RegionalMarket market = new RegionalMarket(MarketRegion.XISHU);
        assertThrows(IllegalStateException.class, () -> market.buy(1000, TradeGood.HERB));
        assertThrows(IllegalArgumentException.class, () -> market.sell(0, TradeGood.HERB));
    }
}
