package me.enander.network;

public class Counter {

    private int currentInnovation = 1;

    public int getInnovation() {
        return currentInnovation++;
    }
}
