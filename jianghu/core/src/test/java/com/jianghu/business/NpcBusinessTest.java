package com.jianghu.business;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NpcBusinessTest {
    @Test void dailyRevenueAccumulatesInTreasury() {
        NpcBusiness inn = new NpcBusiness("inn", "听雨客栈", BusinessType.INN, "owner", 320, 500);
        assertEquals(320, inn.settleDay());
        assertEquals(820, inn.treasuryInWen());
        inn.settleDay();
        assertEquals(2, inn.settledDays());
        assertEquals(1140, inn.treasuryInWen());
    }
    @Test void employeesCanBeHiredAndDismissed() {
        NpcBusiness farm = new NpcBusiness("farm", "河畔农庄", BusinessType.FARM, "owner", 100, 0);
        farm.hire("child");
        assertTrue(farm.employees().contains("child"));
        farm.fire("child");
        assertTrue(farm.employees().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> farm.hire("owner"));
    }
    @Test void ownershipCanBeTransferredWithoutLosingTreasury() {
        NpcBusiness pharmacy = new NpcBusiness("shop", "百草药铺", BusinessType.PHARMACY, "parent", 200, 900);
        pharmacy.hire("worker");
        pharmacy.transferOwnership("child");
        assertEquals("child", pharmacy.ownerNpcId());
        assertEquals(900, pharmacy.treasuryInWen());
        assertTrue(pharmacy.employees().contains("worker"));
    }
    @Test void invalidBusinessDataIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
            new NpcBusiness("", "无名", BusinessType.INN, "owner", 1, 0));
        assertThrows(IllegalArgumentException.class, () ->
            new NpcBusiness("inn", "客栈", BusinessType.INN, "owner", -1, 0));
    }
    @Test void operatingCostsAndPayrollReduceProfitAndEmployeesShareRetainedProfit() {
        NpcBusiness inn = new NpcBusiness("inn", "听雨客栈", BusinessType.INN, "owner", 500, 100, 50, 1000);
        inn.hire("worker");
        var report = inn.settleDay(false);
        assertEquals(500, report.revenueInWen());
        assertEquals(100, report.operatingCostInWen());
        assertEquals(50, report.payrollInWen());
        assertEquals(350, report.netProfitInWen());
        assertEquals(1350, inn.treasuryInWen());
        assertEquals(175, inn.distributeProfit(50).get("worker"));
        assertEquals(1175, inn.treasuryInWen());
        assertEquals(175, inn.retainedProfitInWen());
    }
    @Test void upgradesIncreaseRevenueAndOperatingCosts() {
        NpcBusiness farm = new NpcBusiness("farm", "河畔农庄", BusinessType.FARM, "owner", 1000, 100, 0, 2000);
        assertEquals(1000, farm.upgrade());
        assertEquals(2, farm.level());
        var report = farm.settleDay(false);
        assertEquals(1250, report.revenueInWen());
        assertEquals(115, report.operatingCostInWen());
        assertEquals(1135, report.netProfitInWen());
        assertThrows(IllegalArgumentException.class, () -> farm.distributeProfit(51));
    }
    @Test void adverseWeatherCanCreateBusinessDebt() {
        NpcBusiness pharmacy = new NpcBusiness("shop", "百草药铺", BusinessType.PHARMACY, "owner", 100, 200, 0, 50);
        var report = pharmacy.settleDay(true);
        assertEquals(50, report.revenueInWen());
        assertEquals(-150, report.netProfitInWen());
        assertEquals(0, pharmacy.treasuryInWen());
        assertEquals(100, pharmacy.debtInWen());
    }

    @Test void inventoryPurchasesAndSalesTrackCostBasisAndGrossProfit() {
        NpcBusiness farm = new NpcBusiness("farm", "河畔农庄", BusinessType.FARM, "owner", 100, 0, 0, 1000);
        assertEquals(180, farm.restock(TradeGood.GRAIN, 10, 18));
        assertEquals(10, farm.stockOf(TradeGood.GRAIN));
        var sale = farm.sell(TradeGood.GRAIN, 10, 30);
        assertEquals(300, sale.revenueInWen());
        assertEquals(180, sale.costBasisInWen());
        assertEquals(120, sale.grossProfitInWen());
        assertEquals(1120, farm.treasuryInWen());
        assertEquals(120, farm.retainedProfitInWen());
        assertEquals(0, farm.stockOf(TradeGood.GRAIN));
    }
    @Test void inventoryRejectsUnsupportedGoodsAndOverselling() {
        NpcBusiness inn = new NpcBusiness("inn", "听雨客栈", BusinessType.INN, "owner", 100, 500);
        assertThrows(IllegalArgumentException.class, () -> inn.restock(TradeGood.HERB, 1, 10));
        inn.restock(TradeGood.MEAL, 2, 10);
        assertThrows(IllegalArgumentException.class, () -> inn.sell(TradeGood.MEAL, 3, 20));
        assertThrows(IllegalStateException.class, () -> inn.restock(TradeGood.TEA, 100, 100));
    }

}
