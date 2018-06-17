package me.enander.network;

import java.util.ArrayList;
import java.util.List;

public class Specie {

    private Genome mascot;
    private List<Genome> members;
    private float fitness;

    public Specie(Genome mascot) {
        this.mascot = mascot;
        this.members = new ArrayList<>();
        members.add(mascot);
    }

    public Genome getFittestGenome() {
        Genome best = mascot;
        for (Genome genome : getMembers()) {
            if (genome.getFitness() > best.getFitness()) {
                best = genome;
            }
        }

        return best;
    }

    public float getFitness() {
        return fitness;
    }

    public void addFitness(float fitness) {
        this.fitness += fitness;
    }

    public void setFitness() {
        this.fitness = fitness / members.size();
    }

    public List<Genome> getMembers() {
        return members;
    }

    public void addMember(Genome genome) {
        members.add(genome);
    }

    public Genome getMascot() {
        return mascot;
    }
}
