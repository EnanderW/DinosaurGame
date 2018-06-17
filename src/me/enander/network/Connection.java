package me.enander.network;

public class Connection {

    private Neuron inNeuron, outNeuron;
    private double weight;
    private int innovationNumber;
    private boolean enabled;

    public Connection(Neuron inNeuron, Neuron outNeuron, double weight, boolean enabled, int innovationNumber) {
        this.inNeuron = inNeuron;
        this.outNeuron = outNeuron;
        this.weight = weight;
        this.enabled = enabled;
        this.innovationNumber = innovationNumber;
    }

    public Connection(Connection connection) {
        this.inNeuron = connection.inNeuron;
        this.outNeuron = connection.outNeuron;
        this.weight = connection.weight;
        this.enabled = connection.enabled;
        this.innovationNumber = connection.innovationNumber;
    }

    public void disable() {
        enabled = false;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Neuron getInNeuron() {
        return inNeuron;
    }

    public Neuron getOutNeuron() {
        return outNeuron;
    }

    public int getInnovationNumber() {
        return innovationNumber;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
