package com.jianghu.combat;

/** Renderer-independent animation state machine; frame timing is deterministic and testable. */
public final class CharacterAnimationController {
    private CharacterAnimationState state = CharacterAnimationState.IDLE;
    private float stateTime;
    private int facing = 1;
    private boolean dead;
    public CharacterAnimationState state() { return state; }
    public float stateTime() { return stateTime; }
    public int facing() { return facing; }
    public int frame(int frameCount, float frameDuration) {
        if (frameCount < 1 || frameDuration <= 0) throw new IllegalArgumentException("动画帧参数无效");
        int index = (int)(stateTime / frameDuration);
        return state == CharacterAnimationState.DEATH ? Math.min(frameCount - 1, index) : index % frameCount;
    }
    public void face(int direction) { if (direction != 0) facing = direction < 0 ? -1 : 1; }
    public void walk(float dx) { if (!dead) { face(dx < 0 ? -1 : dx > 0 ? 1 : facing); transition(dx == 0 ? CharacterAnimationState.IDLE : CharacterAnimationState.WALK); } }
    public void attack() { if (!dead) transition(CharacterAnimationState.ATTACK); }
    public void block() { if (!dead) transition(CharacterAnimationState.BLOCK); }
    public void hurt() { if (!dead) transition(CharacterAnimationState.HURT); }
    public void train() { if (!dead) transition(CharacterAnimationState.TRAIN); }
    public void die() { dead = true; transition(CharacterAnimationState.DEATH); }
    public void revive() { dead = false; transition(CharacterAnimationState.IDLE); }
    public void update(float deltaSeconds) {
        if (deltaSeconds < 0) throw new IllegalArgumentException("动画时间不能倒退");
        stateTime += deltaSeconds;
        if ((state == CharacterAnimationState.ATTACK || state == CharacterAnimationState.HURT || state == CharacterAnimationState.BLOCK) && stateTime >= 0.32f)
            transition(CharacterAnimationState.IDLE);
        if (state == CharacterAnimationState.TRAIN && stateTime >= 0.8f) transition(CharacterAnimationState.IDLE);
    }
    private void transition(CharacterAnimationState next) { if (state != next) { state = next; stateTime = 0; } }
}