package com.jianghu.business;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** 行商队伍：管理成员、货物、路程、天气延误与途中遭遇。 */
public final class TradeCaravan {
    public enum State { PREPARING, TRAVELING, ARRIVED, COMPLETED }
    public record Cargo(TradeGood good, int quantity, long totalCostInWen) {}
    private final String id;
    private final String ownerNpcId;
    private final MarketRegion origin;
    private final MarketRegion destination;
    private final Set<String> crew = new LinkedHashSet<>();
    private final Map<String, Integer> crewStrength = new LinkedHashMap<>();
    private final Map<TradeGood, Cargo> cargo = new LinkedHashMap<>();
    private State state = State.PREPARING;
    private int remainingMinutes;
    private int elapsedMinutes;
    private int delayMinutes;
    private boolean weatherEventRecorded;
    private boolean ambushEventRecorded;
    private int lostCargoQuantity;
    private String lastEvent = "商队正在整备";
    public TradeCaravan(String id, String ownerNpcId, MarketRegion origin, MarketRegion destination) {
        if (id == null || id.isBlank() || ownerNpcId == null || ownerNpcId.isBlank() || origin == null || destination == null || origin == destination)
            throw new IllegalArgumentException("商队资料或路线无效");
        this.id = id; this.ownerNpcId = ownerNpcId; this.origin = origin; this.destination = destination;
        crew.add(ownerNpcId);
        crewStrength.put(ownerNpcId, 5);
    }
    public String id() { return id; }
    public String ownerNpcId() { return ownerNpcId; }
    public MarketRegion origin() { return origin; }
    public MarketRegion destination() { return destination; }
    public Set<String> crew() { return Set.copyOf(crew); }
    public Map<String, Integer> crewStrength() { return Map.copyOf(crewStrength); }
    public int escortStrength() { return crewStrength.values().stream().mapToInt(Integer::intValue).sum(); }
    public int lostCargoQuantity() { return lostCargoQuantity; }
    public Map<TradeGood, Cargo> cargo() { return Map.copyOf(cargo); }
    public State state() { return state; }
    public int remainingMinutes() { return remainingMinutes; }
    public int elapsedMinutes() { return elapsedMinutes; }
    public int delayMinutes() { return delayMinutes; }
    public String lastEvent() { return lastEvent; }
    public void addCrew(String npcId) { addCrew(npcId, 5); }
    public void addCrew(String npcId, int combatStrength) {
        requireState(State.PREPARING);
        if (npcId == null || npcId.isBlank() || combatStrength < 1 || combatStrength > 100 || !crew.add(npcId))
            throw new IllegalArgumentException("商队成员无效、重复或武力值不合法");
        crewStrength.put(npcId, combatStrength);
    }
    public void load(TradeGood good, int quantity, long totalCostInWen) {
        requireState(State.PREPARING);
        if (good == null || quantity <= 0 || totalCostInWen <= 0) throw new IllegalArgumentException("货物数量与成本无效");
        Cargo old = cargo.get(good);
        int nextQuantity = Math.addExact(old == null ? 0 : old.quantity(), quantity);
        long nextCost = Math.addExact(old == null ? 0 : old.totalCostInWen(), totalCostInWen);
        cargo.put(good, new Cargo(good, nextQuantity, nextCost));
    }
    public void depart(int travelMinutes) {
        requireState(State.PREPARING);
        if (cargo.isEmpty() || travelMinutes <= 0) throw new IllegalStateException("商队必须装货且路线时间有效");
        remainingMinutes = travelMinutes; state = State.TRAVELING; lastEvent = "商队自" + origin.displayName() + "启程，前往" + destination.displayName();
    }
    /** 推进一分钟；恶劣天气会造成一次额外延误，特定路线种子可能触发劫掠。 */
    public void advanceMinute(String weather) {
        if (state != State.TRAVELING) return;
        if (weather == null || weather.isBlank()) throw new IllegalArgumentException("天气不能为空");
        elapsedMinutes++; remainingMinutes--;
        if (!weatherEventRecorded && (weather.contains("暴雨") || weather.contains("暴雪") || weather.contains("台风"))) {
            weatherEventRecorded = true; delayMinutes = 60; remainingMinutes = Math.addExact(remainingMinutes, delayMinutes);
            lastEvent = "途中遭遇" + weather + "，商队被迫绕路，延误一小时";
        }
        if (!ambushEventRecorded && elapsedMinutes >= Math.max(1, (remainingMinutes + elapsedMinutes) / 2)
                && Math.floorMod(id.hashCode() + origin.ordinal() * 7 + destination.ordinal() * 13, 5) == 0) {
            ambushEventRecorded = true;
            int strength = escortStrength();
            if (strength >= 80) {
                lastEvent = "商路遇袭，护卫阵势严整，劫匪未能靠近货车";
            } else {
                int lossRate = strength >= 40 ? 10 : 25;
                for (Map.Entry<TradeGood, Cargo> entry : new LinkedHashMap<>(cargo).entrySet()) {
                    Cargo item = entry.getValue();
                    int lost = Math.min(item.quantity(), Math.max(1, item.quantity() * lossRate / 100));
                    if (lost > 0) {
                        int remaining = item.quantity() - lost;
                        lostCargoQuantity += lost;
                        if (remaining == 0) cargo.remove(entry.getKey());
                        else cargo.put(entry.getKey(), new Cargo(item.good(), remaining,
                            item.totalCostInWen() * remaining / item.quantity()));
                    }
                }
                lastEvent = "商路遇袭，护卫奋力抵抗，仍有" + lostCargoQuantity + "件货物被劫走";
            }
        }
        if (remainingMinutes <= 0) { remainingMinutes = 0; state = State.ARRIVED; lastEvent = "商队抵达" + destination.displayName(); }
    }
    public Map<TradeGood, Cargo> complete() {
        requireState(State.ARRIVED); state = State.COMPLETED; lastEvent = "商队已在" + destination.displayName() + "完成交货";
        return Map.copyOf(cargo);
    }
    private void requireState(State expected) { if (state != expected) throw new IllegalStateException("商队当前状态为" + state + "，需要" + expected); }
}
