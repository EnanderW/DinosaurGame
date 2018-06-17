package me.enander.network;

import java.util.*;

public abstract class GeneticAlgorithm {

    private static final Random random = new Random();

    public static final int populationSize = 1000;

    private static final float C1 = 1, C2 = 0.4f, C3 = 1f, DT = 10;
    private static final float MUTATION_RATE = 0.2f;
    private static final float ADD_CONNECTION_RATE = 0.2f;
    private static final float ADD_NEURON_RATE = 0.2f;

    public GeneticObject evolve(List<Genome> genomes, List<Specie> species, Game game) {
        List<Genome> nextGenerationGenomes = new ArrayList<>();

        //Place genomes into species
        for (Genome genome : genomes) {
            boolean foundSpecie = false;
            for (Specie specie : species) {
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
                species.remove(s);
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

        for (Specie specie : species) {
            specie.setFitness();
        }


        //Put the best genomes from each species into the next generation
        for (Specie specie : species) {
            Genome fittest = specie.getFittestGenome();
            nextGenerationGenomes.add(fittest);
        }

        //Breed the next genomes
        while (nextGenerationGenomes.size() < populationSize) { // replace removed genomes by randomly breeding
            Specie specie = getRandomSpeciesBiasedAjdustedFitness(random, species);

            Genome genome1 = getRandomGenomeBiasedAdjustedFitness(specie, random);
            Genome genome2 = getRandomGenomeBiasedAdjustedFitness(specie, random);

            Genome child;
            if (genome1.getFitness() >= genome2.getFitness()) {
                child = Genome.crossover(genome1, genome2, random);

                if (Math.random() < MUTATION_RATE) {
                    child.mutate(random, 0.9f);
                }
                if (Math.random() < ADD_CONNECTION_RATE) {
                    child.addMutationConnection(10, genome1.getConnectionCounter(), random);
                }
                if (Math.random() < ADD_NEURON_RATE) {
                    child.addMutationNeuron(genome1.getNeuronCounter(), genome1.getConnectionCounter(), random);
                }

            } else {
                child = Genome.crossover(genome2, genome1, random);

                if (Math.random() < MUTATION_RATE) {
                    child.mutate(random, 0.9f);
                }
                if (Math.random() < ADD_CONNECTION_RATE) {
                    child.addMutationConnection(10, genome2.getConnectionCounter(), random);
                }
                if (Math.random() < ADD_NEURON_RATE) {
                    child.addMutationNeuron(genome2.getNeuronCounter(), genome2.getConnectionCounter(), random);
                }
            }

            nextGenerationGenomes.add(child);
        }

        genomes = nextGenerationGenomes;

        return new GeneticObject(genomes, species);
    }

    public int newNeuron(Genome genome) {
        return genome.getNeuronCounter().getInnovation();
    }

    public Connection newConnection(int input, int output, float weight, Genome genome) {
       return new Connection(input, output, weight, true, genome.getConnectionCounter().getInnovation());
    }

    private Specie getRandomSpeciesBiasedAjdustedFitness(Random random, List<Specie> species) {
        float completeWeight = 0.0f;	// sum of probablities of selecting each species - selection is more probable for species with higher fitness
        for (Specie specie: species) {
            completeWeight += specie.getFitness();
        }
        float r = (float)Math.random() * completeWeight;
        float countWeight = 0.0f;
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
        float completeWeight = 0.0f;	// sum of probablities of selecting each genome - selection is more probable for genomes with higher fitness
        for (Genome genome : specie.getMembers()) {
            completeWeight += genome.getFitness();
        }
        float r = (float)Math.random() * completeWeight;
        float countWeight = 0.0f;
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

        List<Integer> conKeys1 = asSortedList(genome1.getConnections().keySet());
        List<Integer> conKeys2 = asSortedList(genome2.getConnections().keySet());

        int highestInnovation1 = conKeys1.get(conKeys1.size()-1);
        int highestInnovation2 = conKeys2.get(conKeys2.size()-1);

        int indices = Math.max(highestInnovation1, highestInnovation2);
        for (int i = 0; i <= indices; i++) { 					// loop through genes -> i is innovation numbers
            Connection connection1 = genome1.getConnections().get(i);
            Connection connection2 = genome2.getConnections().get(i);
            if (connection1 != null && connection2 != null) {
                // both genomes has the gene w/ this innovation number
                matchingGenes++;
                weightDifference += Math.abs(connection1.getWeight() - connection2.getWeight());
            }
        }

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
