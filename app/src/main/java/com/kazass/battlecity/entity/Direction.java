package com.kazass.battlecity.entity;

public enum Direction {
    UP, DOWN, LEFT, RIGHT;

    public int dx() {
        return this == RIGHT ? 1 : this == LEFT ? -1 : 0;
    }

    public int dy() {
        return this == DOWN ? 1 : this == UP ? -1 : 0;
    }

    public static Direction random() {
        Direction[] vals = values();
        return vals[(int) (Math.random() * vals.length)];
    }
}
