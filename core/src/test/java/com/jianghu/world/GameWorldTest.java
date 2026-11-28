package com.jianghu.world;

import com.jianghu.character.Player;
import com.jianghu.map.GameMap;
import com.jianghu.npc.Npc;
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
        player.moveTo(17,8);
        GameWorld world = new GameWorld(player,new WorldClock(1,7*60),List.of(fisherman));
        assertEquals("fisher",world.nearbyNpc(GameMap.starterTown()).orElseThrow().npcId());
    }
}
