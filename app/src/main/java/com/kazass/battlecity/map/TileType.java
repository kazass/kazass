package com.kazass.battlecity.map;

public enum TileType {
    EMPTY,   // 0
    BRICK,   // 1 - destructible
    STEEL,   // 2 - indestructible
    WATER,   // 3 - impassable, bullet passes
    TREES,   // 4 - passable, hides tank
    ICE,     // 5 - passable, slippery
    BASE;    // 6 - eagle, game over if destroyed

    public boolean isPassable() {
        return this == EMPTY || this == TREES || this == ICE;
    }

    public boolean blocksBullet() {
        return this == STEEL || this == BASE;
    }

    public boolean destroyedByBullet() {
        return this == BRICK || this == BASE;
    }

    public static TileType fromInt(int v) {
        TileType[] vals = values();
        if (v < 0 || v >= vals.length) return EMPTY;
        return vals[v];
    }
}
