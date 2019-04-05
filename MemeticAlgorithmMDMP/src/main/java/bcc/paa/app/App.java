package bcc.paa.app;

import bcc.paa.graphics.InfoLabel;
import bcc.paa.graphics.MainFrame;
import bcc.paa.graphics.MainPanel;
import bcc.paa.memetic.EvolutionaryAlgorithm;
import bcc.paa.memetic.MemeticAlgorithm;
import bcc.paa.structures.Device;
import bcc.paa.structures.Tour;
import bcc.paa.utils.ManipulateData;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// TODO: NAH, YOU CAN'T DO IT BRO.
public class App {

	private static double INCURSION_RATE = 0.03;

	public static void main(String[] args) {
		Random random = new Random(0);
		long start = System.currentTimeMillis();
		HashMap<String, String> argumentsHash = getArgumentsHash(args);

		// program variables
		String inputPath = argumentsHash.get("--inputPath");
		String outputPath = argumentsHash.get("--outputPath");
		int populationSize = Integer.parseInt(argumentsHash.get("--populationSize"));
		int generationQuantity = Integer.parseInt(argumentsHash.get("--generationQuantity"));
		int temperature = Integer.parseInt(argumentsHash.get("--temperature"));
		double coolingRate = Double.parseDouble(argumentsHash.get("--coolingRate"));
		int source = Integer.parseInt(argumentsHash.get("--source"));
		int width = Integer.parseInt(argumentsHash.get("--width"));
		int height = Integer.parseInt(argumentsHash.get("--height"));
		int islandQuantity = Integer.parseInt(argumentsHash.get("--islandQuantity"));
		int ages = Integer.parseInt(argumentsHash.get("--ages"));
		int maxValue = Integer.parseInt(argumentsHash.get("--maxValue"));
		Device[] devices;

		// backend initializing
		if (argumentsHash.containsKey("--readMin") && argumentsHash.containsKey("--readMax")) {
			int readMin = Integer.parseInt(argumentsHash.get("--readMin"));
			int readMax = Integer.parseInt(argumentsHash.get("--readMax"));
			devices = ManipulateData.readBetween(inputPath, readMin, readMax);
		} else {
			devices = ManipulateData.readAll(inputPath);
		}

		// frontend initializing
		MainPanel[] mainPanels = new MainPanel[islandQuantity + 1];
		InfoLabel[] infoLabels = new InfoLabel[islandQuantity + 1];
		for (int i = 0; i < islandQuantity; i++) {
			mainPanels[i] = new MainPanel(devices, null, source, maxValue);
			infoLabels[i] = new InfoLabel(("Population " + i), 0, 0);
		}
		mainPanels[islandQuantity] = new MainPanel(devices, null, source, maxValue);
		infoLabels[islandQuantity] = new InfoLabel("Best selection", 0, 0);

		MainFrame mainFrame = new MainFrame(inputPath, width, height);
		mainFrame.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		int numRow = 4;
		c.insets = new Insets(0, 2, 0, 2);
		c.fill = GridBagConstraints.BOTH;
		c.ipadx = width / (islandQuantity);
		c.weightx = 1;
		c.weighty = 1;

		c.gridy = 0;
		for (int i = 0; i < mainPanels.length; i++) {
			mainFrame.add(mainPanels[i], c);
			c.gridy = (c.gridy + 1) % numRow;
			c.weighty = 0.1;
			mainFrame.add(infoLabels[i], c);
			c.weighty = 1;
			c.gridy = (c.gridy + 1) % numRow;
		}

		EvolutionaryAlgorithm[] evolutionaryAlgorithms = new EvolutionaryAlgorithm[islandQuantity];
		for (int i = 0; i < islandQuantity; i++) {
			evolutionaryAlgorithms[i] = new EvolutionaryAlgorithm(devices,
					(int) Math.ceil(populationSize * random.nextDouble()), generationQuantity, source, mainPanels[i],
					infoLabels[i], i + 1);
			System.out.println("population " + i + "= " + evolutionaryAlgorithms[i].population.length);
		}

		ExecutorService executorService;
		double bestLength = Double.MAX_VALUE;
		App.update(new Tour(devices, source), islandQuantity, infoLabels, mainPanels, generationQuantity);
		Tour best = null;
		for (int i = 0; i < ages; i++) {
			executorService = Executors.newFixedThreadPool(islandQuantity);
			for (int task = 0; task < islandQuantity; task++) {
				executorService.submit(evolutionaryAlgorithms[task]);
			}
			waitExecutor(executorService);
			executorService.shutdown();

			mainFrame.revalidate();
			// we can exchange islands here
			if (islandQuantity > 1) {
				int[][] plan = routePlan(random, islandQuantity, islandQuantity);
				int[] immigrants = generateImmigrants(random, (int) Math.ceil(populationSize * INCURSION_RATE),
						populationSize, evolutionaryAlgorithms, plan);

				for (int[] route : plan) {
					int a = route[0];
					int b = route[1];
					for (int immigrant : immigrants) {
						Tour temp = evolutionaryAlgorithms[a].population[immigrant
								% evolutionaryAlgorithms[a].population.length].getNewInstanceOf();
						evolutionaryAlgorithms[a].population[immigrant
								% evolutionaryAlgorithms[a].population.length] = evolutionaryAlgorithms[b].population[immigrant
										% evolutionaryAlgorithms[b].population.length].getNewInstanceOf();
						evolutionaryAlgorithms[b].population[immigrant
								% evolutionaryAlgorithms[b].population.length] = temp.getNewInstanceOf();
					}
					evolutionaryAlgorithms[a].updateValues();
					evolutionaryAlgorithms[b].updateValues();
				}
			}

			for (int task = 0; task < islandQuantity; task++) {
				System.out.println(evolutionaryAlgorithms[task].best.length);
				if (evolutionaryAlgorithms[task].best.length < bestLength) {
					best = evolutionaryAlgorithms[task].best.getNewInstanceOf();
					bestLength = best.length;
				}
				App.update(best, islandQuantity, infoLabels, mainPanels, generationQuantity);
			}
			for (int k = 0; k < islandQuantity; k++) {
				writeData(evolutionaryAlgorithms[k].data, outputPath, k, i);
			}
			System.out.println("End of age = " + (i + 1));
		}
		mainPanels[islandQuantity].route = best;
		executorService = Executors.newFixedThreadPool(islandQuantity + 1);
		for (int i = 0; i < islandQuantity; i++) {
			executorService.execute(new MemeticAlgorithm(temperature, coolingRate, devices, evolutionaryAlgorithms[i],
					mainPanels[i], infoLabels[i], i));
		}
		MemeticAlgorithm last = new MemeticAlgorithm(temperature, coolingRate, devices, null,
				mainPanels[islandQuantity], infoLabels[islandQuantity], islandQuantity);
		last.start();

		try {
			last.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		waitExecutor(executorService);

		for (int task = 0; task < islandQuantity; task++) {
			if (mainPanels[task].route.length < bestLength) {
				best = mainPanels[task].route.getNewInstanceOf();
				bestLength = mainPanels[task].route.length;
			}
			App.update(best, islandQuantity, infoLabels, mainPanels, generationQuantity);
		}
		App.update(best, islandQuantity, infoLabels, mainPanels, generationQuantity);

		long end = System.currentTimeMillis() - start;
		System.out.println("Execution time = " + end + "ms");
		assert best != null;
		System.out.println("best found length = " + best.length);
	}

	private static void writeData(double[] data, String path, int island, int age) {
		try (BufferedWriter bw = new BufferedWriter(
				new FileWriter(new File(path + "_" + age + "_" + island + ".csv")))) {
			for (double value : data) {
				String toWrite = value + ",\n";
				bw.write(toWrite);
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private static void waitExecutor(ExecutorService executorService) {
		executorService.shutdown();
		try {
			executorService.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private static void update(Tour best, int index, InfoLabel[] infoLabels, MainPanel[] mainPanels,
			int generationQuantity) {
		mainPanels[index].route = best;
		infoLabels[index].bestFitness = best.length;
		infoLabels[index].generationFitnessMean += best.length / generationQuantity;
		infoLabels[index].updateLabel();
		mainPanels[index].repaint();
		mainPanels[index].repaint();
	}

	private static int[][] routePlan(Random random, int islandQuantity, int excursions) {
		int[][] plan = new int[excursions][2];

		for (int i = 0; i < excursions; i++) {
			while (true) {
				boolean canFrom = true;
				int from = random.nextInt(islandQuantity);
				for (int j = 0; j < i; j++) {
					if (from == plan[j][0]) {
						canFrom = false;
						break;
					}
				}
				if (canFrom) {
					plan[i][0] = from;
					break;
				}
			}

			while (true) {
				int to = random.nextInt(islandQuantity);
				if (to != plan[i][0]) {
					plan[i][1] = to;
					break;
				}
			}
		}
		return plan;
	}

	private static int[] generateImmigrants(Random random, int quantity, int populationSize,
			EvolutionaryAlgorithm[] evolutionaryAlgorithms, int[][] plan) {
		int[] immigrants = new int[quantity];

		for (int i = 0; i < quantity; i++) {
			while (true) {
				int generated = random.nextInt(populationSize);
				boolean can = true;
				if (can) {
					immigrants[i] = generated;
					break;
				}
			}
		}

		return immigrants;
	}

	private static HashMap<String, String> getArgumentsHash(String[] args) {
		HashMap<String, String> argumentsHash = new HashMap<>();
		if (args.length % 2 != 0)
			System.err.println("Wrong number of arguments");

		for (int i = 0; i < args.length - 1; i += 2) {
			argumentsHash.put(args[i], args[i + 1]);
		}

		return argumentsHash;
	}
}
