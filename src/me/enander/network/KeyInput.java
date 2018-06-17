package me.enander.network;

import me.enander.network.Game;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyInput extends KeyAdapter {

    private Game game;

    public KeyInput(Game game) {
        this.game = game;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        System.out.println("Jump");
        //game.jump();
    }
}
