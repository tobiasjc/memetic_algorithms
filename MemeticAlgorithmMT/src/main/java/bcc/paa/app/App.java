package bcc.paa.app;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;

import bcc.paa.algorithm.Gene;
import bcc.paa.algorithm.Meme;
import bcc.paa.graphics.InfoLabel;
import bcc.paa.graphics.MainFrame;
import bcc.paa.graphics.MainPanel;
import bcc.paa.util.Device;
import bcc.paa.util.ReadCSV;

public class App {
	private App() {

	}

	public static void main(String[] args) {
		HashMap<String, String> arguments = readArguments(args);

		String inputPath = arguments.get("--inputPath");
		String outputPath = arguments.get("--outputPath");

		int width = Integer.parseInt(arguments.get("--width"));
		int height = Integer.parseInt(arguments.get("--height"));
		int maxValue = Integer.parseInt(arguments.get("--maxValue"));

		int populationSize = Integer.parseInt(arguments.get("--populationSize"));
		int sourceIndex = Integer.parseInt(arguments.get("--sourceIndex"));
		int generationQuantity = Integer.parseInt(arguments.get("--generationQuantity"));

		ReadCSV readCSV = new ReadCSV(inputPath);
		ArrayList<Device> devices;

		if (arguments.containsKey("--readMin") && arguments.containsKey("--readMax")) {
			devices = readCSV.readBetween(Integer.parseInt(arguments.get("--readMin")),
					Integer.parseInt(arguments.get("--readMax")));
		} else {
			devices = readCSV.readAll();
		}

		int islandQuantity = Integer.parseInt(arguments.get("--islandQuantity"));
		ArrayList<Device> best = new ArrayList<>();
		double fitness = Double.MAX_VALUE;

		MainFrame mainFrame = new MainFrame(inputPath, width, height, maxValue);
		mainFrame.setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		MainPanel[] mainPanels = new MainPanel[islandQuantity + 1];

		gridBagConstraints.weightx = 1;
		gridBagConstraints.weighty = 1;
		gridBagConstraints.insets = new Insets(0, 10, 0, 10);
		gridBagConstraints.fill = GridBagConstraints.BOTH;

		gridBagConstraints.gridy = 0;

		gridBagConstraints.gridx = 0;
		mainPanels[0] = new MainPanel(devices, best, sourceIndex, maxValue);
		mainFrame.add(mainPanels[0], gridBagConstraints);

		for (int i = 1; i <= islandQuantity; i++) {
			gridBagConstraints.gridx = i;
			mainPanels[i] = new MainPanel(devices, null, sourceIndex, maxValue);
			mainFrame.add(mainPanels[i], gridBagConstraints);
		}

		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.weighty = 0.02;
		gridBagConstraints.anchor = GridBagConstraints.PAGE_END;

		InfoLabel infoLabel = new InfoLabel(0.0, 0.0);
		mainFrame.add(infoLabel, gridBagConstraints);

		Gene[] memes = new Gene[islandQuantity];
		for (int i = 0; i < islandQuantity; i++) {
			memes[i] = new Gene(best, infoLabel, mainPanels[islandQuantity], populationSize, fitness, sourceIndex,
					generationQuantity, devices, mainPanels[i], outputPath, i);
		}

		Thread[] pool = new Thread[islandQuantity];
		long start = System.currentTimeMillis();
		for (int i = 0; i < islandQuantity; i++) {
			pool[i] = new Thread(memes[i]);
			pool[i].start();
		}

		try {
			for (int i = 0; i < islandQuantity; i++) {
				pool[i].join();
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} finally {
			Meme.run(best, devices, fitness, mainPanels[0], infoLabel, sourceIndex);
		}
		long end = System.currentTimeMillis() - start;
		System.out.println(end);

	}

	private static HashMap<String, String> readArguments(String[] args) {
		if (args.length % 2 != 0) {
			System.err.println("Wrong number of parameters!");
			System.exit(1);
		}

		HashMap<String, String> hashMap = new HashMap<>();

		for (int i = 0; i < args.length; i += 2) {
			hashMap.put(args[i], args[i + 1]);
		}

		return hashMap;
	}
}
