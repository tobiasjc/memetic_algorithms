package bcc.paa.graphics;


import javax.swing.*;
import java.text.DecimalFormat;

public class InfoLabel extends JLabel {
    public int generation;
    public double bestFitness;
    public double generationFitnessMean;

    public InfoLabel(int generation, double bestFitness, double generationFitnessMean) {
        this.generation = generation;
        this.bestFitness = bestFitness;
        this.generationFitnessMean = generationFitnessMean;
    }

    public void updateLabel() {
        DecimalFormat dfd = new DecimalFormat("000000000000000.0000000000000");
        DecimalFormat dfi = new DecimalFormat("000000000000");
        this.setText("Generation = \t" + dfi.format(generation) + "/ Best Fitness = \t" + dfd.format(bestFitness) + " / Generation Fitness Mean = \t" + dfd.format(generationFitnessMean));
    }
}
