package com.jianghu.business;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MarketPriceBookTest {
    @Test void pricesChangeWithWorldDayButRemainDeterministic() {
        MarketPriceBook market = new MarketPriceBook();
        long dayOne = market.priceOf(TradeGood.MEAL, 1, "晴");
        assertEquals(dayOne, market.priceOf(TradeGood.MEAL, 1, "晴"));
        assertNotEquals(dayOne, market.priceOf(TradeGood.MEAL, 2, "晴"));
    }
    @Test void severeWeatherRaisesPricesOfScarceHarvestGoods() {
        MarketPriceBook market = new MarketPriceBook();
        assertTrue(market.priceOf(TradeGood.GRAIN, 1, "暴雨") > market.priceOf(TradeGood.GRAIN, 1, "晴"));
        assertTrue(market.priceOf(TradeGood.HERB, 1, "台风") > market.priceOf(TradeGood.HERB, 1, "晴"));
    }
    @Test void priceBookContainsEveryTradeGood() {
        assertEquals(TradeGood.values().length, new MarketPriceBook().pricesFor(1, "晴").size());
    }
}
