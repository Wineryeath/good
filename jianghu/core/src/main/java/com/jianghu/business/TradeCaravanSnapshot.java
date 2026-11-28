package com.jianghu.business;

import java.util.Map;
import java.util.Set;

/** 商队全程快照，保留成员战力、伤势、货物成本、路程与遭遇事件。 */
public record TradeCaravanSnapshot(String id, String ownerNpcId, MarketRegion origin, MarketRegion destination,
    Set<String> crew, Map<String, Integer> crewStrength, Map<String, Integer> crewWounds,
    Map<TradeGood, TradeCaravan.Cargo> cargo, TradeCaravan.State state, int remainingMinutes,
    int elapsedMinutes, int delayMinutes, boolean weatherEventRecorded, boolean ambushEventRecorded,
    int lostCargoQuantity, String lastEvent) {
    public TradeCaravanSnapshot {
        if (id == null || ownerNpcId == null || origin == null || destination == null || origin == destination
                || state == null || remainingMinutes < 0 || elapsedMinutes < 0 || delayMinutes < 0
                || lostCargoQuantity < 0 || lastEvent == null)
            throw new IllegalArgumentException("商队存档属性无效");
        crew = Set.copyOf(crew); crewStrength = Map.copyOf(crewStrength); crewWounds = Map.copyOf(crewWounds); cargo = Map.copyOf(cargo);
        if (!crew.contains(ownerNpcId) || !crewStrength.keySet().equals(crew))
            throw new IllegalArgumentException("商队成员存档不完整");
    }
}