package bcc.paa.graphics;

import java.awt.Color;
import java.text.DecimalFormat;

import javax.swing.JLabel;

public class InfoLabel extends JLabel {
	private static final long serialVersionUID = 1L;
	public double bestFitness;
	public double generationFitnessMean;
	private String id;

	public InfoLabel(String id, double bestFitness, double generationFitnessMean) {
		this.id = id;
		this.bestFitness = bestFitness;
		this.generationFitnessMean = generationFitnessMean;
		this.setBackground(Color.pink);
		this.setOpaque(true);
	}

	public void updateLabel() {
		DecimalFormat dfd = new DecimalFormat("000000000000000.0000000000000");
		// DecimalFormat dfi = new DecimalFormat("000000000000");
		this.setText("<html>" + id + "<br/>Best Fitness = " + dfd.format(bestFitness)
				+ "<br/>Generation Fitness Mean = " + dfd.format(generationFitnessMean) + "</html>");
	}
}
