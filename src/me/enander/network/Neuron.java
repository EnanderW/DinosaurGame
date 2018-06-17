package me.enander.network;

import java.util.ArrayList;
import java.util.List;

public class Neuron {

    private Type type;

    private int innovationNumber;
    private double output = 0;
    private List<Connection> inConnections;
    private List<Connection> outConnections;

    public Neuron(Type type, int innovationNumber) {
        inConnections = new ArrayList<>();
        outConnections = new ArrayList<>();
        this.type = type;
        this.innovationNumber = innovationNumber;
    }

    public Neuron(Neuron neuron) {
        this.type = neuron.type;
        this.innovationNumber = neuron.innovationNumber;
        inConnections = neuron.inConnections;
        outConnections = neuron.outConnections;
    }

    public void add(double forward) {
        output += forward;
    }

    public void setOutput(double output) {
        this.output = output;
    }

    public enum Type {
        INPUT, HIDDEN, OUTPUT

    }

    public Type getType() {
        return type;
    }

    public int getInnovationNumber() {
        return innovationNumber;
    }

    public double getOutput() {
        return output;
    }

    public List<Connection> getInConnections() {
        return inConnections;
    }

    public List<Connection> getOutConnections() {
        return outConnections;
    }
}
