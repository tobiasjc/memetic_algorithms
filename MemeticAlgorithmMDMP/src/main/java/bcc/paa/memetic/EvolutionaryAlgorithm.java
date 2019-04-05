package bcc.paa.memetic;

import java.util.Random;

import bcc.paa.graphics.InfoLabel;
import bcc.paa.graphics.MainPanel;
import bcc.paa.structures.Device;
import bcc.paa.structures.Tour;

public class EvolutionaryAlgorithm implements Runnable {
	private static Device[] devices;
	private static int source;

	public Tour[] population;
	public EntitiesManagement entitiesManagement;
	public Tour best;
	public double[] data;
	private double[] lengths;
	private int generationQuantity;
	private Random random;
	private MainPanel mainPanel;
	private InfoLabel infoLabel;

	/**
	 * @param devices
	 * @param populationSize
	 * @param generationQuantity
	 * @param source
	 * @param seed
	 */
	public EvolutionaryAlgorithm(Device[] devices, int populationSize, int generationQuantity, int source,
			MainPanel mainPanel, InfoLabel infoLabel, int seed) {
		EvolutionaryAlgorithm.source = source;
		EvolutionaryAlgorithm.devices = devices;
		this.random = new Random(seed);
		this.generationQuantity = generationQuantity;
		this.population = new Tour[populationSize];
		this.lengths = new double[populationSize];
		this.entitiesManagement = new EntitiesManagement(this.population, this.lengths, EvolutionaryAlgorithm.source,
				devices, seed);
		this.mainPanel = mainPanel;
		this.infoLabel = infoLabel;
		firstGen();
	}

	public void updateValues() {
		for (int i = 0; i < this.lengths.length; i++) {
			this.lengths[i] = this.population[i].length;
		}
		entitiesManagement.getBest();
		this.best = this.population[this.entitiesManagement.best];
	}

	/**
	 * @param choices
	 * @param position
	 * @param length
	 */
	private void reduceChoices(int[] choices, int position, int length) {
		if (position != length - 1) {
			if (length - 1 - position >= 0) {
				System.arraycopy(choices, position + 1, choices, position, length - 1 - position);
			}
		}
	}

	/**
	 * @return
	 */
	private Tour generatePath() {
		int choicesLength = EvolutionaryAlgorithm.devices.length;
		int[] choices = new int[choicesLength];
		Tour newTour = new Tour(EvolutionaryAlgorithm.devices.length, source);

		for (int i = 0; i < EvolutionaryAlgorithm.devices.length; i++) {
			choices[i] = i;
		}

		for (int i = 0; i < choices.length; i++) {
			int selected = this.random.nextInt(choicesLength);
			newTour.addDevice(EvolutionaryAlgorithm.devices[choices[selected]]);
			reduceChoices(choices, selected, choicesLength);
			choicesLength--;
		}

		return newTour;
	}

	/**
	 *
	 */
	private void firstGen() {
		for (int i = 0; i < this.population.length; i++) {
			this.population[i] = generatePath();
			this.lengths[i] = this.population[i].length;
		}
	}

	private void update() {
		this.mainPanel.route = this.best;
		this.infoLabel.bestFitness = this.best.length;
		double mean = 0;
		for (double value : this.lengths) {
			mean += value / this.population.length;
		}
		this.infoLabel.generationFitnessMean = mean;
		this.infoLabel.updateLabel();
		this.mainPanel.repaint();
	}

	/**
	 *
	 */
	@Override
	public void run() {
		this.data = new double[generationQuantity];
		this.entitiesManagement.crossover();
		this.entitiesManagement.mutate();
		this.entitiesManagement.selection();
		this.entitiesManagement.memeOver();
		this.entitiesManagement.getBest();
		this.best = this.population[this.entitiesManagement.best];
		update();
		this.data[0] = this.best.length;
		for (int i = 1; i < this.generationQuantity; i++) {
			this.entitiesManagement.crossover();
			this.entitiesManagement.mutate();
			this.entitiesManagement.selection();
			this.entitiesManagement.memeOver();
			this.entitiesManagement.getBest();
			this.best = this.population[this.entitiesManagement.best];
			update();
			data[i] = this.best.length;
		}
	}
}
