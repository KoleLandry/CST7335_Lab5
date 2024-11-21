package com.landrykole.platformjumpgame;

public class Platform {
    public float x, y;
    private static final float SPEED = 5f;

    public Platform(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        x -= SPEED;
    }
}
