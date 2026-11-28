package com.jianghu.save;

import com.jianghu.character.PlayerSnapshot;
import com.jianghu.combat.MartialArtStyle;
import com.jianghu.item.EquipmentItem;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.EnumMap;

/** 将玩家快照编码为可读版本化键值数据，不依赖 Java 原生对象反序列化。 */
public final class PlayerSnapshotCodec {
    private PlayerSnapshotCodec() {}
    public static Map<String, String> encode(PlayerSnapshot p) {
        Map<String, String> out = new LinkedHashMap<>();
        out.put("player.name", b64(p.name()));
        out.put("player.health", Integer.toString(p.health())); out.put("player.stamina", Integer.toString(p.stamina()));
        out.put("player.hunger", Integer.toString(p.hunger())); out.put("player.thirst", Integer.toString(p.thirst()));
        out.put("player.morale", Integer.toString(p.morale())); out.put("player.reputation", Integer.toString(p.reputation()));
        out.put("player.money", Long.toString(p.moneyInWen())); out.put("player.x", Integer.toString(p.x()));
        out.put("player.y", Integer.toString(p.y())); out.put("player.martialExperience", Integer.toString(p.martialExperience()));
        out.put("player.martialLevel", Integer.toString(p.martialLevel())); out.put("player.innerEnergy", Integer.toString(p.innerEnergy()));
        out.put("player.martialStyle", p.martialStyle().name());
        int i = 0; out.put("player.inventory.count", Integer.toString(p.inventory().size()));
        for (var entry : p.inventory().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            out.put("player.inventory." + i + ".id", b64(entry.getKey()));
            out.put("player.inventory." + i + ".count", Integer.toString(entry.getValue())); i++;
        }
        i = 0; out.put("player.equipment.count", Integer.toString(p.equipment().size()));
        for (var entry : p.equipment().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            out.put("player.equipment." + i + ".slot", entry.getKey().name());
            out.put("player.equipment." + i + ".id", b64(entry.getValue())); i++;
        }
        i = 0; out.put("player.styles.count", Integer.toString(p.learnedStyles().size()));
        for (MartialArtStyle style : p.learnedStyles().stream().sorted().toList()) out.put("player.styles." + i++, style.name());
        i = 0; out.put("player.techniques.count", Integer.toString(p.learnedTechniques().size()));
        for (String technique : p.learnedTechniques().stream().sorted().toList()) out.put("player.techniques." + i++, b64(technique));
        return Map.copyOf(out);
    }
    public static PlayerSnapshot decode(Map<String, String> data) {
        String name = unb64(required(data, "player.name"));
        Map<String, Integer> inventory = new LinkedHashMap<>();
        for (int i = 0; i < count(data, "player.inventory.count"); i++)
            inventory.put(unb64(required(data, "player.inventory." + i + ".id")), integer(data, "player.inventory." + i + ".count"));
        Map<EquipmentItem.Slot, String> equipment = new EnumMap<>(EquipmentItem.Slot.class);
        for (int i = 0; i < count(data, "player.equipment.count"); i++)
            equipment.put(EquipmentItem.Slot.valueOf(required(data, "player.equipment." + i + ".slot")),
                unb64(required(data, "player.equipment." + i + ".id")));
        Set<MartialArtStyle> styles = new LinkedHashSet<>();
        for (int i = 0; i < count(data, "player.styles.count"); i++) styles.add(MartialArtStyle.valueOf(required(data, "player.styles." + i)));
        Set<String> techniques = new LinkedHashSet<>();
        for (int i = 0; i < count(data, "player.techniques.count"); i++) techniques.add(unb64(required(data, "player.techniques." + i)));
        return new PlayerSnapshot(name, integer(data, "player.health"), integer(data, "player.stamina"),
            integer(data, "player.hunger"), integer(data, "player.thirst"), integer(data, "player.morale"),
            integer(data, "player.reputation"), number(data, "player.money"), integer(data, "player.x"),
            integer(data, "player.y"), inventory, equipment, integer(data, "player.martialExperience"),
            integer(data, "player.martialLevel"), integer(data, "player.innerEnergy"),
            MartialArtStyle.valueOf(required(data, "player.martialStyle")), styles, techniques);
    }
    private static String b64(String value) { return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8)); }
    private static String unb64(String value) { return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8); }
    private static String required(Map<String, String> data, String key) {
        String value = data.get(key); if (value == null) throw new IllegalArgumentException("存档缺少字段：" + key); return value;
    }
    private static int integer(Map<String, String> data, String key) { return Integer.parseInt(required(data, key)); }
    private static long number(Map<String, String> data, String key) { return Long.parseLong(required(data, key)); }
    private static int count(Map<String, String> data, String key) {
        int value = integer(data, key); if (value < 0 || value > 10000) throw new IllegalArgumentException("存档集合长度无效：" + key); return value;
    }
}