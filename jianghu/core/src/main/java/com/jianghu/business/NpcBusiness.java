package com.jianghu.business;

import java.util.LinkedHashSet;
import java.util.Set;

/** NPC 产业账本：每日营收进入产业金库，雇员与所有权随产业保存。 */
public final class NpcBusiness {
    private final String id;
    private final String name;
    private final BusinessType type;
    private String ownerNpcId;
    private final long dailyIncomeInWen;
    private long treasuryInWen;
    private final Set<String> employees = new LinkedHashSet<>();
    private long settledDays;

    public NpcBusiness(String id, String name, BusinessType type, String ownerNpcId,
                       long dailyIncomeInWen, long initialTreasuryInWen) {
        if (id == null || id.isBlank() || name == null || name.isBlank()
                || type == null || ownerNpcId == null || ownerNpcId.isBlank()
                || dailyIncomeInWen < 0 || initialTreasuryInWen < 0)
            throw new IllegalArgumentException("产业资料或账目金额无效");
        this.id = id; this.name = name; this.type = type; this.ownerNpcId = ownerNpcId;
        this.dailyIncomeInWen = dailyIncomeInWen; this.treasuryInWen = initialTreasuryInWen;
    }
    public String id() { return id; }
    public String name() { return name; }
    public BusinessType type() { return type; }
    public String ownerNpcId() { return ownerNpcId; }
    public long dailyIncomeInWen() { return dailyIncomeInWen; }
    public long treasuryInWen() { return treasuryInWen; }
    public long settledDays() { return settledDays; }
    public Set<String> employees() { return Set.copyOf(employees); }
    public void hire(String npcId) {
        if (npcId == null || npcId.isBlank() || ownerNpcId.equals(npcId) || !employees.add(npcId))
            throw new IllegalArgumentException("雇员无效、重复或不能雇佣产业主人");
    }
    public void fire(String npcId) {
        if (!employees.remove(npcId)) throw new IllegalArgumentException("该 NPC 并非产业雇员");
    }
    /** 结算一个游戏日；收入只进入产业金库，不凭空增加主人私人银钱。 */
    public long settleDay() {
        treasuryInWen = Math.addExact(treasuryInWen, dailyIncomeInWen);
        settledDays = Math.addExact(settledDays, 1);
        return dailyIncomeInWen;
    }
    public void transferOwnership(String newOwnerNpcId) {
        if (newOwnerNpcId == null || newOwnerNpcId.isBlank())
            throw new IllegalArgumentException("新主人无效");
        ownerNpcId = newOwnerNpcId;
        employees.remove(newOwnerNpcId);
    }
}
