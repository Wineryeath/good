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
        font.draw(batch,"背包：草药 "+world.player().inventory().getOrDefault("herb",0)+" 份",36,36);
        font.draw(batch,"WASD / 方向键移动   E：交谈   F：地点交互   空格：等待   ESC：退出",36,16);
        font.draw(batch, interactionMessage, 500, 58);
        for (GameWorld.NpcPosition npc : world.npcPositions(map)) {
            font.draw(batch,npc.name(),ox+npc.x()*tile-8,oy+npc.y()*tile+tile+8);
        }
        map.nearestInteractable(world.player().x(),world.player().y()).ifPresent(point ->
            font.draw(batch,"附近："+point.name()+"（按 F 交互）",500,36));
        batch.end();
    }
    @Override public void dispose() { shapes.dispose(); batch.dispose(); font.dispose(); }
}
