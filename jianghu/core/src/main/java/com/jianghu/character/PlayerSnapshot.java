package com.jianghu.character;

import com.jianghu.combat.MartialArtStyle;
import com.jianghu.item.EquipmentItem;
import java.util.Map;
import java.util.Set;

/** 玩家完整可变状态快照；集合在创建时复制，避免存档内容被运行时继续修改。 */
public record PlayerSnapshot(
    String name, int health, int stamina, int hunger, int thirst, int morale, int reputation,
    long moneyInWen, int x, int y, Map<String, Integer> inventory,
    Map<EquipmentItem.Slot, String> equipment, int martialExperience, int martialLevel,
    int innerEnergy, MartialArtStyle martialStyle, Set<MartialArtStyle> learnedStyles,
    Set<String> learnedTechniques) {
    public PlayerSnapshot {
        if (name == null || name.isBlank() || health < 0 || health > 100 || stamina < 0 || stamina > 100
                || hunger < 0 || hunger > 100 || thirst < 0 || thirst > 100 || morale < 0 || morale > 100
                || reputation < -100 || reputation > 100 || moneyInWen < 0 || x < 0 || y < 0
                || martialExperience < 0 || martialLevel < 1 || martialLevel > 10
                || innerEnergy < 0 || innerEnergy > 100 || martialStyle == null)
            throw new IllegalArgumentException("玩家存档属性无效");
        inventory = Map.copyOf(inventory); equipment = Map.copyOf(equipment);
        learnedStyles = Set.copyOf(learnedStyles); learnedTechniques = Set.copyOf(learnedTechniques);
        if (!learnedStyles.contains(martialStyle)) throw new IllegalArgumentException("当前武学流派必须已被掌握");
        if (inventory.values().stream().anyMatch(count -> count == null || count <= 0))
            throw new IllegalArgumentException("背包数量无效");
    }
    public static PlayerSnapshot capture(Player player) {
        if (player == null) throw new IllegalArgumentException("玩家不能为空");
        return new PlayerSnapshot(player.name(), player.health(), player.stamina(), player.hunger(),
            player.thirst(), player.morale(), player.reputation(), player.moneyInWen(), player.x(), player.y(),
            player.inventory(), player.equipment(), player.martialExperience(), player.martialLevel(),
            player.innerEnergy(), player.martialStyle(), player.learnedStyles(), player.learnedTechniques());
    }
    public Player restore() {
        Player player = new Player(name);
        player.restoreSnapshot(this);
        return player;
    }
}