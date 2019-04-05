package bcc.paa.algorithm;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Random;

import bcc.paa.graphics.InfoLabel;
import bcc.paa.graphics.MainPanel;
import bcc.paa.util.Device;
import bcc.paa.util.Measurement;

/**
 *
 */
public class Gene implements Runnable {
	private static final double MUTATION_RATE = 0.02;
	private static final double MATING_RATE = 0.2;

	private int populationSize;
	private int generationQuantity;
	private Random random;
	private static double meanFitness;
	private double bestFitness;
	private int bestIndex;
	private static String outputPath;
	private static ArrayList<Device> best;
	private static double bestBestFitness;
	private ArrayList<ArrayList<Device>> population;
	private static ArrayList<Device> devices;
	private ArrayList<Integer> matingPool;
	private ArrayList<Double> roulette;
	private ArrayList<Double> fitness;
	private double[][] data;
	private MainPanel mainPanel;
	private static MainPanel bestPanel;
	private static InfoLabel infoLabel;

	/**
	 * @param populationSize     the population size for the end of each generation
	 * @param sourceIndex        the source point, also the last point, of the drone
	 *                           route
	 * @param generationQuantity desired number of generations
	 * @param devices            the devices the drone need to collect data from,
	 *                           with their needed information
	 */
	public Gene(ArrayList<Device> best, InfoLabel infoLabel, MainPanel bestPanel, int populationSize, double fitness,
			int sourceIndex, int generationQuantity, ArrayList<Device> devices, MainPanel mainPanel, String outputPath,
			int seed) {
		this.populationSize = populationSize;
		this.generationQuantity = generationQuantity;
		this.random = new Random(seed);
		this.bestFitness = Double.MAX_VALUE;
		this.bestIndex = -1;
		meanFitness = 0.0;
		Gene.bestPanel = bestPanel;
		Gene.best = new ArrayList<>();
		Gene.bestBestFitness = fitness;
		Gene.outputPath = outputPath;
		this.mainPanel = mainPanel;
		Gene.best = best;
		Gene.infoLabel = infoLabel;

		this.population = new ArrayList<>();
		while (this.population.size() < this.populationSize)
			this.population.add(new ArrayList<>());

		Gene.devices = devices;
		this.matingPool = new ArrayList<>();
		this.roulette = new ArrayList<>();
		this.fitness = new ArrayList<>(this.populationSize);
		this.data = new double[this.generationQuantity][2];
	}

	/**
	 * Compute all generations of the evolutionary algorithm.
	 */
	@Override
	public void run() {
		firstGen();
		mating();
		mutation();
		selection();
		updateBestIndex();

		for (int i = 1; i < this.generationQuantity; i++) {
			// this.infoLabel.generation = i + 1;
			mating();
			mutation();
			selection();
			updateBestIndex();
			updateBest();
		}
	}

	/* PURE GENETIC ALGORITHM METHODS */

	/**
	 * Generate the first generation by doing a randomized chromosome gene
	 * selection.
	 */
	private void firstGen() {
		for (int i = 0; i < this.populationSize; i++) {
			ArrayList<Device> chromosome = generateChromosome();
			this.population.get(i).addAll(chromosome);
			this.fitness.add(Measurement.chromosomeFitness(chromosome));
		}
	}

	/**
	 *
	 */
	private void mating() {
		// System.out.println("Entering mating");
		generateMatingPool();

		while (this.matingPool.size() > 0) {
			ArrayList<Device> parent1 = this.population
					.get(this.matingPool.remove(this.random.nextInt(this.matingPool.size())));
			ArrayList<Device> parent2 = this.population
					.get(this.matingPool.remove(this.random.nextInt(this.matingPool.size())));
			crossover(parent1, parent2);
		}
		// System.out.println("Exiting mating");
	}

	/**
	 *
	 */
	private void mutation() {
		ArrayList<Integer> toMutate = new ArrayList<>();

		for (int i = 0; i < this.population.size() * MUTATION_RATE; i++) {
			int index = this.random.nextInt(this.population.size());
			while (index == bestIndex || index < this.populationSize)
				index = this.random.nextInt(this.population.size());
			toMutate.add(index);
		}

		for (int chromosomeIdx : toMutate) {
			int device1Idx = selectDevice(-1);
			int device2Idx = selectDevice(device1Idx);
			swapDevices(this.population.get(chromosomeIdx), device1Idx, device2Idx);
			this.fitness.set(chromosomeIdx, Measurement.chromosomeFitness(this.population.get(chromosomeIdx)));
		}
	}

	/**
	 *
	 */
	private void selection() {
		// System.out.println("Entering selection");
		rouletteUpdate();

		boolean[] chosen = new boolean[this.population.size()];

		while (this.population.size() > this.populationSize) {
			double value = this.random.nextDouble();
			int idx = -1;
			for (int j = 0; value >= 0; j++) {
				value -= this.roulette.get(j % this.roulette.size());
				idx = j % this.roulette.size();
			}
			if (!chosen[idx] && idx != bestIndex) {
				chosen[idx] = true;
				this.roulette.remove(idx);
				this.population.remove(idx);
				this.fitness.remove(idx);

				if (idx < bestIndex) {
					bestIndex -= 1;
				}
			}
		}
		// System.out.println("Exiting selection");
	}

	/**
	 *
	 */
	private void rouletteUpdate() {
		this.roulette.clear();

		double fitnessSum = 0.0;
		for (double value : this.fitness)
			fitnessSum += value;

		for (double value : this.fitness)
			this.roulette.add(value / fitnessSum);
	}

	/**
	 *
	 */
	private void updateBestIndex() {
		meanFitness = 0.0;
		int idx = bestIndex;
		for (int i = 0; i < this.population.size(); i++) {
			if (this.fitness.get(i) < bestFitness) {
				this.bestFitness = this.fitness.get(i);
				idx = i;
			}
			meanFitness += this.fitness.get(i);
		}

		if (idx != bestIndex) {
			this.bestIndex = idx;
			this.bestFitness = Measurement.chromosomeFitness(this.population.get(idx));
			updateGraphics();
		}
		meanFitness = meanFitness / this.populationSize * 5;
	}

	/* HELPING FUNCTIONS */

	/**
	 * @param startPoint
	 * @param endPoint
	 * @param parent1
	 * @param parent2
	 * @param betweenHalf
	 * @param bordersHalf
	 */
	private void doMating(int startPoint, int endPoint, ArrayList<Device> parent1, ArrayList<Device> parent2,
			ArrayList<Device> betweenHalf, ArrayList<Device> bordersHalf) {

		for (int i = startPoint; i < endPoint; i++) {
			Device parentGene = parent1.get(i);
			betweenHalf.add(new Device(parentGene.x, parentGene.y, parentGene.r, parentGene.n));
		}

		for (Device device1 : parent2) {
			boolean cant = false;
			for (Device device2 : betweenHalf)
				if (device1.compareTo(device2) > 0) {
					cant = true;
					break;
				}
			if (!cant)
				bordersHalf.add(new Device(device1.x, device1.y, device1.r, device1.n));
		}
	}

	/**
	 * @return
	 */
	private ArrayList<Device> generateChromosome() {
		ArrayList<Device> chromosome = new ArrayList<>();
		ArrayList<Integer> choices = new ArrayList<>();

		for (int i = 0; i < devices.size(); i++)
			choices.add(i);

		// chromosome.add(new Device(this.devices.get(this.sourceIndex).x,
		// this.devices.get(this.sourceIndex).y, this.devices.get(this.sourceIndex).r));
		for (int i = 0; i < devices.size(); i++) {
			int chosenIndex = choices.get(this.random.nextInt(choices.size()));
			choices.remove(choices.indexOf(chosenIndex));
			Device chosen = devices.get(chosenIndex);
			chromosome.add(new Device(chosen.x, chosen.y, chosen.r, chosen.n));
		}

		return chromosome;
	}

	private void generateMatingPool() {
		this.matingPool.clear();

		while (this.matingPool.size() < this.populationSize * MATING_RATE) {
			int candidate1 = this.random.nextInt(this.populationSize);
			int candidate2 = this.random.nextInt(this.populationSize);

			if (this.fitness.get(candidate1) < this.fitness.get(candidate2)) {
				if (!this.matingPool.contains(candidate1))
					this.matingPool.add(candidate1);
			} else {
				if (!this.matingPool.contains(candidate2))
					this.matingPool.add(candidate2);
			}
		}
	}

	/**
	 * @param parent1
	 * @param parent2
	 */
	private void crossover(ArrayList<Device> parent1, ArrayList<Device> parent2) {
		// System.out.println("Entering crossover");
		ArrayList<Device> betweenHalf = new ArrayList<>();
		ArrayList<Device> bordersHalf = new ArrayList<>();
		ArrayList<Device> offspring = new ArrayList<>();

		int startPoint = this.random.nextInt(devices.size());
		int endPoint = this.random.nextInt(devices.size());

		while (startPoint == devices.size() - 1)
			startPoint = this.random.nextInt(devices.size());

		while (endPoint <= startPoint)
			endPoint = this.random.nextInt(devices.size());

		doMating(startPoint, endPoint, parent1, parent2, betweenHalf, bordersHalf);
		createOffspring(startPoint, offspring, bordersHalf, betweenHalf);

		addChromosome(offspring);

		betweenHalf = new ArrayList<>();
		bordersHalf = new ArrayList<>();
		offspring = new ArrayList<>();

		doMating(startPoint, endPoint, parent2, parent1, betweenHalf, bordersHalf);
		createOffspring(startPoint, offspring, bordersHalf, betweenHalf);

		addChromosome(offspring);
		// System.out.println("Exiting crossover");
	}

	/**
	 * @param chromosome
	 * @param device1Idx
	 * @param device2Idx
	 */
	private void swapDevices(ArrayList<Device> chromosome, int device1Idx, int device2Idx) {
		Device device1 = new Device(chromosome.get(device1Idx).x, chromosome.get(device1Idx).y,
				chromosome.get(device1Idx).r, chromosome.get(device1Idx).n);
		Device device2 = new Device(chromosome.get(device2Idx).x, chromosome.get(device2Idx).y,
				chromosome.get(device2Idx).r, chromosome.get(device2Idx).n);

		chromosome.set(device1Idx, device2);
		chromosome.set(device2Idx, device1);
	}

	/**
	 * @param other
	 * @return
	 */
	private int selectDevice(int other) {
		int deviceIdx = this.random.nextInt(devices.size());
		while (deviceIdx == other)
			deviceIdx = this.random.nextInt(devices.size());
		return deviceIdx;
	}

	/**
	 * @param startPoint
	 * @param offspring
	 * @param bordersHalf
	 * @param betweenHalf
	 */
	private void createOffspring(int startPoint, ArrayList<Device> offspring, ArrayList<Device> bordersHalf,
			ArrayList<Device> betweenHalf) {
		for (int i = 0; i < startPoint; i++)
			offspring.add(
					new Device(bordersHalf.get(i).x, bordersHalf.get(i).y, bordersHalf.get(i).r, bordersHalf.get(i).n));

		for (Device device : betweenHalf)
			offspring.add(new Device(device.x, device.y, device.r, device.n));

		for (int i = startPoint; i < bordersHalf.size(); i++)
			offspring.add(
					new Device(bordersHalf.get(i).x, bordersHalf.get(i).y, bordersHalf.get(i).r, bordersHalf.get(i).n));
	}

	/**
	 * @param chromosome
	 */
	private void addChromosome(ArrayList<Device> chromosome) {
		this.fitness.add(Measurement.chromosomeFitness(chromosome));
		this.population.add(chromosome);
	}

	public void writeData() {
		try (FileWriter fileWriter = new FileWriter(outputPath);) {
			for (double[] line : this.data) {
				double best = line[0];
				double mean = line[1];
				fileWriter.write(best + "," + mean + "\n");
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	// /**
	// * @param x previously selected x coordinate
	// * @param y previously selected y coordinate
	// * @param d a device with all his information
	// * @return true case (x,y) is inside the device's d radius, false otherwise
	// */
	// private boolean inRadius(double x, double y, Device d) {
	// return Math.sqrt(Math.pow(x - d.x, 2) + Math.pow(y - d.y, 2)) <= d.r;
	// }

	/* THREAD RELATED METHODS */

	private void updateGraphicsBest() {
		Gene.infoLabel.bestFitness = Gene.bestBestFitness;
		Gene.infoLabel.generationFitnessMean = Gene.meanFitness;
		Gene.bestPanel.route = this.population.get(this.bestIndex);
		Gene.infoLabel.updateLabel();
		Gene.bestPanel.repaint();
	}

	private synchronized void updateBest() {
		if (best.size() == 0) {
			Gene.best.addAll(this.population.get(this.bestIndex));
			Gene.bestBestFitness = Measurement.chromosomeFitness(this.population.get(this.bestIndex));
		} else if (this.fitness.get(this.bestIndex) < Gene.bestBestFitness) {
			Gene.best = new ArrayList<>();
			Gene.best.addAll(this.population.get(this.bestIndex));
			Gene.bestBestFitness = this.fitness.get(this.bestIndex);
			updateGraphicsBest();
		}
	}

	private synchronized void updateGraphics() {
		this.mainPanel.route = this.population.get(this.bestIndex);
		this.mainPanel.repaint();
	}
}
