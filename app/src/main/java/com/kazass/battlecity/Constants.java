package com.kazass.battlecity;

public final class Constants {
    private Constants() {}

    public static final int MAP_COLS = 13;
    public static final int MAP_ROWS = 13;

    // Computed at runtime based on screen width; set by GameView
    public static int TILE_SIZE = 48;

    public static final int TARGET_FPS = 60;
    public static final long FRAME_MS = 1000 / TARGET_FPS;

    // Tank dimensions as fraction of tile
    public static final float TANK_SIZE_RATIO = 0.85f;

    // Bullet
    public static final float BULLET_SIZE_RATIO = 0.15f;
    public static final int BULLET_SPEED_PX = 8; // pixels per frame at TILE_SIZE=48

    // Player
    public static final int PLAYER_SPEED_PX = 3;
    public static final long PLAYER_SHOOT_COOLDOWN_MS = 500;
    public static final int PLAYER_LIVES = 3;

    // Enemy
    public static final int ENEMY_SPEED_PX = 2;
    public static final long ENEMY_SHOOT_COOLDOWN_MS = 1500;
    public static final long ENEMY_DIRECTION_CHANGE_MS = 2000;
    public static final int MAX_ENEMIES_ON_MAP = 4;
    public static final int TOTAL_ENEMIES = 20;

    // Score
    public static final int SCORE_PER_ENEMY = 100;

    // Colors (ARGB)
    public static final int COLOR_BG = 0xFF1A1A2E;
    public static final int COLOR_BRICK = 0xFFB84040;
    public static final int COLOR_BRICK_MORTAR = 0xFF7A2020;
    public static final int COLOR_STEEL = 0xFF8888AA;
    public static final int COLOR_STEEL_HIGHLIGHT = 0xFFBBBBDD;
    public static final int COLOR_WATER_1 = 0xFF1E5FAA;
    public static final int COLOR_WATER_2 = 0xFF2070CC;
    public static final int COLOR_TREES = 0xFF228822;
    public static final int COLOR_ICE = 0xFFAADDEE;
    public static final int COLOR_PLAYER_TANK = 0xFFFFDD00;
    public static final int COLOR_ENEMY_TANK = 0xFF888888;
    public static final int COLOR_ENEMY_TANK_FAST = 0xFFAACC44;
    public static final int COLOR_BULLET = 0xFFFFFFFF;
    public static final int COLOR_BASE_ALIVE = 0xFFFFAA00;
    public static final int COLOR_BASE_DEAD = 0xFF555555;
    public static final int COLOR_HUD_TEXT = 0xFFFFFFFF;
    public static final int COLOR_HUD_BG = 0xFF000000;
    public static final int COLOR_OVERLAY = 0xCC000000;
    public static final int COLOR_WIN_TEXT = 0xFF44FF44;
    public static final int COLOR_LOSE_TEXT = 0xFFFF4444;
    public static final int COLOR_BORDER = 0xFF444444;

    // Spawn columns (tile x) at row 0
    public static final int[] ENEMY_SPAWN_COLS = {0, 6, 12};
}
