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
        return new GameWorld(new Player("存档侠客"), new WorldClock(day, minute), List.of(
            new Npc("npc-inn", "周掌柜", "客栈老板", List.of(new Npc.Schedule(6, 20, "inn", 0)), 0)));
    }
    @Test void worldSaveRestoresPlayerNpcLifeRelationshipsAndCalendar() throws Exception {
        GameWorld original = world(2, 350);
        original.player().changeHealth(-18); original.player().addItem("herb", 5); original.player().moveTo(12, 7);
        original.setWeather("连绵细雨");
        original.life("npc-inn").changeMood(13); original.life("npc-inn").creditMoney(260);
        original.life("npc-inn").remember("收到旅人赠予的草药");
        original.relationship("npc-inn").talk(true); original.relationship("npc-inn").changeTrust(4);
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
        assertEquals(original.clock().day(), restored.clock().day());
        assertEquals(original.clock().minuteOfDay(), restored.clock().minuteOfDay());
        assertEquals(original.weather(), restored.weather());
        assertEquals(original.life("npc-inn").mood(), restored.life("npc-inn").mood());
        assertEquals(original.life("npc-inn").moneyInWen(), restored.life("npc-inn").moneyInWen());
        assertEquals(original.life("npc-inn").memories(), restored.life("npc-inn").memories());
        assertEquals(original.relationship("npc-inn").favor(), restored.relationship("npc-inn").favor());
        assertEquals(original.relationship("npc-inn").trust(), restored.relationship("npc-inn").trust());
        Files.deleteIfExists(file); Files.deleteIfExists(directory);
    }
}