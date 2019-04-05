package bcc.paa.app;

import bcc.paa.algorithm.MemeticAlgorithm;
import bcc.paa.graphics.MainFrame;
import bcc.paa.util.Device;
import bcc.paa.util.ReadCSV;

import java.util.ArrayList;
import java.util.HashMap;

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
            devices = readCSV.readBetween(Integer.parseInt(arguments.get("--readMin")), Integer.parseInt(arguments.get("--readMax")));
        } else {
            devices = readCSV.readAll();
        }

        MainFrame mainFrame = new MainFrame(inputPath, width, height, maxValue);

        MemeticAlgorithm memeticAlgorithm = new MemeticAlgorithm(populationSize, sourceIndex, generationQuantity, devices, mainFrame, outputPath);
        memeticAlgorithm.run();
        memeticAlgorithm.fineFitting();
        memeticAlgorithm.writeData();
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
