package com.jianghu.world;

import com.jianghu.character.Player;
import com.jianghu.combat.MartialArtStyle;
import com.jianghu.map.GameMap;
import com.jianghu.time.WorldClock;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MartialMasterTest {
    @Test void teacherCanOnlyBeUsedFromTheTrainingYard() {
        Player player = new Player("旅人");
        GameWorld world = new GameWorld(player, new WorldClock(1, 360), List.of());
        GameMap map = GameMap.starterTown();
        assertTrue(world.nearbyMartialMaster(map).isEmpty());
        player.moveTo(6, 12);
        assertEquals("顾长风", world.nearbyMartialMaster(map).orElseThrow().name());
        assertTrue(world.talkToMartialMaster(map).contains("按 M 拜师"));
    }

    @Test void studentPaysToLearnStyleAndItsTechnique() {
        Player player = new Player("旅人");
        GameWorld world = new GameWorld(player, new WorldClock(1, 360), List.of());
        GameMap map = GameMap.starterTown();
        player.moveTo(6, 12);
        long before = player.moneyInWen();
        assertTrue(world.learnFromMartialMaster(map).contains("剑法"));
        assertEquals(MartialArtStyle.SWORD, player.martialStyle());
        assertEquals(before - 120, player.moneyInWen());
        assertTrue(world.learnTechniqueFromMartialMaster(map).contains("流云剑式"));
        assertTrue(player.knowsTechnique("cloud_sword"));
        assertEquals(before - 300, player.moneyInWen());
    }

    @Test void techniqueCannotBeLearnedBeforeBecomingAStudent() {
        Player player = new Player("旅人");
        GameWorld world = new GameWorld(player, new WorldClock(1, 360), List.of());
        player.moveTo(6, 12);
        assertThrows(IllegalStateException.class, () -> world.learnTechniqueFromMartialMaster(GameMap.starterTown()));
    }
    @Test void dailyTrainingBuildsTrustAndUnlocksAdvancedSwordArt() {
        Player player = new Player("旅人");
        GameWorld world = new GameWorld(player, new WorldClock(1, 360), List.of());
        GameMap map = GameMap.starterTown();
        player.moveTo(6, 12);
        world.learnFromMartialMaster(map);
        world.learnTechniqueFromMartialMaster(map);
        int beforeExperience = player.martialExperience();
        for (int day = 0; day < 3; day++) {
            assertTrue(world.trainWithMartialMaster(map).contains("武学经验 +20"));
            if (day < 2) world.advanceMinutes(1440);
        }
        assertEquals(3, world.martialMasterFavor("master-river-sword"));
        assertEquals(beforeExperience + 60, player.martialExperience());
        long beforeMoney = player.moneyInWen();
        assertTrue(world.learnAdvancedTechniqueFromMartialMaster(map).contains("穿云破月"));
        assertTrue(player.knowsTechnique("cloud_piercing"));
        assertEquals(beforeMoney - 360, player.moneyInWen());
    }

    @Test void masterWillNotTrainTheStudentTwiceInOneDay() {
        Player player = new Player("旅人");
        GameWorld world = new GameWorld(player, new WorldClock(1, 360), List.of());
        GameMap map = GameMap.starterTown();
        player.moveTo(6, 12);
        world.learnFromMartialMaster(map);
        world.trainWithMartialMaster(map);
        assertThrows(IllegalStateException.class, () -> world.trainWithMartialMaster(map));
    }
}
