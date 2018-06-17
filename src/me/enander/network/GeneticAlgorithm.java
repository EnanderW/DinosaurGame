package me.enander.network;

import java.util.*;

public abstract class GeneticAlgorithm {

    private Counter neuronCounter = new Counter();
    private Counter connectionCounter = new Counter();

    private static final Random random = new Random();

    public static final int populationSize = 100;

    private static final float C1 = 1, C2 = 0.4f, C3 = 1f, DT = 10;
    private static final float MUTATION_RATE = 0.5f;
    private static final float ADD_CONNECTION_RATE = 0.5f;
    private static final float ADD_NEURON_RATE = 0.5f;

    public GeneticObject evolve(List<Dino> dinos, List<Specie> species, Game game) {
        List<Genome> genomes = new ArrayList<>();
        for (Dino dino : dinos) {
            genomes.add(dino.getGenome());
        }

        List<Genome> nextGenerationGenomes = new ArrayList<>();

        //Place genomes into species
        for (Genome genome : genomes) {
            boolean foundSpecie = false;
            System.out.println(species.size());
            for (Specie specie : species) {
                System.out.println(compatibilityDistance(genome, specie.getMascot(), C1, C2, C3));
                if (compatibilityDistance(genome, specie.getMascot(), C1, C2, C3) < DT) {
                    specie.addMember(genome);
                    foundSpecie = true;
                }
            }

            if (!foundSpecie) {
                species.add(newSpecie(genome));
            }
        }

        Iterator<Specie> iter = species.iterator();
        while(iter.hasNext()) {
            Specie s = iter.next();
            if (s.getMembers().isEmpty()) {
                iter.remove();
            }
        }

        //Evaluate genomes and assign fitness
        for (Genome genome : genomes) {
            Specie specie = getSpecie(genome, species);
            if (specie != null) {
                genome.setFitness(evaluateFitness(genome));
                specie.addFitness(genome.getFitness());
            }

        }

        int amount = 0;
        for (Specie specie : species) {
            amount++;
            specie.setFitness();
        }

        System.out.println(amount);

        //Put the best genomes from each species into the next generation
        for (Specie specie : species) {
            Genome fittest = specie.getFittestGenome();
            nextGenerationGenomes.add(fittest);
        }

        //Breed the next genomes
        while (nextGenerationGenomes.size() < populationSize) { // replace removed genomes by randomly breeding
            System.out.println("While");
            Specie specie = getRandomSpeciesBiasedAjdustedFitness(random, species);

            Genome genome1 = getRandomGenomeBiasedAdjustedFitness(specie, random);
            Genome genome2 = getRandomGenomeBiasedAdjustedFitness(specie, random);

            Genome child;
            if (genome1.getFitness() >= genome2.getFitness()) {
                child = Genome.crossover(genome1, genome2, random);
            } else {
                child = Genome.crossover(genome2, genome1, random);
            }
            if (random.nextFloat() < MUTATION_RATE) {
                child.mutate(random, 0.9f);
            }
            if (random.nextFloat() < ADD_CONNECTION_RATE) {
                child.addMutationConnection(10, connectionCounter, random);
            }
            if (random.nextFloat() < ADD_NEURON_RATE) {
                child.addMutationNeuron(neuronCounter, connectionCounter, random);
            }

            nextGenerationGenomes.add(child);
        }

        return new GeneticObject(genomes, species);
    }

    public Neuron newNeuron(Neuron.Type type) {
        return new Neuron(type, neuronCounter.getInnovation());
    }

    public Connection newConnection(Neuron input, Neuron output, float weight) {
       return new Connection(input, output, weight, true, connectionCounter.getInnovation());
    }

    private Specie getRandomSpeciesBiasedAjdustedFitness(Random random, List<Specie> species) {
        double completeWeight = 0.0;	// sum of probablities of selecting each species - selection is more probable for species with higher fitness
        for (Specie specie: species) {
            completeWeight += specie.getFitness();
        }
        double r = Math.random() * completeWeight;
        double countWeight = 0.0;
        for (Specie specie: species) {
            countWeight += specie.getFitness();
            if (countWeight >= r) {
                return specie;
            }
        }
        throw new RuntimeException("Couldn't find a species... Number is species in total is "+species.size()+", and the total adjusted fitness is "+completeWeight);
    }

    /**
     * Selects a random genome from the species chosen, where genomes with a higher adjusted fitness have a higher chance of being selected
     */
    private Genome getRandomGenomeBiasedAdjustedFitness(Specie specie, Random random) {
        double completeWeight = 0.0;	// sum of probablities of selecting each genome - selection is more probable for genomes with higher fitness
        for (Genome genome : specie.getMembers()) {
            completeWeight += genome.getFitness();
        }
        double r = Math.random() * completeWeight;
        double countWeight = 0.0;
        for (Genome genome : specie.getMembers()) {
            countWeight += genome.getFitness();
            if (countWeight >= r) {
                return genome;
            }
        }
        throw new RuntimeException("Couldn't find a genome... Number is genomes in sel�ected species is, and the total adjusted fitness is "+completeWeight);
    }

    private Specie getSpecie(Genome genome, List<Specie> species) {
        for (Specie specie : species) {
            if (specie.getMembers().contains(genome)) {
                return specie;
            }
        }

        return null;
    }

    public abstract float evaluateFitness(Genome genome);

    private Specie newSpecie(Genome genome) {
        return new Specie(genome);
    }

    private float compatibilityDistance(Genome genome1, Genome genome2, float c1, float c2, float c3) {
        float disjoint = countDisjointGenes(genome1, genome2);
        float excess = countExcessGenes(genome1, genome2);
        float weightDifference = averageWeightDiff(genome1, genome2);

        return excess * c1 + disjoint * c2 + weightDifference * c3;
    }

    public static float countDisjointGenes(Genome genome1, Genome genome2) {
        float disjointGenes = 0;

        /*List<Integer> nodeKeys1 = asSortedList(genome1.getAllNeurons().keySet());
        List<Integer> nodeKeys2 = asSortedList(genome2.getAllNeurons().keySet());

        int highestInnovation1 = nodeKeys1.get(nodeKeys1.size()-1);
        int highestInnovation2 = nodeKeys2.get(nodeKeys2.size()-1);
        int indices = Math.max(highestInnovation1, highestInnovation2);

        for (int i = 0; i <= indices; i++) {
            Neuron node1 = genome1.getAllNeurons().get(i);
            Neuron node2 = genome2.getAllNeurons().get(i);
            if (node1 == null && highestInnovation1 > i && node2 != null) {
                // genome 1 lacks gene, genome 2 has gene, genome 1 has more genes w/ higher innovation numbers
                disjointGenes++;
            } else if (node2 == null && highestInnovation2 > i && node1 != null) {
                disjointGenes++;
            }
        }*/

        List<Integer> conKeys1 = asSortedList(genome1.getConnections().keySet());
        List<Integer> conKeys2 = asSortedList(genome2.getConnections().keySet());

        int highestInnovation1 = conKeys1.get(conKeys1.size()-1);
        int highestInnovation2 = conKeys2.get(conKeys2.size()-1);

        int indices = Math.max(highestInnovation1, highestInnovation2);
        for (int i = 0; i <= indices; i++) {
            Connection connection1 = genome1.getConnections().get(i);
            Connection connection2 = genome2.getConnections().get(i);
            if (connection1 == null && highestInnovation1 > i && connection2 != null) {
                disjointGenes++;
            } else if (connection2 == null && highestInnovation2 > i && connection1 != null) {
                disjointGenes++;
            }
        }

        return disjointGenes;
    }

    public static float countExcessGenes(Genome genome1, Genome genome2) {
        float excessGenes = 0;

        /*List<Integer> nodeKeys1 = asSortedList(genome1.getAllNeurons().keySet());
        List<Integer> nodeKeys2 = asSortedList(genome2.getAllNeurons().keySet());

        int highestInnovation1 = nodeKeys1.get(nodeKeys1.size()-1);
        int highestInnovation2 = nodeKeys2.get(nodeKeys2.size()-1);
        int indices = Math.max(highestInnovation1, highestInnovation2);

        for (int i = 0; i <= indices; i++) {
            Neuron node1 = genome1.getAllNeurons().get(i);
            Neuron node2 = genome2.getAllNeurons().get(i);
            if (node1 == null && highestInnovation1 < i && node2 != null) {
                excessGenes++;
            } else if (node2 == null && highestInnovation2 < i && node1 != null) {
                excessGenes++;
            }
        }*/

        List<Integer> conKeys1 = asSortedList(genome1.getConnections().keySet());
        List<Integer> conKeys2 = asSortedList(genome2.getConnections().keySet());

        int highestInnovation1 = conKeys1.get(conKeys1.size()-1);
        int highestInnovation2 = conKeys2.get(conKeys2.size()-1);

        int indices = Math.max(highestInnovation1, highestInnovation2);
        for (int i = 0; i <= indices; i++) {
            Connection connection1 = genome1.getConnections().get(i);
            Connection connection2 = genome2.getConnections().get(i);
            if (connection1 == null && highestInnovation1 < i && connection2 != null) {
                excessGenes++;
            } else if (connection2 == null && highestInnovation2 < i && connection1 != null) {
                excessGenes++;
            }
        }

        return excessGenes;
    }

    public static float averageWeightDiff(Genome genome1, Genome genome2) {
        float matchingGenes = 0;
        float weightDifference = 0;

        System.out.println("Size 1: " + genome1.getConnections().size());

        List<Integer> conKeys1 = asSortedList(genome1.getConnections().keySet());
        List<Integer> conKeys2 = asSortedList(genome2.getConnections().keySet());

        int highestInnovation1 = conKeys1.get(conKeys1.size()-1);
        int highestInnovation2 = conKeys2.get(conKeys2.size()-1);

        System.out.println("H1: " + highestInnovation1);
        System.out.println("H2: " + highestInnovation2);

        int indices = Math.max(highestInnovation1, highestInnovation2);
        System.out.println("Indices: " + indices);
        for (int i = 0; i <= indices; i++) { 					// loop through genes -> i is innovation numbers
            Connection connection1 = genome1.getConnections().get(i);
            Connection connection2 = genome2.getConnections().get(i);
            if (connection1 != null && connection2 != null) {
                // both genomes has the gene w/ this innovation number
                matchingGenes++;
                weightDifference += Math.abs(connection1.getWeight() - connection2.getWeight());
            }
        }

        System.out.println("M: " + matchingGenes);
        System.out.println("WD: " + weightDifference);
        return (weightDifference / matchingGenes);
    }

    private static List<Integer> asSortedList(Collection e) {
        List<Integer> l = new ArrayList<>(e);
        Collections.sort(l);
        return l;
    }

    public class GeneticObject {

        public List<Genome> genomes;
        public List<Specie> species;

        public GeneticObject(List<Genome> genomes, List<Specie> species) {
            this.genomes = genomes;
            this.species = species;
        }
    }
}
