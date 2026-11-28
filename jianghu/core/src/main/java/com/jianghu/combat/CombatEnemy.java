package com.jianghu.combat;

/** 一场遭遇战中的敌人；模板数值与当前生命值一并由遭遇战持有。 */
public final class CombatEnemy {
    private final String id, name, description, dropItem;
    private final int maxHealth, attack, defense, rewardWen, experienceReward, dropCount;
    private int health;
    public CombatEnemy(String id, String name, String description, int health, int attack, int defense,
                       int rewardWen, int experienceReward, String dropItem, int dropCount) {
        if (id == null || id.isBlank() || name == null || name.isBlank() || health <= 0 || attack < 0
                || defense < 0 || rewardWen < 0 || experienceReward < 0 || dropCount < 0)
            throw new IllegalArgumentException("敌人属性无效");
        this.id=id; this.name=name; this.description=description == null ? "" : description;
        this.maxHealth=health; this.health=health; this.attack=attack; this.defense=defense;
        this.rewardWen=rewardWen; this.experienceReward=experienceReward;
        this.dropItem=dropItem; this.dropCount=dropCount;
    }
    public String id(){return id;} public String name(){return name;} public String description(){return description;}
    public int health(){return health;} public int maxHealth(){return maxHealth;} public int attack(){return attack;} public int defense(){return defense;}
    public int rewardWen(){return rewardWen;} public int experienceReward(){return experienceReward;}
    public String dropItem(){return dropItem;} public int dropCount(){return dropCount;}
    public void takeDamage(int amount){if(amount<0)throw new IllegalArgumentException("伤害不能为负");health=Math.max(0,health-amount);}
    public boolean defeated(){return health==0;}
}