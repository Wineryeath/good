package com.jianghu.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.jianghu.combat.CharacterAnimationState;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Optional high-resolution character atlas. Java-rendered actors remain the fallback until painted art is installed. */
public final class ArtAssetCatalog implements AutoCloseable {
    public static final String ATLAS_PATH = "art/jianghu.atlas";
    private final TextureAtlas atlas;
    private final Map<String, Animation<TextureRegion>> animations = new HashMap<>();

    public ArtAssetCatalog() {
        if (!Gdx.files.internal(ATLAS_PATH).exists()) { atlas = null; return; }
        atlas = new TextureAtlas(Gdx.files.internal(ATLAS_PATH));
        register("hero");
        for (String variant : new String[] {"villager", "guard", "merchant", "scholar", "disciple"}) register("npc_" + variant);
        for (String variant : new String[] {"bandit", "wolf", "boss", "martial_artist"}) register("enemy_" + variant);
    }

    private void register(String actorPrefix) {
        for (CharacterAnimationState state : CharacterAnimationState.values()) {
            String prefix = actorPrefix + "_" + state.name().toLowerCase(Locale.ROOT);
            Array<TextureRegion> frames = new Array<>();
            for (TextureAtlas.AtlasRegion region : atlas.findRegions(prefix)) frames.add(region);
            if (frames.size > 0) {
                Animation.PlayMode mode = state == CharacterAnimationState.DEATH ? Animation.PlayMode.NORMAL : Animation.PlayMode.LOOP;
                animations.put(key(actorPrefix, state), new Animation<>(frameDuration(state), frames, mode));
            }
        }
    }

    private static float frameDuration(CharacterAnimationState state) {
        return switch (state) { case ATTACK, HURT, BLOCK -> 0.075f; case WALK -> 0.09f; case DEATH -> 0.12f; case TRAIN -> 0.11f; default -> 0.16f; };
    }
    private static String key(String actorPrefix, CharacterAnimationState state) { return actorPrefix + ":" + state.name(); }
    public boolean hasHeroAnimation(CharacterAnimationState state) { return hasActorAnimation("hero", state); }
    public boolean hasActorAnimation(String actorPrefix, CharacterAnimationState state) { return animations.containsKey(key(actorPrefix, state)); }

    public boolean drawHero(SpriteBatch batch, CharacterAnimationState state, float stateTime, float centerX, float feetY, float height, int facing) {
        return drawActor(batch, "hero", state, stateTime, centerX, feetY, height, facing);
    }

    /** Draws a named atlas actor. Prefixes: hero, npc_villager, npc_guard, npc_merchant, npc_scholar, npc_disciple,
     * enemy_bandit, enemy_wolf, enemy_boss, enemy_martial_artist. Frame names append _idle/_walk/... plus numeric suffixes. */
    public boolean drawActor(SpriteBatch batch, String actorPrefix, CharacterAnimationState state, float stateTime,
                             float centerX, float feetY, float height, int facing) {
        Animation<TextureRegion> animation = animations.get(key(actorPrefix, state));
        if (animation == null) animation = animations.get(key(actorPrefix, CharacterAnimationState.IDLE));
        if (animation == null || atlas == null) return false;
        TextureRegion frame = animation.getKeyFrame(stateTime, state != CharacterAnimationState.DEATH);
        float width = height * frame.getRegionWidth() / (float) frame.getRegionHeight();
        float drawX = facing < 0 ? centerX + width * 0.5f : centerX - width * 0.5f;
        batch.draw(frame, drawX, feetY, facing < 0 ? -width : width, height);
        return true;
    }

    @Override public void close() { if (atlas != null) atlas.dispose(); }
}