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

    @Override public void create() {
        shapes = new ShapeRenderer(); batch = new SpriteBatch(); font = new BitmapFont();
        map = GameMap.starterTown();
        world = new GameWorld(new Player("无名旅人"), new WorldClock(1, 360), List.of(
            new Npc("npc-tea-owner", "沈掌柜", "茶摊老板", List.of(
                new Npc.Schedule(6,11,"tea-stall",0), new Npc.Schedule(11,14,"market",0),
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
                if (map.isWalkable(nx, ny)) { world.player().moveTo(nx, ny); world.clock().advance(2); }
                moveCooldown = .12f;
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) world.waitMinutes(30);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) Gdx.app.exit();

        Gdx.gl.glClearColor(.08f,.15f,.15f,1); Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        float tile = 28, ox = 36, oy = 80;
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int y=0;y<17;y++) for(int x=0;x<25;x++) {
            boolean water = x >= 14 && y >= 2 && y <= 12;
            shapes.setColor(water ? new Color(.30f,.58f,.62f,1) : new Color(.48f,.65f,.43f,1));
            shapes.rect(ox+x*tile, oy+y*tile, tile-1, tile-1);
        }
        shapes.setColor(new Color(.55f,.38f,.24f,1)); shapes.rect(ox+6*tile,oy+2*tile,4*tile,3*tile);
        shapes.rect(ox+3*tile,oy+8*tile,3*tile,3*tile);
        shapes.setColor(Color.GOLD); shapes.circle(ox+world.player().x()*tile+tile/2,oy+world.player().y()*tile+tile/2,10);
        shapes.end();

        batch.begin(); font.setColor(Color.WHITE);
        font.draw(batch,"一人一江湖  |  江南水乡 · 乌篷镇",36,Gdx.graphics.getHeight()-24);
        font.draw(batch,"第 "+world.clock().day()+" 日  "+world.clock().traditionalHour()+"（"+world.clock().phase()+"）  天气："+world.weather(),36,Gdx.graphics.getHeight()-52);
        font.draw(batch,"旅人："+world.player().name()+"   银两："+(world.player().moneyInWen()/100)+" 两 "+(world.player().moneyInWen()%100)+" 文   体力："+world.player().stamina(),36,48);
        font.draw(batch,"WASD / 方向键移动    空格：等待半时辰    ESC：退出",36,25);
        batch.end();
    }
    @Override public void dispose() { shapes.dispose(); batch.dispose(); font.dispose(); }
}
