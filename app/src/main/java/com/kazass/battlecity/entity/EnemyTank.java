package com.kazass.battlecity.entity;

import com.kazass.battlecity.Constants;
import com.kazass.battlecity.map.GameMap;

public class EnemyTank extends Tank {

    public final int type; // 0=basic, 1=fast
    private long lastDirectionChange = 0;
    private long nextDirectionInterval;

    public EnemyTank(float x, float y, int type) {
        super(x, y,
              (type == 1 ? Constants.ENEMY_SPEED_PX * 1.5f : Constants.ENEMY_SPEED_PX)
                  * (Constants.TILE_SIZE / 48f),
              Constants.ENEMY_SHOOT_COOLDOWN_MS);
        this.type = type;
        this.direction = Direction.DOWN;
        this.nextDirectionInterval = Constants.ENEMY_DIRECTION_CHANGE_MS
                + (long) (Math.random() * 1000);
    }

    @Override
    public void update(GameMap map, long nowMs) {
        if (!alive) return;

        boolean moved = tryMove(direction, map);

        if (!moved || (nowMs - lastDirectionChange > nextDirectionInterval)) {
            pickNewDirection();
            lastDirectionChange = nowMs;
            nextDirectionInterval = Constants.ENEMY_DIRECTION_CHANGE_MS
                    + (long) (Math.random() * 1000);
        }
    }

    /** Returns a bullet if it's time to fire, else null. */
    public Bullet tryShoot(long nowMs) {
        if (!alive) return null;
        return shoot(nowMs);
    }

    private void pickNewDirection() {
        Direction[] dirs = Direction.values();
        Direction newDir;
        int tries = 0;
        do {
            newDir = dirs[(int) (Math.random() * dirs.length)];
            tries++;
        } while (newDir == direction && tries < 5);
        direction = newDir;
    }

    @Override
    public boolean isPlayer() { return false; }
}
