package com.jianghu.save;

import com.jianghu.npc.NpcLifeSnapshot;
import com.jianghu.social.NpcRelationshipSnapshot;
import com.jianghu.world.GameWorldSnapshot;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 世界核心快照编解码器；字符串采用 Base64 URL 编码，避免名称与记忆破坏键值格式。 */
public final class GameWorldSnapshotCodec {
    private GameWorldSnapshotCodec() {}
    public static Map<String, String> encode(GameWorldSnapshot s) {
        Map<String, String> out = new LinkedHashMap<>(PlayerSnapshotCodec.encode(s.player()));
        put(out, "world.day", s.day()); put(out, "world.minute", s.minuteOfDay()); put(out, "world.weather", b64(s.weather()));
        put(out, "world.lastAgedDay", s.lastAgedDay()); put(out, "world.lastBusinessDay", s.lastBusinessDay());
        int i = 0; out.put("world.npc.count", Integer.toString(s.npcLives().size()));
        for (var entry : s.npcLives().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            String p = "world.npc." + i++ + "."; NpcLifeSnapshot n = entry.getValue();
            out.put(p + "id", b64(entry.getKey())); put(out, p + "age", n.ageYears()); put(out, p + "days", n.daysLivedThisYear());
            put(out, p + "ageInitialized", n.ageInitialized()); put(out, p + "hunger", n.hunger()); put(out, p + "energy", n.energy());
            put(out, p + "mood", n.mood()); put(out, p + "education", n.education()); put(out, p + "hasProfession", n.profession() != null);
            if (n.profession() != null) out.put(p + "profession", b64(n.profession()));
            put(out, p + "money", n.moneyInWen()); put(out, p + "health", n.health()); put(out, p + "alive", n.alive());
            put(out, p + "estateTransferred", n.estateTransferred()); put(out, p + "hasIllness", n.illness() != null);
            if (n.illness() != null) out.put(p + "illness", b64(n.illness())); put(out, p + "illnessSeverity", n.illnessSeverity());
            put(out, p + "training.count", n.vocationalTraining().size()); int j = 0;
            for (var t : n.vocationalTraining().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
                out.put(p + "training." + j + ".name", b64(t.getKey())); put(out, p + "training." + j + ".points", t.getValue()); j++;
            }
            put(out, p + "memory.count", n.memories().size());
            for (j = 0; j < n.memories().size(); j++) out.put(p + "memory." + j, b64(n.memories().get(j)));
            NpcRelationshipSnapshot r = s.relationships().get(entry.getKey());
            if (r == null) throw new IllegalArgumentException("NPC 关系快照缺失：" + entry.getKey());
            put(out, p + "favor", r.favor()); put(out, p + "trust", r.trust()); put(out, p + "conversations", r.conversations());
        }
        putSet(out, "world.deathRecorded", s.deathRecorded()); putLongMap(out, "world.lastSocialDay", s.lastSocialDay());
        putStringMap(out, "world.households", s.independentHouseholds()); putIntMap(out, "world.courtshipDays", s.courtshipDays());
        putLongMap(out, "world.masterTrainingDays", s.masterTrainingDays()); putIntMap(out, "world.masterFavor", s.masterFavor());
        return Map.copyOf(out);
    }
    public static GameWorldSnapshot decode(Map<String, String> data) {
        var player = PlayerSnapshotCodec.decode(data);
        Map<String, NpcLifeSnapshot> lives = new LinkedHashMap<>();
        Map<String, NpcRelationshipSnapshot> relations = new LinkedHashMap<>();
        for (int i = 0; i < count(data, "world.npc.count"); i++) {
            String p = "world.npc." + i + "."; String id = unb64(req(data, p + "id"));
            Map<String, Integer> training = new LinkedHashMap<>();
            for (int j = 0; j < count(data, p + "training.count"); j++)
                training.put(unb64(req(data, p + "training." + j + ".name")), num(data, p + "training." + j + ".points"));
            List<String> memories = new ArrayList<>();
            for (int j = 0; j < count(data, p + "memory.count"); j++) memories.add(unb64(req(data, p + "memory." + j)));
            String profession = bool(data, p + "hasProfession") ? unb64(req(data, p + "profession")) : null;
            String illness = bool(data, p + "hasIllness") ? unb64(req(data, p + "illness")) : null;
            lives.put(id, new NpcLifeSnapshot(num(data,p+"age"), num(data,p+"days"), bool(data,p+"ageInitialized"),
                num(data,p+"hunger"), num(data,p+"energy"), num(data,p+"mood"), num(data,p+"education"), profession,
                training, longNum(data,p+"money"), num(data,p+"health"), bool(data,p+"alive"),
                bool(data,p+"estateTransferred"), illness, num(data,p+"illnessSeverity"), memories));
            relations.put(id, new NpcRelationshipSnapshot(num(data,p+"favor"), num(data,p+"trust"), num(data,p+"conversations")));
        }
        return new GameWorldSnapshot(player, longNum(data,"world.day"), num(data,"world.minute"), unb64(req(data,"world.weather")),
            lives, relations, longNum(data,"world.lastAgedDay"), longNum(data,"world.lastBusinessDay"),
            readSet(data,"world.deathRecorded"), readLongMap(data,"world.lastSocialDay"), readStringMap(data,"world.households"),
            readIntMap(data,"world.courtshipDays"), readLongMap(data,"world.masterTrainingDays"), readIntMap(data,"world.masterFavor"));
    }
    private static void put(Map<String,String> m,String k,Object v){m.put(k,String.valueOf(v));}
    private static String b64(String s){return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes(StandardCharsets.UTF_8));}
    private static String unb64(String s){return new String(Base64.getUrlDecoder().decode(s),StandardCharsets.UTF_8);}
    private static String req(Map<String,String>d,String k){String v=d.get(k);if(v==null)throw new IllegalArgumentException("存档缺少字段："+k);return v;}
    private static int num(Map<String,String>d,String k){return Integer.parseInt(req(d,k));}
    private static long longNum(Map<String,String>d,String k){return Long.parseLong(req(d,k));}
    private static boolean bool(Map<String,String>d,String k){return Boolean.parseBoolean(req(d,k));}
    private static int count(Map<String,String>d,String k){int n=num(d,k);if(n<0||n>10000)throw new IllegalArgumentException("存档集合长度无效："+k);return n;}
    private static void putSet(Map<String,String>m,String p,Set<String>s){put(m,p+".count",s.size());int i=0;for(String v:s.stream().sorted().toList())m.put(p+"."+i++,b64(v));}
    private static Set<String> readSet(Map<String,String>d,String p){Set<String>s=new LinkedHashSet<>();for(int i=0;i<count(d,p+".count");i++)s.add(unb64(req(d,p+"."+i)));return s;}
    private static void putStringMap(Map<String,String>m,String p,Map<String,String>v){put(m,p+".count",v.size());int i=0;for(var e:v.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()){m.put(p+"."+i+".key",b64(e.getKey()));m.put(p+"."+i+".value",b64(e.getValue()));i++;}}
    private static Map<String,String> readStringMap(Map<String,String>d,String p){Map<String,String>m=new LinkedHashMap<>();for(int i=0;i<count(d,p+".count");i++)m.put(unb64(req(d,p+"."+i+".key")),unb64(req(d,p+"."+i+".value")));return m;}
    private static void putLongMap(Map<String,String>m,String p,Map<String,Long>v){put(m,p+".count",v.size());int i=0;for(var e:v.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()){m.put(p+"."+i+".key",b64(e.getKey()));put(m,p+"."+i+".value",e.getValue());i++;}}
    private static Map<String,Long> readLongMap(Map<String,String>d,String p){Map<String,Long>m=new LinkedHashMap<>();for(int i=0;i<count(d,p+".count");i++)m.put(unb64(req(d,p+"."+i+".key")),longNum(d,p+"."+i+".value"));return m;}
    private static void putIntMap(Map<String,String>m,String p,Map<String,Integer>v){put(m,p+".count",v.size());int i=0;for(var e:v.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()){m.put(p+"."+i+".key",b64(e.getKey()));put(m,p+"."+i+".value",e.getValue());i++;}}
    private static Map<String,Integer> readIntMap(Map<String,String>d,String p){Map<String,Integer>m=new LinkedHashMap<>();for(int i=0;i<count(d,p+".count");i++)m.put(unb64(req(d,p+"."+i+".key")),num(d,p+"."+i+".value"));return m;}
}