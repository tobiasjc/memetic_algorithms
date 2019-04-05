package bcc.paa.app;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;

import bcc.paa.algorithm.EvolutionaryAlgorithm;
import bcc.paa.graphics.MainFrame;
import bcc.paa.util.Device;
import bcc.paa.util.ReadCSV;

/**
 * Hello world!
 */
public final class App {
	private App() {
	}

	/**
	 * Says hello to the world.
	 *
	 * @param args The arguments of the program.
	 */
	public static void main(String[] args) {
		HashMap<String, String> argsHash = getHash(args);
		int radiusScale = Integer.parseInt(argsHash.get("--radiusScale"));
		ReadCSV fileReader = new ReadCSV(argsHash.get("--input"), radiusScale);
		ArrayList<Device> devices = fileReader.readAll();
		MainFrame mainFrame = new MainFrame(argsHash.get("--input"), 1000, 1000,
				Double.parseDouble(argsHash.get("--maxValue")));
		mainFrame.setLayout(new BorderLayout());
		int source = Integer.parseInt(argsHash.get("--source"));
		int generationQuantity = Integer.parseInt(argsHash.get("--generationQuantity"));
		String outputPath = argsHash.get("--output");
		int populationSize = Integer.parseInt(argsHash.get("--populationSize"));
		EvolutionaryAlgorithm evolutionaryAlgorithm = new EvolutionaryAlgorithm(populationSize, source,
				generationQuantity, devices, mainFrame, radiusScale, outputPath);
		evolutionaryAlgorithm.run();
		evolutionaryAlgorithm.fineFitting();
		evolutionaryAlgorithm.wirteData();
	}

	private static HashMap<String, String> getHash(String[] args) {
		HashMap<String, String> readed = new HashMap<>();
		if (args.length % 2 != 0) {
			System.err.println("Wrong number of parameters.");
			System.exit(1);
		}

		for (int i = 0; i < args.length - 1; i++) {
			readed.put(args[i], args[i + 1]);
		}
		return readed;
	}
}
