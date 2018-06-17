package me.enander.network;

import java.util.*;

public class Genome {

    private float fitness;
    private Map<Integer, Connection> connections;
    private Map<Integer, Neuron> allNeurons;

    public Genome() {
        this.connections = new HashMap<>();
        this.allNeurons = new HashMap<>();
    }

    public Genome(Genome toBeCopied) {
        this.connections = new HashMap<>();
        this.allNeurons = new HashMap<>();

        for (Integer index : toBeCopied.getAllNeurons().keySet()) {
            addNeuron(new Neuron(toBeCopied.getAllNeurons().get(index)));
        }

        for (Integer index : toBeCopied.getConnections().keySet()) {
            connections.put(index, new Connection(toBeCopied.getConnections().get(index)));
        }
    }

    public static Genome crossover(Genome genome1, Genome genome2, Random random) {
        Genome child = new Genome();

        for (Neuron parent1Node : genome1.getAllNeurons().values()) {
            child.addNeuron(new Neuron(parent1Node));
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

    public void addNeuron(Neuron neuron) {
        allNeurons.put(neuron.getInnovationNumber(), neuron);
    }

    public void addConnection(Connection connection) {
        connections.put(connection.getInnovationNumber(), connection);
    }

    public void mutate(Random random, float probability) {
        System.out.println("called");
        for(Connection connection : connections.values()) {
            System.out.println("cHANGE WEIGHT");
            if (random.nextFloat() < probability) { 			// uniformly perturbing weights
                connection.setWeight(connection.getWeight() * (random.nextFloat() * 12f - 6f));

            } else { 												// assigning new weight
                connection.setWeight(random.nextFloat() * 12f - 6f);
            }
        }
    }

    public double[] calculate(double... input) {
        //System.out.println("Amount: " + allNeurons.size());
        int oI = 0;
        List<Double> outputs = new ArrayList<>();
        for (Neuron neuron : allNeurons.values()) {
            if (neuron.getType() == Neuron.Type.INPUT) {
                double output = input[oI++];
                for (Connection connection : neuron.getOutConnections()) {
                    if (connection.isEnabled()) {
                        double forward = connection.getWeight() * output;
                        connection.getOutNeuron().add(forward);
                    }
                }
            }

            if (neuron.getType() == Neuron.Type.HIDDEN) {
                double output = sigmoid(neuron.getOutput());
                for (Connection connection : neuron.getOutConnections()) {
                    if (connection.isEnabled()) {
                        double forward = connection.getWeight() * output;
                        connection.getOutNeuron().add(forward);
                    }
                }
            }

            if (neuron.getType() == Neuron.Type.OUTPUT) {
                double output = sigmoid(neuron.getOutput());
                neuron.setOutput(output);
                outputs.add(output);
            }
        }

        double[] returnOutput = new double[outputs.size()];
        for (int i = 0; i < outputs.size(); i++) {
            returnOutput[i] = outputs.get(i);
        }

        return returnOutput;
    }

    private double sigmoid(double x) {
        return 1d / (1 + (Math.exp(-x)));
    }

    public void addMutationConnection(int maxAttempts, Counter innovation, Random random) {
        int tries = 0;
        boolean success = false;
        while (tries < maxAttempts && !success) {
            tries++;

            Integer[] nodeInnovationNumbers = new Integer[getAllNeurons().keySet().size()];
            getAllNeurons().keySet().toArray(nodeInnovationNumbers);
            Integer keyNode1 = nodeInnovationNumbers[random.nextInt(nodeInnovationNumbers.length)];
            Integer keyNode2 = nodeInnovationNumbers[random.nextInt(nodeInnovationNumbers.length)];

            Neuron neuron1 = getAllNeurons().get(keyNode1);
            Neuron neuron2 = getAllNeurons().get(keyNode2);

            float weight = random.nextFloat()*2f-1f;

            boolean reversed = false;
            if (neuron1.getType() == Neuron.Type.HIDDEN && neuron2.getType() == Neuron.Type.INPUT) {
                reversed = true;
            } else if (neuron1.getType() == Neuron.Type.OUTPUT && neuron2.getType() == Neuron.Type.HIDDEN) {
                reversed = true;
            } else if (neuron1.getType() == Neuron.Type.OUTPUT && neuron2.getType() == Neuron.Type.INPUT) {
                reversed = true;
            }

            boolean connectionImpossible = false;
            if (neuron1.getType() == Neuron.Type.INPUT && neuron2.getType() == Neuron.Type.INPUT) {
                connectionImpossible = true;
            } else if (neuron1.getType() == Neuron.Type.OUTPUT && neuron2.getType() == Neuron.Type.OUTPUT) {
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
                neuron2.getOutConnections().add(newCon);
                neuron1.getInConnections().add(newCon);
                connections.put(newCon.getInnovationNumber(), newCon);
            } else {
                Connection newCon = new Connection(neuron1, neuron2, weight, true, innovation.getInnovation());
                neuron1.getOutConnections().add(newCon);
                neuron2.getInConnections().add(newCon);
                connections.put(newCon.getInnovationNumber(), newCon);
            }

            success = true;
        }

        if (!success) {
            System.out.println("Tried, but could not add more connections");
        }
    }

    public void addMutationNeuron(Counter neuronInnovation, Counter connectionInnovation, Random random) {
        Connection con = (Connection) connections.values().toArray()[random.nextInt(connections.size())];

        Neuron inNeuron = getAllNeurons().get(con.getInNeuron().getInnovationNumber());
        Neuron outNeuron = getAllNeurons().get(con.getOutNeuron().getInnovationNumber());

        con.disable();

        Neuron newNode = new Neuron(Neuron.Type.HIDDEN, neuronInnovation.getInnovation());
        Connection inToNew = new Connection(inNeuron, newNode, 1.0, true, connectionInnovation.getInnovation());
        Connection newToOut = new Connection(newNode, outNeuron, con.getWeight(), true, connectionInnovation.getInnovation());

        newNode.getInConnections().add(inToNew);
        inNeuron.getOutConnections().add(inToNew);
        outNeuron.getInConnections().add(newToOut);

        allNeurons.put(newNode.getInnovationNumber(), newNode);
        connections.put(inToNew.getInnovationNumber(), inToNew);
        connections.put(newToOut.getInnovationNumber(), newToOut);
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

    public Map<Integer, Neuron> getAllNeurons() {
        return allNeurons;
    }
}
