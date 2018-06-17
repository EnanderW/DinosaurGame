package me.enander.network;

import me.enander.network.Game;

import java.awt.*;
import java.util.Random;

public class Obstacle {

    private float x = 800, sizeX, sizeY, type;

    public Obstacle() {

        int random = new Random().nextInt(3) + 1;
        this.type = random;
        switch (random) {
            case 1:
                sizeX = 15;
                sizeY = 30;
                break;
            case 2:
                sizeX = 25;
                sizeY = 50;
                break;
            case 3:
                sizeY = 15;
                sizeX = 30;
                break;
        }
    }

    public void update(float speed) {
        x -= speed * Game.delta;
    }

    public void draw(Graphics g) {
        g.fillRect((int) x, 320 - (int) sizeY, (int) sizeX, (int) sizeY);
    }

    public float getX() {
        return x;
    }

    public float getSizeX() {
        return sizeX;
    }

    public float getSizeY() {
        return sizeY;
    }
}
