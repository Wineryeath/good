package com.jianghu.world;

import com.jianghu.business.TradeGood;
import com.jianghu.character.Player;
import com.jianghu.map.GameMap;
import com.jianghu.quest.Quest;
import com.jianghu.time.WorldClock;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class PlayerMarketQuestTest {
    @Test void playerCanBuyAndSellGoodsOnlyAtTheMarket() {
        Player player = new Player("行旅人");
        GameMap map = GameMap.starterTown();
        GameWorld world = new GameWorld(player, new WorldClock(1, 8 * 60), List.of());
        assertThrows(IllegalStateException.class, () -> world.buyFromMarket(map, TradeGood.HERB, 1));
        player.moveTo(4, 9);
        long price = world.marketPrice(TradeGood.HERB);
        long paid = world.buyFromMarket(map, TradeGood.HERB, 2);
        assertEquals(price * 2, paid);
        assertEquals(2, player.inventory().get("herb"));
        assertEquals(2_000 - paid, player.moneyInWen());
        long revenue = world.sellToMarket(map, TradeGood.HERB, 1);
        assertTrue(revenue > 0);
        assertEquals(1, player.inventory().get("herb"));
        assertEquals(2_000 - paid + revenue, player.moneyInWen());
        assertThrows(IllegalStateException.class, () -> world.sellToMarket(map, TradeGood.TEA, 1));
    }

    @Test void marketRejectsUnaffordablePurchaseWithoutChangingInventoryOrSupply() {
        Player player = new Player("穷旅人");
        player.addMoney(-1_990);
        player.moveTo(4, 9);
        GameWorld world = new GameWorld(player, new WorldClock(1, 8 * 60), List.of());
        int stock = world.regionalMarket(com.jianghu.business.MarketRegion.JIANGNAN).stockOf(TradeGood.MEDICINE);
        assertThrows(IllegalStateException.class, () -> world.buyFromMarket(GameMap.starterTown(), TradeGood.MEDICINE, 1));
        assertEquals(stock, world.regionalMarket(com.jianghu.business.MarketRegion.JIANGNAN).stockOf(TradeGood.MEDICINE));
        assertTrue(player.inventory().isEmpty());
        assertEquals(10, player.moneyInWen());
    }

    @Test void herbDeliveryQuestTracksGatheringAndPaysOnlyAfterTurnIn() {
        Player player = new Player("采药人");
        player.moveTo(9, 11);
        GameWorld world = new GameWorld(player, new WorldClock(1, 8 * 60), List.of());
        Quest quest = new Quest("herb-delivery", Map.of("herb", 3));
        world.offerQuest(quest, 180);
        world.acceptQuest("herb-delivery");
        for (int i = 0; i < 3; i++) {
            world.interactWithNearbyPoint(GameMap.starterTown()).orElseThrow();
            if (i < 2) world.advanceMinutes(24 * 60);
        }
        assertEquals(Quest.Status.READY_TO_TURN_IN, quest.status());
        assertEquals(3, player.inventory().get("herb"));
        long before = player.moneyInWen();
        assertEquals(180, world.turnInQuest("herb-delivery"));
        assertEquals(Quest.Status.COMPLETED, quest.status());
        assertFalse(player.inventory().containsKey("herb"));
        assertEquals(before + 180, player.moneyInWen());
        assertEquals(1, player.reputation());
        assertThrows(IllegalStateException.class, () -> world.turnInQuest("herb-delivery"));
    }

    @Test void questTurnInDoesNotConsumeItemsWhenInventoryIsInsufficient() {
        Player player = new Player("行旅人");
        GameWorld world = new GameWorld(player, new WorldClock(1, 8 * 60), List.of());
        Quest quest = new Quest("small-order", Map.of("herb", 2));
        world.offerQuest(quest, 50);
        world.acceptQuest("small-order");
        quest.record("herb", 2);
        player.addItem("herb", 1);
        assertThrows(IllegalStateException.class, () -> world.turnInQuest("small-order"));
        assertEquals(1, player.inventory().get("herb"));
        assertEquals(2_000, player.moneyInWen());
    }
}