package me.enander.network;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game extends Canvas implements Runnable {

    public static void main(String[] args) {
        new Game();
    }

    //private List<Genome> genomes;
    private List<Specie> species;

    public GeneticAlgorithm geneticAlgorithm;

    private Thread thread;
    private boolean running = false;

    public static double delta = 0;

    protected float speed = 1.5f, minimumTimeBetweenObstacles = 150, obstacleTimer = 0, randomAddition = 0;
    protected float score = 0;

    private java.util.List<Dino> totalDinos;
    private java.util.List<Dino> entitiesToUpdate;
    public java.util.List<Dino> pendingEntities;
    private java.util.List<Obstacle> obstacles = new ArrayList<>();

    public Game() {
        entitiesToUpdate = new ArrayList<>();
        pendingEntities = new ArrayList<>();
        totalDinos = new ArrayList<>();
        //Do neural network stuff
        geneticAlgorithm = new GeneticAlgorithm() {
            @Override
            public float evaluateFitness(Genome genome) {
                return genome.getFitness();
            }
        };

        species = new ArrayList<>();

        for (int i = 0; i < GeneticAlgorithm.populationSize; i++) {
            Genome startingGenome = new Genome(new Counter(), new Counter());
            int input1 = geneticAlgorithm.newNeuron(startingGenome);
            int input2 = geneticAlgorithm.newNeuron(startingGenome);
            int input3 = geneticAlgorithm.newNeuron(startingGenome);
            int input4 = geneticAlgorithm.newNeuron(startingGenome);
            int output1 = geneticAlgorithm.newNeuron( startingGenome);

            Connection inToOut1 = geneticAlgorithm.newConnection(input1, output1, -41.5f, startingGenome);
            Connection inToOut2 = geneticAlgorithm.newConnection(input2, output1, -41.5f, startingGenome);
            Connection inToOut3 = geneticAlgorithm.newConnection(input3, output1, -41.5f, startingGenome);
            Connection inToOut4 = geneticAlgorithm.newConnection(input4, output1, -41.5f, startingGenome);

            startingGenome.addNeuron(input1, Genome.Type.INPUT);
            startingGenome.addNeuron(input2, Genome.Type.INPUT);
            startingGenome.addNeuron(input3, Genome.Type.INPUT);
            startingGenome.addNeuron(input4, Genome.Type.INPUT);
            startingGenome.addNeuron(output1, Genome.Type.OUTPUT);

            startingGenome.addConnection(inToOut1);
            startingGenome.addConnection(inToOut2);
            startingGenome.addConnection(inToOut3);
            startingGenome.addConnection(inToOut4);

            for (Connection connection : startingGenome.getConnections().values()) {
                System.out.println("W: " + connection.getWeight());
            }

            Dino dino = new Dino(startingGenome, 60, 270, this);
            totalDinos.add(dino);
            entitiesToUpdate.add(dino);
        }

        this.addKeyListener(new KeyInput(this));
        new Window(800, 400, "Dinosaur Game", this);
    }

    public List<Dino> getEntitiesToUpdate() {
        return getEntitiesToUpdate();
    }

    public synchronized void start() {
        thread = new Thread(this);
        thread.start();
        running = true;
    }

    public void reset() {
        List<Genome> genomes = new ArrayList<>();
        for (int i = 0; i < totalDinos.size(); i++) {
            Dino dino = totalDinos.get(i);
            genomes.add(dino.getGenome());
        }

         GeneticAlgorithm.GeneticObject newGenomes = geneticAlgorithm.evolve(genomes, species, this);
        for (int i = 0; i < totalDinos.size(); i++) {
            Dino dino = totalDinos.get(i);
            dino.setGenome(newGenomes.genomes.get(i));
        }

        this.species = newGenomes.species;

        for (Dino dino : totalDinos) {
            entitiesToUpdate.add(dino);
            dino.x = 60;
            dino.y = 270;
        }

        obstacleTimer = 0;
        randomAddition = 0;
        score = 0;
        speed = 1.5f;
        obstacles.clear();
    }

    public synchronized void stop() {
        try {
            thread.join();
            running = false;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = 144;
        double ns = 1000000000 / amountOfTicks;
        long timer =  System.currentTimeMillis();
        int frames = 0;
        while(running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            while (delta >= 1) {
                update();
                delta--;
            }

            draw();

            frames++;

            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                System.out.println("FPS: " + frames);
                frames = 0;
            }
        }

        stop();
    }

    public void update() {
        for (Dino entity : pendingEntities) {
            entitiesToUpdate.remove(entity);
        }

        pendingEntities.clear();

        if (entitiesToUpdate.isEmpty()) {
            reset();
            return;
        }

        for (Dino entity : entitiesToUpdate) {
            Obstacle find = findNextObstacle();
            if (find != null) {
                entity.update(speed, find.getX() - entity.x, find.getSizeY(), find.getSizeX());
                Rectangle rectangle = new Rectangle((int) entity.x, (int) entity.y, 50, 50);
                Rectangle r = new Rectangle((int) find.getX(), 320 - (int) find.getSizeY(), (int) find.getSizeX(), (int) find.getSizeY());
                if (r.intersects(rectangle)) {
                    entity.setFitness(score);
                    pendingEntities.add(entity);
                }
            } else {
                entity.update(speed, 800, 0, 0);
            }
        }


        for (int i = 0; i < obstacles.size(); i++) {
            Obstacle obstacle = obstacles.get(i);
                obstacle.update(speed);
                if (obstacle.getX() < 0) {
                    obstacles.remove(obstacle);
                    i--;
                }
        }

        obstacleTimer += delta;
        score += speed;
        speed += 0.0005 * delta;
        if (obstacleTimer >= minimumTimeBetweenObstacles + randomAddition) {
            addObstacle();
        }
    }

    public void draw() {
        BufferStrategy buffer = this.getBufferStrategy();
        if (buffer == null) {
            this.createBufferStrategy(3);
        } else {
            Graphics g = buffer.getDrawGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, 800, 400);


            g.setColor(Color.BLACK);
            gameDraw(g);

            g.dispose();
            buffer.show();
        }
    }

    public void gameDraw(Graphics g) {
        g.drawLine(0, 320, getWidth(), 320);
        g.drawString("Score: " + score, 100, 50);
        g.drawString("Alive: " + entitiesToUpdate.size(), 100, 100);
        for (Dino entity : entitiesToUpdate) {
            entity.draw(g);
        }

        for (int i = 0; i < obstacles.size(); i++) {
            Obstacle obstacle = obstacles.get(i);
            obstacle.draw(g);
        }
    }

    public void addObstacle() {
        Obstacle obstacle = new Obstacle();
        randomAddition = new Random().nextInt(60);
        obstacleTimer = 0;
        obstacles.add(obstacle);
    }

    private Obstacle findNextObstacle() {
        for (int i = 0; i < obstacles.size(); i++) {
            Obstacle obstacle = obstacles.get(i);
            if (obstacle.getX() + obstacle.getSizeX() > 60) {
                return obstacle;
            }
        }

        return null;
    }
}
