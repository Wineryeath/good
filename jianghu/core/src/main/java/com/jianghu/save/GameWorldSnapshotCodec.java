package com.jianghu.save;

import com.jianghu.npc.NpcLifeSnapshot;
import com.jianghu.business.NpcBusinessSnapshot;
import com.jianghu.business.NpcBusiness;
import com.jianghu.business.BusinessType;
import com.jianghu.business.TradeGood;
import com.jianghu.business.MarketRegion;
import com.jianghu.business.RegionalMarketSnapshot;
import com.jianghu.quest.Quest;
import com.jianghu.quest.QuestSnapshot;
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
        int bi = 0; put(out, "world.business.count", s.businesses().size());
        for (var entry : s.businesses().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            String p = "world.business." + bi++ + "."; var b = entry.getValue();
            out.put(p + "id", b64(b.id())); out.put(p + "name", b64(b.name())); out.put(p + "type", b.type().name()); out.put(p + "owner", b64(b.ownerNpcId()));
            put(out,p+"dailyIncome",b.dailyIncomeInWen()); put(out,p+"dailyCost",b.dailyOperatingCostInWen()); put(out,p+"dailyWage",b.dailyWagePerEmployeeInWen());
            put(out,p+"treasury",b.treasuryInWen()); put(out,p+"debt",b.debtInWen()); put(out,p+"retained",b.retainedProfitInWen());
            put(out,p+"settledDays",b.settledDays()); put(out,p+"level",b.level()); putSet(out,p+"employees",b.employees());
            put(out,p+"inventory.count",b.inventory().size()); int gi=0;
            for (var stock : b.inventory().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
                put(out,p+"inventory."+gi+".good",stock.getKey().name()); put(out,p+"inventory."+gi+".quantity",stock.getValue());
                put(out,p+"inventory."+gi+".basis",b.inventoryCostBasis().getOrDefault(stock.getKey(),0L)); gi++;
            }
            put(out,p+"hasReport",b.lastReport()!=null);
            if (b.lastReport()!=null) { var r=b.lastReport(); put(out,p+"report.revenue",r.revenueInWen()); put(out,p+"report.operating",r.operatingCostInWen());
                put(out,p+"report.payroll",r.payrollInWen()); put(out,p+"report.profit",r.netProfitInWen()); put(out,p+"report.debt",r.debtAfterInWen());
                put(out,p+"report.treasury",r.treasuryAfterInWen()); put(out,p+"report.level",r.level()); put(out,p+"report.adverse",r.adverseDay()); }
        }
        put(out,"world.market.count",s.markets().size()); int mi=0;
        for (var entry : s.markets().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            String p="world.market."+mi++ +"."; put(out,p+"region",entry.getKey().name());
            for (TradeGood good : TradeGood.values()) { put(out,p+good.name()+".stock",entry.getValue().stock().get(good)); put(out,p+good.name()+".demand",entry.getValue().demand().get(good)); }
        }
        put(out,"world.quest.count",s.quests().size()); int qi=0;
        for(var entry:s.quests().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()){
            String p="world.quest."+qi++ +"."; QuestSnapshot q=entry.getValue(); out.put(p+"id",b64(q.id())); put(out,p+"status",q.status().name());
            putIntMap(out,p+"objectives",q.objectives()); putIntMap(out,p+"progress",q.progress());
        }
        putLongMap(out,"world.questRewards",s.questRewards());
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
        Map<String, NpcBusinessSnapshot> businesses = new LinkedHashMap<>();
        for (int i=0;i<count(data,"world.business.count");i++) {
            String p="world.business."+i+"."; Map<TradeGood,Integer> stock=new LinkedHashMap<>(); Map<TradeGood,Long> basis=new LinkedHashMap<>();
            for(int j=0;j<count(data,p+"inventory.count");j++){String q=p+"inventory."+j+".";TradeGood g=TradeGood.valueOf(req(data,q+"good"));stock.put(g,num(data,q+"quantity"));basis.put(g,longNum(data,q+"basis"));}
            NpcBusiness.DailyReport report=null;
            if(bool(data,p+"hasReport")) report=new NpcBusiness.DailyReport(longNum(data,p+"report.revenue"),longNum(data,p+"report.operating"),
                longNum(data,p+"report.payroll"),longNum(data,p+"report.profit"),longNum(data,p+"report.debt"),longNum(data,p+"report.treasury"),
                num(data,p+"report.level"),bool(data,p+"report.adverse"));
            String id=unb64(req(data,p+"id"));
            businesses.put(id,new NpcBusinessSnapshot(id,unb64(req(data,p+"name")),BusinessType.valueOf(req(data,p+"type")),unb64(req(data,p+"owner")),
                longNum(data,p+"dailyIncome"),longNum(data,p+"dailyCost"),longNum(data,p+"dailyWage"),longNum(data,p+"treasury"),
                longNum(data,p+"debt"),longNum(data,p+"retained"),longNum(data,p+"settledDays"),num(data,p+"level"),
                readSet(data,p+"employees"),stock,basis,report));
        }
        Map<MarketRegion,RegionalMarketSnapshot> markets=new LinkedHashMap<>();
        for(int i=0;i<count(data,"world.market.count");i++){String p="world.market."+i+".";MarketRegion region=MarketRegion.valueOf(req(data,p+"region"));
            Map<TradeGood,Integer> stock=new LinkedHashMap<>(),demand=new LinkedHashMap<>();
            for(TradeGood good:TradeGood.values()){stock.put(good,num(data,p+good.name()+".stock"));demand.put(good,num(data,p+good.name()+".demand"));}
            markets.put(region,new RegionalMarketSnapshot(region,stock,demand));}
        Map<String,QuestSnapshot> quests=new LinkedHashMap<>();
        for(int i=0;i<count(data,"world.quest.count");i++){String p="world.quest."+i+".";String id=unb64(req(data,p+"id"));
            quests.put(id,new QuestSnapshot(id,readIntMap(data,p+"objectives"),readIntMap(data,p+"progress"),Quest.Status.valueOf(req(data,p+"status"))));}
        return new GameWorldSnapshot(player, longNum(data,"world.day"), num(data,"world.minute"), unb64(req(data,"world.weather")),
            lives, relations, businesses, markets, quests, readLongMap(data,"world.questRewards"), longNum(data,"world.lastAgedDay"), longNum(data,"world.lastBusinessDay"),
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