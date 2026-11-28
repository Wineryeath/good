package com.jianghu.world;

import com.jianghu.character.PlayerSnapshot;
import com.jianghu.npc.NpcLifeSnapshot;
import com.jianghu.social.NpcRelationshipSnapshot;
import com.jianghu.business.NpcBusinessSnapshot;
import com.jianghu.business.MarketRegion;
import com.jianghu.business.RegionalMarketSnapshot;
import com.jianghu.quest.QuestSnapshot;
import com.jianghu.business.TradeCaravanSnapshot;
import com.jianghu.npc.NpcFamilySnapshot;
import com.jianghu.social.NpcSocialGraphSnapshot;
import com.jianghu.map.OverworldRegion;
import java.util.Map;
import java.util.Set;

/** 世界核心快照：玩家、日历、天气、NPC 人生状态与玩家关系。 */
public record GameWorldSnapshot(PlayerSnapshot player, long day, int minuteOfDay, String weather,
    Map<String, NpcLifeSnapshot> npcLives, Map<String, NpcRelationshipSnapshot> relationships,
    Map<String, NpcBusinessSnapshot> businesses, Map<MarketRegion, RegionalMarketSnapshot> markets,
    Map<String, QuestSnapshot> quests, Map<String, Long> questRewards, Map<String, TradeCaravanSnapshot> caravans,
    NpcFamilySnapshot family, NpcSocialGraphSnapshot socialGraph,
    OverworldRegion.RegionId overworldRegion, int overworldX, int overworldY,
    long lastAgedDay, long lastBusinessDay, Set<String> deathRecorded,
    Map<String, Long> lastSocialDay, Map<String, String> independentHouseholds,
    Map<String, Integer> courtshipDays, Map<String, Long> masterTrainingDays, Map<String, Integer> masterFavor,
    Set<String> schoolMemberships, Map<String, Long> schoolTrainingDays, Map<String, Integer> schoolFavor,
    Map<String, Integer> schoolContribution, Map<String, Long> resourceHarvestDays) {
    public GameWorldSnapshot {
        if (player == null || day < 1 || minuteOfDay < 0 || minuteOfDay >= 1440 || weather == null || weather.isBlank()
                || lastAgedDay < 1 || lastBusinessDay < 1) throw new IllegalArgumentException("世界存档数据无效");
        npcLives = Map.copyOf(npcLives); relationships = Map.copyOf(relationships);
        businesses = Map.copyOf(businesses); markets = Map.copyOf(markets); quests = Map.copyOf(quests); questRewards = Map.copyOf(questRewards); caravans = Map.copyOf(caravans);
        if (family == null || socialGraph == null || overworldRegion == null
                || !com.jianghu.map.OverworldRegionCatalog.find(overworldRegion).isWalkable(overworldX, overworldY))
            throw new IllegalArgumentException("NPC关系网络或世界地图位置存档无效");
        deathRecorded = Set.copyOf(deathRecorded); lastSocialDay = Map.copyOf(lastSocialDay);
        independentHouseholds = Map.copyOf(independentHouseholds); courtshipDays = Map.copyOf(courtshipDays);
        masterTrainingDays = Map.copyOf(masterTrainingDays); masterFavor = Map.copyOf(masterFavor);
        schoolMemberships = Set.copyOf(schoolMemberships); schoolTrainingDays = Map.copyOf(schoolTrainingDays); schoolFavor = Map.copyOf(schoolFavor); schoolContribution = Map.copyOf(schoolContribution);
        resourceHarvestDays = Map.copyOf(resourceHarvestDays);
        if (resourceHarvestDays.values().stream().anyMatch(dayValue -> dayValue == null || dayValue < 1 || dayValue > day))
            throw new IllegalArgumentException("采集冷却存档无效");
        if (!schoolMemberships.containsAll(schoolTrainingDays.keySet()) || !schoolMemberships.containsAll(schoolFavor.keySet()) || !schoolMemberships.containsAll(schoolContribution.keySet())
                || schoolContribution.values().stream().anyMatch(value -> value == null || value < 0))
            throw new IllegalArgumentException("门派修习记录与弟子名册不一致");
    }
}