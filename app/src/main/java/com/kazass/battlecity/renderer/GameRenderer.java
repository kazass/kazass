package com.kazass.battlecity.renderer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.kazass.battlecity.Constants;
import com.kazass.battlecity.GameState;
import com.kazass.battlecity.entity.Bullet;
import com.kazass.battlecity.entity.Direction;
import com.kazass.battlecity.entity.EnemyTank;
import com.kazass.battlecity.entity.PlayerTank;
import com.kazass.battlecity.map.GameMap;
import com.kazass.battlecity.map.TileType;

import java.util.List;

public class GameRenderer {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    // Animated water tick (alternate every 30 frames)
    private int waterTick = 0;

    public void tick() {
        waterTick++;
    }

    // ──────────────────────────────────────────────
    // Main draw entry
    // ──────────────────────────────────────────────

    public void draw(Canvas canvas, GameMap map, PlayerTank player,
                     List<EnemyTank> enemies, List<Bullet> bullets,
                     GameState state, int score, int enemiesLeft,
                     int screenWidth, int screenHeight) {

        int ts = Constants.TILE_SIZE;
        int mapWidth  = Constants.MAP_COLS * ts;
        int mapHeight = Constants.MAP_ROWS * ts;
        int hudHeight = screenHeight - mapHeight;
        int controlsHeight = hudHeight > 0 ? hudHeight : (int)(screenHeight * 0.2f);

        // Background
        paint.setColor(Constants.COLOR_BG);
        canvas.drawRect(0, 0, screenWidth, screenHeight, paint);

        // Map tiles (draw all except TREES first, trees on top)
        drawMap(canvas, map, false);

        // Bullets
        for (Bullet b : bullets) {
            if (b.active) drawBullet(canvas, b);
        }

        // Enemies
        for (EnemyTank e : enemies) {
            if (e.alive) drawTank(canvas, e.x, e.y, e.direction, false, e.type);
        }

        // Player
        if (player.alive) {
            drawTank(canvas, player.x, player.y, player.direction, true, 0);
        }

        // Trees layer (drawn on top to hide tanks underneath)
        drawMap(canvas, map, true);

        // Controls overlay
        drawControls(canvas, screenWidth, screenHeight, controlsHeight);

        // HUD
        drawHUD(canvas, player, score, enemiesLeft, screenWidth, mapHeight);

        // Overlay for game over / win
        if (state == GameState.GAME_OVER || state == GameState.WIN) {
            drawGameOverlay(canvas, state, score, screenWidth, screenHeight);
        }
    }

    // ──────────────────────────────────────────────
    // Map tiles
    // ──────────────────────────────────────────────

    private void drawMap(Canvas canvas, GameMap map, boolean treesOnly) {
        int ts = Constants.TILE_SIZE;
        TileType[][] grid = map.getGrid();
        for (int r = 0; r < Constants.MAP_ROWS; r++) {
            for (int c = 0; c < Constants.MAP_COLS; c++) {
                TileType t = grid[r][c];
                float left = c * ts;
                float top  = r * ts;
                if (treesOnly) {
                    if (t == TileType.TREES) drawTrees(canvas, left, top, ts);
                } else {
                    drawTile(canvas, t, left, top, ts, map.isBaseAlive());
                }
            }
        }
    }

    private void drawTile(Canvas canvas, TileType t, float left, float top, int ts, boolean baseAlive) {
        switch (t) {
            case BRICK: drawBrick(canvas, left, top, ts); break;
            case STEEL: drawSteel(canvas, left, top, ts); break;
            case WATER: drawWater(canvas, left, top, ts); break;
            case ICE:   drawIce(canvas, left, top, ts);   break;
            case BASE:  drawBase(canvas, left, top, ts, baseAlive); break;
            default: break;
        }
    }

    private void drawBrick(Canvas canvas, float left, float top, int ts) {
        paint.setColor(Constants.COLOR_BRICK);
        canvas.drawRect(left, top, left + ts, top + ts, paint);
        paint.setColor(Constants.COLOR_BRICK_MORTAR);
        paint.setStrokeWidth(1f);
        // Horizontal mortar lines
        canvas.drawLine(left, top + ts * 0.5f, left + ts, top + ts * 0.5f, paint);
        // Vertical mortar lines (staggered)
        canvas.drawLine(left + ts * 0.5f, top, left + ts * 0.5f, top + ts * 0.5f, paint);
        canvas.drawLine(left, top + ts * 0.5f, left, top + ts, paint);
        canvas.drawLine(left + ts, top + ts * 0.5f, left + ts, top + ts, paint);
    }

    private void drawSteel(Canvas canvas, float left, float top, int ts) {
        paint.setColor(Constants.COLOR_STEEL);
        canvas.drawRect(left, top, left + ts, top + ts, paint);
        paint.setColor(Constants.COLOR_STEEL_HIGHLIGHT);
        paint.setStrokeWidth(1.5f);
        canvas.drawLine(left + 2, top + 2, left + ts - 2, top + 2, paint);
        canvas.drawLine(left + 2, top + 2, left + 2, top + ts - 2, paint);
    }

    private void drawWater(Canvas canvas, float left, float top, int ts) {
        boolean alt = (waterTick / 30) % 2 == 0;
        paint.setColor(alt ? Constants.COLOR_WATER_1 : Constants.COLOR_WATER_2);
        canvas.drawRect(left, top, left + ts, top + ts, paint);
        // Simple wave lines
        paint.setColor(alt ? Constants.COLOR_WATER_2 : Constants.COLOR_WATER_1);
        paint.setStrokeWidth(1.5f);
        float y1 = top + ts * 0.35f;
        float y2 = top + ts * 0.65f;
        canvas.drawLine(left + 2, y1, left + ts - 2, y1, paint);
        canvas.drawLine(left + 2, y2, left + ts - 2, y2, paint);
    }

    private void drawIce(Canvas canvas, float left, float top, int ts) {
        paint.setColor(Constants.COLOR_ICE);
        canvas.drawRect(left, top, left + ts, top + ts, paint);
    }

    private void drawTrees(Canvas canvas, float left, float top, int ts) {
        paint.setColor(Constants.COLOR_TREES);
        canvas.drawRect(left, top, left + ts, top + ts, paint);
        // Darker inner dots
        paint.setColor(0xFF114411);
        float d = ts * 0.2f;
        canvas.drawCircle(left + ts * 0.3f, top + ts * 0.4f, d, paint);
        canvas.drawCircle(left + ts * 0.7f, top + ts * 0.3f, d, paint);
        canvas.drawCircle(left + ts * 0.5f, top + ts * 0.7f, d, paint);
    }

    private void drawBase(Canvas canvas, float left, float top, int ts, boolean alive) {
        int color = alive ? Constants.COLOR_BASE_ALIVE : Constants.COLOR_BASE_DEAD;
        float cx = left + ts / 2f;
        float cy = top  + ts / 2f;
        float r  = ts * 0.38f;

        // Draw an eagle/star shape
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        Path star = new Path();
        for (int i = 0; i < 5; i++) {
            double angle = Math.toRadians(i * 72 - 90);
            float x = cx + (float)(r * Math.cos(angle));
            float y = cy + (float)(r * Math.sin(angle));
            if (i == 0) star.moveTo(x, y);
            else star.lineTo(x, y);
            double innerAngle = angle + Math.toRadians(36);
            float ix = cx + (float)(r * 0.45f * Math.cos(innerAngle));
            float iy = cy + (float)(r * 0.45f * Math.sin(innerAngle));
            star.lineTo(ix, iy);
        }
        star.close();
        canvas.drawPath(star, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xFF000000);
        paint.setStrokeWidth(1f);
        canvas.drawPath(star, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    // ──────────────────────────────────────────────
    // Tank
    // ──────────────────────────────────────────────

    private void drawTank(Canvas canvas, float x, float y, Direction dir,
                          boolean isPlayer, int type) {
        float ts = Constants.TILE_SIZE;
        float sz = Constants.TILE_SIZE * Constants.TANK_SIZE_RATIO;
        int bodyColor = isPlayer
                ? Constants.COLOR_PLAYER_TANK
                : (type == 1 ? Constants.COLOR_ENEMY_TANK_FAST : Constants.COLOR_ENEMY_TANK);

        // Body
        paint.setColor(bodyColor);
        canvas.drawRect(x, y, x + sz, y + sz, paint);

        // Tracks (dark strips on sides)
        paint.setColor(0x88000000);
        float trackW = sz * 0.18f;
        canvas.drawRect(x, y, x + trackW, y + sz, paint);
        canvas.drawRect(x + sz - trackW, y, x + sz, y + sz, paint);

        // Barrel
        paint.setColor(bodyColor);
        float cx = x + sz / 2;
        float cy = y + sz / 2;
        float barrelW = sz * 0.18f;
        float barrelH = sz * 0.45f;
        float bx, by, bw, bh;
        switch (dir) {
            case UP:    bx = cx - barrelW/2; by = y - barrelH * 0.3f; bw = barrelW; bh = barrelH; break;
            case DOWN:  bx = cx - barrelW/2; by = cy;                 bw = barrelW; bh = barrelH; break;
            case LEFT:  bx = x - barrelH * 0.3f; by = cy - barrelW/2; bw = barrelH; bh = barrelW; break;
            default:    bx = cx;             by = cy - barrelW/2;     bw = barrelH; bh = barrelW; break;
        }
        canvas.drawRect(bx, by, bx + bw, by + bh, paint);

        // Outline
        paint.setColor(0x88000000);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        canvas.drawRect(x, y, x + sz, y + sz, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    // ──────────────────────────────────────────────
    // Bullet
    // ──────────────────────────────────────────────

    private void drawBullet(Canvas canvas, Bullet b) {
        paint.setColor(Constants.COLOR_BULLET);
        float r = b.getSize() / 2;
        canvas.drawCircle(b.x, b.y, r, paint);
    }

    // ──────────────────────────────────────────────
    // On-screen controls
    // ──────────────────────────────────────────────

    private void drawControls(Canvas canvas, int screenWidth, int screenHeight, int controlsHeight) {
        int ts = Constants.TILE_SIZE;
        int mapBottom = Constants.MAP_ROWS * ts;
        float controlTop = mapBottom;
        float controlBottom = screenHeight;

        // Background strip
        paint.setColor(0xFF111111);
        canvas.drawRect(0, controlTop, screenWidth, controlBottom, paint);

        float divX = screenWidth * 0.6f;

        // D-pad hint (left side)
        float dpCx = divX * 0.5f;
        float dpCy = (controlTop + controlBottom) / 2f;
        float dpR  = Math.min(divX * 0.35f, (controlBottom - controlTop) * 0.4f);

        paint.setColor(0x44FFFFFF);
        // D-pad arrows
        drawArrow(canvas, dpCx, dpCy - dpR, Direction.UP,   dpR * 0.5f);
        drawArrow(canvas, dpCx, dpCy + dpR, Direction.DOWN, dpR * 0.5f);
        drawArrow(canvas, dpCx - dpR, dpCy, Direction.LEFT, dpR * 0.5f);
        drawArrow(canvas, dpCx + dpR, dpCy, Direction.RIGHT,dpR * 0.5f);

        // Center circle
        paint.setColor(0x22FFFFFF);
        canvas.drawCircle(dpCx, dpCy, dpR * 0.3f, paint);

        // Fire button (right side)
        float fireCx = divX + (screenWidth - divX) / 2f;
        float fireCy = (controlTop + controlBottom) / 2f;
        float fireR  = Math.min((screenWidth - divX) * 0.35f, (controlBottom - controlTop) * 0.4f);

        paint.setColor(0x55FF4444);
        canvas.drawCircle(fireCx, fireCy, fireR, paint);
        paint.setColor(0x99FFFFFF);
        paint.setTextSize(fireR * 0.6f);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("FIRE", fireCx, fireCy + fireR * 0.2f, paint);
        paint.setTextAlign(Paint.Align.LEFT);

        // Divider
        paint.setColor(0x44FFFFFF);
        paint.setStrokeWidth(1f);
        canvas.drawLine(divX, controlTop, divX, controlBottom, paint);
    }

    private void drawArrow(Canvas canvas, float cx, float cy, Direction dir, float size) {
        Path p = new Path();
        switch (dir) {
            case UP:
                p.moveTo(cx, cy - size); p.lineTo(cx - size, cy + size * 0.5f);
                p.lineTo(cx + size, cy + size * 0.5f); break;
            case DOWN:
                p.moveTo(cx, cy + size); p.lineTo(cx - size, cy - size * 0.5f);
                p.lineTo(cx + size, cy - size * 0.5f); break;
            case LEFT:
                p.moveTo(cx - size, cy); p.lineTo(cx + size * 0.5f, cy - size);
                p.lineTo(cx + size * 0.5f, cy + size); break;
            default:
                p.moveTo(cx + size, cy); p.lineTo(cx - size * 0.5f, cy - size);
                p.lineTo(cx - size * 0.5f, cy + size); break;
        }
        p.close();
        canvas.drawPath(p, paint);
    }

    // ──────────────────────────────────────────────
    // HUD
    // ──────────────────────────────────────────────

    private void drawHUD(Canvas canvas, PlayerTank player, int score,
                         int enemiesLeft, int screenWidth, int mapHeight) {
        // Thin bar above the map
        float hudH = Constants.TILE_SIZE * 0.6f;
        if (mapHeight == 0) return;

        // No top HUD — just draw border around map
        paint.setColor(Constants.COLOR_BORDER);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        canvas.drawRect(0, 0, Constants.MAP_COLS * Constants.TILE_SIZE,
                Constants.MAP_ROWS * Constants.TILE_SIZE, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    // ──────────────────────────────────────────────
    // Game Over / Win overlay
    // ──────────────────────────────────────────────

    private void drawGameOverlay(Canvas canvas, GameState state, int score,
                                  int screenWidth, int screenHeight) {
        paint.setColor(Constants.COLOR_OVERLAY);
        canvas.drawRect(0, 0, screenWidth, screenHeight, paint);

        float cx = screenWidth / 2f;
        float cy = screenHeight / 2f;

        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);

        if (state == GameState.WIN) {
            paint.setColor(Constants.COLOR_WIN_TEXT);
            paint.setTextSize(screenWidth * 0.12f);
            canvas.drawText("YOU WIN!", cx, cy - screenHeight * 0.08f, paint);
        } else {
            paint.setColor(Constants.COLOR_LOSE_TEXT);
            paint.setTextSize(screenWidth * 0.12f);
            canvas.drawText("GAME OVER", cx, cy - screenHeight * 0.08f, paint);
        }

        paint.setColor(Constants.COLOR_HUD_TEXT);
        paint.setTextSize(screenWidth * 0.06f);
        canvas.drawText("SCORE: " + score, cx, cy + screenHeight * 0.02f, paint);

        paint.setTextSize(screenWidth * 0.045f);
        canvas.drawText("Tap to restart", cx, cy + screenHeight * 0.1f, paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.DEFAULT);
    }
}
