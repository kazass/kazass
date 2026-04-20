package com.kazass.battlecity.entity;

import com.kazass.battlecity.Constants;

public class Bullet {

    public float x, y; // center position in pixels
    public final Direction direction;
    public final boolean fromPlayer;
    public boolean active = true;

    private final float speed;
    private final float size;

    public Bullet(float x, float y, Direction direction, boolean fromPlayer) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.fromPlayer = fromPlayer;
        this.speed = Constants.BULLET_SPEED_PX * (Constants.TILE_SIZE / 48f);
        this.size = Constants.TILE_SIZE * Constants.BULLET_SIZE_RATIO;
    }

    public void update() {
        x += direction.dx() * speed;
        y += direction.dy() * speed;
    }

    public float getLeft()   { return x - size / 2; }
    public float getTop()    { return y - size / 2; }
    public float getRight()  { return x + size / 2; }
    public float getBottom() { return y + size / 2; }
    public float getSize()   { return size; }

    public boolean intersects(float left, float top, float right, float bottom) {
        return getRight() > left && getLeft() < right
            && getBottom() > top && getTop() < bottom;
    }
}
