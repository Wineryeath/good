package com.jianghu.combat;

import java.util.Arrays;
import java.util.Optional;

/** 可学习的门派招式。劲力是额外伤害，内力与体力分别支付。 */
public enum MartialTechnique {
    BASIC_PALM("basic_palm", "基础掌法", MartialArtStyle.FIST, 2, 8, 0, "以掌劲直取中门，入门即可施展。"),
    IRON_PALM("iron_palm", "铁砂掌", MartialArtStyle.FIST, 8, 12, 18, "沉肩运劲，掌力刚猛。"),
    CLOUD_SWORD("cloud_sword", "流云剑式", MartialArtStyle.SWORD, 10, 12, 20, "剑随身转，连绵不绝。"),
    CLOUD_PIERCING("cloud_piercing", "穿云破月", MartialArtStyle.SWORD, 16, 18, 30, "凝全身劲力于剑尖，一线破空。"),
    WIND_SABER("wind_saber", "断风刀", MartialArtStyle.SABER, 13, 16, 24, "刀势如风，重斩破敌。"),
    PIERCING_SPEAR("piercing_spear", "穿云枪", MartialArtStyle.SPEAR, 11, 14, 22, "凝劲于一点，直线突刺。"),
    SWEEPING_STAFF("sweeping_staff", "横江棍", MartialArtStyle.STAFF, 7, 10, 16, "横扫卸力，攻守兼备。");
    private final String id, name, description;
    private final MartialArtStyle style;
    private final int power, staminaCost, innerCost;
    MartialTechnique(String id,String name,MartialArtStyle style,int power,int staminaCost,int innerCost,String description){
        this.id=id;this.name=name;this.style=style;this.power=power;this.staminaCost=staminaCost;this.innerCost=innerCost;this.description=description;
    }
    public String id(){return id;} public String displayName(){return name;} public MartialArtStyle style(){return style;}
    public int power(){return power;} public int staminaCost(){return staminaCost;} public int innerCost(){return innerCost;}
    public String description(){return description;}
    public static Optional<MartialTechnique> find(String id){return Arrays.stream(values()).filter(t->t.id.equals(id)).findFirst();}
}