package com.jianghu.combat;

/** 五类江湖武学路数；招式风格会改变角色的攻防倾向。 */
public enum MartialArtStyle {
    FIST("fist", "拳掌", 1, 1, "刚柔并济，近身制敌，攻守均衡。"),
    SWORD("sword", "剑法", 3, 0, "剑走轻灵，出手迅捷，偏重进攻。"),
    SABER("saber", "刀法", 4, -1, "刀势沉猛，以攻代守。"),
    SPEAR("spear", "枪法", 2, 1, "长兵守中带攻，擅长稳扎稳打。"),
    STAFF("staff", "棍法", 1, 3, "棍影绵密，借势卸力，长于防守。");
    private final String id, name, description;
    private final int attackBonus, defenseBonus;
    MartialArtStyle(String id, String name, int attackBonus, int defenseBonus, String description) {
        this.id=id; this.name=name; this.attackBonus=attackBonus; this.defenseBonus=defenseBonus; this.description=description;
    }
    public String id(){return id;} public String name(){return name;} public int attackBonus(){return attackBonus;}
    public int defenseBonus(){return defenseBonus;} public String description(){return description;}
}