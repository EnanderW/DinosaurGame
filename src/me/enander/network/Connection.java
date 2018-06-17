package me.enander.network;

public class Connection {

    private int inNeuron, outNeuron;
    private float weight;
    private int innovationNumber;
    private boolean enabled;

    public Connection(int inNeuron, int outNeuron, float weight, boolean enabled, int innovationNumber) {
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

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public int getInNeuron() {
        return inNeuron;
    }

    public int getOutNeuron() {
        return outNeuron;
    }

    public int getInnovationNumber() {
        return innovationNumber;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
