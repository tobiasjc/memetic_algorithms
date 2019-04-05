package bcc.paa.algorithm;

import java.awt.BorderLayout;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Random;

import bcc.paa.graphics.InfoLabel;
import bcc.paa.graphics.MainFrame;
import bcc.paa.graphics.MainPanel;
import bcc.paa.util.Device;

/**
 *
 */
public class EvolutionaryAlgorithm {
	private static final int SEED = 1;
	private static final double MUTATION_RATE = 0.02;
	private static final double MATING_RATE = 0.2;
	// private static final double

	private int populationSize;
	private int generationQuantity;
	private int sourceIndex;
	private Random random;
	private double bestFitness;
	private double meanFitness;
	private int bestIndex;
	private int radiusScale;
	private String outputPath;
	private ArrayList<ArrayList<Device>> population;
	private ArrayList<Device> devices;
	private ArrayList<Integer> matingPool;
	private ArrayList<Double> roulette;
	private ArrayList<Double> fitness;
	private MainPanel mainPanel;
	private MainFrame mainFrame;
	private InfoLabel infoLabel;
	private double[][] data;

	/**
	 * @param populationSize     the population size for the end of each generation
	 * @param sourceIndex        the source point, also the last point, of the drone
	 *                           route
	 * @param generationQuantity desired number of generations
	 * @param devices            the devices the drone need to collect data from,
	 *                           with their needed information
	 * @param mainFrame          the main frame observer
	 */
	public EvolutionaryAlgorithm(int populationSize, int sourceIndex, int generationQuantity, ArrayList<Device> devices,
			MainFrame mainFrame, int radiusScale, String outputPath) {
		this.populationSize = populationSize;
		this.generationQuantity = generationQuantity;
		this.sourceIndex = sourceIndex;
		this.random = new Random(SEED);
		this.bestFitness = 0.0;
		this.meanFitness = 0.0;
		this.bestIndex = 0;
		this.radiusScale = radiusScale;
		this.outputPath = outputPath;

		this.population = new ArrayList<>();
		while (this.population.size() < this.populationSize)
			this.population.add(new ArrayList<>());

		this.devices = devices;
		this.matingPool = new ArrayList<>();
		this.roulette = new ArrayList<>();
		this.fitness = new ArrayList<>(this.populationSize);
		this.data = new double[this.generationQuantity][2];

		this.mainPanel = new MainPanel(devices, devices, this.sourceIndex, mainFrame.maxValue);
		this.infoLabel = new InfoLabel(0, 0, 0);
		mainFrame.add(this.mainPanel, BorderLayout.CENTER);
		mainFrame.add(infoLabel, BorderLayout.PAGE_END);
		this.mainFrame = mainFrame;
		mainFrame.revalidate();
	}

	/**
	 * Compute all generations of the evolutionary algorithm.
	 */
	public void run() {
		firstGen();
		mating();
		mutation();
		selection();
		updateBest();
		this.data[0][0] = this.bestFitness;
		this.data[0][1] = this.meanFitness;
		this.mainPanel.repaint();

		for (int i = 0; i < generationQuantity; i++) {
			this.infoLabel.generation = i + 1;
			mating();
			mutation();
			selection();
			updateBest();
			this.mainPanel.route = this.population.get(this.bestIndex);
			this.infoLabel.generationFitnessMean = this.meanFitness;
			this.infoLabel.bestFitness = this.bestFitness;
			this.data[i][0] = this.bestFitness;
			this.data[i][1] = this.meanFitness;
			this.infoLabel.updateLabel();
			this.mainPanel.repaint();
		}
	}

	public void fineFitting() {
		int countExit = 0;
		while (true) {
			int selectedDevice = this.random.nextInt(this.devices.size());

			Device selected = this.population.get(this.bestIndex).get(selectedDevice);
			while (selected.n == this.sourceIndex) {
				selectedDevice = this.random.nextInt(this.devices.size());
				selected = this.population.get(this.bestIndex).get(selectedDevice);
			}

			double lengthBefore = chromosomeFitness(this.population.get(this.bestIndex));

			Device backup = new Device(selected.x, selected.y, selected.r, selected.n);

			double quantityX = this.random.nextDouble();
			move(quantityX, selected, true);
			double quantityY = this.random.nextDouble();
			move(quantityY, selected, false);

			if (checkBbackup(backup, lengthBefore, selected)) {
				countExit = 0;
				this.mainPanel.route = this.population.get(this.bestIndex);
				this.infoLabel.updateLabel();
				this.mainFrame.revalidate();
				this.mainPanel.repaint();
			} else {
				countExit++;
				if (countExit > 10e4)
					break;
			}
		}
	}

	public boolean checkBbackup(Device backup, double lengthBefore, Device selected) {
		double lengthNow = chromosomeFitness(this.population.get(this.bestIndex));
		if (lengthNow > lengthBefore || pointDistance(this.devices.get(selected.n),
				selected) > this.devices.get(selected.n).r * this.radiusScale) {
			selected.x = backup.x;
			selected.y = backup.y;
			return false;
		}
		this.infoLabel.bestFitness = lengthNow;
		return true;
	}

	public void move(double quantity, Device device, boolean onX) {
		boolean side = this.random.nextBoolean();
		if (onX) {
			if (side) {
				device.x += quantity;
			} else {
				device.x -= quantity;
			}
		} else {
			if (side) {
				device.y += quantity;
			} else {
				device.y -= quantity;
			}
		}
	}

	/**
	 *
	 */
	private void mutation() {
		ArrayList<Integer> toMutate = new ArrayList<>();

		for (int i = 0; i < this.population.size() * MUTATION_RATE; i++) {
			int index = this.random.nextInt(this.population.size());
			while (index == this.bestIndex || index < this.populationSize)
				index = this.random.nextInt(this.population.size());
			toMutate.add(index);
		}

		for (int chromosomeIdx : toMutate) {
			int device1Idx = selectDevice(-1);
			int device2Idx = selectDevice(device1Idx);
			swapDevices(this.population.get(chromosomeIdx), device1Idx, device2Idx);
			this.fitness.set(chromosomeIdx, chromosomeFitness(this.population.get(chromosomeIdx)));
		}
	}

	/**
	 * @param chromosome
	 * @param device1Idx
	 * @param device2Idx
	 */
	private void swapDevices(ArrayList<Device> chromosome, int device1Idx, int device2Idx) {
		Device device1 = chromosome.get(device1Idx);
		Device device2 = chromosome.get(device2Idx);

		chromosome.set(device1Idx, new Device(device2.x, device2.y, device2.r, device2.n));
		chromosome.set(device2Idx, new Device(device1.x, device1.y, device1.r, device1.n));
	}

	/**
	 * @param other
	 * @return
	 */
	private int selectDevice(int other) {
		int deviceIdx = this.random.nextInt(this.devices.size());
		while (deviceIdx == other)
			deviceIdx = this.random.nextInt(this.devices.size());
		return deviceIdx;
	}

	/**
	 *
	 */
	private void updateBest() {
		double max = Double.MAX_VALUE;
		this.meanFitness = 0.0;
		int idx = -1;
		for (int i = 0; i < this.population.size(); i++) {
			if (this.fitness.get(i) < max) {
				max = this.fitness.get(i);
				idx = i;
			}
			this.meanFitness += this.fitness.get(i);
		}

		if (idx != this.bestIndex) {
			this.bestIndex = idx;
			this.bestFitness = chromosomeFitness(this.population.get(idx));
		}
		this.meanFitness = this.meanFitness / this.populationSize;
	}

	/**
	 * Generate the first generation by doing a randomized chromosome gene
	 * selection.
	 */
	private void firstGen() {
		for (int i = 0; i < this.populationSize; i++) {
			ArrayList<Device> chromosome = generateChromosome();
			this.population.get(i).addAll(chromosome);
			this.fitness.add(chromosomeFitness(chromosome));
		}
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
	 * @param startPoint
	 * @param offspring
	 * @param bordersHalf
	 * @param betweenHalf
	 */
	public void createOffspring(int startPoint, ArrayList<Device> offspring, ArrayList<Device> bordersHalf,
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
	public void addChromosome(ArrayList<Device> chromosome) {
		this.fitness.add(chromosomeFitness(chromosome));
		this.population.add(chromosome);
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

		int startPoint = this.random.nextInt(this.devices.size());
		int endPoint = this.random.nextInt(this.devices.size());

		while (startPoint == this.devices.size() - 1)
			startPoint = this.random.nextInt(this.devices.size());

		while (endPoint <= startPoint)
			endPoint = this.random.nextInt(this.devices.size());

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
			if (!chosen[idx] && idx != this.bestIndex) {
				chosen[idx] = true;
				this.roulette.remove(idx);
				this.population.remove(idx);
				this.fitness.remove(idx);
				if (idx < this.bestIndex)
					this.bestIndex -= 1;
			}
		}
		// System.out.println("Exiting selection");
	}

	/**
	 * @param d1 a device
	 * @param d2 another deivce
	 * @return the Euclidean distance between device d1 and device d2
	 */
	private double pointDistance(Device d1, Device d2) {
		return Math.sqrt(Math.pow(d1.x - d2.x, 2) + Math.pow(d1.y - d2.y, 2));
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
	 * @return
	 */
	private ArrayList<Device> generateChromosome() {
		ArrayList<Device> chromosome = new ArrayList<>();
		ArrayList<Integer> choices = new ArrayList<>();

		for (int i = 0; i < this.devices.size(); i++)
			choices.add(i);

		// chromosome.add(new Device(this.devices.get(this.sourceIndex).x,
		// this.devices.get(this.sourceIndex).y, this.devices.get(this.sourceIndex).r));
		for (int i = 0; i < this.devices.size(); i++) {
			int chosenIndex = choices.get(this.random.nextInt(choices.size()));
			choices.remove(choices.indexOf(chosenIndex));
			Device chosen = this.devices.get(chosenIndex);
			chromosome.add(new Device(chosen.x, chosen.y, chosen.r, chosen.n));
		}

		return chromosome;
	}

	/**
	 * @param path a certain path to the drone to complete
	 * @return the route length
	 */
	private double chromosomeFitness(ArrayList<Device> path) {
		int size = path.size();
		double total = 0.0;

		for (int i = 0; i < size; i++) {
			Device d1 = path.get(i % size);
			Device d2 = path.get((i + 1) % size);
			total += pointDistance(d1, d2);
		}
		return total;
	}

	public void wirteData() {
		try (FileWriter fileWriter = new FileWriter(this.outputPath);) {
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
}
