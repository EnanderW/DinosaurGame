package me.enander.network;

import java.awt.*;

public class Dino {

    protected Game game;
    protected float x, y, velY;
    protected float gravity = 0.17f, groundY;

    private Genome genome;

    public Dino(Genome genome, float x, float y, Game game) {
        this.genome = genome;
        this.game = game;
        this.x = x;
        this.y = y;
        groundY = y;
    }

    public void jump() {
        velY = 7f;
    }

    public void update(float speed, float distanceToNextObstacle) {
        if (y >= groundY) {
            double[] output = genome.calculate(speed / 10.0f, distanceToNextObstacle / 1000.0f);
            for (double d : output) {
                if (d > 0.5) {
                    jump();
                }
            }
        }

        y -= (velY * Game.delta);

        if (y < groundY) {
            velY -= (gravity * Game.delta);
        } else {
            velY = 0;
            y = groundY;
        }
    }

    public void draw(Graphics g) {
        g.drawRect((int) x, (int) y, 50, 50);
    }

    public void setFitness(float fitness) {
        genome.setFitness(fitness);
    }

    public void setGenome(Genome genome) {
        this.genome = genome;
    }

    public Genome getGenome() {
        return genome;
    }
}
