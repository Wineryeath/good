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
}
