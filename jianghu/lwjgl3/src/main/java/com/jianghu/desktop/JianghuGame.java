package com.jianghu.desktop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.jianghu.character.Player;
import com.jianghu.map.GameMap;
import com.jianghu.npc.Npc;
import com.jianghu.time.WorldClock;
import com.jianghu.world.GameWorld;
import java.util.List;

public final class JianghuGame extends ApplicationAdapter {
    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont font;
    private GameWorld world;
    private GameMap map;
    private float moveCooldown;
    private String interactionMessage = "沿着河埠慢慢探索，留意镇中人物的作息。";
    private boolean inventoryOpen;

    @Override public void create() {
        shapes = new ShapeRenderer(); batch = new SpriteBatch(); font = new BitmapFont();
        map = GameMap.starterTown();
        world = new GameWorld(new Player("无名旅人"), new WorldClock(1, 360), List.of(
            new Npc("npc-tea-owner", "沈掌柜", "茶摊老板", List.of(
                new Npc.Schedule(6,11,"tea-stall",0), new Npc.Schedule(11,14,"market-west",0),
                new Npc.Schedule(14,20,"tea-stall",0), new Npc.Schedule(20,6,"home",0)), 0),
            new Npc("npc-old-fisher", "陆老七", "渔夫", List.of(
                new Npc.Schedule(5,10,"east-bank",0), new Npc.Schedule(10,16,"river-hut",0),
                new Npc.Schedule(16,19,"east-bank",0), new Npc.Schedule(19,5,"home",0)), 5)
        ));
        world.offerQuest(new com.jianghu.quest.Quest("herb-delivery", java.util.Map.of("herb", 3)), 180);
    }

    @Override public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        moveCooldown -= delta;
        if (moveCooldown <= 0) {
            int dx = 0, dy = 0;
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) dx = -1;
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) dx = 1;
            if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) dy = 1;
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) dy = -1;
            if (dx != 0 || dy != 0) {
                int nx = world.player().x() + dx;
                int ny = world.player().y() + dy;
                if (map.isWalkable(nx, ny)) { world.player().moveTo(nx, ny); world.advanceMinutes(2); }
                moveCooldown = .12f;
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) world.waitMinutes(30);
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) inventoryOpen = !inventoryOpen;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) useItem("meal");
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) useItem("tea");
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) useItem("medicine");
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) useItem("herb");
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) equipItem("iron_sword");
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)) equipItem("leather_armor");
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) startCombat();
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            try { interactionMessage = world.meditate(); }
            catch (IllegalStateException ex) { interactionMessage = ex.getMessage(); }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            try { interactionMessage = world.learnFromMartialMaster(map); }
            catch (IllegalStateException | IllegalArgumentException ex) { interactionMessage = ex.getMessage(); }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            try { interactionMessage = world.learnTechniqueFromMartialMaster(map); }
            catch (IllegalStateException | IllegalArgumentException ex) { interactionMessage = ex.getMessage(); }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_7)) useTechnique("basic_palm");
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_8)) useTechniqueForCurrentStyle();
        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) combatAction(false);
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) combatAction(true);
        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) craft("medicine");
        if (Gdx.input.isKeyJustPressed(Input.Keys.Y)) craft("meal");

        if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
            try { interactionMessage = "购入草药 " + world.buyFromMarket(map, com.jianghu.business.TradeGood.HERB, 1) + " 文。"; }
            catch (IllegalStateException ex) { interactionMessage = ex.getMessage(); }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.V)) {
            try { interactionMessage = "售出草药，收入 " + world.sellToMarket(map, com.jianghu.business.TradeGood.HERB,
                world.player().inventory().getOrDefault("herb", 0)) + " 文。"; }
            catch (IllegalStateException | IllegalArgumentException ex) { interactionMessage = ex.getMessage(); }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            try { interactionMessage = "做工完成，获得 " + world.workForNearbyNpc(map) + " 文。"; }
            catch (IllegalStateException ex) { interactionMessage = ex.getMessage(); }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
            try { world.giftToNearbyNpc(map, "herb"); interactionMessage = "你送出一份草药，对方记下了这份心意。"; }
            catch (IllegalStateException | IllegalArgumentException ex) { interactionMessage = ex.getMessage(); }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            var quest = world.quest("herb-delivery").orElseThrow();
            try {
                if (quest.status() == com.jianghu.quest.Quest.Status.LOCKED) { world.acceptQuest("herb-delivery"); interactionMessage = "接取委托：采集三份草药交给镇中药铺。"; }
                else if (quest.status() == com.jianghu.quest.Quest.Status.READY_TO_TURN_IN) { long reward = world.turnInQuest("herb-delivery"); interactionMessage = "委托完成，获得 " + reward + " 文与 1 点声望。"; }
                else interactionMessage = "采药委托进度：" + quest.progress().get("herb") + "/3";
            } catch (IllegalStateException ex) { interactionMessage = ex.getMessage(); }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            world.interactWithNearbyPoint(map).ifPresentOrElse(
                line -> interactionMessage = line,
                () -> interactionMessage = "附近没有可以交互的地点。");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            world.talkToNearby(map).ifPresentOrElse(
                line -> interactionMessage = line,
                () -> interactionMessage = "附近没有可以交谈的人。");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) Gdx.app.exit();

        Gdx.gl.glClearColor(.08f,.15f,.15f,1); Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        float tile = 28, ox = 36, oy = 80;
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int y=0;y<map.height();y++) for(int x=0;x<map.width();x++) {
            Color terrain = switch (map.tileAt(x,y)) {
                case WATER -> new Color(.30f,.58f,.62f,1);
                case WALL -> new Color(.25f,.34f,.29f,1);
                case BRIDGE -> new Color(.66f,.49f,.30f,1);
                case PATH -> new Color(.68f,.58f,.40f,1);
                case BUILDING -> new Color(.48f,.32f,.25f,1);
                case GRASS -> new Color(.48f,.65f,.43f,1);
            };
            shapes.setColor(terrain);
            shapes.rect(ox+x*tile, oy+y*tile, tile-1, tile-1);
        }
        shapes.setColor(new Color(.55f,.38f,.24f,1)); shapes.rect(ox+6*tile,oy+2*tile,4*tile,3*tile);
        shapes.rect(ox+3*tile,oy+8*tile,3*tile,3*tile);
        shapes.setColor(new Color(.78f,.30f,.20f,1));
        for (GameWorld.NpcPosition npc : world.npcPositions(map)) {
            shapes.circle(ox+npc.x()*tile+tile/2,oy+npc.y()*tile+tile/2,8);
        }
        shapes.setColor(Color.GOLD); shapes.circle(ox+world.player().x()*tile+tile/2,oy+world.player().y()*tile+tile/2,10);
        shapes.end();

        batch.begin(); font.setColor(Color.WHITE);
        font.draw(batch,"一人一江湖  |  江南水乡 · 乌篷镇",36,Gdx.graphics.getHeight()-24);
        font.draw(batch,"第 "+world.clock().day()+" 日  "+world.clock().traditionalHour()+"（"+world.clock().phase()+"）  天气："+world.weather(),36,Gdx.graphics.getHeight()-52);
        font.draw(batch,"旅人："+world.player().name()+"   健康："+world.player().health()+"   体力："+world.player().stamina()+"   银钱："+(world.player().moneyInWen()/100)+" 两 "+(world.player().moneyInWen()%100)+" 文",36,58);
        font.draw(batch,"需求：饥饿 "+world.player().hunger()+" 口渴 "+world.player().thirst()+"  心情 "+world.player().morale(),36,78);
        font.draw(batch,"装备：攻击 "+world.player().attackPower()+" 防御 "+world.player().defensePower()+"  武器 "+world.player().equipment().getOrDefault(com.jianghu.item.EquipmentItem.Slot.WEAPON, "无")+"  护甲 "+world.player().equipment().getOrDefault(com.jianghu.item.EquipmentItem.Slot.ARMOR, "无"),36,98);
        font.draw(batch,"武学："+world.player().martialStyle().displayName()+"  内力 "+world.player().innerEnergy()+"  境界 "+world.player().martialLevel()+" 重  经验 "+world.player().martialExperience(),36,118);
        font.draw(batch,"背包：草药 "+world.player().inventory().getOrDefault("herb",0)+"  熟食 "+world.player().inventory().getOrDefault("meal",0)+"  清茶 "+world.player().inventory().getOrDefault("tea",0)+"  药 "+world.player().inventory().getOrDefault("medicine",0),36,36);
        if (inventoryOpen) {
            shapes.begin(ShapeRenderer.ShapeType.Filled); shapes.setColor(0.04f,0.06f,0.07f,0.92f);
            shapes.rect(Gdx.graphics.getWidth()-330, Gdx.graphics.getHeight()-300, 310, 250); shapes.end();
            font.draw(batch,"行囊 · 物品分类",Gdx.graphics.getWidth()-310,Gdx.graphics.getHeight()-72);
            int line = 0;
            for (var category : com.jianghu.item.InventoryItem.Category.values()) {
                String label = switch (category) { case FOOD -> "食物"; case MEDICINE -> "药品"; case MATERIAL -> "材料"; case QUEST -> "任务"; };
                font.draw(batch,label+"：",Gdx.graphics.getWidth()-310,Gdx.graphics.getHeight()-102-line*34); line++;
                for (var entry : world.inventoryByCategory().get(category).entrySet()) {
                    String name = com.jianghu.item.InventoryCatalog.find(entry.getKey()).map(com.jianghu.item.InventoryItem::name).orElse(entry.getKey());
                    font.draw(batch,"  "+name+" ×"+entry.getValue(),Gdx.graphics.getWidth()-290,Gdx.graphics.getHeight()-102-line*34); line++;
                }
            }
            font.draw(batch,"1 熟食  2 清茶  3 金疮药  4 草药（草药不可直接使用）",Gdx.graphics.getWidth()-310,Gdx.graphics.getHeight()-275);
        }
        font.draw(batch,"WASD移动 E交谈 F地点 M拜师 N学招 H吐纳 7基础掌法 8施展流派招式 X攻击 Z撤退 C遇敌 I背包 空格等待 ESC退出",36,16);
        if (world.activeCombat().isPresent()) {
            var fight = world.activeCombat().orElseThrow();
            shapes.begin(ShapeRenderer.ShapeType.Filled); shapes.setColor(0.03f,0.02f,0.02f,0.88f);
            shapes.rect(Gdx.graphics.getWidth()/2f-210,Gdx.graphics.getHeight()/2f-105,420,210); shapes.end();
            font.draw(batch,"遭遇战 · "+fight.enemy().name()+"  "+fight.status(),Gdx.graphics.getWidth()/2f-190,Gdx.graphics.getHeight()/2f+78);
            font.draw(batch,"敌方生命："+fight.enemy().health()+"/"+fight.enemy().maxHealth()+"  攻击："+fight.enemy().attack()+"  防御："+fight.enemy().defense(),Gdx.graphics.getWidth()/2f-190,Gdx.graphics.getHeight()/2f+48);
            font.draw(batch,"你：健康 "+world.player().health()+" 体力 "+world.player().stamina()+"  攻击 "+(3+world.player().attackPower()+world.player().martialPower())+" 防御 "+world.player().defensePower(),Gdx.graphics.getWidth()/2f-190,Gdx.graphics.getHeight()/2f+18);
            var logs=fight.log();
            for(int i=Math.max(0,logs.size()-3);i<logs.size();i++) font.draw(batch,logs.get(i),Gdx.graphics.getWidth()/2f-190,Gdx.graphics.getHeight()/2f-18-(i-Math.max(0,logs.size()-3))*24);
            if(fight.status()==com.jianghu.combat.CombatEncounter.Status.ONGOING) font.draw(batch,"X 出招攻击    Z 脱身撤退",Gdx.graphics.getWidth()/2f-190,Gdx.graphics.getHeight()/2f-82);
        }
        font.draw(batch, interactionMessage, 500, 58);
        for (GameWorld.NpcPosition npc : world.npcPositions(map)) {
            font.draw(batch,npc.name(),ox+npc.x()*tile-8,oy+npc.y()*tile+tile+8);
        }
        world.nearbyMartialMaster(map).ifPresent(master -> font.draw(batch,master.name()+" · "+master.title(),ox+6*tile-28,oy+14*tile));
        map.nearestInteractable(world.player().x(),world.player().y()).ifPresent(point ->
            font.draw(batch,"附近："+point.name()+"（按 F 交互）",500,36));
        batch.end();
    }
    private void useTechnique(String id) {
        try {
            var fight = world.activeCombat().orElseThrow(() -> new IllegalStateException("当前没有进行中的战斗"));
            interactionMessage = fight.useTechnique(id);
        } catch (IllegalStateException | IllegalArgumentException ex) { interactionMessage = ex.getMessage(); }
    }
    private void useTechniqueForCurrentStyle() {
        String id = switch (world.player().martialStyle()) {
            case FIST -> "iron_palm"; case SWORD -> "cloud_sword"; case SABER -> "wind_saber";
            case SPEAR -> "piercing_spear"; case STAFF -> "sweeping_staff";
        };
        useTechnique(id);
    }
    private void startCombat() {
        try { world.startBanditEncounter(); interactionMessage = "山道劫匪出现！按 X 出招，按 Z 撤退。"; }
        catch (IllegalStateException ex) { interactionMessage = ex.getMessage(); }
    }
    private void combatAction(boolean flee) {
        try { interactionMessage = flee ? world.fleeCombat() : world.combatAttack(); }
        catch (IllegalStateException ex) { interactionMessage = ex.getMessage(); }
    }
    private void equipItem(String itemId) {
        try { interactionMessage = world.equipPlayerItem(itemId); }
        catch (IllegalStateException | IllegalArgumentException ex) { interactionMessage = ex.getMessage(); }
    }
    private void craft(String recipe) {
        try { interactionMessage = recipe.equals("medicine") ? world.craftMedicine() : world.cookMeal(); }
        catch (IllegalStateException ex) { interactionMessage = ex.getMessage(); }
    }
    private void useItem(String itemId) {
        try { interactionMessage = world.useInventoryItem(itemId); }
        catch (IllegalStateException | IllegalArgumentException ex) { interactionMessage = ex.getMessage(); }
    }
    @Override public void dispose() { shapes.dispose(); batch.dispose(); font.dispose(); }
}
