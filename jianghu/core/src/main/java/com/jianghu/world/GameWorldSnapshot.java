package com.jianghu.world;

import com.jianghu.character.PlayerSnapshot;
import com.jianghu.npc.NpcLifeSnapshot;
import com.jianghu.social.NpcRelationshipSnapshot;
import com.jianghu.business.NpcBusinessSnapshot;
import com.jianghu.business.MarketRegion;
import com.jianghu.business.RegionalMarketSnapshot;
import java.util.Map;
import java.util.Set;

/** 世界核心快照：玩家、日历、天气、NPC 人生状态与玩家关系。 */
public record GameWorldSnapshot(PlayerSnapshot player, long day, int minuteOfDay, String weather,
    Map<String, NpcLifeSnapshot> npcLives, Map<String, NpcRelationshipSnapshot> relationships,
    Map<String, NpcBusinessSnapshot> businesses, Map<MarketRegion, RegionalMarketSnapshot> markets,
    long lastAgedDay, long lastBusinessDay, Set<String> deathRecorded,
    Map<String, Long> lastSocialDay, Map<String, String> independentHouseholds,
    Map<String, Integer> courtshipDays, Map<String, Long> masterTrainingDays, Map<String, Integer> masterFavor) {
    public GameWorldSnapshot {
        if (player == null || day < 1 || minuteOfDay < 0 || minuteOfDay >= 1440 || weather == null || weather.isBlank()
                || lastAgedDay < 1 || lastBusinessDay < 1) throw new IllegalArgumentException("世界存档数据无效");
        npcLives = Map.copyOf(npcLives); relationships = Map.copyOf(relationships);
        businesses = Map.copyOf(businesses); markets = Map.copyOf(markets);
        deathRecorded = Set.copyOf(deathRecorded); lastSocialDay = Map.copyOf(lastSocialDay);
        independentHouseholds = Map.copyOf(independentHouseholds); courtshipDays = Map.copyOf(courtshipDays);
        masterTrainingDays = Map.copyOf(masterTrainingDays); masterFavor = Map.copyOf(masterFavor);
    }
}