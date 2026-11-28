package com.jianghu.business;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** NPC 产业账本：收入、运营成本、工资、债务、升级与员工分红。 */
public final class NpcBusiness {
    public record DailyReport(long revenueInWen, long operatingCostInWen, long payrollInWen,
                              long netProfitInWen, long debtAfterInWen, long treasuryAfterInWen,
                              int level, boolean adverseDay) {}
    private final String id;
    private final String name;
    private final BusinessType type;
    private String ownerNpcId;
    private final long dailyIncomeInWen;
    private final long dailyOperatingCostInWen;
    private final long dailyWagePerEmployeeInWen;
    private long treasuryInWen;
    private long debtInWen;
    private long retainedProfitInWen;
    private final Set<String> employees = new LinkedHashSet<>();
    private long settledDays;
    private int level = 1;
    private DailyReport lastReport;

    public NpcBusiness(String id, String name, BusinessType type, String ownerNpcId,
                       long dailyIncomeInWen, long initialTreasuryInWen) {
        this(id, name, type, ownerNpcId, dailyIncomeInWen, 0, 0, initialTreasuryInWen);
    }
    public NpcBusiness(String id, String name, BusinessType type, String ownerNpcId,
                       long dailyIncomeInWen, long dailyOperatingCostInWen,
                       long dailyWagePerEmployeeInWen, long initialTreasuryInWen) {
        if (id == null || id.isBlank() || name == null || name.isBlank() || type == null
                || ownerNpcId == null || ownerNpcId.isBlank() || dailyIncomeInWen < 0
                || dailyOperatingCostInWen < 0 || dailyWagePerEmployeeInWen < 0 || initialTreasuryInWen < 0)
            throw new IllegalArgumentException("产业资料或账目金额无效");
        this.id = id; this.name = name; this.type = type; this.ownerNpcId = ownerNpcId;
        this.dailyIncomeInWen = dailyIncomeInWen;
        this.dailyOperatingCostInWen = dailyOperatingCostInWen;
        this.dailyWagePerEmployeeInWen = dailyWagePerEmployeeInWen;
        this.treasuryInWen = initialTreasuryInWen;
    }
    public String id() { return id; }
    public String name() { return name; }
    public BusinessType type() { return type; }
    public String ownerNpcId() { return ownerNpcId; }
    public long dailyIncomeInWen() { return dailyIncomeInWen; }
    public long dailyOperatingCostInWen() { return dailyOperatingCostInWen; }
    public long dailyWagePerEmployeeInWen() { return dailyWagePerEmployeeInWen; }
    public long treasuryInWen() { return treasuryInWen; }
    public long debtInWen() { return debtInWen; }
    public long retainedProfitInWen() { return retainedProfitInWen; }
    public long settledDays() { return settledDays; }
    public int level() { return level; }
    public DailyReport lastReport() { return lastReport; }
    public Set<String> employees() { return Set.copyOf(employees); }
    public void hire(String npcId) {
        if (npcId == null || npcId.isBlank() || ownerNpcId.equals(npcId) || !employees.add(npcId))
            throw new IllegalArgumentException("雇员无效、重复或不能雇佣产业主人");
    }
    public void fire(String npcId) {
        if (!employees.remove(npcId)) throw new IllegalArgumentException("该 NPC 并非产业雇员");
    }
    /** 升级消耗产业金库；等级越高，营收与固定运营成本同步上升。 */
    public long upgrade() {
        if (level >= 5) throw new IllegalStateException("产业已达到最高等级");
        long cost = Math.multiplyExact(1000L, level);
        if (treasuryInWen < cost) throw new IllegalStateException("产业金库不足，升级需要 " + cost + " 文");
        treasuryInWen -= cost;
        level++;
        return cost;
    }
    public long settleDay() { return settleDay(false).netProfitInWen(); }
    /** 恶劣天气会令营收减半；经营亏损先耗尽金库，缺口记为产业债务。 */
    public DailyReport settleDay(boolean adverseDay) {
        long revenue = Math.multiplyExact(dailyIncomeInWen, 100L + 25L * (level - 1)) / 100L;
        if (adverseDay) revenue /= 2;
        long operating = Math.multiplyExact(dailyOperatingCostInWen, 100L + 15L * (level - 1)) / 100L;
        long payroll = Math.multiplyExact(dailyWagePerEmployeeInWen, employees.size());
        long profit = Math.subtractExact(revenue, Math.addExact(operating, payroll));
        if (profit >= 0) {
            long debtPayment = Math.min(debtInWen, profit);
            debtInWen -= debtPayment;
            long surplus = profit - debtPayment;
            treasuryInWen = Math.addExact(treasuryInWen, surplus);
            retainedProfitInWen = Math.addExact(retainedProfitInWen, surplus);
        } else {
            long loss = -profit;
            long paid = Math.min(treasuryInWen, loss);
            treasuryInWen -= paid;
            retainedProfitInWen = Math.max(0, retainedProfitInWen - paid);
            debtInWen = Math.addExact(debtInWen, loss - paid);
        }
        settledDays = Math.addExact(settledDays, 1);
        lastReport = new DailyReport(revenue, operating, payroll, profit, debtInWen, treasuryInWen, level, adverseDay);
        return lastReport;
    }
    /** 将累计留存利润按比例平均分给雇员；不动用本金与借款。 */
    public Map<String, Long> distributeProfit(int percentage) {
        if (percentage < 0 || percentage > 50) throw new IllegalArgumentException("分红比例必须在 0..50% 之间");
        if (employees.isEmpty() || percentage == 0 || retainedProfitInWen == 0) return Map.of();
        if (debtInWen > 0 || treasuryInWen == 0) return Map.of();
        long availableProfit = Math.min(retainedProfitInWen, treasuryInWen);
        long pool = availableProfit * percentage / 100;
        long each = pool / employees.size();
        if (each == 0) return Map.of();
        Map<String, Long> result = new LinkedHashMap<>();
        for (String employee : employees) result.put(employee, each);
        long distributed = Math.multiplyExact(each, employees.size());
        retainedProfitInWen -= distributed;
        treasuryInWen -= distributed;
        return Map.copyOf(result);
    }
    public void transferOwnership(String newOwnerNpcId) {
        if (newOwnerNpcId == null || newOwnerNpcId.isBlank()) throw new IllegalArgumentException("新主人无效");
        ownerNpcId = newOwnerNpcId;
        employees.remove(newOwnerNpcId);
    }
}
