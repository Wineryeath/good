package com.jianghu.world;

import com.jianghu.character.Player;
import com.jianghu.business.BusinessType;
import com.jianghu.business.TradeGood;
import com.jianghu.business.MarketRegion;
import com.jianghu.map.GameMap;
import com.jianghu.npc.Npc;
import com.jianghu.npc.NpcFamilyRegistry;
import com.jianghu.time.WorldClock;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GameWorldTest {
    @Test void npcPositionChangesWithScheduleAndWorldTime() {
        Npc teaOwner = new Npc("tea-owner","沈掌柜","茶摊老板",List.of(
            new Npc.Schedule(6,11,"tea-stall",0),
            new Npc.Schedule(11,14,"market-west",0),
            new Npc.Schedule(14,20,"tea-stall",0),
            new Npc.Schedule(20,6,"home",0)),0);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(1,7*60),List.of(teaOwner));
        GameMap map = GameMap.starterTown();
        assertEquals("tea-stall",world.npcPositions(map).get(0).schedulePlace());
        world.waitMinutes(5*60);
        assertEquals("market-west",world.npcPositions(map).get(0).schedulePlace());
    }
    @Test void nearbyNpcCanBeFoundAtCurrentScheduleLocation() {
        Npc fisherman = new Npc("fisher","陆老七","渔夫",List.of(
            new Npc.Schedule(5,10,"east-bank",0)),5);
        Player player = new Player("旅人");
        player.moveTo(13,8);
        GameWorld world = new GameWorld(player,new WorldClock(1,7*60),List.of(fisherman));
        assertEquals("fisher",world.nearbyNpc(GameMap.starterTown()).orElseThrow().npcId());
    }
    @Test void talkingBuildsNpcRelationship() {
        Npc teaOwner = new Npc("tea-owner","沈掌柜","茶摊老板",List.of(
            new Npc.Schedule(6,20,"tea-stall",0)),0);
        Player player = new Player("旅人");
        player.moveTo(10,5);
        GameWorld world = new GameWorld(player,new WorldClock(1,8*60),List.of(teaOwner));
        String first = world.talkToNearby(GameMap.starterTown()).orElseThrow();
        assertTrue(first.contains("沈掌柜"));
        assertEquals(1,world.relationship("tea-owner").conversations());
        assertEquals(2,world.relationship("tea-owner").favor());
    }

    @Test void npcNeedsAndLivelihoodAdvanceAtEachWorldHour() {
        Npc fisherman = new Npc("fisher","陆老七","渔夫",List.of(
            new Npc.Schedule(5,19,"river-hut",0)),5);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(1,7*60+30),List.of(fisherman));
        int hungerBefore = world.life("fisher").hunger();
        long moneyBefore = world.life("fisher").moneyInWen();
        world.advanceMinutes(30);
        assertEquals(hungerBefore + 4,world.life("fisher").hunger());
        assertEquals(moneyBefore + 12,world.life("fisher").moneyInWen());
    }
    @Test void talkingIsRememberedByNpc() {
        Npc teaOwner = new Npc("tea-owner","沈掌柜","茶摊老板",List.of(
            new Npc.Schedule(6,20,"tea-stall",0)),0);
        Player player = new Player("旅人");
        player.moveTo(10,5);
        GameWorld world = new GameWorld(player,new WorldClock(1,8*60),List.of(teaOwner));
        world.talkToNearby(GameMap.starterTown());
        assertTrue(world.life("tea-owner").remembers("与玩家交谈（第 1 次）"));
    }


    @Test void playerActionsChangeNpcRelationshipAndLeaveMemories() {
        Npc shopkeeper = new Npc("shop","沈掌柜","茶摊老板",List.of(),0);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(1,8*60),List.of(shopkeeper));
        world.giveGiftToNpc("shop","好茶",120);
        assertEquals(12,world.relationship("shop").favor());
        assertEquals(2,world.relationship("shop").trust());
        world.helpNpc("shop","修好漏雨的屋顶");
        assertEquals(20,world.relationship("shop").favor());
        assertEquals(8,world.relationship("shop").trust());
        assertTrue(world.life("shop").memories().stream().anyMatch(m -> m.contains("修好漏雨的屋顶")));
        world.deceiveNpc("shop","谎称茶叶已经付款");
        assertEquals(8,world.relationship("shop").favor());
        assertEquals(-17,world.relationship("shop").trust());
        assertTrue(world.life("shop").memories().stream().anyMatch(m -> m.contains("欺骗")));
    }

    @Test void unknownNpcCannotReceivePlayerActions() {
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(1,8*60),List.of());
        assertThrows(IllegalArgumentException.class,() -> world.helpNpc("missing","帮忙"));
    }


    @Test void npcsSocializeWhenTheirSchedulesOverlap() {
        Npc shopkeeper = new Npc("shop","沈掌柜","茶摊老板",List.of(
            new Npc.Schedule(6,20,"tea-stall",0)),0);
        Npc neighbor = new Npc("neighbor","阿禾","居民",List.of(
            new Npc.Schedule(6,20,"tea-stall",0)),0);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(1,7*60+30),List.of(shopkeeper,neighbor));
        world.socialGraph().connect("shop","neighbor",com.jianghu.social.NpcSocialGraph.RelationType.FRIEND,10);
        int moodBefore = world.life("shop").mood();
        world.advanceMinutes(30);
        assertEquals(11,world.socialGraph().relation("shop","neighbor").orElseThrow().strength());
        assertEquals(moodBefore + 3,world.life("shop").mood());
        assertTrue(world.life("shop").memories().stream().anyMatch(m -> m.contains("阿禾") && m.contains("相聚")));
    }

    @Test void npcSocialInteractionHappensAtMostOncePerDay() {
        Npc first = new Npc("first","甲","居民",List.of(new Npc.Schedule(6,20,"square",0)),0);
        Npc second = new Npc("second","乙","居民",List.of(new Npc.Schedule(6,20,"square",0)),0);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(1,7*60+30),List.of(first,second));
        world.socialGraph().connect("first","second",com.jianghu.social.NpcSocialGraph.RelationType.ENEMY,-10);
        world.advanceMinutes(120);
        assertEquals(-11,world.socialGraph().relation("first","second").orElseThrow().strength());
    }

    @Test void marriageAndChildbirthUpdateFamilyGraphAndMemories() {
        Npc mother = new Npc("mother","阿兰","居民",List.of(),0);
        Npc father = new Npc("father","阿远","居民",List.of(),0);
        Npc child = new Npc("child","小禾","居民",List.of(),0);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(1,8*60),List.of(mother,father,child));
        world.marryNpcs("mother","father");
        world.addNpcChild("mother","child");
        assertEquals("father",world.familyRegistry().spouseOf("mother").orElseThrow());
        assertEquals(List.of("mother","father"),world.familyRegistry().parentsOf("child"));
        assertEquals(80,world.socialGraph().relation("mother","father").orElseThrow().strength());
        assertEquals(85,world.socialGraph().relation("father","child").orElseThrow().strength());
        assertTrue(world.life("mother").memories().stream().anyMatch(m -> m.contains("结为夫妻")));
        assertTrue(world.life("child").memories().stream().anyMatch(m -> m.contains("阿兰")));
        assertEquals(0, world.life("child").ageYears());
        assertEquals(com.jianghu.npc.NpcLifeState.LifeStage.CHILD, world.life("child").lifeStage());
    }

    @Test void npcCannotHaveTwoSpouses() {
        Npc a = new Npc("a","甲","居民",List.of(),0);
        Npc b = new Npc("b","乙","居民",List.of(),0);
        Npc c = new Npc("c","丙","居民",List.of(),0);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(1,8*60),List.of(a,b,c));
        world.marryNpcs("a","b");
        assertThrows(IllegalStateException.class, () -> world.marryNpcs("a","c"));
    }

    @Test void familyConflictCanBeReconciledAndIsRecorded() {
        Npc parent = new Npc("parent","阿兰","居民",List.of(),0);
        Npc child = new Npc("child","小禾","居民",List.of(),0);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(4,9*60),List.of(parent,child));
        world.addNpcChild("parent","child");
        world.familyConflict("parent","child","因家中生计意见不合");
        assertEquals(com.jianghu.social.NpcSocialGraph.RelationType.ENEMY,
            world.socialGraph().relation("parent","child").orElseThrow().type());
        assertEquals(NpcFamilyRegistry.EventType.CONFLICT,world.familyRegistry().events().get(1).type());
        world.reconcileFamily("parent","child","坦诚交谈后互相谅解");
        assertEquals(com.jianghu.social.NpcSocialGraph.RelationType.FAMILY,
            world.socialGraph().relation("child","parent").orElseThrow().type());
        assertEquals(NpcFamilyRegistry.EventType.RECONCILIATION,world.familyRegistry().events().get(2).type());
        assertEquals(4,world.familyRegistry().events().get(0).worldDay());
    }

    @Test void crossingMidnightAdvancesNpcAgeByOneGameDay() {
        Npc npc = new Npc("npc","老周","居民",List.of(),0);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(10,23*60+59),List.of(npc));
        assertEquals(0,world.life("npc").daysLivedThisYear());
        world.advanceMinutes(1);
        assertEquals(11,world.clock().day());
        assertEquals(1,world.life("npc").daysLivedThisYear());
    }

    @Test void familyMemberInheritsEstateAfterNpcDeath() {
        Npc parent = new Npc("parent","阿兰","居民",List.of(),0);
        Npc child = new Npc("child","小禾","居民",List.of(),0);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(8,10*60),List.of(parent,child));
        world.addNpcChild("parent","child");
        long estate = world.life("parent").moneyInWen();
        long childBefore = world.life("child").moneyInWen();
        world.recordNpcDeath("parent","寿终正寝");
        assertFalse(world.life("parent").alive());
        assertFalse(world.npcPositions(GameMap.starterTown()).stream().anyMatch(p -> p.npcId().equals("parent")));
        assertEquals(NpcFamilyRegistry.EventType.DEATH,world.familyRegistry().events().get(1).type());
        assertEquals(estate,world.inheritNpcEstate("parent","child"));
        assertEquals(childBefore + estate,world.life("child").moneyInWen());
        assertEquals(NpcFamilyRegistry.EventType.INHERITANCE,world.familyRegistry().events().get(2).type());
        assertTrue(world.life("child").memories().stream().anyMatch(m -> m.contains("继承阿兰")));
    }

    @Test void unrelatedNpcCannotInheritAndDuplicateBirthIsIdempotent() {
        Npc parent = new Npc("parent","阿兰","居民",List.of(),0);
        Npc child = new Npc("child","小禾","居民",List.of(),0);
        Npc stranger = new Npc("stranger","路人","居民",List.of(),0);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(1,8*60),List.of(parent,child,stranger));
        world.addNpcChild("parent","child");
        int eventCount = world.familyRegistry().events().size();
        world.addNpcChild("parent","child");
        assertEquals(eventCount,world.familyRegistry().events().size());
        world.recordNpcDeath("parent","病逝");
        assertThrows(IllegalArgumentException.class,() -> world.inheritNpcEstate("parent","stranger"));
    }

    @Test void fatalInjuryAutomaticallyRecordsDeath() {
        Npc npc = new Npc("npc","老周","居民",List.of(),0);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(2,8*60),List.of(npc));
        world.injureNpc("npc",100,"重伤不治");
        assertFalse(world.life("npc").alive());
        assertEquals(NpcFamilyRegistry.EventType.DEATH,world.familyRegistry().events().get(0).type());
        assertThrows(IllegalStateException.class,() -> world.recordNpcDeath("npc","重复登记"));
    }

    @Test void illnessWorsensDailyAndTreatmentStopsItsProgression() {
        Npc npc = new Npc("npc","老周","居民",List.of(),0);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(1,0),List.of(npc));
        world.giveNpcIllness("npc","风寒",2);
        assertTrue(world.life("npc").sick());
        world.advanceMinutes(24*60);
        assertEquals(96,world.life("npc").health());
        world.treatNpc("npc","服用草药",2,3);
        assertFalse(world.life("npc").sick());
        assertEquals(99,world.life("npc").health());
        world.advanceMinutes(24*60);
        assertEquals(100,world.life("npc").health());
        assertTrue(world.life("npc").memories().stream().anyMatch(m -> m.contains("服用草药")));
    }

    @Test void untreatedSevereIllnessCanCauseRecordedDeath() {
        Npc npc = new Npc("npc","老周","居民",List.of(),0);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(1,0),List.of(npc));
        world.giveNpcIllness("npc","重症寒疾",3);
        world.advanceMinutes(17*24*60);
        assertFalse(world.life("npc").alive());
        assertEquals(NpcFamilyRegistry.EventType.DEATH,world.familyRegistry().events().get(0).type());
    }

    @Test void adultNpcCanEstablishAnIndependentHousehold() {
        Npc npc = new Npc("adult","阿禾","居民",List.of(
            new Npc.Schedule(6,18,"market-west",0),
            new Npc.Schedule(18,6,"home",0)),0);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(1,20*60),List.of(npc));
        world.establishIndependentHousehold("adult","禾风小院");
        assertEquals("禾风小院",world.independentHouseholdOf("adult").orElseThrow());
        assertEquals("禾风小院",world.npcLocationAt("adult",20));
        assertEquals("market-west",world.npcLocationAt("adult",10));
        assertTrue(world.life("adult").memories().stream().anyMatch(m -> m.contains("独立居所")));
        world.advanceMinutes(24*60);
        assertEquals("禾风小院",world.npcLocationAt("adult",22));
    }

    @Test void underageNpcCannotEstablishIndependentHousehold() {
        Npc parent = new Npc("parent","阿兰","居民",List.of(),0);
        Npc child = new Npc("child","小禾","居民",List.of(),0);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(1,8*60),List.of(parent,child));
        world.addNpcChild("parent","child");
        assertThrows(IllegalStateException.class,
            () -> world.establishIndependentHousehold("child","河畔小屋"));
        assertTrue(world.independentHouseholdOf("child").isEmpty());
    }

    @Test void npcCannotEstablishTwoIndependentHouseholds() {
        Npc npc = new Npc("adult","阿禾","居民",List.of(),0);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(1,8*60),List.of(npc));
        world.establishIndependentHousehold("adult","禾风小院");
        assertThrows(IllegalStateException.class,
            () -> world.establishIndependentHousehold("adult","听雨别院"));
    }

    @Test void adultNpcsWithMutualFriendshipCanMarryAfterThirtyDaysTogether() {
        Npc first = new Npc("first","阿青","居民",List.of(new Npc.Schedule(0,24,"village-square",0)),0);
        Npc second = new Npc("second","阿月","居民",List.of(new Npc.Schedule(0,24,"village-square",0)),0);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(1,0),List.of(first,second));
        world.socialGraph().connect("first","second",com.jianghu.social.NpcSocialGraph.RelationType.FRIEND,60);
        world.socialGraph().connect("second","first",com.jianghu.social.NpcSocialGraph.RelationType.FRIEND,60);
        world.advanceMinutes(30 * 24 * 60);
        assertEquals("second",world.familyRegistry().spouseOf("first").orElseThrow());
        assertEquals(NpcFamilyRegistry.EventType.MARRIAGE,
            world.familyRegistry().events().get(0).type());
        assertTrue(world.life("first").memories().stream().anyMatch(m -> m.contains("结为夫妻")));
        assertTrue(world.life("second").memories().stream().anyMatch(m -> m.contains("结为夫妻")));
    }

    @Test void npcCourtshipRequiresMutualFriendshipAndSharedDailyLocation() {
        Npc first = new Npc("first","阿青","居民",List.of(new Npc.Schedule(0,24,"east-village",0)),0);
        Npc second = new Npc("second","阿月","居民",List.of(new Npc.Schedule(0,24,"west-village",0)),0);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(1,0),List.of(first,second));
        world.socialGraph().connect("first","second",com.jianghu.social.NpcSocialGraph.RelationType.FRIEND,80);
        world.advanceMinutes(35 * 24 * 60);
        assertTrue(world.familyRegistry().spouseOf("first").isEmpty());
        assertEquals(0,world.courtshipDays("first","second"));
    }

    @Test void spousesAndChildrenShareAnExistingIndependentHousehold() {
        Npc first = new Npc("first","阿青","居民",List.of(new Npc.Schedule(0,24,"home",0)),0);
        Npc second = new Npc("second","阿月","居民",List.of(new Npc.Schedule(0,24,"home",0)),0);
        Npc child = new Npc("child","小禾","居民",List.of(new Npc.Schedule(0,24,"home",0)),0);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(1,8*60),List.of(first,second,child));
        world.establishIndependentHousehold("first","禾风小院");
        world.marryNpcs("first","second");
        assertEquals("禾风小院",world.familyHouseholdOf("second").orElseThrow());
        assertEquals("禾风小院",world.npcLocationAt("second",22));
        world.addNpcChild("first","child");
        assertEquals("禾风小院",world.familyHouseholdOf("child").orElseThrow());
        assertEquals("禾风小院",world.npcLocationAt("child",22));
    }

    @Test void marryingTwoNpcWithDifferentHomesUnifiesThemUnderFirstSpousesHome() {
        Npc first = new Npc("first","阿青","居民",List.of(),0);
        Npc second = new Npc("second","阿月","居民",List.of(),0);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(1,8*60),List.of(first,second));
        world.establishIndependentHousehold("first","禾风小院");
        world.establishIndependentHousehold("second","听雨别院");
        world.marryNpcs("first","second");
        assertEquals("禾风小院",world.familyHouseholdOf("first").orElseThrow());
        assertEquals("禾风小院",world.familyHouseholdOf("second").orElseThrow());
    }

    @Test void parentCanTeachSchoolAgeChildAndProfessionImprovesLearning() {
        Npc parent = new Npc("parent","夫子","教书先生",List.of(),0);
        Npc child = new Npc("child","小禾","居民",List.of(),0);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(1,8*60),List.of(parent,child));
        world.life("child").initializeAge(10);
        world.addNpcChild("parent","child");
        int gained = world.teachNpcChild("parent","child","识字",5);
        assertEquals(8,gained);
        assertEquals(8,world.life("child").education());
        assertEquals("启蒙",world.life("child").educationStage());
        assertTrue(world.life("child").memories().stream().anyMatch(m -> m.contains("识字")));
        assertTrue(world.life("parent").memories().stream().anyMatch(m -> m.contains("教导孩子")));
    }

    @Test void onlyRegisteredParentsCanTeachChildren() {
        Npc parent = new Npc("parent","阿兰","居民",List.of(),0);
        Npc child = new Npc("child","小禾","居民",List.of(),0);
        Npc stranger = new Npc("stranger","路人","学者",List.of(),0);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(1,8*60),List.of(parent,child,stranger));
        world.life("child").initializeAge(10);
        world.addNpcChild("parent","child");
        assertThrows(IllegalArgumentException.class,
            () -> world.teachNpcChild("stranger","child","算学",5));
    }


    @Test void adultNpcCanChooseProfessionAccordingToEducationAndEarnItsWage() {
        Npc parent = new Npc("parent","阿兰","居民",List.of(),0);
        Npc child = new Npc("child","小禾","居民",List.of(new Npc.Schedule(6,19,"market",0)),0);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(1,7*60),List.of(parent,child));
        world.life("child").initializeAge(18);
        world.addNpcChild("parent","child");
        world.life("child").study(30);
        long before = world.life("child").moneyInWen();
        world.chooseNpcProfession("child","药师");
        assertEquals("药师",world.life("child").profession().orElseThrow());
        world.advanceMinutes(60);
        assertEquals(before + 20,world.life("child").moneyInWen());
        assertTrue(world.life("child").memories().stream().anyMatch(m -> m.contains("选择职业")));
    }

    @Test void professionChoiceRequiresAdulthoodAndSufficientEducation() {
        Npc young = new Npc("young","小禾","居民",List.of(),0);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(1,8*60),List.of(young));
        world.life("young").initializeAge(17);
        assertThrows(IllegalStateException.class,() -> world.chooseNpcProfession("young","农夫"));
        Npc adult = new Npc("adult","阿青","居民",List.of(),0);
        GameWorld adultWorld = new GameWorld(new Player("旅人"),new WorldClock(1,8*60),List.of(adult));
        adultWorld.life("adult").initializeAge(18);
        assertThrows(IllegalStateException.class,() -> adultWorld.chooseNpcProfession("adult","教书先生"));
    }


    @Test void apprenticeLearnsMentorsProfessionAndCanChooseItWithoutSchoolEducation() {
        Npc mentor = new Npc("mentor","老周","药师",List.of(),0);
        Npc apprentice = new Npc("apprentice","小满","学徒",List.of(),0);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(1,8*60),List.of(mentor,apprentice));
        world.life("mentor").initializeAge(40);
        world.life("apprentice").initializeAge(18);
        world.life("mentor").study(30);
        world.chooseNpcProfession("mentor","药师");
        world.socialGraph().connect("mentor","apprentice",com.jianghu.social.NpcSocialGraph.RelationType.MENTOR,70);
        for (int i = 0; i < 3; i++) world.trainNpcApprentice("mentor","apprentice",10);
        assertEquals(39,world.life("apprentice").vocationalTraining("药师"));
        world.chooseNpcProfession("apprentice","药师");
        assertEquals("药师",world.life("apprentice").profession().orElseThrow());
        assertTrue(world.life("apprentice").memories().stream().anyMatch(m -> m.contains("跟随老周学习药师技艺")));
    }

    @Test void vocationalTrainingRequiresAnEstablishedMentorBond() {
        Npc mentor = new Npc("mentor","老周","药师",List.of(),0);
        Npc apprentice = new Npc("apprentice","小满","学徒",List.of(),0);
        GameWorld world = new GameWorld(new Player("旅人"),new WorldClock(1,8*60),List.of(mentor,apprentice));
        world.life("mentor").initializeAge(40);
        world.life("apprentice").initializeAge(15);
        world.life("mentor").study(30);
        world.chooseNpcProfession("mentor","药师");
        assertThrows(IllegalArgumentException.class,() -> world.trainNpcApprentice("mentor","apprentice",5));
    }

    @Test void familyBusinessEarnsDailyRevenueAndCanEmployFamily() {
        Npc parent = new Npc("parent", "阿兰", "居民", List.of(), 0);
        Npc child = new Npc("child", "小禾", "居民", List.of(), 0);
        GameWorld world = new GameWorld(new Player("旅人"), new WorldClock(1, 8 * 60), List.of(parent, child));
        world.addNpcChild("parent", "child");
        var inn = world.foundNpcBusiness("parent", "inn", "听雨客栈", BusinessType.INN, 320, 500);
        world.hireForNpcBusiness("parent", "inn", "child");
        assertTrue(inn.employees().contains("child"));
        world.advanceMinutes(24 * 60);
        assertEquals(820, inn.treasuryInWen());
        assertEquals(1, inn.settledDays());
        assertThrows(IllegalArgumentException.class, () -> world.foundNpcBusiness("parent", "inn", "重复客栈", BusinessType.INN, 1, 0));
    }

    @Test void familyBusinessCanBeInheritedWithItsTreasuryAndStaff() {
        Npc parent = new Npc("parent", "阿兰", "居民", List.of(), 0);
        Npc child = new Npc("child", "小禾", "居民", List.of(), 0);
        Npc worker = new Npc("worker", "阿福", "居民", List.of(), 0);
        GameWorld world = new GameWorld(new Player("旅人"), new WorldClock(3, 8 * 60), List.of(parent, child, worker));
        world.addNpcChild("parent", "child");
        world.addNpcChild("parent", "worker");
        var pharmacy = world.foundNpcBusiness("parent", "pharmacy", "百草药铺", BusinessType.PHARMACY, 200, 900);
        world.hireForNpcBusiness("parent", "pharmacy", "child");
        world.hireForNpcBusiness("parent", "pharmacy", "worker");
        world.recordNpcDeath("parent", "寿终正寝");
        world.inheritNpcBusiness("pharmacy", "child");
        assertEquals("child", pharmacy.ownerNpcId());
        assertEquals(900, pharmacy.treasuryInWen());
        assertFalse(pharmacy.employees().contains("child"));
        assertTrue(pharmacy.employees().contains("worker"));
        assertEquals(NpcFamilyRegistry.EventType.INHERITANCE, world.familyRegistry().events().get(3).type());
        assertThrows(IllegalStateException.class, () -> world.inheritNpcBusiness("pharmacy", "worker"));
    }

    @Test void unrelatedNpcCannotBeHiredIntoFamilyBusinessOrInheritIt() {
        Npc owner = new Npc("owner", "掌柜", "居民", List.of(), 0);
        Npc stranger = new Npc("stranger", "路人", "居民", List.of(), 0);
        GameWorld world = new GameWorld(new Player("旅人"), new WorldClock(1, 8 * 60), List.of(owner, stranger));
        world.foundNpcBusiness("owner", "farm", "河畔农庄", BusinessType.FARM, 100, 0);
        assertThrows(IllegalArgumentException.class, () -> world.hireForNpcBusiness("owner", "farm", "stranger"));
        world.recordNpcDeath("owner", "病逝");
        assertThrows(IllegalArgumentException.class, () -> world.inheritNpcBusiness("farm", "stranger"));
    }

    @Test void businessPaysWagesAndOwnerCanDistributeProfit() {
        Npc owner = new Npc("owner", "掌柜", "居民", List.of(), 0);
        Npc child = new Npc("child", "小禾", "居民", List.of(), 0);
        GameWorld world = new GameWorld(new Player("旅人"), new WorldClock(1, 8 * 60), List.of(owner, child));
        world.addNpcChild("owner", "child");
        var inn = world.foundNpcBusiness("owner", "inn", "听雨客栈", BusinessType.INN, 500, 100, 50, 1000);
        world.hireForNpcBusiness("owner", "inn", "child");
        long before = world.life("child").moneyInWen();
        world.advanceMinutes(24 * 60);
        assertEquals(before + 50, world.life("child").moneyInWen());
        assertEquals(350, inn.retainedProfitInWen());
        assertEquals(175, world.distributeNpcBusinessProfit("owner", "inn", 50));
        assertEquals(before + 225, world.life("child").moneyInWen());
        assertEquals(1175, inn.treasuryInWen());
    }

    @Test void severeWeatherReducesBusinessRevenueAndCanCauseDebt() {
        Npc owner = new Npc("owner", "掌柜", "居民", List.of(), 0);
        GameWorld world = new GameWorld(new Player("旅人"), new WorldClock(1, 23 * 60), List.of(owner));
        var farm = world.foundNpcBusiness("owner", "farm", "河畔农庄", BusinessType.FARM, 100, 200, 0, 50);
        world.setWeather("暴雨");
        world.advanceMinutes(60);
        assertEquals(0, farm.treasuryInWen());
        assertEquals(100, farm.debtInWen());
        assertTrue(farm.lastReport().adverseDay());
    }

    @Test void worldMarketConnectsBusinessRestockingAndSales() {
        Npc owner = new Npc("owner", "农庄主人", "居民", List.of(), 0);
        GameWorld world = new GameWorld(new Player("旅人"), new WorldClock(1, 8 * 60), List.of(owner));
        var farm = world.foundNpcBusiness("owner", "farm", "河畔农庄", BusinessType.FARM, 100, 0, 0, 1000);
        long grainPrice = world.marketPrice(TradeGood.GRAIN);
        assertEquals(grainPrice * 5, world.restockNpcBusiness("owner", "farm", TradeGood.GRAIN, 5));
        assertEquals(5, farm.stockOf(TradeGood.GRAIN));
        var sale = world.sellNpcBusinessGoods("owner", "farm", TradeGood.GRAIN, 5);
        assertEquals(5, sale.quantity());
        assertTrue(sale.revenueInWen() > 0);
        assertEquals(0, farm.stockOf(TradeGood.GRAIN));
        assertThrows(IllegalArgumentException.class, () -> world.restockNpcBusiness("owner", "farm", TradeGood.MEAL, 1));
    }

    @Test void regionalTradeUsesLocalSupplyAndUpdatesTheMarket() {
        Npc owner = new Npc("owner", "掌柜", "居民", List.of(), 0);
        GameWorld world = new GameWorld(new Player("旅人"), new WorldClock(1, 8 * 60), List.of(owner));
        var inn = world.foundNpcBusiness("owner", "inn", "听雨客栈", BusinessType.INN, 100, 0, 0, 10000);
        long initialPrice = world.marketPrice(TradeGood.TEA, MarketRegion.JIANGNAN);
        assertTrue(world.marketPrice(TradeGood.TEA, MarketRegion.BEILING) > initialPrice);
        world.restockNpcBusiness("owner", "inn", TradeGood.TEA, 20, MarketRegion.JIANGNAN);
        assertEquals(180, world.regionalMarket(MarketRegion.JIANGNAN).stockOf(TradeGood.TEA));
        var receipt = world.sellNpcBusinessGoods("owner", "inn", TradeGood.TEA, 20, MarketRegion.BEILING);
        assertEquals(20, receipt.quantity());
        assertEquals(120, world.regionalMarket(MarketRegion.BEILING).stockOf(TradeGood.TEA));
    }

    @Test void merchantCaravanBuysTravelsAndSellsAtDestinationMarket() {
        Npc owner = new Npc("merchant", "行商", "居民", List.of(), 0);
        Npc guard = new Npc("guard", "护卫", "居民", List.of(), 0);
        GameWorld world = new GameWorld(new Player("旅人"), new WorldClock(1, 8 * 60), List.of(owner, guard));
        world.life("merchant").creditMoney(10000);
        var caravan = world.createTradeCaravan("merchant", "tea-run", MarketRegion.JIANGNAN, MarketRegion.BEILING);
        world.addTradeCaravanCrew("merchant", "tea-run", "guard");
        long cost = world.loadTradeCaravan("merchant", "tea-run", TradeGood.TEA, 10);
        assertTrue(cost > 0);
        int travelMinutes = world.departTradeCaravan("merchant", "tea-run");
        assertEquals(540, travelMinutes);
        world.advanceMinutes(travelMinutes);
        assertEquals(TradeCaravan.State.COMPLETED, caravan.state());
        assertTrue(world.life("merchant").moneyInWen() > 10500 - cost);
        assertTrue(world.life("merchant").memories().stream().anyMatch(m -> m.contains("净收益")));
        assertTrue(world.life("guard").memories().stream().anyMatch(m -> m.contains("净收益")));
    }
    @Test void caravanWeatherCausesDelayAndCrewMustBeAlive() {
        Npc owner = new Npc("merchant", "行商", "居民", List.of(), 0);
        Npc dead = new Npc("dead", "病者", "居民", List.of(), 0);
        GameWorld world = new GameWorld(new Player("旅人"), new WorldClock(1, 8 * 60), List.of(owner, dead));
        var caravan = world.createTradeCaravan("merchant", "storm-run", MarketRegion.JIANGNAN, MarketRegion.DONGHAI);
        world.recordNpcDeath("dead", "病逝");
        assertThrows(IllegalStateException.class, () -> world.addTradeCaravanCrew("merchant", "storm-run", "dead"));
        world.life("merchant").creditMoney(5000);
        world.loadTradeCaravan("merchant", "storm-run", TradeGood.TEA, 2);
        world.departTradeCaravan("merchant", "storm-run");
        world.setWeather("暴雨");
        world.advanceMinutes(960);
        assertEquals(TradeCaravan.State.COMPLETED, caravan.state());
        assertEquals(60, caravan.delayMinutes());
    }

}
