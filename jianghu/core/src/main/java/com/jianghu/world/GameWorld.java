package com.jianghu.world;

import com.jianghu.character.Player;
import com.jianghu.map.GameMap;
import com.jianghu.map.MapPoint;
import com.jianghu.npc.Npc;
import com.jianghu.npc.NpcLifeState;
import com.jianghu.npc.NpcFamilyRegistry;
import com.jianghu.time.WorldClock;
import com.jianghu.social.NpcRelationship;
import com.jianghu.social.DialogueService;
import com.jianghu.social.NpcSocialGraph;
import com.jianghu.business.BusinessType;
import com.jianghu.business.NpcBusiness;
import com.jianghu.business.TradeGood;
import com.jianghu.quest.Quest;
import com.jianghu.combat.MartialMaster;
import com.jianghu.combat.MartialArtStyle;
import com.jianghu.business.MarketPriceBook;
import com.jianghu.business.MarketRegion;
import com.jianghu.business.RegionalMarket;
import com.jianghu.business.TradeCaravan;
import java.util.EnumMap;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;

public final class GameWorld {
    public record NpcPosition(String npcId, String name, String role, int x, int y, String schedulePlace) {}
    private final Player player;
    private final com.jianghu.map.OverworldExplorer overworldExplorer = new com.jianghu.map.OverworldExplorer();
    private final WorldClock clock;
    private final List<Npc> npcs;
    private String weather = "晴";
    private final Map<String, NpcRelationship> relationships = new LinkedHashMap<>();
    private final Map<String, NpcLifeState> npcLives = new LinkedHashMap<>();
    private final NpcSocialGraph socialGraph;
    private final NpcFamilyRegistry familyRegistry;
    private final Map<String, Long> lastSocialDay = new LinkedHashMap<>();
    private long lastAgedDay;
    private final java.util.Set<String> deathRecorded = new java.util.LinkedHashSet<>();
    private final Map<String, String> independentHouseholds = new LinkedHashMap<>();
    private final Map<String, Integer> courtshipDays = new LinkedHashMap<>();
    private final Map<String, NpcBusiness> businesses = new LinkedHashMap<>();
    private final MarketPriceBook marketPriceBook = new MarketPriceBook();
    private final Map<MarketRegion, RegionalMarket> regionalMarkets = new EnumMap<>(MarketRegion.class);
    private final Map<String, TradeCaravan> caravans = new LinkedHashMap<>();
    private long lastBusinessDay;
    private com.jianghu.combat.CombatEncounter activeCombat;
    private com.jianghu.combat.RealtimeCombatEncounter activeRealtimeCombat;
    private final Map<String, Long> masterTrainingDays = new LinkedHashMap<>();
    private final Map<String, Integer> masterFavor = new LinkedHashMap<>();
    private final java.util.Set<String> schoolMemberships = new java.util.LinkedHashSet<>();
    private final Map<String, Long> schoolTrainingDays = new LinkedHashMap<>();
    private final Map<String, Integer> schoolFavor = new LinkedHashMap<>();
    private final List<MartialMaster> martialMasters = List.of(new MartialMaster(
        "master-river-sword", "顾长风", "河畔剑师", MartialArtStyle.SWORD,
        "cloud_sword", 120, 180, 0));
    public GameWorld(Player player, WorldClock clock, List<Npc> npcs) {
        this.player = player; this.clock = clock; this.npcs = List.copyOf(npcs);
        this.socialGraph = new NpcSocialGraph(this.npcs.stream().map(Npc::id).collect(Collectors.toSet()));
        this.familyRegistry = new NpcFamilyRegistry(this.npcs.stream().map(Npc::id).collect(Collectors.toSet()));
        this.lastAgedDay = clock.day();
        this.lastBusinessDay = clock.day();
        for (MarketRegion region : MarketRegion.values()) regionalMarkets.put(region, new RegionalMarket(region));
    }
    /** 发起一场山道遭遇战；战斗结束后保留战报供玩家查看。 */
    public com.jianghu.combat.CombatEncounter startBanditEncounter() {
        if ((activeCombat != null && activeCombat.status() == com.jianghu.combat.CombatEncounter.Status.ONGOING)
                || (activeRealtimeCombat != null && activeRealtimeCombat.status() == com.jianghu.combat.RealtimeCombatEncounter.Status.ONGOING))
            throw new IllegalStateException("你正与敌人交手，不能再触发新的战斗");
        activeCombat = new com.jianghu.combat.CombatEncounter(player, new com.jianghu.combat.CombatEnemy(
            "road-bandit", "山道劫匪", "蒙面劫匪挥刀拦路，伺机夺取行旅财物。", 42, 11, 2, 120, 35, "herb", 1));
        return activeCombat;
    }
    public Optional<com.jianghu.combat.CombatEncounter> activeCombat() { return Optional.ofNullable(activeCombat); }
    /** 启动带敌方预警、闪避判定与格挡结算的实时战斗。 */
    public com.jianghu.combat.RealtimeCombatEncounter startRealtimeBanditEncounter() {
        if ((activeCombat != null && activeCombat.status() == com.jianghu.combat.CombatEncounter.Status.ONGOING)
                || (activeRealtimeCombat != null && activeRealtimeCombat.status() == com.jianghu.combat.RealtimeCombatEncounter.Status.ONGOING))
            throw new IllegalStateException("你正与敌人交手，不能再触发新的战斗");
        activeRealtimeCombat = new com.jianghu.combat.RealtimeCombatEncounter(player,
            new com.jianghu.combat.CombatEnemy("road-bandit", "山道劫匪", "蒙面劫匪挥刀拦路，伺机夺取行旅财物。", 58, 12, 2, 150, 45, "herb", 1));
        return activeRealtimeCombat;
    }
    public Optional<com.jianghu.combat.RealtimeCombatEncounter> activeRealtimeCombat() { return Optional.ofNullable(activeRealtimeCombat); }
    public com.jianghu.combat.RealtimeCombatController.ActionResult realtimeCombatAction(com.jianghu.combat.CombatAction action) {
        if (activeRealtimeCombat == null) throw new IllegalStateException("当前没有进行中的实时战斗");
        return activeRealtimeCombat.perform(action);
    }
    public com.jianghu.combat.RealtimeCombatController.ActionResult realtimeCombatTechnique(String techniqueId) {
        if (activeRealtimeCombat == null) throw new IllegalStateException("当前没有进行中的实时战斗");
        return activeRealtimeCombat.performTechnique(techniqueId);
    }
    public void updateRealtimeCombat(float deltaSeconds) {
        if (activeRealtimeCombat != null) activeRealtimeCombat.update(deltaSeconds);
    }
    public String fleeRealtimeCombat() {
        if (activeRealtimeCombat == null) throw new IllegalStateException("当前没有进行中的实时战斗");
        return activeRealtimeCombat.flee();
    }
    /** 当前地图附近的实体门派驻地；只有抵达对应区域才能拜入该门。 */
    public Optional<com.jianghu.map.MartialSchoolSite> nearbyMartialSchoolSite() {
        return com.jianghu.map.MartialSchoolSite.nearby(overworldExplorer.regionId(), overworldExplorer.x(), overworldExplorer.y());
    }
    public List<com.jianghu.combat.MartialSchool> martialSchools() { return com.jianghu.combat.MartialSchool.all(); }
    public java.util.Set<String> schoolMemberships() { return java.util.Set.copyOf(schoolMemberships); }
    public int martialSchoolFavor(String schoolId) { return schoolFavor.getOrDefault(schoolId, 0); }
    public String inspectNearbyMartialSchool() {
        var site = nearbyMartialSchoolSite().orElseThrow(() -> new IllegalStateException("此处没有门派驻地；请前往武林群山、北岭或西蜀寻找山门"));
        var school = site.school();
        return site.landmark() + " · " + school.name() + "。" + school.philosophy() + "。入门需声望 "
            + school.entryReputation() + "、束脩 " + school.entryFeeInWen() + " 文。按 M 拜入门墙，按 L 随门中弟子修习。"
            + (schoolMemberships.contains(school.id()) ? " 你已是本门弟子，门中信任 " + martialSchoolFavor(school.id()) + "/3。" : "");
    }
    public String joinNearbyMartialSchool() {
        var site = nearbyMartialSchoolSite().orElseThrow(() -> new IllegalStateException("附近没有门派山门"));
        var school = site.school();
        if (schoolMemberships.contains(school.id())) return "你已拜入" + school.name() + "，不必重复行礼。";
        if (player.reputation() < school.entryReputation()) throw new IllegalStateException("江湖声望不足，" + school.name() + "暂不收徒；需要声望 " + school.entryReputation());
        if (player.moneyInWen() < school.entryFeeInWen()) throw new IllegalStateException("束脩不足，入门需 " + school.entryFeeInWen() + " 文");
        player.addMoney(-school.entryFeeInWen()); player.learnStyle(school.style()); player.selectStyle(school.style());
        schoolMemberships.add(school.id()); schoolFavor.putIfAbsent(school.id(), 0);
        return "你在" + site.landmark() + "向" + school.name() + "执弟子礼，正式入门。习得" + school.style().displayName() + "，门规：" + school.philosophy();
    }
    /** 每个门派每日可修习一次；三次修习后师门传授该门派独门招式。 */
    public String trainAtNearbyMartialSchool() {
        var site = nearbyMartialSchoolSite().orElseThrow(() -> new IllegalStateException("附近没有门派驻地"));
        var school = site.school();
        if (!schoolMemberships.contains(school.id())) throw new IllegalStateException("尚未拜入" + school.name() + "，不可在此修习");
        if (schoolTrainingDays.getOrDefault(school.id(), -1L) == clock.day()) throw new IllegalStateException("今日已在本门修习过，明日再来");
        if (!player.spendStamina(12)) throw new IllegalStateException("体力不足，无法完成门中修习");
        schoolTrainingDays.put(school.id(), clock.day()); int favor = schoolFavor.merge(school.id(), 1, Integer::sum);
        int levels = player.gainMartialExperience(18); advanceMinutes(60);
        String result = "你在" + school.name() + "依门规修习一个时辰，武学经验 +18，师门信任 " + favor + "/3。";
        if (levels > 0) result += "武学境界提升至第" + player.martialLevel() + "重。";
        if (favor >= 3 && !player.knowsTechnique(school.signatureTechnique())) {
            player.learnTechnique(school.signatureTechnique());
            result += "师长认可你的勤勉，传授独门招式「" + com.jianghu.combat.MartialTechnique.find(school.signatureTechnique()).map(com.jianghu.combat.MartialTechnique::displayName).orElse(school.signatureTechnique()) + "」。";
        }
        return result;
    }
    public List<MartialMaster> martialMasters() { return martialMasters; }
    /** 返回当前可接触的师傅；师傅固定驻守河畔演武场。 */
    public Optional<MartialMaster> nearbyMartialMaster(GameMap map) {
        if (map == null) return Optional.empty();
        return map.points().stream().filter(p -> p.id().equals("training-yard"))
            .filter(p -> p.canInteractFrom(player.x(), player.y()))
            .findFirst().flatMap(p -> martialMasters.stream().findFirst());
    }
    public String talkToMartialMaster(GameMap map) {
        MartialMaster master = nearbyMartialMaster(map)
            .orElseThrow(() -> new IllegalStateException("请先前往河畔演武场寻找武学师傅"));
        return master.name() + "（" + master.title() + "）说道：剑法重在心静意定。拜师需 "
            + master.styleFeeInWen() + " 文，进阶招式需 " + master.techniqueFeeInWen()
            + " 文。当前声望门槛：" + master.requiredReputation() + "。按 M 拜师，按 N 学招。";
    }
    public String learnFromMartialMaster(GameMap map) {
        MartialMaster master = nearbyMartialMaster(map)
            .orElseThrow(() -> new IllegalStateException("附近没有武学师傅"));
        if (player.reputation() < master.requiredReputation())
            throw new IllegalStateException("声望不足，师傅暂不收徒");
        String result = learnMartialStyle(master.style(), master.styleFeeInWen());
        player.selectStyle(master.style());
        return result + " 当前已切换为" + master.style().displayName() + "。";
    }
    public int martialMasterFavor(String masterId) { return masterFavor.getOrDefault(masterId, 0); }
    /** 每位师傅每天只安排一次正式训练；训练消耗体力与一小时，积累武学经验及师门信任。 */
    public String trainWithMartialMaster(GameMap map) {
        MartialMaster master = nearbyMartialMaster(map)
            .orElseThrow(() -> new IllegalStateException("请先前往河畔演武场找师傅练功"));
        if (!player.learnedStyles().contains(master.style()))
            throw new IllegalStateException("先拜师习得" + master.style().displayName() + "，师傅才肯指点");
        if (masterTrainingDays.getOrDefault(master.id(), -1L) == clock.day())
            throw new IllegalStateException("今日已经练过功了，明日再来接受指点");
        if (!player.spendStamina(12)) throw new IllegalStateException("体力不足，无法完成一轮练功");
        masterTrainingDays.put(master.id(), clock.day());
        int favor = masterFavor.merge(master.id(), 1, Integer::sum);
        int levels = player.gainMartialExperience(20);
        advanceMinutes(60);
        return "你随" + master.name() + "练剑一个时辰，武学经验 +20，师门信任 " + favor
            + "/3。" + (levels > 0 ? "境界提升至第" + player.martialLevel() + "重！" : "");
    }
    /** 与师傅建立三日练功默契后，可付费学习进阶剑招。 */
    public String learnAdvancedTechniqueFromMartialMaster(GameMap map) {
        MartialMaster master = nearbyMartialMaster(map)
            .orElseThrow(() -> new IllegalStateException("附近没有武学师傅"));
        if (!player.learnedStyles().contains(master.style()) || !player.knowsTechnique(master.techniqueId()))
            throw new IllegalStateException("先拜师并学会入门剑式");
        if (martialMasterFavor(master.id()) < 3)
            throw new IllegalStateException("师傅认为火候未到；再完成 " + (3 - martialMasterFavor(master.id())) + " 次练功");
        if (player.knowsTechnique("cloud_piercing")) return "你已经学会穿云破月。";
        if (player.moneyInWen() < 360) throw new IllegalStateException("进阶剑招学费需要 360 文");
        player.addMoney(-360);
        player.learnTechnique("cloud_piercing");
        return "顾长风传你进阶剑招「穿云破月」：凝全身劲力于剑尖，一线破空。";
    }
    public String learnTechniqueFromMartialMaster(GameMap map) {
        MartialMaster master = nearbyMartialMaster(map)
            .orElseThrow(() -> new IllegalStateException("附近没有武学师傅"));
        if (!player.learnedStyles().contains(master.style()))
            throw new IllegalStateException("先向师傅拜师，习得" + master.style().displayName());
        return learnMartialTechnique(master.techniqueId(), master.techniqueFeeInWen());
    }
    public String combatAttack() {
        if (activeCombat == null) throw new IllegalStateException("当前没有进行中的战斗");
        return activeCombat.playerAttack();
    }
    public String fleeCombat() {
        if (activeCombat == null) throw new IllegalStateException("当前没有进行中的战斗");
        return activeCombat.flee();
    }
    /** 付费向江湖师友求教一门武学流派；已学会的流派不会重复收费。 */
    public String learnMartialStyle(com.jianghu.combat.MartialArtStyle style, long feeInWen) {
        if (style == null || feeInWen < 0) throw new IllegalArgumentException("武学或学费无效");
        if (player.learnedStyles().contains(style)) return "你已经掌握" + style.displayName() + "。";
        if (player.moneyInWen() < feeInWen) throw new IllegalStateException("银钱不足，无法拜师学艺");
        player.addMoney(-feeInWen); player.learnStyle(style);
        return "你付出" + feeInWen + "文，习得" + style.displayName() + "：" + style.description();
    }
    /** 学习招式前必须先掌握其所属流派；招式一经习得永久保留。 */
    public String learnMartialTechnique(String techniqueId, long feeInWen) {
        var technique = com.jianghu.combat.MartialTechnique.find(techniqueId)
            .orElseThrow(() -> new IllegalArgumentException("未知招式：" + techniqueId));
        if (player.knowsTechnique(techniqueId)) return "你已经学会" + technique.displayName() + "。";
        if (!player.learnedStyles().contains(technique.style())) throw new IllegalStateException("先习得" + technique.style().displayName() + "再来求教此招");
        if (feeInWen < 0) throw new IllegalArgumentException("学费不能为负");
        if (player.moneyInWen() < feeInWen) throw new IllegalStateException("银钱不足，无法学习招式");
        player.addMoney(-feeInWen); player.learnTechnique(techniqueId);
        return "你付出" + feeInWen + "文，学会" + technique.displayName() + "：" + technique.description();
    }
    /** 静坐吐纳半个时辰，恢复内力与少量体力；战斗中不可运功。 */
    public String meditate() {
        if (activeCombat != null && activeCombat.status() == com.jianghu.combat.CombatEncounter.Status.ONGOING)
            throw new IllegalStateException("交手之际无法静坐运功");
        player.recoverInnerEnergy(25); player.recoverStamina(5); advanceMinutes(30);
        return "你静坐吐纳半个时辰，内力恢复至" + player.innerEnergy() + "。";
    }
    /** 捕获可恢复的核心世界状态；NPC 身份与初始日程由游戏场景定义。 */
    public GameWorldSnapshot snapshot() {
        Map<String, com.jianghu.npc.NpcLifeSnapshot> lives = new LinkedHashMap<>();
        Map<String, com.jianghu.social.NpcRelationshipSnapshot> bonds = new LinkedHashMap<>();
        for (Npc npc : npcs) {
            lives.put(npc.id(), com.jianghu.npc.NpcLifeSnapshot.capture(life(npc.id())));
            bonds.put(npc.id(), com.jianghu.social.NpcRelationshipSnapshot.capture(relationship(npc.id())));
        }
        Map<String, com.jianghu.business.NpcBusinessSnapshot> businessSnapshots = new LinkedHashMap<>();
        for (NpcBusiness business : businesses.values()) businessSnapshots.put(business.id(), com.jianghu.business.NpcBusinessSnapshot.capture(business));
        Map<MarketRegion, com.jianghu.business.RegionalMarketSnapshot> marketSnapshots = new EnumMap<>(MarketRegion.class);
        for (var entry : regionalMarkets.entrySet()) marketSnapshots.put(entry.getKey(), com.jianghu.business.RegionalMarketSnapshot.capture(entry.getValue()));
        Map<String, com.jianghu.quest.QuestSnapshot> questSnapshots = new LinkedHashMap<>();
        for (var entry : playerQuests.entrySet()) questSnapshots.put(entry.getKey(), com.jianghu.quest.QuestSnapshot.capture(entry.getValue()));
        Map<String, com.jianghu.business.TradeCaravanSnapshot> caravanSnapshots = new LinkedHashMap<>();
        for (var entry : caravans.entrySet()) caravanSnapshots.put(entry.getKey(), entry.getValue().snapshot());
        return new GameWorldSnapshot(com.jianghu.character.PlayerSnapshot.capture(player), clock.day(), clock.minuteOfDay(),
            weather, lives, bonds, businessSnapshots, marketSnapshots, questSnapshots, questRewards, caravanSnapshots,
            familyRegistry.snapshot(), socialGraph.snapshot(), overworldExplorer.regionId(), overworldExplorer.x(), overworldExplorer.y(), lastAgedDay, lastBusinessDay, deathRecorded, lastSocialDay,
            independentHouseholds, courtshipDays, masterTrainingDays, masterFavor, schoolMemberships, schoolTrainingDays, schoolFavor);
    }
    /** 恢复核心世界状态；快照中的 NPC 身份必须与当前场景完全一致。 */
    public void restoreSnapshot(GameWorldSnapshot snapshot) {
        if (snapshot == null) throw new IllegalArgumentException("世界快照不能为空");
        java.util.Set<String> expectedIds = npcs.stream().map(Npc::id).collect(java.util.stream.Collectors.toSet());
        if (!snapshot.npcLives().keySet().equals(expectedIds) || !snapshot.relationships().keySet().equals(expectedIds))
            throw new IllegalArgumentException("存档 NPC 名册与当前场景不一致");
        player.restoreSnapshot(snapshot.player());
        overworldExplorer.restorePosition(snapshot.overworldRegion(), snapshot.overworldX(), snapshot.overworldY());
        clock.restore(snapshot.day(), snapshot.minuteOfDay());
        weather = snapshot.weather();
        for (Npc npc : npcs) {
            life(npc.id()).restoreSnapshot(snapshot.npcLives().get(npc.id()));
            relationship(npc.id()).restoreSnapshot(snapshot.relationships().get(npc.id()));
        }
        familyRegistry.restoreSnapshot(snapshot.family());
        socialGraph.restoreSnapshot(snapshot.socialGraph());
        if (!snapshot.markets().keySet().equals(java.util.EnumSet.allOf(MarketRegion.class)))
            throw new IllegalArgumentException("存档区域市场名册不完整");
        for (var entry : snapshot.markets().entrySet()) regionalMarkets.get(entry.getKey()).restoreSnapshot(entry.getValue());
        if (!snapshot.quests().keySet().equals(playerQuests.keySet()))
            throw new IllegalArgumentException("存档任务列表与当前场景不一致");
        for (var entry : snapshot.quests().entrySet()) playerQuests.get(entry.getKey()).restoreSnapshot(entry.getValue());
        questRewards.clear(); questRewards.putAll(snapshot.questRewards());
        caravans.clear();
        for (var entry : snapshot.caravans().entrySet()) {
            var c = entry.getValue();
            if (!expectedIds.containsAll(c.crew())) throw new IllegalArgumentException("商队成员与当前 NPC 名册不匹配");
            TradeCaravan restored = new TradeCaravan(c.id(), c.ownerNpcId(), c.origin(), c.destination());
            restored.restoreSnapshot(c); caravans.put(entry.getKey(), restored);
        }
        businesses.clear();
        for (var entry : snapshot.businesses().entrySet()) {
            var b = entry.getValue();
            NpcBusiness restored = new NpcBusiness(b.id(), b.name(), b.type(), b.ownerNpcId(), b.dailyIncomeInWen(),
                b.dailyOperatingCostInWen(), b.dailyWagePerEmployeeInWen(), b.treasuryInWen());
            restored.restoreSnapshot(b); businesses.put(entry.getKey(), restored);
        }
        lastAgedDay = snapshot.lastAgedDay(); lastBusinessDay = snapshot.lastBusinessDay();
        deathRecorded.clear(); deathRecorded.addAll(snapshot.deathRecorded());
        lastSocialDay.clear(); lastSocialDay.putAll(snapshot.lastSocialDay());
        independentHouseholds.clear(); independentHouseholds.putAll(snapshot.independentHouseholds());
        courtshipDays.clear(); courtshipDays.putAll(snapshot.courtshipDays());
        masterTrainingDays.clear(); masterTrainingDays.putAll(snapshot.masterTrainingDays());
        masterFavor.clear(); masterFavor.putAll(snapshot.masterFavor());
        schoolMemberships.clear(); schoolMemberships.addAll(snapshot.schoolMemberships());
        schoolTrainingDays.clear(); schoolTrainingDays.putAll(snapshot.schoolTrainingDays());
        schoolFavor.clear(); schoolFavor.putAll(snapshot.schoolFavor());
        activeCombat = null; activeRealtimeCombat = null;
    }
    public Player player() { return player; }
    public com.jianghu.map.OverworldExplorer overworld() { return overworldExplorer; }
    public WorldClock clock() { return clock; }
    public List<Npc> npcs() { return npcs; }
    public NpcSocialGraph socialGraph() { return socialGraph; }
    public NpcFamilyRegistry familyRegistry() { return familyRegistry; }

    /** 登记婚姻，并同步社会关系、双方心情与人生记忆。 */
    public void marryNpcs(String firstNpcId, String secondNpcId) {
        Npc first = requireNpc(firstNpcId);
        Npc second = requireNpc(secondNpcId);
        familyRegistry.marry(firstNpcId, secondNpcId);
        String sharedHome = independentHouseholds.get(firstNpcId);
        if (sharedHome == null) sharedHome = independentHouseholds.get(secondNpcId);
        if (sharedHome != null) {
            independentHouseholds.put(firstNpcId, sharedHome);
            independentHouseholds.put(secondNpcId, sharedHome);
        }
        familyRegistry.recordEvent(NpcFamilyRegistry.EventType.MARRIAGE, List.of(firstNpcId, secondNpcId), clock.day(), first.name() + " 与 " + second.name() + " 结为夫妻");
        socialGraph.connect(firstNpcId, secondNpcId, NpcSocialGraph.RelationType.FAMILY, 80);
        socialGraph.connect(secondNpcId, firstNpcId, NpcSocialGraph.RelationType.FAMILY, 80);
        life(firstNpcId).changeMood(8);
        life(secondNpcId).changeMood(8);
        life(firstNpcId).remember("与" + second.name() + "结为夫妻");
        life(secondNpcId).remember("与" + first.name() + "结为夫妻");
    }

    /** 登记亲子关系；若父母已婚，自动将另一方也登记为孩子的父母。 */
    public void addNpcChild(String parentNpcId, String childNpcId) {
        Npc parent = requireNpc(parentNpcId);
        Npc child = requireNpc(childNpcId);
        if (familyRegistry.parentsOf(childNpcId).contains(parentNpcId)) return;
        if (!life(childNpcId).ageInitialized()) life(childNpcId).initializeAge(0);
        List<String> parentIds = new java.util.ArrayList<>();
        parentIds.add(parentNpcId);
        familyRegistry.spouseOf(parentNpcId).ifPresent(parentIds::add);
        for (String id : parentIds) familyRegistry.addChild(id, childNpcId);
        String familyHome = parentIds.stream().map(independentHouseholds::get).filter(java.util.Objects::nonNull).findFirst().orElse(null);
        if (familyHome != null) independentHouseholds.put(childNpcId, familyHome);
        familyRegistry.recordEvent(NpcFamilyRegistry.EventType.BIRTH, java.util.stream.Stream.concat(parentIds.stream(), java.util.stream.Stream.of(childNpcId)).toList(), clock.day(), "家中添了孩子" + child.name());
        for (String id : parentIds) {
            Npc familyParent = requireNpc(id);
            socialGraph.connect(id, childNpcId, NpcSocialGraph.RelationType.FAMILY, 85);
            socialGraph.connect(childNpcId, id, NpcSocialGraph.RelationType.FAMILY, 85);
            life(id).changeMood(10);
            life(id).remember("家中添了孩子" + child.name());
            life(childNpcId).remember(familyParent.name() + "成为自己的父母");
        }
    }
    public String weather() { return weather; }
    public void setWeather(String weather) {
        if (weather == null || weather.isBlank()) throw new IllegalArgumentException("天气不能为空");
        this.weather = weather;
    }
    public void waitMinutes(int minutes) {
        advanceMinutes(minutes);
        player.recoverStamina(Math.max(0, minutes / 10));
    }
    /** 推进世界时间，并在每个整点结算所有 NPC 的生活需求与工作收入。 */
    public void advanceMinutes(int minutes) {
        if (minutes < 0) throw new IllegalArgumentException("不能倒退时间");
        for (int i = 0; i < minutes; i++) {
            long previousDay = clock.day();
            clock.advance(1);
            for (TradeCaravan caravan : caravans.values()) {
                caravan.advanceMinute(weather);
                if (caravan.state() == TradeCaravan.State.ARRIVED) settleArrivedCaravan(caravan);
            }
            if (clock.day() != previousDay) { settleNpcAging(); settleNpcRomance(); settleNpcBusinesses(); }
            if (clock.minuteOfDay() % 60 == 0) {
                for (Npc npc : npcs) {
                    if (life(npc.id()).alive()) life(npc.id()).passHour(life(npc.id()).profession().orElse(npc.role()), locationFor(npc, clock.hour()), clock.hour());
                }
                settleNpcSocialLife();
            }
        }
    }

    /** 每跨过一个游戏日，NPC 增长一天；每 360 天增长一岁并记录成长节点。 */
    private void settleNpcAging() {
        while (lastAgedDay < clock.day()) {
            lastAgedDay++;
            for (Npc npc : npcs) {
                NpcLifeState state = life(npc.id());
                if (!state.alive()) continue;
                if (state.passDay() && state.alive()) {
                    String milestone = npc.name() + " 年满 " + state.ageYears() + " 岁";
                    state.remember(milestone);
                    if (state.ageYears() == 12 || state.ageYears() == 18 || state.ageYears() == 60) {
                        state.remember(switch (state.ageYears()) {
                            case 12 -> npc.name() + "进入少年时期";
                            case 18 -> npc.name() + "成年了";
                            default -> npc.name() + "步入晚年";
                        });
                    }
                }
                if (!state.alive() && !deathRecorded.contains(npc.id())) {
                    recordNpcDeath(npc.id(), "疾病恶化，未能救治");
                }
            }
        }
    }




    /** 监护人教导 6 至 17 岁子女；职业经验与家境会提升教学效果。 */
    public int teachNpcChild(String guardianNpcId, String childNpcId, String subject, int effort) {
        Npc guardian = requireNpc(guardianNpcId);
        Npc child = requireNpc(childNpcId);
        requireDescription(subject);
        if (effort < 1 || effort > 10) throw new IllegalArgumentException("教学投入必须在 1..10 之间");
        if (!familyRegistry.parentsOf(childNpcId).contains(guardianNpcId))
            throw new IllegalArgumentException("只有登记的父母才能教导孩子");
        NpcLifeState guardianLife = life(guardianNpcId);
        NpcLifeState childLife = life(childNpcId);
        if (!guardianLife.alive()) throw new IllegalStateException("已故监护人无法教学");
        if (!childLife.alive()) throw new IllegalStateException("已故孩子无法学习");
        if (childLife.ageYears() < 6 || childLife.ageYears() >= 18)
            throw new IllegalStateException("只有 6 至 17 岁的孩子适合接受家庭教学");
        int bonus = 0;
        if (guardian.role().contains("先生") || guardian.role().contains("教书")
                || guardian.role().contains("学者")) bonus += 3;
        if (guardianLife.moneyInWen() >= 2000) bonus += 2;
        int gained = effort + bonus;
        childLife.study(gained);
        childLife.remember(guardian.name() + "教导自己" + subject + "，学识增长 " + gained);
        guardianLife.remember("教导孩子" + child.name() + subject);
        guardianLife.changeMood(1);
        return gained;
    }


    /** 已正式择业的成年师傅可指导登记为徒弟的 NPC，积累对应职业技艺。 */
    public int trainNpcApprentice(String mentorNpcId, String apprenticeNpcId, int effort) {
        Npc mentor = requireNpc(mentorNpcId);
        Npc apprentice = requireNpc(apprenticeNpcId);
        NpcLifeState mentorLife = life(mentorNpcId);
        NpcLifeState apprenticeLife = life(apprenticeNpcId);
        if (!mentorLife.alive() || mentorLife.ageYears() < 18 || mentorLife.profession().isEmpty())
            throw new IllegalStateException("师傅必须是有职业的成年在世 NPC");
        if (!apprenticeLife.alive() || apprenticeLife.ageYears() < 12)
            throw new IllegalStateException("徒弟必须年满 12 岁且在世");
        if (apprenticeLife.profession().isPresent())
            throw new IllegalStateException("已有职业者不能作为初学徒弟");
        if (effort < 1 || effort > 10) throw new IllegalArgumentException("训练投入必须在 1..10 之间");
        var bond = socialGraph.relation(mentorNpcId, apprenticeNpcId);
        if (bond.isEmpty() || bond.get().type() != NpcSocialGraph.RelationType.MENTOR)
            throw new IllegalArgumentException("必须先建立师徒关系");
        String craft = mentorLife.profession().orElseThrow();
        int gained = effort + 3;
        apprenticeLife.trainProfession(craft, gained);
        apprenticeLife.remember("跟随" + mentor.name() + "学习" + craft + "技艺，成长 " + gained);
        mentorLife.remember("传授" + apprentice.name() + craft + "技艺");
        mentorLife.changeMood(1);
        return gained;
    }

    /** 成年 NPC 可按学识或师承技艺选择职业；每人只能选择一次。 */
    public void chooseNpcProfession(String npcId, String profession) {
        Npc npc = requireNpc(npcId);
        requireDescription(profession);
        NpcLifeState state = life(npcId);
        if (!state.alive()) throw new IllegalStateException("已故 NPC 无法选择职业");
        if (state.ageYears() < 18) throw new IllegalStateException("未成年 NPC 尚不能独立择业");
        int requiredEducation = switch (profession) {
            case "农夫", "渔夫", "茶摊老板", "镖师" -> 0;
            case "药师", "书生" -> 30;
            case "教书先生" -> 50;
            default -> throw new IllegalArgumentException("暂不支持该职业");
        };
        int training = state.vocationalTraining(profession);
        if (state.education() + training < requiredEducation)
            throw new IllegalStateException("学识或师承技艺不足，无法胜任该职业");
        state.chooseProfession(profession);
        state.remember("成年后选择职业：" + profession);
    }

    /** 每个游戏日结算自主婚恋：成年单身 NPC 的互相信任关系在共同生活中逐渐发展。 */
    private void settleNpcRomance() {
        for (int i = 0; i < npcs.size(); i++) {
            Npc first = npcs.get(i);
            NpcLifeState firstLife = life(first.id());
            if (!firstLife.alive() || firstLife.ageYears() < 18
                    || familyRegistry.spouseOf(first.id()).isPresent()) continue;
            for (int j = i + 1; j < npcs.size(); j++) {
                Npc second = npcs.get(j);
                NpcLifeState secondLife = life(second.id());
                if (!secondLife.alive() || secondLife.ageYears() < 18
                        || familyRegistry.spouseOf(second.id()).isPresent()) continue;
                if (familyRegistry.familyOf(first.id()).contains(second.id())
                        || familyRegistry.familyOf(second.id()).contains(first.id())) continue;
                String key = first.id() + "|" + second.id();
                var forward = socialGraph.relation(first.id(), second.id());
                var reverse = socialGraph.relation(second.id(), first.id());
                boolean mutualTrust = forward.isPresent() && reverse.isPresent()
                    && forward.get().type() == NpcSocialGraph.RelationType.FRIEND
                    && reverse.get().type() == NpcSocialGraph.RelationType.FRIEND
                    && forward.get().strength() >= 60 && reverse.get().strength() >= 60;
                boolean together = locationFor(first, 12).equals(locationFor(second, 12));
                if (!mutualTrust || !together) {
                    courtshipDays.remove(key);
                    continue;
                }
                int days = courtshipDays.merge(key, 1, Integer::sum);
                firstLife.remember("与" + second.name() + "相处融洽（第 " + days + " 日）");
                secondLife.remember("与" + first.name() + "相处融洽（第 " + days + " 日）");
                if (days >= 30) {
                    marryNpcs(first.id(), second.id());
                    courtshipDays.remove(key);
                }
            }
        }
    }

    public int courtshipDays(String firstNpcId, String secondNpcId) {
        requireNpc(firstNpcId);
        requireNpc(secondNpcId);
        String key = npcs.indexOf(requireNpc(firstNpcId)) < npcs.indexOf(requireNpc(secondNpcId))
            ? firstNpcId + "|" + secondNpcId : secondNpcId + "|" + firstNpcId;
        return courtshipDays.getOrDefault(key, 0);
    }

    /** 成年 NPC 可建立独立居所；仅在原日程回家时改为返回新居。 */
    public void establishIndependentHousehold(String npcId, String householdPlace) {
        Npc npc = requireNpc(npcId);
        if (householdPlace == null || householdPlace.isBlank())
            throw new IllegalArgumentException("独立居所不能为空");
        NpcLifeState state = life(npcId);
        if (!state.alive()) throw new IllegalStateException("已故 NPC 无法建立居所");
        if (state.ageYears() < 18) throw new IllegalStateException("未成年 NPC 不能独立建立居所");
        if (independentHouseholds.containsKey(npcId)) throw new IllegalStateException("NPC 已有独立居所");
        independentHouseholds.put(npcId, householdPlace);
        state.remember("成年后建立独立居所：" + householdPlace);
        state.changeMood(5);
    }

    /** 查询 NPC 当前所属的家庭住所；未建立独立住所时返回空。 */
    public Optional<String> familyHouseholdOf(String npcId) {
        requireNpc(npcId);
        return Optional.ofNullable(independentHouseholds.get(npcId));
    }

    public Optional<String> independentHouseholdOf(String npcId) {
        requireNpc(npcId);
        return Optional.ofNullable(independentHouseholds.get(npcId));
    }

    public String npcLocationAt(String npcId, int hour) {
        if (hour < 0 || hour > 23) throw new IllegalArgumentException("时辰无效");
        return locationFor(requireNpc(npcId), hour);
    }

    private String locationFor(Npc npc, int hour) {
        String scheduled = npc.locationAt(hour);
        if ("home".equals(scheduled)) return independentHouseholds.getOrDefault(npc.id(), scheduled);
        return scheduled;
    }

    public NpcLifeState life(String npcId) {
        if (npcs.stream().noneMatch(n -> n.id().equals(npcId))) throw new IllegalArgumentException("未知 NPC：" + npcId);
        return npcLives.computeIfAbsent(npcId, ignored -> new NpcLifeState());
    }
    /** 根据当前时辰把 NPC 的日程地点解析为地图坐标。未配置地点的 NPC 暂不显示。 */
    public List<NpcPosition> npcPositions(GameMap map) {
        return npcs.stream().filter(npc -> life(npc.id()).alive()).map(npc -> {
            String place = locationFor(npc, clock.hour());
            Optional<MapPoint> point = map.points().stream().filter(p -> p.id().equals(place)).findFirst();
            return point.map(p -> new NpcPosition(npc.id(), npc.name(), npc.role(), p.x(), p.y(), place)).orElse(null);
        }).filter(java.util.Objects::nonNull).toList();
    }
    public NpcRelationship relationship(String npcId) {
        if (npcs.stream().noneMatch(n -> n.id().equals(npcId))) throw new IllegalArgumentException("未知 NPC：" + npcId);
        return relationships.computeIfAbsent(npcId, ignored -> new NpcRelationship());
    }
    /** 送礼：礼物价值以文计，产生有限好感；贵重礼物也会略增信任。 */
    public void giveGiftToNpc(String npcId, String itemId, int valueInWen) {
        Npc npc = requireNpc(npcId);
        if (itemId == null || itemId.isBlank() || valueInWen <= 0)
            throw new IllegalArgumentException("礼物与正价值不能为空");
        int favorGain = Math.max(1, Math.min(15, valueInWen / 10));
        NpcRelationship bond = relationship(npcId);
        bond.changeFavor(favorGain);
        if (valueInWen >= 100) bond.changeTrust(2);
        life(npcId).changeMood(3);
        life(npcId).remember("玩家赠予" + itemId + "（价值 " + valueInWen + " 文）");
    }

    /** 帮助 NPC：同时改善好感、信任与心情，并留下可追溯记忆。 */
    public void helpNpc(String npcId, String description) {
        Npc npc = requireNpc(npcId);
        requireDescription(description);
        relationship(npcId).changeFavor(8);
        relationship(npcId).changeTrust(6);
        life(npcId).changeMood(5);
        life(npcId).remember("玩家帮助了自己：" + description);
    }

    /** 欺骗 NPC 会显著损害信任；即使玩家暂时获利，关系也会留下后果。 */
    public void deceiveNpc(String npcId, String description) {
        requireNpc(npcId);
        requireDescription(description);
        relationship(npcId).changeFavor(-12);
        relationship(npcId).changeTrust(-25);
        life(npcId).changeMood(-8);
        life(npcId).remember("玩家曾欺骗自己：" + description);
    }




    /** NPC 患病；疾病会在每日结算时持续损害健康。 */
    public void giveNpcIllness(String npcId, String illness, int severity) {
        Npc npc = requireNpc(npcId);
        life(npcId).contractIllness(illness, severity);
        life(npcId).remember("染上疾病：" + illness + "（病情 " + severity + "）");
    }

    /** 为患病 NPC 治疗；治愈后停止每日疾病伤害。 */
    public void treatNpc(String npcId, String treatment, int severityReduction, int healthRestored) {
        Npc npc = requireNpc(npcId);
        requireDescription(treatment);
        life(npcId).treatIllness(severityReduction, healthRestored);
        life(npcId).remember("接受治疗：" + treatment);
        life(npcId).changeMood(3);
    }

    /** 记录 NPC 去世；死亡不会自动抹去其家庭谱系或社会关系历史。 */
    public void recordNpcDeath(String npcId, String cause) {
        Npc npc = requireNpc(npcId);
        requireDescription(cause);
        NpcLifeState state = life(npcId);
        if (deathRecorded.contains(npcId)) throw new IllegalStateException("NPC 去世事件已登记");
        if (state.alive()) state.die();
        if (state.health() != 0) throw new IllegalStateException("NPC 健康状态异常");
        deathRecorded.add(npcId);
        state.remember("去世：" + cause);
        familyRegistry.recordEvent(NpcFamilyRegistry.EventType.DEATH, List.of(npcId), clock.day(), npc.name() + " 去世：" + cause);
    }


    /** NPC 遭受伤害；健康降至零时自动登记死亡事件。 */
    public void injureNpc(String npcId, int damage, String cause) {
        requireNpc(npcId);
        if (damage <= 0) throw new IllegalArgumentException("伤害必须为正数");
        requireDescription(cause);
        NpcLifeState state = life(npcId);
        state.changeHealth(-damage);
        if (!state.alive()) recordNpcDeath(npcId, cause);
    }

    /** 家族成员继承逝者全部遗产；转移与事件登记作为一次操作完成。 */
    public long inheritNpcEstate(String deceasedNpcId, String heirNpcId) {
        Npc deceased = requireNpc(deceasedNpcId);
        Npc heir = requireNpc(heirNpcId);
        if (life(deceasedNpcId).alive()) throw new IllegalStateException("遗产所有者尚在人世");
        if (!familyRegistry.familyOf(deceasedNpcId).contains(heirNpcId))
            throw new IllegalArgumentException("继承人必须属于逝者的直系或登记家庭");
        if (!life(heirNpcId).alive()) throw new IllegalStateException("继承人已故");
        long amount = life(deceasedNpcId).transferWealthTo(life(heirNpcId));
        familyRegistry.recordEvent(NpcFamilyRegistry.EventType.INHERITANCE,
            List.of(deceasedNpcId, heirNpcId), clock.day(),
            heir.name() + " 继承 " + deceased.name() + " 的遗产 " + amount + " 文");
        life(heirNpcId).remember("继承" + deceased.name() + "的遗产（" + amount + "文）");
        return amount;
    }

    /** 创办家族产业；简化入口不设置额外成本与工资。 */
    public NpcBusiness foundNpcBusiness(String ownerNpcId, String businessId, String name,
                                        BusinessType type, long dailyIncomeInWen, long initialTreasuryInWen) {
        return foundNpcBusiness(ownerNpcId, businessId, name, type, dailyIncomeInWen, 0, 0, initialTreasuryInWen);
    }
    /** 创办产业并设置每日运营成本与每名雇员工资。 */
    public NpcBusiness foundNpcBusiness(String ownerNpcId, String businessId, String name, BusinessType type,
                                        long dailyIncomeInWen, long dailyOperatingCostInWen,
                                        long dailyWagePerEmployeeInWen, long initialTreasuryInWen) {
        requireNpc(ownerNpcId);
        NpcLifeState owner = life(ownerNpcId);
        if (!owner.alive() || owner.ageYears() < 18) throw new IllegalStateException("产业主人必须成年且在世");
        if (businesses.containsKey(businessId)) throw new IllegalArgumentException("产业编号已存在");
        NpcBusiness business = new NpcBusiness(businessId, name, type, ownerNpcId, dailyIncomeInWen,
            dailyOperatingCostInWen, dailyWagePerEmployeeInWen, initialTreasuryInWen);
        businesses.put(businessId, business);
        owner.remember("创办" + name + "，成为" + type.displayName() + "主人");
        return business;
    }

    /** 查询江南默认市价；兼容起始区域的交易入口。 */
    public long marketPrice(TradeGood good) { return marketPrice(good, MarketRegion.JIANGNAN); }
    /** 区域报价同时考虑日期、天气、当地库存与需求。 */
    public long marketPrice(TradeGood good, MarketRegion region) { return requireMarket(region).quote(good, clock.day(), weather); }
    public Map<TradeGood, Long> marketPrices() { return marketPrices(MarketRegion.JIANGNAN); }
    public Map<TradeGood, Long> marketPrices(MarketRegion region) {
        Map<TradeGood, Long> prices = new LinkedHashMap<>();
        for (TradeGood good : TradeGood.values()) prices.put(good, marketPrice(good, region));
        return Map.copyOf(prices);
    }
    public RegionalMarket regionalMarket(MarketRegion region) { return requireMarket(region); }
    private RegionalMarket requireMarket(MarketRegion region) {
        RegionalMarket market = regionalMarkets.get(region);
        if (market == null) throw new IllegalArgumentException("未知市场区域");
        return market;
    }

    /** 按当日市价采购专营商品，库存与成本随产业保存。 */
    public long restockNpcBusiness(String ownerNpcId, String businessId, TradeGood good, int quantity) {
        return restockNpcBusiness(ownerNpcId, businessId, good, quantity, MarketRegion.JIANGNAN);
    }
    public long restockNpcBusiness(String ownerNpcId, String businessId, TradeGood good, int quantity, MarketRegion region) {
        NpcBusiness business = requireBusiness(businessId);
        if (!business.ownerNpcId().equals(ownerNpcId)) throw new IllegalArgumentException("只有产业主人可以采购");
        RegionalMarket market = requireMarket(region);
        long unitPrice = marketPrice(good, region);
        long cost = Math.multiplyExact(unitPrice, quantity);
        if (cost > business.treasuryInWen()) throw new IllegalStateException("产业金库不足，无法进货");
        market.buy(quantity, good);
        try { business.restock(good, quantity, unitPrice); }
        catch (RuntimeException ex) { market.sell(quantity, good); throw ex; }
        life(ownerNpcId).remember("在" + region.displayName() + "为" + business.name() + "采购" + good.displayName() + " " + quantity + " 件");
        return cost;
    }

    /** 按市场价加产业等级溢价销售库存；销售所得进入金库。 */
    public NpcBusiness.SaleReceipt sellNpcBusinessGoods(String ownerNpcId, String businessId, TradeGood good, int quantity) {
        return sellNpcBusinessGoods(ownerNpcId, businessId, good, quantity, MarketRegion.JIANGNAN);
    }
    public NpcBusiness.SaleReceipt sellNpcBusinessGoods(String ownerNpcId, String businessId, TradeGood good, int quantity, MarketRegion region) {
        NpcBusiness business = requireBusiness(businessId);
        if (!business.ownerNpcId().equals(ownerNpcId)) throw new IllegalArgumentException("只有产业主人可以安排销售");
        long unitPrice = Math.multiplyExact(marketPrice(good, region), 100L + 3L * (business.level() - 1)) / 100L;
        NpcBusiness.SaleReceipt receipt = business.sell(good, quantity, unitPrice);
        requireMarket(region).sell(quantity, good);
        life(ownerNpcId).remember("在" + region.displayName() + "的" + business.name() + "售出" + good.displayName() + " " + quantity + " 件，毛利 " + receipt.grossProfitInWen() + " 文");
        return receipt;
    }
    /** 创建行商队伍，发起人自动成为商队领队。 */
    public TradeCaravan createTradeCaravan(String ownerNpcId, String caravanId, MarketRegion origin, MarketRegion destination) {
        Npc owner = requireNpc(ownerNpcId);
        if (!life(ownerNpcId).alive()) throw new IllegalStateException("已故 NPC 不能组织商队");
        if (caravanId == null || caravanId.isBlank() || caravans.containsKey(caravanId)) throw new IllegalArgumentException("商队编号无效或重复");
        TradeCaravan caravan = new TradeCaravan(caravanId, ownerNpcId, origin, destination);
        caravans.put(caravanId, caravan);
        life(ownerNpcId).remember("组织商队" + caravanId + "，计划从" + origin.displayName() + "前往" + destination.displayName());
        return caravan;
    }
    public void addTradeCaravanCrew(String ownerNpcId, String caravanId, String crewNpcId) {
        TradeCaravan caravan = requireCaravan(caravanId);
        if (!caravan.ownerNpcId().equals(ownerNpcId)) throw new IllegalArgumentException("只有商队领队可以招募成员");
        requireNpc(crewNpcId);
        if (!life(crewNpcId).alive()) throw new IllegalStateException("已故 NPC 不能加入商队");
        String profession = life(crewNpcId).profession().orElse(requireNpc(crewNpcId).role());
        int combatStrength = switch (profession) {
            case "镖师" -> 30;
            case "武师", "侠客", "护院" -> 25;
            case "捕快", "军士" -> 20;
            default -> 5;
        };
        caravan.addCrew(crewNpcId, combatStrength);
        life(crewNpcId).remember("加入商队" + caravanId + "，随队前往" + caravan.destination().displayName());
    }
    /** 玩家亲自加入商队押运；出发前消耗体力，并以声望影响护卫战力。 */
    public void joinPlayerTradeCaravan(String caravanId) {
        TradeCaravan caravan = requireCaravan(caravanId);
        if (caravan.state() != TradeCaravan.State.PREPARING) throw new IllegalStateException("商队已经出发，无法中途加入");
        if (caravan.crew().contains("@player")) throw new IllegalStateException("玩家已经加入这支商队");
        if (!player.spendStamina(10)) throw new IllegalStateException("体力不足，无法加入押运");
        int strength = Math.max(5, Math.min(35, 15 + player.reputation() / 5));
        try { caravan.addCrew("@player", strength); }
        catch (RuntimeException ex) { player.recoverStamina(10); throw ex; }
    }

    /** 在出发地采购货物并装车，货款由领队个人承担。 */
    public long loadTradeCaravan(String ownerNpcId, String caravanId, TradeGood good, int quantity) {
        TradeCaravan caravan = requireCaravan(caravanId);
        if (!caravan.ownerNpcId().equals(ownerNpcId)) throw new IllegalArgumentException("只有商队领队可以装货");
        long unitPrice = marketPrice(good, caravan.origin());
        long cost = Math.multiplyExact(unitPrice, quantity);
        if (cost > life(ownerNpcId).moneyInWen()) throw new IllegalStateException("领队银钱不足，无法采购");
        RegionalMarket market = requireMarket(caravan.origin());
        market.buy(quantity, good);
        try {
            life(ownerNpcId).debitMoney(cost);
            caravan.load(good, quantity, cost);
        } catch (RuntimeException ex) {
            life(ownerNpcId).creditMoney(cost);
            market.sell(quantity, good);
            throw ex;
        }
        life(ownerNpcId).remember("为商队" + caravanId + "采购" + good.displayName() + " " + quantity + " 件，花费 " + cost + " 文");
        return cost;
    }
    /** 按区域距离估算路程；跨越越远，所需游戏时间越长。 */
    public int departTradeCaravan(String ownerNpcId, String caravanId) {
        TradeCaravan caravan = requireCaravan(caravanId);
        if (!caravan.ownerNpcId().equals(ownerNpcId)) throw new IllegalArgumentException("只有商队领队可以下令出发");
        int distance = Math.abs(caravan.origin().ordinal() - caravan.destination().ordinal());
        int travelMinutes = 360 + distance * 180;
        caravan.depart(travelMinutes);
        return travelMinutes;
    }
    public List<TradeCaravan> tradeCaravans() { return List.copyOf(caravans.values()); }
    public Optional<TradeCaravan> tradeCaravan(String caravanId) { return Optional.ofNullable(caravans.get(caravanId)); }
    private TradeCaravan requireCaravan(String caravanId) {
        TradeCaravan caravan = caravans.get(caravanId);
        if (caravan == null) throw new IllegalArgumentException("未知商队：" + caravanId);
        return caravan;
    }
    private void settleArrivedCaravan(TradeCaravan caravan) {
        Map<TradeGood, TradeCaravan.Cargo> cargo = caravan.complete();
        for (Map.Entry<String, Integer> wound : caravan.crewWounds().entrySet()) {
            if (wound.getKey().equals("@player")) {
                player.changeHealth(-wound.getValue());
            } else if (life(wound.getKey()).alive()) {
                injureNpc(wound.getKey(), wound.getValue(), "商队" + caravan.id() + "途中遭遇劫匪");
                life(wound.getKey()).remember("商队遇袭受伤，健康损失 " + wound.getValue() + " 点");
            }
        }
        long revenueTotal = 0;
        for (TradeCaravan.Cargo item : cargo.values()) {
            long revenue = Math.multiplyExact(marketPrice(item.good(), caravan.destination()), item.quantity());
            requireMarket(caravan.destination()).sell(item.quantity(), item.good());
            revenueTotal = Math.addExact(revenueTotal, revenue);
        }
        life(caravan.ownerNpcId()).creditMoney(revenueTotal);
        long costTotal = cargo.values().stream().mapToLong(TradeCaravan.Cargo::totalCostInWen).sum();
        long profit = revenueTotal - costTotal;
        String report = "商队" + caravan.id() + "抵达" + caravan.destination().displayName() + "，销售收入 " + revenueTotal + " 文，净收益 " + profit + " 文，途中损失货物 " + caravan.lostCargoQuantity() + " 件";
        for (String memberId : caravan.crew()) {
            if (memberId.equals("@player")) {
                if (revenueTotal > 0) player.changeReputation(2);
                player.recoverStamina(5);
            } else if (life(memberId).alive()) life(memberId).remember(report);
        }
    }
    /** 用产业金库升级经营规模，最高五级。 */
    public long upgradeNpcBusiness(String ownerNpcId, String businessId) {
        NpcBusiness business = requireBusiness(businessId);
        if (!business.ownerNpcId().equals(ownerNpcId)) throw new IllegalArgumentException("只有产业主人可以升级");
        long cost = business.upgrade();
        life(ownerNpcId).remember("投入" + cost + "文，将" + business.name() + "升级至" + business.level() + "级");
        return cost;
    }

    /** 产业主人决定分红比例；分红直接进入员工个人账目。 */
    public long distributeNpcBusinessProfit(String ownerNpcId, String businessId, int percentage) {
        NpcBusiness business = requireBusiness(businessId);
        if (!business.ownerNpcId().equals(ownerNpcId)) throw new IllegalArgumentException("只有产业主人可以分红");
        Map<String, Long> shares = business.distributeProfit(percentage);
        long total = 0;
        for (Map.Entry<String, Long> share : shares.entrySet()) {
            if (life(share.getKey()).alive()) {
                life(share.getKey()).creditMoney(share.getValue());
                life(share.getKey()).remember("获得" + business.name() + "利润分红 " + share.getValue() + " 文");
                total = Math.addExact(total, share.getValue());
            }
        }
        life(ownerNpcId).remember("向" + business.name() + "员工分配利润 " + total + " 文");
        return total;
    }
    /** 只雇佣在世的家庭成员或已建立师徒关系的 NPC。 */
    public void hireForNpcBusiness(String ownerNpcId, String businessId, String employeeNpcId) {
        NpcBusiness business = requireBusiness(businessId);
        if (!business.ownerNpcId().equals(ownerNpcId)) throw new IllegalArgumentException("只有产业主人可以雇佣");
        requireNpc(employeeNpcId);
        if (!life(ownerNpcId).alive() || !life(employeeNpcId).alive()) throw new IllegalStateException("产业主人和雇员都必须在世");
        boolean family = familyRegistry.familyOf(ownerNpcId).contains(employeeNpcId)
            || familyRegistry.familyOf(employeeNpcId).contains(ownerNpcId);
        boolean apprenticeship = socialGraph.relation(ownerNpcId, employeeNpcId)
            .filter(b -> b.type() == NpcSocialGraph.RelationType.MENTOR).isPresent()
            || socialGraph.relation(employeeNpcId, ownerNpcId)
            .filter(b -> b.type() == NpcSocialGraph.RelationType.MENTOR).isPresent();
        if (!family && !apprenticeship) throw new IllegalArgumentException("雇员必须是家庭成员或师徒");
        business.hire(employeeNpcId);
        life(ownerNpcId).remember("雇佣" + requireNpc(employeeNpcId).name() + "加入" + business.name());
        life(employeeNpcId).remember("受雇于" + business.name());
    }

    public void fireFromNpcBusiness(String ownerNpcId, String businessId, String employeeNpcId) {
        NpcBusiness business = requireBusiness(businessId);
        if (!business.ownerNpcId().equals(ownerNpcId)) throw new IllegalArgumentException("只有产业主人可以解雇雇员");
        business.fire(employeeNpcId);
        life(ownerNpcId).remember("辞退" + requireNpc(employeeNpcId).name() + "，其离开" + business.name());
        life(employeeNpcId).remember("离开产业" + business.name());
    }

    /** 逝者的家庭成员可继承产业所有权，产业金库和其他雇员保持不变。 */
    public void inheritNpcBusiness(String businessId, String heirNpcId) {
        NpcBusiness business = requireBusiness(businessId);
        String deceasedId = business.ownerNpcId();
        Npc deceased = requireNpc(deceasedId);
        Npc heir = requireNpc(heirNpcId);
        if (life(deceasedId).alive()) throw new IllegalStateException("产业主人尚在人世");
        if (!familyRegistry.familyOf(deceasedId).contains(heirNpcId)) throw new IllegalArgumentException("产业继承人必须是登记家庭成员");
        if (!life(heirNpcId).alive()) throw new IllegalStateException("继承人已故");
        business.transferOwnership(heirNpcId);
        familyRegistry.recordEvent(NpcFamilyRegistry.EventType.INHERITANCE, List.of(deceasedId, heirNpcId), clock.day(),
            heir.name() + "继承" + deceased.name() + "的产业" + business.name());
        life(heirNpcId).remember("继承" + deceased.name() + "的产业" + business.name());
    }

    public List<NpcBusiness> npcBusinesses() { return List.copyOf(businesses.values()); }
    public Optional<NpcBusiness> npcBusiness(String businessId) { return Optional.ofNullable(businesses.get(businessId)); }
    private NpcBusiness requireBusiness(String businessId) {
        NpcBusiness business = businesses.get(businessId);
        if (business == null) throw new IllegalArgumentException("未知产业：" + businessId);
        return business;
    }
    private void settleNpcBusinesses() {
        while (lastBusinessDay < clock.day()) {
            lastBusinessDay++;
            boolean adverse = weather.contains("暴雨") || weather.contains("暴雪") || weather.contains("台风");
            for (NpcBusiness business : businesses.values()) {
                NpcBusiness.DailyReport report = business.settleDay(adverse);
                for (String employeeId : business.employees()) {
                    if (life(employeeId).alive()) life(employeeId).creditMoney(business.dailyWagePerEmployeeInWen());
                }
                if (report.netProfitInWen() < 0) {
                    life(business.ownerNpcId()).remember(business.name() + "今日经营亏损 " + (-report.netProfitInWen()) + " 文");
                }
            }
        }
    }
    /** 家庭成员发生冲突：谱系保留，社会关系暂时转为仇敌。 */
    public void familyConflict(String firstNpcId, String secondNpcId, String description) {
        Npc first = requireNpc(firstNpcId);
        Npc second = requireNpc(secondNpcId);
        requireDescription(description);
        if (!familyRegistry.familyOf(firstNpcId).contains(secondNpcId)
                && !familyRegistry.familyOf(secondNpcId).contains(firstNpcId))
            throw new IllegalArgumentException("双方不是已登记的家庭成员");
        socialGraph.connect(firstNpcId, secondNpcId, NpcSocialGraph.RelationType.ENEMY, -30);
        socialGraph.connect(secondNpcId, firstNpcId, NpcSocialGraph.RelationType.ENEMY, -30);
        life(firstNpcId).changeMood(-6);
        life(secondNpcId).changeMood(-6);
        life(firstNpcId).remember("与家人" + second.name() + "发生争执：" + description);
        life(secondNpcId).remember("与家人" + first.name() + "发生争执：" + description);
        familyRegistry.recordEvent(NpcFamilyRegistry.EventType.CONFLICT, List.of(firstNpcId, secondNpcId), clock.day(), description);
    }

    /** 和解恢复家庭关系并记录事件。 */
    public void reconcileFamily(String firstNpcId, String secondNpcId, String description) {
        Npc first = requireNpc(firstNpcId);
        Npc second = requireNpc(secondNpcId);
        requireDescription(description);
        if (!familyRegistry.familyOf(firstNpcId).contains(secondNpcId)
                && !familyRegistry.familyOf(secondNpcId).contains(firstNpcId))
            throw new IllegalArgumentException("双方不是已登记的家庭成员");
        socialGraph.connect(firstNpcId, secondNpcId, NpcSocialGraph.RelationType.FAMILY, 70);
        socialGraph.connect(secondNpcId, firstNpcId, NpcSocialGraph.RelationType.FAMILY, 70);
        life(firstNpcId).changeMood(8);
        life(secondNpcId).changeMood(8);
        life(firstNpcId).remember("与家人" + second.name() + "和解：" + description);
        life(secondNpcId).remember("与家人" + first.name() + "和解：" + description);
        familyRegistry.recordEvent(NpcFamilyRegistry.EventType.RECONCILIATION, List.of(firstNpcId, secondNpcId), clock.day(), description);
    }

    private Npc requireNpc(String npcId) {
        return npcs.stream().filter(n -> n.id().equals(npcId)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("未知 NPC：" + npcId));
    }
    private static void requireDescription(String description) {
        if (description == null || description.isBlank())
            throw new IllegalArgumentException("行为描述不能为空");
    }

    /** 每个游戏日，同处一地的 NPC 会按既有关系发生一次日常互动。 */
    private void settleNpcSocialLife() {
        for (NpcSocialGraph.Bond bond : socialGraph.allRelations()) {
            if (!life(bond.fromNpcId()).alive() || !life(bond.toNpcId()).alive()) continue;
            String pairKey = bond.fromNpcId() + "->" + bond.toNpcId();
            if (lastSocialDay.getOrDefault(pairKey, 0L) == clock.day()) continue;
            Npc from = requireNpc(bond.fromNpcId());
            Npc to = requireNpc(bond.toNpcId());
            String place = locationFor(from, clock.hour());
            if (!place.equals(locationFor(to, clock.hour()))) continue;
            boolean warm = switch (bond.type()) {
                case FAMILY, FRIEND, MENTOR, DISCIPLE -> true;
                case RIVAL, ENEMY -> false;
            };
            socialGraph.changeStrength(from.id(), to.id(), warm ? 1 : -1);
            life(from.id()).changeMood(warm ? 2 : -1);
            life(to.id()).changeMood(warm ? 2 : -1);
            String event = warm
                ? "今日在" + place + "与" + to.name() + "相聚"
                : "今日在" + place + "与" + to.name() + "发生争执";
            life(from.id()).remember(event);
            life(to.id()).remember(warm
                ? "今日在" + place + "与" + from.name() + "相聚"
                : "今日在" + place + "与" + from.name() + "发生争执");
            lastSocialDay.put(pairKey, clock.day());
        }
    }

    /** 与脚边的地图地点交互：采集资源、在客栈歇脚或查看集市。 */
    public Optional<String> interactWithNearbyPoint(GameMap map) {
        Optional<MapPoint> target = map.nearestInteractable(player.x(), player.y());
        if (target.isEmpty()) return Optional.empty();
        MapPoint point = target.get();
        return Optional.of(switch (point.kind()) {
            case RESOURCE -> harvestResource(point);
            case INN -> restAtInn(point);
            case MARKET -> "乌篷集市：草药 " + marketPrice(TradeGood.HERB) + " 文/份，茶叶 " + marketPrice(TradeGood.TEA) + " 文/份，熟食 " + marketPrice(TradeGood.MEAL) + " 文/份。按 B 购买草药，按 V 出售草药。";
            case LANDMARK -> point.name() + "：河风拂面，远处传来市井人声。";
            case NPC -> "这里是人物活动地点，按 E 与附近的人交谈。";
            case EXIT -> "道路通向镇外，新的旅途还在前方。";
        });
    }

    private final Map<String, Long> resourceHarvestDays = new LinkedHashMap<>();
    private final Map<String, Quest> playerQuests = new LinkedHashMap<>();
    private final Map<String, Long> questRewards = new LinkedHashMap<>();
    /** 在集市购买商品；先验证余额和库存，再一次性结算银钱与背包。 */
    public long buyFromMarket(GameMap map, TradeGood good, int quantity) {
        requireMarketAccess(map);
        requireTradeQuantity(quantity);
        long unitPrice = marketPrice(good);
        long cost = Math.multiplyExact(unitPrice, quantity);
        if (player.moneyInWen() < cost) throw new IllegalStateException("银钱不足，无法购买");
        RegionalMarket market = requireMarket(MarketRegion.JIANGNAN);
        market.buy(quantity, good);
        try { player.addMoney(-cost); player.addItem(itemId(good), quantity); }
        catch (RuntimeException ex) { market.sell(quantity, good); throw ex; }
        return cost;
    }

    /** 将背包货物卖给集市；玩家按当日市价的九成成交。 */
    public long sellToMarket(GameMap map, TradeGood good, int quantity) {
        requireMarketAccess(map);
        requireTradeQuantity(quantity);
        String id = itemId(good);
        if (player.inventory().getOrDefault(id, 0) < quantity) throw new IllegalStateException("背包货物不足");
        long unitPrice = Math.max(1, marketPrice(good) * 90 / 100);
        long revenue = Math.multiplyExact(unitPrice, quantity);
        if (!player.removeItem(id, quantity)) throw new IllegalStateException("背包货物不足");
        requireMarket(MarketRegion.JIANGNAN).sell(quantity, good);
        player.addMoney(revenue);
        return revenue;
    }

    private void requireMarketAccess(GameMap map) {
        if (map == null || map.nearestInteractable(player.x(), player.y())
                .filter(point -> point.kind() == MapPoint.Kind.MARKET).isEmpty())
            throw new IllegalStateException("请先走到乌篷集市附近再交易");
    }
    private static void requireTradeQuantity(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("交易数量必须为正数");
    }
    private static String itemId(TradeGood good) {
        if (good == null) throw new IllegalArgumentException("商品不能为空");
        return switch (good) {
            case MEAL -> "meal"; case TEA -> "tea"; case HERB -> "herb";
            case MEDICINE -> "medicine"; case GRAIN -> "grain";
            case VEGETABLE -> "vegetable"; case FISH -> "fish";
        };
    }

    /** 登记可交付物品的委托。任务目标键使用背包物品 ID，奖励单位为文。 */
    public void offerQuest(Quest quest, long rewardInWen) {
        if (quest == null || rewardInWen <= 0) throw new IllegalArgumentException("任务或奖励无效");
        if (playerQuests.putIfAbsent(quest.id(), quest) != null) throw new IllegalArgumentException("任务编号重复");
        questRewards.put(quest.id(), rewardInWen);
    }
    public Optional<Quest> quest(String questId) { return Optional.ofNullable(playerQuests.get(questId)); }
    public List<Quest> quests() { return List.copyOf(playerQuests.values()); }
    public void acceptQuest(String questId) {
        Quest quest = requireQuest(questId);
        if (quest.status() != Quest.Status.LOCKED) throw new IllegalStateException("该任务不能再次接取");
        quest.activate();
    }
    /** 交付任务物品并领取报酬；库存不足时不会消耗任何物品。 */
    public long turnInQuest(String questId) {
        Quest quest = requireQuest(questId);
        if (quest.status() != Quest.Status.READY_TO_TURN_IN) throw new IllegalStateException("任务目标尚未完成");
        Map<String, Integer> objectives = quest.objectives();
        for (Map.Entry<String, Integer> entry : objectives.entrySet())
            if (player.inventory().getOrDefault(entry.getKey(), 0) < entry.getValue())
                throw new IllegalStateException("背包中的" + entry.getKey() + "不足，无法交付");
        objectives.forEach(player::removeItem);
        quest.turnIn();
        long reward = questRewards.get(questId);
        player.addMoney(reward);
        player.changeReputation(1);
        return reward;
    }
    private Quest requireQuest(String questId) {
        Quest quest = playerQuests.get(questId);
        if (quest == null) throw new IllegalArgumentException("未知任务：" + questId);
        return quest;
    }
    private String harvestResource(MapPoint point) {
        if (resourceHarvestDays.getOrDefault(point.id(), 0L) == clock.day())
            return point.name() + "今日已经采过了，明日或许会有新草药。";
        if (!player.spendStamina(5)) return "体力不足，歇息一会儿再采集吧。";
        player.addItem("herb", 1);
        for (Quest quest : playerQuests.values()) quest.record("herb", 1);
        resourceHarvestDays.put(point.id(), clock.day());
        advanceMinutes(10);
        return "你在" + point.name() + "采得草药一份（体力 -5）。";
    }

    private String restAtInn(MapPoint point) {
        if (player.moneyInWen() < 30) return "住店歇脚需要 30 文，你的钱不够。";
        player.addMoney(-30);
        waitMinutes(60);
        player.recoverStamina(20);
        player.changeHealth(2);
        return "你在" + point.name() + "喝茶歇息一个时辰，花费 30 文，恢复了体力与健康。";
    }

    /** 草药炼制金疮药：两份草药换一份成药。 */
    public String craftMedicine() {
        if (player.inventory().getOrDefault("herb", 0) < 2) throw new IllegalStateException("炼制金疮药需要两份草药");
        player.removeItem("herb", 2); player.addItem("medicine", 1);
        advanceMinutes(30);
        return "你以两份草药炼成一份金疮药。";
    }

    /** 烹制熟食：一份粮食搭配一份蔬菜或鲜鱼。 */
    public String cookMeal() {
        if (player.inventory().getOrDefault("grain", 0) < 1)
            throw new IllegalStateException("烹饪熟食需要一份粮食");
        String side = player.inventory().getOrDefault("vegetable", 0) > 0 ? "vegetable" :
            player.inventory().getOrDefault("fish", 0) > 0 ? "fish" : null;
        if (side == null) throw new IllegalStateException("还需要一份时蔬或鲜鱼");
        player.removeItem("grain", 1); player.removeItem(side, 1); player.addItem("meal", 1);
        advanceMinutes(20);
        return "你烹制出一份江南熟食。";
    }

    public String equipPlayerItem(String itemId) {
        var item = com.jianghu.item.EquipmentCatalog.find(itemId)
            .orElseThrow(() -> new IllegalArgumentException("这件物品不能装备"));
        if (!player.equip(itemId)) throw new IllegalStateException("背包中没有" + item.name());
        return "已装备" + item.name() + "：" + item.description();
    }

    public String unequipPlayerItem(com.jianghu.item.EquipmentItem.Slot slot) {
        if (!player.unequip(slot)) throw new IllegalStateException("该装备栏目前是空的");
        return "装备已放回背包。";
    }
    /** 使用一件可消耗物品；先确认物品存在且有效，再扣除库存并应用效果。 */
    public String useInventoryItem(String itemId) {
        com.jianghu.item.InventoryItem item = com.jianghu.item.InventoryCatalog.find(itemId)
            .orElseThrow(() -> new IllegalArgumentException("未知物品：" + itemId));
        if (item.health() == 0 && item.stamina() == 0 && item.hunger() == 0
                && item.thirst() == 0 && item.morale() == 0)
            throw new IllegalStateException("这件物品暂时无法直接使用");
        if (player.inventory().getOrDefault(itemId, 0) < 1)
            throw new IllegalStateException("背包中没有" + item.name());
        player.removeItem(itemId, 1);
        player.changeHealth(item.health());
        player.recoverStamina(item.stamina());
        player.changeHunger(item.hunger());
        player.changeThirst(item.thirst());
        player.changeMorale(item.morale());
        advanceMinutes(5);
        return "使用" + item.name() + "：" + item.description();
    }

    /** 按图鉴分类汇总背包；未知物品统一归入材料，避免存档物品丢失。 */
    public Map<com.jianghu.item.InventoryItem.Category, Map<String, Integer>> inventoryByCategory() {
        Map<com.jianghu.item.InventoryItem.Category, Map<String, Integer>> grouped = new EnumMap<>(com.jianghu.item.InventoryItem.Category.class);
        for (com.jianghu.item.InventoryItem.Category category : com.jianghu.item.InventoryItem.Category.values())
            grouped.put(category, new LinkedHashMap<>());
        player.inventory().forEach((id, count) -> {
            var item = com.jianghu.item.InventoryCatalog.find(id);
            var category = item.map(com.jianghu.item.InventoryItem::category)
                .orElse(com.jianghu.item.InventoryItem.Category.MATERIAL);
            grouped.get(category).put(id, count);
        });
        Map<com.jianghu.item.InventoryItem.Category, Map<String, Integer>> result = new EnumMap<>(com.jianghu.item.InventoryItem.Category.class);
        grouped.forEach((category, items) -> result.put(category, Map.copyOf(items)));
        return Map.copyOf(result);
    }
    /** 在附近 NPC 处打短工；劳动消耗体力与时间，收入取决于雇主行当。 */
    public long workForNearbyNpc(GameMap map) {
        NpcPosition position = nearbyNpc(map).orElseThrow(() -> new IllegalStateException("附近没有可以雇用你的 NPC"));
        Npc employer = requireNpc(position.npcId());
        if (!life(employer.id()).alive()) throw new IllegalStateException("雇主已不在人世");
        if (!player.spendStamina(15)) throw new IllegalStateException("体力不足，无法继续做工");
        long wage = switch (employer.role()) {
            case "茶摊老板" -> 45;
            case "渔夫" -> 50;
            case "药师" -> 55;
            case "农夫" -> 35;
            default -> 25;
        };
        advanceMinutes(120);
        player.addMoney(wage);
        relationship(employer.id()).changeFavor(2);
        life(employer.id()).remember("雇玩家帮忙做工两个时辰，支付 " + wage + " 文");
        return wage;
    }

    /** 将背包中一件礼物送给附近 NPC；成功后才消耗物品并改变关系。 */
    public void giftToNearbyNpc(GameMap map, String itemId) {
        NpcPosition position = nearbyNpc(map).orElseThrow(() -> new IllegalStateException("附近没有可以赠礼的人"));
        Npc recipient = requireNpc(position.npcId());
        if (!life(recipient.id()).alive()) throw new IllegalStateException("对方已不在人世");
        long value = switch (itemId) {
            case "herb" -> 42; case "tea" -> 36; case "meal" -> 24;
            case "medicine" -> 96; case "fish" -> 30; case "grain" -> 18;
            case "vegetable" -> 12;
            default -> throw new IllegalArgumentException("这件物品不适合作为礼物");
        };
        if (!player.removeItem(itemId, 1)) throw new IllegalStateException("背包中没有可赠送的" + itemId);
        giveGiftToNpc(recipient.id(), itemId, (int) value);
    }
    public Optional<String> talkToNearby(GameMap map) {
        return nearbyNpc(map).flatMap(position -> npcs.stream()
            .filter(n -> n.id().equals(position.npcId())).findFirst())
            .map(npc -> {
                NpcRelationship bond = relationship(npc.id());
                bond.talk(true);
                life(npc.id()).remember("与玩家交谈（第 " + bond.conversations() + " 次）");
                return DialogueService.greeting(npc, clock.hour(), bond);
            });
    }
    public Optional<NpcPosition> nearbyNpc(GameMap map) {
        return npcPositions(map).stream()
            .filter(n -> Math.abs(player.x()-n.x()) + Math.abs(player.y()-n.y()) <= 1)
            .min((a,b) -> Integer.compare(Math.abs(player.x()-a.x())+Math.abs(player.y()-a.y()),
                                          Math.abs(player.x()-b.x())+Math.abs(player.y()-b.y())));
    }
}
