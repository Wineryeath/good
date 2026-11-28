package com.jianghu.combat;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CharacterAnimationControllerTest {
    @Test void blockPoseIsEnteredAndReturnsToIdleAfterItsWindow() {
        CharacterAnimationController animation = new CharacterAnimationController();
        animation.block();
        assertEquals(CharacterAnimationState.BLOCK, animation.state());
        animation.update(0.20f);
        assertEquals(CharacterAnimationState.BLOCK, animation.state());
        animation.update(0.13f);
        assertEquals(CharacterAnimationState.IDLE, animation.state());
    }

    @Test void deadCharacterCannotEnterBlockPose() {
        CharacterAnimationController animation = new CharacterAnimationController();
        animation.die();
        animation.block();
        assertEquals(CharacterAnimationState.DEATH, animation.state());
    }
}