package com.jianghu.world;

import com.jianghu.character.Player;
import com.jianghu.npc.Npc;
import com.jianghu.time.WorldClock;
import com.jianghu.save.GameWorldSaveService;
import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameWorldSaveServiceTest {
    private GameWorld world(long day, int minute) {
        GameWorld world = new GameWorld(new Player("存档侠客"), new WorldClock(day, minute), List.of(
            new Npc("npc-inn", "周掌柜", "客栈老板", List.of(new Npc.Schedule(6, 20, "inn", 0)), 0),
            new Npc("npc-worker", "阿福", "伙计", List.of(new Npc.Schedule(6, 20, "inn", 0)), 0),
            new Npc("npc-child", "小满", "学徒", List.of(new Npc.Schedule(6, 20, "home", 0)), 0)));
        world.offerQuest(new com.jianghu.quest.Quest("herb-delivery", java.util.Map.of("herb", 3)), 180);
        return world;
    }
    @Test void defeatedPlayerCanRecoverAtTheTownClinic() {
        GameWorld defeated = world(1, 360);
        defeated.player().changeHealth(-100);
        defeated.player().spendStamina(100);
        long moneyBefore = defeated.player().moneyInWen();
        String message = defeated.recoverFromDefeat();
        assertEquals(35, defeated.player().health());
        assertEquals(30, defeated.player().stamina());
        assertEquals(moneyBefore - 200, defeated.player().moneyInWen());
        assertEquals(9, defeated.player().x());
        assertEquals(9, defeated.player().y());
        assertTrue(message.contains("医馆"));
        assertThrows(IllegalStateException.class, defeated::recoverFromDefeat);
    }

    @Test void worldSaveRestoresPlayerNpcLifeRelationshipsAndCalendar() throws Exception {
        GameWorld original = world(2, 350);
        original.player().changeHealth(-18); original.player().addItem("herb", 5); original.player().moveTo(12, 7);
        original.overworld().move(1, 0);
        original.player().moveTo(9, 11);
        assertTrue(original.interactWithNearbyPoint(com.jianghu.map.GameMap.starterTown()).orElseThrow().contains("采得草药"));
        original.setWeather("连绵细雨");
        original.acceptQuest("herb-delivery"); original.quest("herb-delivery").orElseThrow().record("herb", 1);
        original.life("npc-inn").changeMood(13); original.life("npc-inn").creditMoney(260);
        original.life("npc-inn").remember("收到旅人赠予的草药");
        original.relationship("npc-inn").talk(true); original.relationship("npc-inn").changeTrust(4);
        original.marryNpcs("npc-inn", "npc-worker"); original.addNpcChild("npc-inn", "npc-child");
        original.socialGraph().connect("npc-worker", "npc-child", com.jianghu.social.NpcSocialGraph.RelationType.DISCIPLE, 32);
        var inn = original.foundNpcBusiness("npc-inn", "family-inn", "周家客栈", com.jianghu.business.BusinessType.INN, 120, 15, 8, 3000);
        inn.restock(com.jianghu.business.TradeGood.MEAL, 4, 25); inn.hire("npc-worker");
        original.regionalMarket(com.jianghu.business.MarketRegion.JIANGNAN).buy(7, com.jianghu.business.TradeGood.HERB);
        var caravan = original.createTradeCaravan("npc-inn", "caravan-east", com.jianghu.business.MarketRegion.JIANGNAN, com.jianghu.business.MarketRegion.DONGHAI);
        original.addTradeCaravanCrew("npc-inn", "caravan-east", "npc-worker");
        caravan.load(com.jianghu.business.TradeGood.HERB, 5, 500); caravan.depart(500);
        original.advanceMinutes(70);
        var directory = Files.createTempDirectory("jianghu-world-save");
        var file = directory.resolve("world-1.sav");
        var service = new GameWorldSaveService();
        service.save(file, original);

        GameWorld restored = world(1, 0);
        restored.restoreSnapshot(service.load(file));
        assertEquals(original.player().health(), restored.player().health());
        assertEquals(original.player().inventory(), restored.player().inventory());
        assertEquals(original.player().x(), restored.player().x());
        assertEquals(original.overworld().regionId(), restored.overworld().regionId());
        assertEquals(original.overworld().x(), restored.overworld().x()); assertEquals(original.overworld().y(), restored.overworld().y());
        assertEquals(original.clock().day(), restored.clock().day());
        assertEquals(original.clock().minuteOfDay(), restored.clock().minuteOfDay());
        assertEquals(original.weather(), restored.weather());
        assertEquals(original.snapshot().resourceHarvestDays(), restored.snapshot().resourceHarvestDays());
        assertTrue(restored.interactWithNearbyPoint(com.jianghu.map.GameMap.starterTown()).orElseThrow().contains("今日已经采过"));
        assertEquals(original.life("npc-inn").mood(), restored.life("npc-inn").mood());
        assertEquals(original.life("npc-inn").moneyInWen(), restored.life("npc-inn").moneyInWen());
        assertEquals(original.life("npc-inn").memories(), restored.life("npc-inn").memories());
        assertEquals(original.relationship("npc-inn").favor(), restored.relationship("npc-inn").favor());
        assertEquals(original.relationship("npc-inn").trust(), restored.relationship("npc-inn").trust());
        assertEquals(original.familyRegistry().spouseOf("npc-inn"), restored.familyRegistry().spouseOf("npc-inn"));
        assertEquals(new java.util.HashSet<>(original.familyRegistry().parentsOf("npc-child")), new java.util.HashSet<>(restored.familyRegistry().parentsOf("npc-child")));
        assertEquals(original.socialGraph().relation("npc-worker", "npc-child"), restored.socialGraph().relation("npc-worker", "npc-child"));
        var restoredInn = restored.npcBusiness("family-inn").orElseThrow();
        assertEquals(inn.treasuryInWen(), restoredInn.treasuryInWen());
        assertEquals(inn.inventory(), restoredInn.inventory());
        assertEquals(inn.inventoryCostBasis(), restoredInn.inventoryCostBasis());
        assertEquals(original.regionalMarket(com.jianghu.business.MarketRegion.JIANGNAN).stock(),
            restored.regionalMarket(com.jianghu.business.MarketRegion.JIANGNAN).stock());
        assertEquals(original.regionalMarket(com.jianghu.business.MarketRegion.JIANGNAN).demand(),
            restored.regionalMarket(com.jianghu.business.MarketRegion.JIANGNAN).demand());
        assertEquals(original.quest("herb-delivery").orElseThrow().progress(), restored.quest("herb-delivery").orElseThrow().progress());
        assertEquals(original.quest("herb-delivery").orElseThrow().status(), restored.quest("herb-delivery").orElseThrow().status());
        var restoredCaravan = restored.tradeCaravan("caravan-east").orElseThrow();
        assertEquals(caravan.state(), restoredCaravan.state()); assertEquals(caravan.remainingMinutes(), restoredCaravan.remainingMinutes());
        assertEquals(caravan.crew(), restoredCaravan.crew()); assertEquals(caravan.cargo(), restoredCaravan.cargo());
        Files.deleteIfExists(file); Files.deleteIfExists(directory);
    }
}