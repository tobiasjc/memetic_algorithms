package bcc.paa.graphics;

import java.text.DecimalFormat;

import javax.swing.JLabel;

public class InfoLabel extends JLabel {
	private static final long serialVersionUID = 1L;
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
		this.setText("Generation = \t" + dfi.format(generation) + "/ Best Fitness = \t" + dfd.format(bestFitness)
				+ " / Generation Fitness Mean = \t" + dfd.format(generationFitnessMean));
	}
}
