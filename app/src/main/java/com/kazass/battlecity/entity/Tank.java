package com.kazass.battlecity.entity;

import com.kazass.battlecity.Constants;
import com.kazass.battlecity.map.GameMap;

public abstract class Tank {

    public float x, y;        // top-left pixel position
    public Direction direction = Direction.UP;
    public boolean alive = true;

    protected float speed;
    protected long lastShotTime = 0;
    protected long shootCooldown;
    protected final float tankSize;

    public Tank(float x, float y, float speed, long shootCooldown) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.shootCooldown = shootCooldown;
        this.tankSize = Constants.TILE_SIZE * Constants.TANK_SIZE_RATIO;
    }

    protected boolean tryMove(Direction dir, GameMap map) {
        float nx = x + dir.dx() * speed;
        float ny = y + dir.dy() * speed;

        // Snap to tile grid on the perpendicular axis to avoid getting stuck on corners
        int ts = Constants.TILE_SIZE;
        if (dir == Direction.LEFT || dir == Direction.RIGHT) {
            float snapped = Math.round(ny / ts) * ts + (ts - tankSize) / 2f;
            float diff = snapped - ny;
            if (Math.abs(diff) <= speed) ny = snapped;
        } else {
            float snapped = Math.round(nx / ts) * ts + (ts - tankSize) / 2f;
            float diff = snapped - nx;
            if (Math.abs(diff) <= speed) nx = snapped;
        }

        if (map.canMove(nx, ny, tankSize)) {
            x = nx;
            y = ny;
            return true;
        }
        return false;
    }

    public Bullet shoot(long nowMs) {
        if (nowMs - lastShotTime < shootCooldown) return null;
        lastShotTime = nowMs;
        float cx = x + tankSize / 2;
        float cy = y + tankSize / 2;
        return new Bullet(cx, cy, direction, isPlayer());
    }

    public float getLeft()   { return x; }
    public float getTop()    { return y; }
    public float getRight()  { return x + tankSize; }
    public float getBottom() { return y + tankSize; }
    public float getSize()   { return tankSize; }

    public boolean intersects(float left, float top, float right, float bottom) {
        return getRight() > left && getLeft() < right
            && getBottom() > top && getTop() < bottom;
    }

    public abstract boolean isPlayer();

    public abstract void update(GameMap map, long nowMs);
}
