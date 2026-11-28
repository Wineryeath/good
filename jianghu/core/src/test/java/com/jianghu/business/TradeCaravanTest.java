package com.jianghu.business;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TradeCaravanTest {
    @Test void caravanRequiresCargoAndCompletesItsJourney() {
        TradeCaravan caravan = new TradeCaravan("c1", "leader", MarketRegion.JIANGNAN, MarketRegion.BEILING);
        assertThrows(IllegalStateException.class, () -> caravan.depart(300));
        caravan.load(TradeGood.TEA, 5, 150);
        caravan.depart(3);
        caravan.advanceMinute("晴");
        assertEquals(TradeCaravan.State.TRAVELING, caravan.state());
        caravan.advanceMinute("晴"); caravan.advanceMinute("晴");
        assertEquals(TradeCaravan.State.ARRIVED, caravan.state());
        assertEquals(5, caravan.complete().get(TradeGood.TEA).quantity());
        assertEquals(TradeCaravan.State.COMPLETED, caravan.state());
    }
    @Test void severeWeatherDelaysTheCaravanOnlyOnce() {
        TradeCaravan caravan = new TradeCaravan("storm", "leader", MarketRegion.JIANGNAN, MarketRegion.DONGHAI);
        caravan.load(TradeGood.TEA, 1, 30);
        caravan.depart(4);
        caravan.advanceMinute("暴雨");
        assertEquals(63, caravan.remainingMinutes());
        for (int i = 0; i < 63; i++) caravan.advanceMinute("暴雨");
        assertEquals(TradeCaravan.State.ARRIVED, caravan.state());
        assertEquals(60, caravan.delayMinutes());
    }
    @Test void caravanRejectsDuplicateCrewAndInvalidCargo() {
        TradeCaravan caravan = new TradeCaravan("crew", "leader", MarketRegion.XISHU, MarketRegion.JINGCHENG);
        assertThrows(IllegalArgumentException.class, () -> caravan.addCrew("leader"));
        assertThrows(IllegalArgumentException.class, () -> caravan.load(TradeGood.HERB, 0, 10));
    }
    @Test void weakEscortCanLoseCargoDuringAnAmbush() {
        TradeCaravan caravan = new TradeCaravan("raid", "leader", MarketRegion.JIANGNAN, MarketRegion.DONGHAI);
        caravan.load(TradeGood.TEA, 8, 240);
        caravan.depart(4);
        caravan.advanceMinute("晴");
        caravan.advanceMinute("晴");
        assertEquals(2, caravan.lostCargoQuantity());
        assertEquals(6, caravan.cargo().get(TradeGood.TEA).quantity());
        assertTrue(caravan.lastEvent().contains("被劫走"));
        assertEquals(25, caravan.crewWounds().get("leader"));
        assertTrue(caravan.woundedCrew().contains("leader"));
    }
    @Test void strongEscortProtectsCargoFromTheSameAmbush() {
        TradeCaravan caravan = new TradeCaravan("raid", "leader", MarketRegion.JIANGNAN, MarketRegion.DONGHAI);
        caravan.addCrew("guard1", 30); caravan.addCrew("guard2", 30); caravan.addCrew("guard3", 30);
        caravan.load(TradeGood.TEA, 8, 240);
        caravan.depart(4);
        caravan.advanceMinute("晴"); caravan.advanceMinute("晴");
        assertEquals(95, caravan.escortStrength());
        assertEquals(0, caravan.lostCargoQuantity());
        assertEquals(8, caravan.cargo().get(TradeGood.TEA).quantity());
        assertTrue(caravan.lastEvent().contains("劫匪未能靠近"));
        assertTrue(caravan.crewWounds().isEmpty());
    }

}
