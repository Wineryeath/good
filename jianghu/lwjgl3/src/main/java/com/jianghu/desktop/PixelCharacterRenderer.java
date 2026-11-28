package com.jianghu.desktop;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.jianghu.combat.CharacterAnimationController;
import com.jianghu.combat.CharacterAnimationState;

/** 程序化像素武侠角色绘制器；无需外部图片资源。 */
public final class PixelCharacterRenderer {
    private static final Color INK = new Color(0.16f, 0.13f, 0.12f, 1f);
    private static final Color ROBE = new Color(0.72f, 0.23f, 0.18f, 1f);
    private static final Color TRIM = new Color(0.88f, 0.69f, 0.39f, 1f);
    private static final Color SKIN = new Color(0.91f, 0.70f, 0.53f, 1f);
    private PixelCharacterRenderer() {}

    public static void draw(ShapeRenderer shapes, float centerX, float feetY, float scale,
                            CharacterAnimationController animation) {
        CharacterAnimationState state = animation.state();
        int frame = animation.frame(state == CharacterAnimationState.WALK ? 4 : 3, 0.11f);
        float bob = state == CharacterAnimationState.WALK ? (frame % 2 == 0 ? 1.5f : 0f) : 0f;
        float lean = state == CharacterAnimationState.ATTACK ? 3f : state == CharacterAnimationState.HURT ? -2f : 0f;
        float direction = animation.facing();
        float x = centerX - 8f * scale + lean * direction;
        float y = feetY + bob;

        // 衣摆与双腿：步行动画交替迈步，攻击时前踏。
        shapes.setColor(INK);
        float stride = state == CharacterAnimationState.WALK ? (frame % 2 == 0 ? 2f : -2f) : 0f;
        shapes.rect(x + 4*scale, y, 4*scale, 6*scale);
        shapes.rect(x + (state == CharacterAnimationState.ATTACK ? 8 : 3 + stride)*scale, y, 3*scale, 5*scale);
        shapes.rect(x + (state == CharacterAnimationState.ATTACK ? 1 : 9 - stride)*scale, y, 3*scale, 5*scale);
        shapes.setColor(ROBE);
        shapes.rect(x + 2*scale, y + 5*scale, 12*scale, 10*scale);
        shapes.rect(x + 4*scale, y + 2*scale, 8*scale, 5*scale);
        shapes.setColor(TRIM);
        shapes.rect(x + 7*scale, y + 5*scale, 2*scale, 9*scale);
        shapes.rect(x + 3*scale, y + 3*scale, 10*scale, 2*scale);

        // 手臂姿态随攻击、格挡、练功改变。
        shapes.setColor(SKIN);
        if (state == CharacterAnimationState.ATTACK) {
            shapes.rect(x + (direction > 0 ? 12 : -2)*scale, y + 8*scale, 5*scale, 3*scale);
        } else if (state == CharacterAnimationState.TRAIN || state == CharacterAnimationState.BLOCK) {
            shapes.rect(x + 1*scale, y + 11*scale, 4*scale, 3*scale);
            shapes.rect(x + 11*scale, y + 11*scale, 4*scale, 3*scale);
        } else {
            shapes.rect(x, y + 8*scale, 3*scale, 6*scale);
            shapes.rect(x + 13*scale, y + 8*scale, 3*scale, 6*scale);
        }

        // 头部、发髻与面部。
        shapes.setColor(SKIN);
        shapes.rect(x + 4*scale, y + 15*scale, 8*scale, 7*scale);
        shapes.setColor(INK);
        shapes.rect(x + 3*scale, y + 20*scale, 10*scale, 4*scale);
        shapes.rect(x + 5*scale, y + 23*scale, 6*scale, 2*scale);
        shapes.setColor(Color.WHITE);
        float eyeX = direction > 0 ? x + 9*scale : x + 5*scale;
        shapes.rect(eyeX, y + 18*scale, scale, scale);

        if (state == CharacterAnimationState.ATTACK) {
            shapes.setColor(TRIM);
            shapes.rect(x + (direction > 0 ? 15 : -7)*scale, y + 10*scale, 7*scale, scale);
        } else if (state == CharacterAnimationState.HURT) {
            shapes.setColor(new Color(0.85f, 0.18f, 0.14f, 1f));
            shapes.rect(x + 14*scale, y + 17*scale, 2*scale, 2*scale);
        }
        if (state == CharacterAnimationState.DEATH) {
            shapes.setColor(INK);
            shapes.rect(x + 1*scale, y + 2*scale, 14*scale, 2*scale);
        }
    }
}