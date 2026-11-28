package com.jianghu.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.jianghu.combat.CharacterAnimationState;
import java.util.EnumMap;
import java.util.Map;

/** Optional high-resolution sprite atlas. Procedural rendering remains the safe fallback until art is installed. */
public final class ArtAssetCatalog implements AutoCloseable {
    public static final String ATLAS_PATH = "art/jianghu.atlas";
    private final TextureAtlas atlas;
    private final Map<CharacterAnimationState, Animation<TextureRegion>> heroAnimations = new EnumMap<>(CharacterAnimationState.class);

    public ArtAssetCatalog() {
        if (!Gdx.files.internal(ATLAS_PATH).exists()) {
            atlas = null;
            return;
        }
        atlas = new TextureAtlas(Gdx.files.internal(ATLAS_PATH));
        for (CharacterAnimationState state : CharacterAnimationState.values()) {
            String prefix = "hero_" + state.name().toLowerCase(java.util.Locale.ROOT);
            Array<TextureRegion> frames = new Array<>();
            for (TextureAtlas.AtlasRegion region : atlas.findRegions(prefix)) frames.add(region);
            if (frames.size > 0) {
                Animation.PlayMode mode = state == CharacterAnimationState.DEATH
                    ? Animation.PlayMode.NORMAL : Animation.PlayMode.LOOP;
                heroAnimations.put(state, new Animation<>(0.10f, frames, mode));
            }
        }
    }

    public boolean hasHeroAnimation(CharacterAnimationState state) {
        return heroAnimations.containsKey(state);
    }

    /** Draws a hand-painted frame when present; returns false when the procedural sprite should be used. */
    public boolean drawHero(SpriteBatch batch, CharacterAnimationState state, float stateTime,
                            float centerX, float feetY, float height, int facing) {
        Animation<TextureRegion> animation = heroAnimations.get(state);
        if (animation == null || atlas == null) return false;
        TextureRegion frame = animation.getKeyFrame(stateTime, state != CharacterAnimationState.DEATH);
        float width = height * frame.getRegionWidth() / (float) frame.getRegionHeight();
        float drawX = facing < 0 ? centerX + width * 0.5f : centerX - width * 0.5f;
        float drawWidth = facing < 0 ? -width : width;
        batch.draw(frame, drawX, feetY, drawWidth, height);
        return true;
    }

    @Override public void close() { if (atlas != null) atlas.dispose(); }
}