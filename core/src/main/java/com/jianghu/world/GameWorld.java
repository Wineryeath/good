package com.jianghu.world;

import com.jianghu.character.Player;
import com.jianghu.npc.Npc;
import com.jianghu.time.WorldClock;
import java.util.List;

public final class GameWorld {
    private final Player player;
    private final WorldClock clock;
    private final List<Npc> npcs;
    private String weather = "晴";
    public GameWorld(Player player, WorldClock clock, List<Npc> npcs) {
        this.player = player; this.clock = clock; this.npcs = List.copyOf(npcs);
    }
    public Player player() { return player; }
    public WorldClock clock() { return clock; }
    public List<Npc> npcs() { return npcs; }
    public String weather() { return weather; }
    public void setWeather(String weather) { this.weather = weather; }
    public void waitMinutes(int minutes) {
        clock.advance(minutes);
        player.recoverStamina(Math.max(0, minutes / 10));
    }
}
