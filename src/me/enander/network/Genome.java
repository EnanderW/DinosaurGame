package me.enander.network;

import java.util.*;

public class Genome {


    public enum Type {
        INPUT, HIDDEN, OUTPUT;
    }

    private Counter connectionCounter;
    private Counter neuronCounter;

    private float fitness;
    private Map<Integer, Connection> connections;
    private Map<Integer, Type> neurons;

    public Genome(Counter connectionCounter, Counter neuronCounter) {
        this.connections = new HashMap<>();
        this.neurons = new HashMap<>();
        this.connectionCounter = connectionCounter;
        this.neuronCounter = neuronCounter;
    }

    public Genome(Genome toBeCopied) {
        this.connections = new HashMap<>();

        for (Integer index : toBeCopied.getConnections().keySet()) {
            connections.put(index, new Connection(toBeCopied.getConnections().get(index)));
        }
    }

    public static Genome crossover(Genome genome1, Genome genome2, Random random) {
        Genome child = new Genome(genome1.connectionCounter, genome1.neuronCounter);

        for (int i : genome1.getNeurons().keySet()) {
            child.addNeuron(i, genome1.getNeurons().get(i));
        }

        for (Connection parent1Connection : genome1.getConnections().values()) {
            if (genome2.getConnections().containsKey(parent1Connection.getInnovationNumber())) { // matching gene
                Connection childConGene = random.nextBoolean() ? new Connection(parent1Connection) : new Connection(genome2.getConnections().get(parent1Connection.getInnovationNumber()));
                child.addConnection(childConGene);
            } else { // disjoint or excess gene
                Connection childConGene = new Connection(parent1Connection);
                child.addConnection(childConGene);
            }
        }

        return child;
    }

    public int addNeuron(int i, Type type) {
        neurons.put(i, type);
        return i;
    }

    public void addConnection(Connection connection) {
        connections.put(connection.getInnovationNumber(), connection);
    }

    public void mutate(Random random, float probability) {
        for(Connection connection : connections.values()) {
            if (random.nextFloat() < probability) { 			// uniformly perturbing weights
                connection.setWeight(connection.getWeight() * (random.nextFloat() * 4f - 2f));
            } else { // assigning new weight
                connection.setWeight(random.nextFloat() * 12f - 6f);
            }
        }
    }

    public float[] calculate(float... input) {
        //System.out.println("Amount: " + allNeurons.size());
        List<Float > outputs = new ArrayList<>();
        Map<Integer, Float> neuronOutputs = new HashMap<>();
        int outputIndex = 0;
        for (int i : neurons.keySet()) {
            Type type = neurons.get(i);
            if (type == Type.INPUT) {
                List<Connection> connections = getConnectionsOut(i);
                for (Connection connection : connections) {
                    if (!connection.isEnabled()) {
                        continue;
                    }

                    int outNeuron = connection.getOutNeuron();
                    if (neuronOutputs.containsKey(outNeuron)) {
                        neuronOutputs.put(outNeuron, neuronOutputs.get(outNeuron) + input[outputIndex] * connection.getWeight());
                    } else {
                        neuronOutputs.put(outNeuron, input[outputIndex] * connection.getWeight());
                    }
                }

                outputIndex++;
            }
        }

        for (int i : neurons.keySet()) {
            Type type = neurons.get(i);
            if (type == Type.HIDDEN) {
                List<Connection> connections2 = getConnectionsOut(i);
                for (Connection connection : connections2) {
                    if (!connection.isEnabled()) {
                        continue;
                    }

                    int outNeuron = connection.getOutNeuron();
                    if (!neuronOutputs.containsKey(i)) {
                        continue;
                    }

                    float output = sigmoid(neuronOutputs.get(i));
                    if (neuronOutputs.containsKey(outNeuron)) {
                        neuronOutputs.put(outNeuron, neuronOutputs.get(outNeuron) + output * connection.getWeight());
                    } else {
                        neuronOutputs.put(outNeuron, output);
                    }
                }
            }
        }

        for (int i : neurons.keySet()) {
            Type type = neurons.get(i);
            if (type == Type.OUTPUT) {
                if (!neuronOutputs.containsKey(i)) {
                    continue;
                }

                float output = sigmoid(neuronOutputs.get(i));
                outputs.add(output);
            }
        }

        float[] returnOutput = new float[outputs.size()];
        for (int i = 0; i < outputs.size(); i++) {
            returnOutput[i] = outputs.get(i);
        }


        return returnOutput;
    }

    private List<Connection> getConnectionsOut(int i) {
        List<Connection> toReturn = new ArrayList<>();
        for (Connection connection : connections.values()) {
            if (connection.getInNeuron() == i) {
                toReturn.add(connection);
            }
        }

        return toReturn;
    }

    private float sigmoid(float x) {
        return 1f / (1f + (float)(Math.exp(-x)));
    }

    public void addMutationConnection(int maxAttempts, Counter innovation, Random random) {
        int tries = 0;
        boolean success = false;
        while (tries < maxAttempts && !success) {
            tries++;

            int neuron1 = (int) neurons.keySet().toArray()[random.nextInt(neurons.size())];
            int neuron2 = (int) neurons.keySet().toArray()[random.nextInt(neurons.size())];

            Type type1 = neurons.get(neuron1);
            Type type2 = neurons.get(neuron2);

            float weight = random.nextFloat()*2f-1f;

            boolean reversed = false;
            if (type1 == Type.HIDDEN && type2 == Type.INPUT) {
                reversed = true;
            } else if (type1 == Type.OUTPUT && type2 == Type.HIDDEN) {
                reversed = true;
            } else if (type1 == Type.OUTPUT && type2 == Type.INPUT) {
                reversed = true;
            }

            boolean connectionImpossible = false;
            if (type1 == Type.INPUT && type2 == Type.INPUT) {
                connectionImpossible = true;
            } else if (type1 == Type.OUTPUT && type2 == Type.OUTPUT) {
                connectionImpossible = true;
            }

            boolean connectionExists = false;
            for (Connection con : connections.values()) {
                if (con.getInNeuron() == neuron1 && con.getOutNeuron() == neuron2) { // existing connection
                    connectionExists = true;
                    break;
                } else if (con.getInNeuron() == neuron2 && con.getOutNeuron() == neuron1) { // existing connection
                    connectionExists = true;
                    break;
                }
            }

            if (connectionExists || connectionImpossible) {
                continue;
            }

            if (reversed) {
                Connection newCon = new Connection(neuron2, neuron1 , weight, true, innovation.getInnovation());
                connections.put(newCon.getInnovationNumber(), newCon);
            } else {
                Connection newCon = new Connection(neuron1, neuron2, weight, true, innovation.getInnovation());
                connections.put(newCon.getInnovationNumber(), newCon);
            }

            success = true;
        }
    }

    public void addMutationNeuron(Counter neuronInnovation, Counter connectionInnovation, Random random) {
        Connection con = (Connection) connections.values().toArray()[random.nextInt(connections.size())];

        int inNeuron = con.getInNeuron();
        int outNeuron = con.getOutNeuron();

        con.disable();

        int newNeuron = addNeuron(neuronInnovation.getInnovation(), Type.HIDDEN);
        Connection inToNew = new Connection(inNeuron, newNeuron, 1.0f, true, connectionInnovation.getInnovation());
        Connection newToOut = new Connection(newNeuron, outNeuron, con.getWeight(), true, connectionInnovation.getInnovation());

        connections.put(inToNew.getInnovationNumber(), inToNew);
        connections.put(newToOut.getInnovationNumber(), newToOut);
    }

    private static List<Integer> asSortedList(Collection e) {
        List<Integer> l = new ArrayList<>(e);
        Collections.sort(l);
        return l;
    }

    public Counter getConnectionCounter() {
        return connectionCounter;
    }

    public Counter getNeuronCounter() {
        return neuronCounter;
    }

    public float getFitness() {
        return fitness;
    }

    public Map<Integer, Connection> getConnections() {
        return connections;
    }

    public void setFitness(float fitness) {
        this.fitness = fitness;
    }

    public Map<Integer, Type> getNeurons() {
        return neurons;
    }
}
