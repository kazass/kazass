package com.kazass.battlecity.map;

import com.kazass.battlecity.Constants;

public class GameMap {

    private final TileType[][] grid;
    private boolean baseAlive = true;

    public GameMap() {
        int rows = Constants.MAP_ROWS;
        int cols = Constants.MAP_COLS;
        grid = new TileType[rows][cols];
        int[][] src = Level.DATA;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = TileType.fromInt(src[r][c]);
            }
        }
    }

    public TileType getTile(int col, int row) {
        if (col < 0 || col >= Constants.MAP_COLS || row < 0 || row >= Constants.MAP_ROWS) {
            return TileType.STEEL; // treat out-of-bounds as indestructible wall
        }
        return grid[row][col];
    }

    /** Returns true if the tile was destroyed. */
    public boolean hitTile(int col, int row) {
        TileType t = getTile(col, row);
        if (t == TileType.BRICK) {
            grid[row][col] = TileType.EMPTY;
            return true;
        }
        if (t == TileType.BASE) {
            grid[row][col] = TileType.EMPTY;
            baseAlive = false;
            return true;
        }
        return false;
    }

    /**
     * Check if a rectangle (in pixel coords) can move to (newLeft, newTop).
     * Returns true if the path is clear.
     */
    public boolean canMove(float newLeft, float newTop, float size) {
        int ts = Constants.TILE_SIZE;
        float newRight = newLeft + size - 1;
        float newBottom = newTop + size - 1;

        // Clamp to map bounds
        if (newLeft < 0 || newTop < 0) return false;
        if (newRight >= Constants.MAP_COLS * ts || newBottom >= Constants.MAP_ROWS * ts) return false;

        // Check all four corners
        int c0 = (int) (newLeft / ts);
        int c1 = (int) (newRight / ts);
        int r0 = (int) (newTop / ts);
        int r1 = (int) (newBottom / ts);

        for (int r = r0; r <= r1; r++) {
            for (int c = c0; c <= c1; c++) {
                if (!getTile(c, r).isPassable()) return false;
            }
        }
        return true;
    }

    public boolean isBaseAlive() {
        return baseAlive;
    }

    public TileType[][] getGrid() {
        return grid;
    }
}
