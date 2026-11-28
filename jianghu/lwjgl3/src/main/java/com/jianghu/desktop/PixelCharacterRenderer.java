package com.jianghu.desktop;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.jianghu.combat.CharacterAnimationController;
import com.jianghu.combat.CharacterAnimationState;

/** Layered, deterministic pixel-art wuxia character built from crisp rectangular pixels. */
public final class PixelCharacterRenderer {
    private static final Color INK = new Color(0.12f, 0.10f, 0.11f, 1f);
    private static final Color ROBE = new Color(0.68f, 0.20f, 0.16f, 1f);
    private static final Color ROBE_SHADE = new Color(0.42f, 0.13f, 0.14f, 1f);
    private static final Color ROBE_LIGHT = new Color(0.86f, 0.36f, 0.25f, 1f);
    private static final Color TRIM = new Color(0.91f, 0.72f, 0.39f, 1f);
    private static final Color SKIN = new Color(0.91f, 0.70f, 0.53f, 1f);
    private static final Color HAIR = new Color(0.13f, 0.11f, 0.12f, 1f);
    private static final Color BOOT = new Color(0.25f, 0.18f, 0.15f, 1f);
    private static final Color STEEL = new Color(0.72f, 0.81f, 0.82f, 1f);
    private PixelCharacterRenderer() {}

    public static void draw(ShapeRenderer shapes, float centerX, float feetY, float scale, CharacterAnimationController animation) {
        CharacterAnimationState state = animation.state();
        int frame = animation.frame(state == CharacterAnimationState.WALK ? 4 : 3, 0.11f);
        float bob = state == CharacterAnimationState.WALK ? (frame % 2 == 0 ? 1.5f : 0f) : 0f;
        float lean = state == CharacterAnimationState.ATTACK ? 3f : state == CharacterAnimationState.HURT ? -2f : 0f;
        float direction = animation.facing();
        float x = centerX - 8f * scale + lean * direction;
        float y = feetY + bob;
        float stride = state == CharacterAnimationState.WALK ? (frame % 2 == 0 ? 2f : -2f) : 0f;

        // Ground shadow anchors the sprite to the tile instead of making it appear to float.
        shapes.setColor(0.08f, 0.10f, 0.09f, 0.34f);
        shapes.ellipse(centerX - 7f * scale, feetY - 1.5f * scale, 14f * scale, 3f * scale);

        // Boots and split robe hem.
        shapes.setColor(INK);
        shapes.rect(x + (3f + stride) * scale, y, 4f * scale, 6f * scale);
        shapes.rect(x + (9f - stride) * scale, y, 4f * scale, 6f * scale);
        shapes.setColor(BOOT);
        shapes.rect(x + (2f + stride) * scale, y, 5f * scale, 2f * scale);
        shapes.rect(x + (9f - stride) * scale, y, 5f * scale, 2f * scale);
        shapes.setColor(ROBE_SHADE);
        shapes.rect(x + 2f * scale, y + 4f * scale, 12f * scale, 5f * scale);
        shapes.setColor(ROBE);
        shapes.rect(x + 2f * scale, y + 6f * scale, 12f * scale, 9f * scale);
        shapes.rect(x + 4f * scale, y + 3f * scale, 8f * scale, 5f * scale);
        shapes.setColor(ROBE_LIGHT);
        shapes.rect(x + 3f * scale, y + 8f * scale, 2f * scale, 5f * scale);
        shapes.rect(x + 11f * scale, y + 8f * scale, 2f * scale, 5f * scale);

        // Belt, knot and vertical embroidered trim.
        shapes.setColor(INK); shapes.rect(x + 3f * scale, y + 7f * scale, 10f * scale, 2f * scale);
        shapes.setColor(TRIM);
        shapes.rect(x + 4f * scale, y + 8f * scale, 8f * scale, scale);
        shapes.rect(x + 7f * scale, y + 5f * scale, 2f * scale, 9f * scale);
        shapes.rect(x + 10f * scale, y + 6f * scale, 2f * scale, 2f * scale);

        // Sleeves and hands react to the current animation state.
        shapes.setColor(ROBE_SHADE);
        if (state == CharacterAnimationState.ATTACK) {
            shapes.rect(x + (direction > 0 ? 11f : -1f) * scale, y + 9f * scale, 6f * scale, 4f * scale);
        } else if (state == CharacterAnimationState.BLOCK) {
            shapes.rect(x + 1f * scale, y + 12f * scale, 4f * scale, 5f * scale);
            shapes.rect(x + 11f * scale, y + 12f * scale, 4f * scale, 5f * scale);
        } else if (state == CharacterAnimationState.TRAIN) {
            shapes.rect(x + 1f * scale, y + 12f * scale, 5f * scale, 4f * scale);
            shapes.rect(x + 10f * scale, y + 12f * scale, 5f * scale, 4f * scale);
        } else {
            shapes.rect(x, y + 8f * scale, 4f * scale, 6f * scale);
            shapes.rect(x + 12f * scale, y + 8f * scale, 4f * scale, 6f * scale);
        }
        shapes.setColor(SKIN);
        if (state == CharacterAnimationState.ATTACK) {
            shapes.rect(x + (direction > 0 ? 14f : -2f) * scale, y + 9f * scale, 3f * scale, 3f * scale);
        } else {
            shapes.rect(x + 1f * scale, y + 8f * scale, 2f * scale, 4f * scale);
            shapes.rect(x + 13f * scale, y + 8f * scale, 2f * scale, 4f * scale);
        }

        // Face, tied-back hair, topknot and headband.
        shapes.setColor(SKIN); shapes.rect(x + 4f * scale, y + 15f * scale, 8f * scale, 7f * scale);
        shapes.setColor(HAIR);
        shapes.rect(x + 3f * scale, y + 20f * scale, 10f * scale, 4f * scale);
        shapes.rect(x + 5f * scale, y + 23f * scale, 6f * scale, 2f * scale);
        shapes.rect(x + 11f * scale, y + 19f * scale, 3f * scale, 5f * scale);
        shapes.setColor(TRIM); shapes.rect(x + 4f * scale, y + 20f * scale, 8f * scale, scale);
        shapes.setColor(INK);
        float eyeX = direction > 0 ? x + 9f * scale : x + 5f * scale;
        shapes.rect(eyeX, y + 18f * scale, scale, scale);

        // Sword hilt and blade flash on an attack; no weapon is drawn during peaceful poses.
        if (state == CharacterAnimationState.ATTACK) {
            float bladeX = x + (direction > 0 ? 16f : -10f) * scale;
            shapes.setColor(INK); shapes.rect(bladeX, y + 9f * scale, 2f * scale, 3f * scale);
            shapes.setColor(STEEL); shapes.rect(bladeX + (direction > 0 ? 1f : -3f) * scale, y + 10f * scale, 5f * scale, scale);
            shapes.setColor(TRIM); shapes.rect(bladeX, y + 8f * scale, 3f * scale, scale);
        }
        if (state == CharacterAnimationState.HURT) {
            shapes.setColor(new Color(0.88f, 0.16f, 0.13f, 1f));
            shapes.rect(x + 14f * scale, y + 17f * scale, 2f * scale, 2f * scale);
        }
        if (state == CharacterAnimationState.DEATH) {
            shapes.setColor(INK); shapes.rect(x + 1f * scale, y + 2f * scale, 14f * scale, 2f * scale);
        }
    }
}