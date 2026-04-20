package com.kazass.battlecity;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.kazass.battlecity.entity.Bullet;
import com.kazass.battlecity.entity.Direction;
import com.kazass.battlecity.entity.EnemyTank;
import com.kazass.battlecity.entity.PlayerTank;
import com.kazass.battlecity.input.TouchController;
import com.kazass.battlecity.map.GameMap;
import com.kazass.battlecity.map.TileType;
import com.kazass.battlecity.renderer.GameRenderer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread gameThread;
    private final GameRenderer renderer = new GameRenderer();
    private final TouchController controller = new TouchController();

    private GameMap map;
    private PlayerTank player;
    private final List<EnemyTank> enemies = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();

    private GameState state = GameState.PLAYING;
    private int score = 0;
    private int enemiesDefeated = 0;
    private int enemiesSpawned  = 0;
    private int spawnIndex = 0; // cycles through ENEMY_SPAWN_COLS

    private int screenWidth, screenHeight;

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
    }

    // ──────────────────────────────────────────────
    // Init
    // ──────────────────────────────────────────────

    private void initGame() {
        map = new GameMap();

        int ts = Constants.TILE_SIZE;
        // Player spawns at bottom-center-left (tile 4, row 12)
        float px = 4 * ts + (ts - ts * Constants.TANK_SIZE_RATIO) / 2f;
        float py = 12 * ts + (ts - ts * Constants.TANK_SIZE_RATIO) / 2f;
        player = new PlayerTank(px, py);

        enemies.clear();
        bullets.clear();
        score = 0;
        enemiesDefeated = 0;
        enemiesSpawned = 0;
        spawnIndex = 0;
        state = GameState.PLAYING;

        // Spawn initial batch
        for (int i = 0; i < Math.min(Constants.MAX_ENEMIES_ON_MAP, Constants.TOTAL_ENEMIES); i++) {
            spawnEnemy();
        }
    }

    private void spawnEnemy() {
        if (enemiesSpawned >= Constants.TOTAL_ENEMIES) return;
        int col = Constants.ENEMY_SPAWN_COLS[spawnIndex % Constants.ENEMY_SPAWN_COLS.length];
        spawnIndex++;
        int ts = Constants.TILE_SIZE;
        float ex = col * ts + (ts - ts * Constants.TANK_SIZE_RATIO) / 2f;
        float ey = (ts - ts * Constants.TANK_SIZE_RATIO) / 2f;
        int type = (enemiesSpawned % 5 == 4) ? 1 : 0; // every 5th is fast
        enemies.add(new EnemyTank(ex, ey, type));
        enemiesSpawned++;
    }

    // ──────────────────────────────────────────────
    // Update loop
    // ──────────────────────────────────────────────

    public void update(long nowMs) {
        if (state != GameState.PLAYING) return;

        renderer.tick();

        // Player input
        player.setInput(controller.getDirection(), controller.isFirePressed());
        player.update(map, nowMs);

        // Player shoot
        Bullet pb = player.tryShoot(nowMs);
        if (pb != null) bullets.add(pb);

        // Enemies update + shoot
        for (EnemyTank e : enemies) {
            e.update(map, nowMs);
            Bullet eb = e.tryShoot(nowMs);
            if (eb != null) bullets.add(eb);
        }

        // Bullets update + collision
        Iterator<Bullet> bit = bullets.iterator();
        while (bit.hasNext()) {
            Bullet b = bit.next();
            if (!b.active) { bit.remove(); continue; }
            b.update();

            // Out of bounds
            if (b.x < 0 || b.y < 0
                    || b.x > Constants.MAP_COLS * Constants.TILE_SIZE
                    || b.y > Constants.MAP_ROWS * Constants.TILE_SIZE) {
                b.active = false;
                bit.remove();
                continue;
            }

            // Bullet vs tiles
            int tc = (int)(b.x / Constants.TILE_SIZE);
            int tr = (int)(b.y / Constants.TILE_SIZE);
            TileType tile = map.getTile(tc, tr);
            if (!tile.isPassable()) {
                if (tile.destroyedByBullet()) {
                    map.hitTile(tc, tr);
                    if (!map.isBaseAlive()) {
                        state = GameState.GAME_OVER;
                    }
                }
                b.active = false;
                bit.remove();
                continue;
            }

            // Bullet vs tanks
            if (b.fromPlayer) {
                // Player bullet hits enemies
                Iterator<EnemyTank> eit = enemies.iterator();
                boolean hit = false;
                while (eit.hasNext()) {
                    EnemyTank e = eit.next();
                    if (e.alive && b.intersects(e.getLeft(), e.getTop(), e.getRight(), e.getBottom())) {
                        e.alive = false;
                        eit.remove();
                        b.active = false;
                        score += Constants.SCORE_PER_ENEMY;
                        enemiesDefeated++;
                        hit = true;
                        break;
                    }
                }
                if (hit) { bit.remove(); continue; }
            } else {
                // Enemy bullet hits player
                if (player.alive && b.intersects(
                        player.getLeft(), player.getTop(),
                        player.getRight(), player.getBottom())) {
                    b.active = false;
                    bit.remove();
                    player.alive = false;
                    player.lives--;
                    if (player.lives <= 0) {
                        state = GameState.GAME_OVER;
                    } else {
                        // Respawn player after short delay (just respawn immediately for now)
                        respawnPlayer();
                    }
                    continue;
                }
            }
        }

        // Spawn more enemies
        int aliveCount = enemies.size();
        while (aliveCount < Constants.MAX_ENEMIES_ON_MAP
                && enemiesSpawned < Constants.TOTAL_ENEMIES) {
            spawnEnemy();
            aliveCount++;
        }

        // Win condition
        if (enemiesDefeated >= Constants.TOTAL_ENEMIES) {
            state = GameState.WIN;
        }
    }

    private void respawnPlayer() {
        int ts = Constants.TILE_SIZE;
        float px = 4 * ts + (ts - ts * Constants.TANK_SIZE_RATIO) / 2f;
        float py = 12 * ts + (ts - ts * Constants.TANK_SIZE_RATIO) / 2f;
        player.respawn(px, py);
    }

    // ──────────────────────────────────────────────
    // Render
    // ──────────────────────────────────────────────

    public void render(Canvas canvas) {
        int enemiesLeft = Constants.TOTAL_ENEMIES - enemiesDefeated;
        renderer.draw(canvas, map, player, enemies, bullets,
                state, score, enemiesLeft, screenWidth, screenHeight);
    }

    // ──────────────────────────────────────────────
    // SurfaceHolder.Callback
    // ──────────────────────────────────────────────

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        screenWidth  = getWidth();
        screenHeight = getHeight();

        // Compute tile size to fill width
        Constants.TILE_SIZE = screenWidth / Constants.MAP_COLS;
        controller.setScreenWidth(screenWidth);

        initGame();

        gameThread = new GameThread(holder, this);
        gameThread.setRunning(true);
        gameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        screenWidth  = width;
        screenHeight = height;
        Constants.TILE_SIZE = width / Constants.MAP_COLS;
        controller.setScreenWidth(width);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        gameThread.setRunning(false);
        while (retry) {
            try {
                gameThread.join();
                retry = false;
            } catch (InterruptedException ignored) {}
        }
    }

    // ──────────────────────────────────────────────
    // Touch input
    // ──────────────────────────────────────────────

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (state == GameState.GAME_OVER || state == GameState.WIN) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                initGame();
            }
            return true;
        }
        controller.onTouchEvent(event);
        return true;
    }
}
