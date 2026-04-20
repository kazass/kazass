package com.kazass.battlecity.entity;

import com.kazass.battlecity.Constants;
import com.kazass.battlecity.map.GameMap;

public class PlayerTank extends Tank {

    public int lives;
    public int starLevel = 1; // 1-4 upgrades

    private Direction inputDirection = null;
    private boolean fireInput = false;

    public PlayerTank(float x, float y) {
        super(x, y,
              Constants.PLAYER_SPEED_PX * (Constants.TILE_SIZE / 48f),
              Constants.PLAYER_SHOOT_COOLDOWN_MS);
        this.lives = Constants.PLAYER_LIVES;
        this.direction = Direction.UP;
    }

    public void setInput(Direction dir, boolean fire) {
        this.inputDirection = dir;
        this.fireInput = fire;
    }

    @Override
    public void update(GameMap map, long nowMs) {
        if (!alive) return;
        if (inputDirection != null) {
            direction = inputDirection;
            tryMove(inputDirection, map);
        }
    }

    /** Called by GameView each frame after update(); returns bullet or null. */
    public Bullet tryShoot(long nowMs) {
        if (!alive) return null;
        if (fireInput) {
            return shoot(nowMs);
        }
        return null;
    }

    public void respawn(float x, float y) {
        this.x = x;
        this.y = y;
        this.alive = true;
        this.starLevel = 1;
        this.direction = Direction.UP;
    }

    @Override
    public boolean isPlayer() { return true; }
}
