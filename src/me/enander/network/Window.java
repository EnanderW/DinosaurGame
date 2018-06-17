package me.enander.network;

import me.enander.network.Game;

import javax.swing.*;
import java.awt.*;

public class Window extends Canvas {

    public Window(int width, int height, String title, Game game) {
        JFrame frame = new JFrame(title);

        frame.setSize(width, height);
        frame.setDefaultCloseOperation(3);
        frame.setVisible(true);
        frame.add(game);
        game.start();
    }
}
