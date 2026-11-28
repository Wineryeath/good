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
import com.jianghu.combat.CharacterAnimationController;
import com.jianghu.combat.CombatAction;
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
    private final CharacterAnimationController playerAnimation = new CharacterAnimationController();
    private int previousPlayerHealth = 100;
    private com.jianghu.map.OverworldExplorer overworld;
    private boolean overworldMode;
    private final com.jianghu.save.GameWorldSaveService saveService = new com.jianghu.save.GameWorldSaveService();

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
        overworld = world.overworld();
    }

    @Override public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        playerAnimation.update(delta);
        world.updateRealtimeCombat(delta);
        if (world.player().health() < previousPlayerHealth) playerAnimation.hurt();
        previousPlayerHealth = world.player().health();
        moveCooldown -= delta;
        if (moveCooldown <= 0) {
            int dx = 0, dy = 0;
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) dx = -1;
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) dx = 1;
            if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) dy = 1;
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) dy = -1;
            if (dx != 0 || dy != 0) {
                if (world.insideMartialSchool()) {
                    if (world.moveInsideMartialSchool(dx, dy)) playerAnimation.walk(dx);
                } else if (overworldMode) {
                    if (overworld.move(dx, dy)) {
                        playerAnimation.walk(dx); world.advanceMinutes(3);
                        overworld.travelThroughGate().ifPresent(message -> interactionMessage = message);
                    }
                } else {
                    int nx = world.player().x() + dx;
                    int ny = world.player().y() + dy;
                    if (map.isWalkable(nx, ny)) { world.player().moveTo(nx, ny); playerAnimation.walk(dx); world.advanceMinutes(2); }
                }
                moveCooldown = .12f;
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (world.activeRealtimeCombat().filter(c -> c.status() == com.jianghu.combat.RealtimeCombatEncounter.Status.ONGOING).isPresent()) performRealtimeAction(CombatAction.DODGE);
            else world.waitMinutes(30);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)) performRealtimeAction(CombatAction.HEAVY_ATTACK);
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) inventoryOpen = !inventoryOpen;
        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) saveGame();
        if (Gdx.input.isKeyJustPressed(Input.Keys.F9)) loadGame();
        if (Gdx.input.isKeyJustPressed(Input.Keys.O)) {
            if (world.insideMartialSchool()) {
                try { interactionMessage = world.leaveMartialSchool(); }
                catch (IllegalStateException ex) { interactionMessage = ex.getMessage(); }
            } else if (world.activeRealtimeCombat().filter(c -> c.status() == com.jianghu.combat.RealtimeCombatEncounter.Status.ONGOING).isPresent()) interactionMessage = "交战之中无法打开世界地图。";
            else { overworldMode = !overworldMode; interactionMessage = overworldMode ? "已打开六域世界地图，沿关隘可前往其他地域。" : "已返回乌篷镇。"; }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && overworldMode) {
            try { interactionMessage = world.enterNearbyMartialSchool(); }
            catch (IllegalStateException | IllegalArgumentException ex) { interactionMessage = ex.getMessage(); }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) useItem("meal");
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) useItem("tea");
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) useItem("medicine");
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) useItem("herb");
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) equipItem("iron_sword");
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)) equipItem("leather_armor");
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) startCombat();
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            try {
                if (world.insideMartialSchool()) interactionMessage = world.useInsideMartialSchoolFacility(com.jianghu.map.MartialSchoolFacility.Type.MEDITATION_HALL);
                else if (overworldMode) {
                    var site = world.nearbyMartialSchoolSite().orElseThrow(() -> new IllegalStateException("附近没有门派静室"));
                    interactionMessage = world.useNearbyMartialSchoolFacility(site.schoolId() + "-meditation");
                } else interactionMessage = world.meditate();
            } catch (IllegalStateException | IllegalArgumentException ex) { interactionMessage = ex.getMessage(); }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) && (overworldMode || world.insideMartialSchool())) {
            try {
                if (world.insideMartialSchool()) interactionMessage = world.useInsideMartialSchoolFacility(com.jianghu.map.MartialSchoolFacility.Type.SCRIPTURE_HALL);
                else {
                    var site = world.nearbyMartialSchoolSite().orElseThrow(() -> new IllegalStateException("附近没有门派藏经阁"));
                    interactionMessage = world.useNearbyMartialSchoolFacility(site.schoolId() + "-scripture");
                }
            } catch (IllegalStateException | IllegalArgumentException ex) { interactionMessage = ex.getMessage(); }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R) && (overworldMode || world.insideMartialSchool())) {
            try {
                if (world.insideMartialSchool()) interactionMessage = world.useInsideMartialSchoolFacility(com.jianghu.map.MartialSchoolFacility.Type.INFIRMARY);
                else {
                    var site = world.nearbyMartialSchoolSite().orElseThrow(() -> new IllegalStateException("附近没有门派医馆"));
                    interactionMessage = world.useNearbyMartialSchoolFacility(site.schoolId() + "-infirmary");
                }
            } catch (IllegalStateException | IllegalArgumentException ex) { interactionMessage = ex.getMessage(); }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q) && overworldMode && !world.insideMartialSchool()) {
            try { interactionMessage = world.donateToNearbyMartialSchool(100); }
            catch (IllegalStateException | IllegalArgumentException ex) { interactionMessage = ex.getMessage(); }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            try { interactionMessage = overworldMode ? world.joinNearbyMartialSchool() : world.learnFromMartialMaster(map); }
            catch (IllegalStateException | IllegalArgumentException ex) { interactionMessage = ex.getMessage(); }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            try { interactionMessage = world.learnTechniqueFromMartialMaster(map); }
            catch (IllegalStateException | IllegalArgumentException ex) { interactionMessage = ex.getMessage(); }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_7)) useTechniqueOrRealtimeSkill("basic_palm");
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_8)) useTechniqueOrRealtimeSkillForCurrentStyle();
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_9)) useTechniqueOrRealtimeSkill("cloud_piercing");
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            try {
                interactionMessage = (overworldMode || world.insideMartialSchool()) ? (world.insideMartialSchool() ? world.useInsideMartialSchoolFacility(com.jianghu.map.MartialSchoolFacility.Type.TRAINING_GROUND) : world.trainAtNearbyMartialSchool()) : world.trainWithMartialMaster(map);
                playerAnimation.train();
            } catch (IllegalStateException ex) { interactionMessage = ex.getMessage(); }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            if (world.activeRealtimeCombat().filter(c -> c.status() == com.jianghu.combat.RealtimeCombatEncounter.Status.ONGOING).isPresent()) performRealtimeAction(CombatAction.LIGHT_ATTACK);
            else combatAction(false);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            if (world.activeRealtimeCombat().filter(c -> c.status() == com.jianghu.combat.RealtimeCombatEncounter.Status.ONGOING).isPresent()) {
                try { interactionMessage = world.fleeRealtimeCombat(); } catch (IllegalStateException ex) { interactionMessage = ex.getMessage(); }
            } else combatAction(true);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) craft("medicine");
        if (Gdx.input.isKeyJustPressed(Input.Keys.Y)) craft("meal");

        if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
            try { interactionMessage = "购入草药 " + world.buyFromMarket(map, com.jianghu.business.TradeGood.HERB, 1) + " 文。"; }
            catch (IllegalStateException ex) { interactionMessage = ex.getMessage(); }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.V)) {
            if (world.activeRealtimeCombat().filter(c -> c.status() == com.jianghu.combat.RealtimeCombatEncounter.Status.ONGOING).isPresent()) {
                playerAnimation.block();
                performRealtimeAction(CombatAction.BLOCK);
            }
            else try { interactionMessage = "售出草药，收入 " + world.sellToMarket(map, com.jianghu.business.TradeGood.HERB,
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
            if (world.insideMartialSchool()) {
                try { interactionMessage = world.interactInsideMartialSchool(); }
                catch (IllegalStateException | IllegalArgumentException ex) { interactionMessage = ex.getMessage(); }
            } else world.interactWithNearbyPoint(map).ifPresentOrElse(
                line -> interactionMessage = line,
                () -> interactionMessage = "附近没有可以交互的地点。");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            if (overworldMode) {
                try { interactionMessage = world.inspectNearbyMartialSchool(); }
                catch (IllegalStateException ex) { interactionMessage = ex.getMessage(); }
            } else if (world.nearbyMartialMaster(map).isPresent()) interactionMessage = world.talkToMartialMaster(map);
            else world.talkToNearby(map).ifPresentOrElse(
                line -> interactionMessage = line,
                () -> interactionMessage = "附近没有可以交谈的人。");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) Gdx.app.exit();

        Gdx.gl.glClearColor(.08f,.15f,.15f,1); Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        float tile = overworldMode && !world.insideMartialSchool() ? 12f : 28f, ox = overworldMode && !world.insideMartialSchool() ? 16f : 36f, oy = overworldMode && !world.insideMartialSchool() ? 48f : 80f;
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        if (world.insideMartialSchool()) {
            var interior = world.activeMartialSchoolInterior().orElseThrow();
            var indoorMap = interior.map();
            for (int y=0;y<indoorMap.height();y++) for(int x=0;x<indoorMap.width();x++)
                PixelTerrainRenderer.drawInteriorTile(shapes, indoorMap.tileAt(x,y), x, y, ox, oy, tile);
            shapes.setColor(new Color(.82f,.72f,.48f,1));
            for (var point : indoorMap.points()) shapes.circle(ox+point.x()*tile+tile/2,oy+point.y()*tile+tile/2,5);
            PixelCharacterRenderer.draw(shapes,ox+world.interiorX()*tile+tile/2,oy+world.interiorY()*tile+tile/2-8,1.05f,playerAnimation);
        } else if (overworldMode) {
            var region = overworld.region();
            for (int y = 0; y < region.height(); y++) for (int x = 0; x < region.width(); x++)
                PixelTerrainRenderer.drawOverworldTile(shapes, region.terrainAt(x, y), x, y, ox, oy, tile);
            shapes.setColor(Color.GOLD);
            for (var gate : overworld.gates()) shapes.rect(ox+gate.x()*tile+3, oy+gate.y()*tile+3, tile-6, tile-6);
            shapes.setColor(new Color(.88f,.78f,.34f,1));
            for (var site : com.jianghu.map.MartialSchoolSite.all()) if (site.region() == overworld.regionId()) {
                shapes.rect(ox+site.x()*tile+2, oy+site.y()*tile+2, tile-4, tile-4);
                shapes.rect(ox+site.x()*tile+4, oy+site.y()*tile+tile-5, tile-8, 3);
            }
            PixelCharacterRenderer.draw(shapes, ox+overworld.x()*tile+tile/2, oy+overworld.y()*tile+tile/2-3, .65f, playerAnimation);
        } else {
            for (int y=0;y<map.height();y++) for(int x=0;x<map.width();x++)
                PixelTerrainRenderer.drawTownTile(shapes, map.tileAt(x,y), x, y, ox, oy, tile);
            shapes.setColor(new Color(.55f,.38f,.24f,1)); shapes.rect(ox+6*tile,oy+2*tile,4*tile,3*tile);
            shapes.rect(ox+3*tile,oy+8*tile,3*tile,3*tile);
            shapes.setColor(new Color(.78f,.30f,.20f,1));
            for (GameWorld.NpcPosition npc : world.npcPositions(map))
                shapes.circle(ox+npc.x()*tile+tile/2,oy+npc.y()*tile+tile/2,8);
            PixelCharacterRenderer.draw(shapes, ox+world.player().x()*tile+tile/2, oy+world.player().y()*tile+tile/2-8, 1.15f, playerAnimation);
        }
        shapes.end();

        batch.begin(); font.setColor(Color.WHITE);
        font.draw(batch,world.insideMartialSchool() ? "一人一江湖  |  "+world.activeMartialSchoolInterior().orElseThrow().name() : overworldMode ? "一人一江湖  |  世界地图 · "+com.jianghu.map.OverworldExplorer.regionName(overworld.regionId()) : "一人一江湖  |  江南水乡 · 乌篷镇",36,Gdx.graphics.getHeight()-24);
        font.draw(batch,"第 "+world.clock().day()+" 日  "+world.clock().traditionalHour()+"（"+world.clock().phase()+"）  天气："+world.weather(),36,Gdx.graphics.getHeight()-52);
        font.draw(batch,"旅人："+world.player().name()+"   健康："+world.player().health()+"   体力："+world.player().stamina()+"   银钱："+(world.player().moneyInWen()/100)+" 两 "+(world.player().moneyInWen()%100)+" 文",36,58);
        font.draw(batch,"需求：饥饿 "+world.player().hunger()+" 口渴 "+world.player().thirst()+"  心情 "+world.player().morale(),36,78);
        font.draw(batch,"装备：攻击 "+world.player().attackPower()+" 防御 "+world.player().defensePower()+"  武器 "+world.player().equipment().getOrDefault(com.jianghu.item.EquipmentItem.Slot.WEAPON, "无")+"  护甲 "+world.player().equipment().getOrDefault(com.jianghu.item.EquipmentItem.Slot.ARMOR, "无"),36,98);
        font.draw(batch,"武学："+world.player().martialStyle().displayName()+"  内力 "+world.player().innerEnergy()+"  境界 "+world.player().martialLevel()+" 重  经验 "+world.player().martialExperience()+"  师门信任 "+world.martialMasterFavor("master-river-sword")+"/3",36,118);
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
        font.draw(batch,world.insideMartialSchool() ? "门派内院：WASD行走 F使用附近设施 O离开内院；金色圆点为演武坪、藏经阁、静修室与医馆" : overworldMode ? "世界地图：WASD行走 E查看门派 Enter进入内院 M拜师 Q捐献100文 L修习 P读谱 H静修 R疗伤 O返回城镇 F5存档 F9读档" : "WASD移动 E交谈 F地点 M拜师 N学招 L练功 H吐纳 X轻击 Shift重击 7/8/9绝招 V格挡 空格闪避 Z撤退 C遇敌 O世界地图 I背包 F5存档 F9读档 ESC退出",36,16);
        if (world.activeRealtimeCombat().isPresent()) {
            var fight = world.activeRealtimeCombat().orElseThrow();
            shapes.begin(ShapeRenderer.ShapeType.Filled); shapes.setColor(0.03f,0.02f,0.02f,0.90f);
            shapes.rect(Gdx.graphics.getWidth()/2f-230,Gdx.graphics.getHeight()/2f-110,460,220); shapes.end();
            font.draw(batch,"实时遭遇 · "+fight.enemy().name()+"  "+fight.status(),Gdx.graphics.getWidth()/2f-210,Gdx.graphics.getHeight()/2f+82);
            font.draw(batch,"敌方生命："+fight.enemy().health()+" / "+fight.enemy().maxHealth()+"   "+(fight.enemyTelegraphing()?"<危险> 敌人即将出刀":"敌人正在寻找破绽"),Gdx.graphics.getWidth()/2f-210,Gdx.graphics.getHeight()/2f+52);
            font.draw(batch,"你的生命："+world.player().health()+"   体力："+world.player().stamina()+"   连击："+fight.controller().comboCount(),Gdx.graphics.getWidth()/2f-210,Gdx.graphics.getHeight()/2f+22);
            var rtLogs=fight.log();
            for(int i=Math.max(0,rtLogs.size()-3);i<rtLogs.size();i++) font.draw(batch,rtLogs.get(i),Gdx.graphics.getWidth()/2f-210,Gdx.graphics.getHeight()/2f-18-(i-Math.max(0,rtLogs.size()-3))*24);
            if(fight.status()==com.jianghu.combat.RealtimeCombatEncounter.Status.ONGOING) font.draw(batch,"X 轻击  Shift 重击  7/8/9绝招  V 格挡  空格闪避  Z 撤退",Gdx.graphics.getWidth()/2f-210,Gdx.graphics.getHeight()/2f-88);
        }
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
        if (world.insideMartialSchool()) {
            var indoorMap = world.activeMartialSchoolInterior().orElseThrow().map();
            indoorMap.nearestInteractable(world.interiorX(),world.interiorY()).ifPresent(point -> font.draw(batch,"附近："+point.name()+"（按 F 使用）",500,36));
        } else if (!overworldMode) {
            for (GameWorld.NpcPosition npc : world.npcPositions(map)) font.draw(batch,npc.name(),ox+npc.x()*tile-8,oy+npc.y()*tile+tile+8);
            world.nearbyMartialMaster(map).ifPresent(master -> font.draw(batch,master.name()+" · "+master.title(),ox+6*tile-28,oy+14*tile));
            map.nearestInteractable(world.player().x(),world.player().y()).ifPresent(point -> font.draw(batch,"附近："+point.name()+"（按 F 交互）",500,36));
        } else {
            overworld.gateAtPlayer().ifPresent(gate -> font.draw(batch,"关隘："+gate.name()+"（继续前进即可通行）",500,36));
            com.jianghu.map.MartialSchoolSite.nearby(overworld.regionId(), overworld.x(), overworld.y()).ifPresent(site ->
                font.draw(batch, "门派驻地："+site.school().name()+"（E查看 Enter进入 M拜师 L修习）", 500, 36));
        }
        batch.end();
    }
    private void useTechnique(String id) {
        try {
            var fight = world.activeCombat().orElseThrow(() -> new IllegalStateException("当前没有进行中的战斗"));
            interactionMessage = fight.useTechnique(id);
        } catch (IllegalStateException | IllegalArgumentException ex) { interactionMessage = ex.getMessage(); }
    }
    private void useTechniqueOrRealtimeSkill(String id) {
        if (world.activeRealtimeCombat().filter(c -> c.status() == com.jianghu.combat.RealtimeCombatEncounter.Status.ONGOING).isPresent()) {
            var technique = com.jianghu.combat.MartialTechnique.find(id);
            if (technique.isEmpty() || !world.player().knowsTechnique(id)) { interactionMessage = "尚未学会这门招式。"; return; }
            if (technique.get().style() != world.player().martialStyle()) { interactionMessage = "当前武学路数与招式不合。"; return; }
            try {
                var result = world.realtimeCombatTechnique(id);
                interactionMessage = result.accepted() ? result.message() + "（体力 -" + result.staminaSpent() + "）" : result.message();
                if (result.accepted()) playerAnimation.attack();
            } catch (IllegalStateException | IllegalArgumentException ex) { interactionMessage = ex.getMessage(); }
        } else useTechnique(id);
    }
    private void useTechniqueOrRealtimeSkillForCurrentStyle() {
        String id = switch (world.player().martialStyle()) {
            case FIST -> "iron_palm"; case SWORD -> "cloud_sword"; case SABER -> "wind_saber";
            case SPEAR -> "piercing_spear"; case STAFF -> "sweeping_staff";
        };
        useTechniqueOrRealtimeSkill(id);
    }
    private void useTechniqueForCurrentStyle() {
        String id = switch (world.player().martialStyle()) {
            case FIST -> "iron_palm"; case SWORD -> "cloud_sword"; case SABER -> "wind_saber";
            case SPEAR -> "piercing_spear"; case STAFF -> "sweeping_staff";
        };
        useTechnique(id);
    }
    private java.nio.file.Path savePath() { return Gdx.files.local("saves/slot-1.sav").file().toPath(); }
    private void saveGame() {
        try { saveService.save(savePath(), world); interactionMessage = "存档成功：玩家、NPC生活、关系、世界时间与天气已保存。"; }
        catch (java.io.IOException | IllegalArgumentException ex) { interactionMessage = "存档失败：" + ex.getMessage(); }
    }
    private void loadGame() {
        try { world.restoreSnapshot(saveService.load(savePath())); previousPlayerHealth = world.player().health();
            playerAnimation.revive(); interactionMessage = "读档成功：已恢复玩家、NPC生活、关系、世界时间与天气。"; }
        catch (java.io.IOException | IllegalArgumentException ex) { interactionMessage = "读档失败：" + ex.getMessage(); }
    }
    private void startCombat() {
        try { world.startRealtimeBanditEncounter(); interactionMessage = "山道劫匪出现！注意敌人出刀预警：X轻击、Shift重击、V格挡、空格闪避。"; }
        catch (IllegalStateException ex) { interactionMessage = ex.getMessage(); }
    }
    private void performRealtimeAction(CombatAction action) {
        try {
            var result = world.realtimeCombatAction(action);
            interactionMessage = result.accepted() ? result.message() + "（体力 -" + result.staminaSpent() + "）" : result.message();
            if (result.accepted() && (action == CombatAction.LIGHT_ATTACK || action == CombatAction.HEAVY_ATTACK)) playerAnimation.attack();
        } catch (IllegalStateException ex) { interactionMessage = ex.getMessage(); }
    }
    private void combatAction(boolean flee) {
        try {
            interactionMessage = flee ? world.fleeCombat() : world.combatAttack();
            if (!flee) playerAnimation.attack();
        }
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
