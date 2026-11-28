package com.jianghu.social;

import com.jianghu.npc.Npc;

/** 依据时辰、身份和关系生成可复现的基础对话，不使用随机文本掩盖状态。 */
public final class DialogueService {
    private DialogueService() {}
    public static String greeting(Npc npc, int hour, NpcRelationship relationship) {
        String time = hour >= 5 && hour < 11 ? "清早" : hour >= 11 && hour < 17 ? "白日" : hour >= 17 && hour < 20 ? "傍晚" : "夜深";
        String tone = relationship.favor() >= 20 ? "熟络地" : relationship.favor() <= -20 ? "冷淡地" : "平常地";
        String line = switch (npc.role()) {
            case "茶摊老板" -> hour >= 20 || hour < 6 ? "小店打烊了，明日再来喝茶吧。" : "客官，来碗热茶暖暖身子？";
            case "渔夫" -> hour >= 5 && hour < 10 ? "水面刚起风，正是下竿的好时候。" : "这条河的鱼啊，也认时辰。";
            default -> "江湖路远，少侠今日也要保重。";
        };
        return npc.name() + tone + "向你招呼：" + time + "，" + line;
    }
}
