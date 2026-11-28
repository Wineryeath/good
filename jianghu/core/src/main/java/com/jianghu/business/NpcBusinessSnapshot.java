package com.jianghu.business;

import java.util.Map;
import java.util.Set;

/** NPC 产业账本快照，包含本金、债务、利润、库存成本、雇员与最近结算报表。 */
public record NpcBusinessSnapshot(String id, String name, BusinessType type, String ownerNpcId,
    long dailyIncomeInWen, long dailyOperatingCostInWen, long dailyWagePerEmployeeInWen,
    long treasuryInWen, long debtInWen, long retainedProfitInWen, long settledDays, int level,
    Set<String> employees, Map<TradeGood, Integer> inventory, Map<TradeGood, Long> inventoryCostBasis,
    NpcBusiness.DailyReport lastReport) {
    public NpcBusinessSnapshot {
        if (id == null || name == null || type == null || ownerNpcId == null || treasuryInWen < 0
                || debtInWen < 0 || retainedProfitInWen < 0 || settledDays < 0 || level < 1 || level > 5)
            throw new IllegalArgumentException("产业存档属性无效");
        employees = Set.copyOf(employees); inventory = Map.copyOf(inventory); inventoryCostBasis = Map.copyOf(inventoryCostBasis);
    }
    public static NpcBusinessSnapshot capture(NpcBusiness b) {
        return new NpcBusinessSnapshot(b.id(), b.name(), b.type(), b.ownerNpcId(), b.dailyIncomeInWen(),
            b.dailyOperatingCostInWen(), b.dailyWagePerEmployeeInWen(), b.treasuryInWen(), b.debtInWen(),
            b.retainedProfitInWen(), b.settledDays(), b.level(), b.employees(), b.inventory(),
            b.inventoryCostBasis(), b.lastReport());
    }
}