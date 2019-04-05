package bcc.paa.algorithm;

import bcc.paa.graphics.InfoLabel;
import bcc.paa.graphics.MainPanel;
import bcc.paa.util.Device;
import bcc.paa.util.Measurement;

import java.util.ArrayList;
import java.util.Random;

public class Meme {

    private static ArrayList<Device> best;
    private static ArrayList<Device> devices;
    private static double fitness;
    private static MainPanel mainPanel;
    private static InfoLabel infoLabel;
    private static Random random;
    private static int sourceIndex;

    public static void run(ArrayList<Device> best, ArrayList<Device> devices, double fitness, MainPanel mainPanel, InfoLabel infoLabel, int sourceIndex) {
        Meme.devices = devices;
        Meme.best = best;
        Meme.fitness = fitness;
        Meme.mainPanel = mainPanel;
        Meme.infoLabel = infoLabel;
        Meme.sourceIndex = sourceIndex;
        random = new Random(0);
        opt();
        fineFitting();
    }

    private static void optSwap(ArrayList<Device> path, ArrayList<Device> chosen, int i, int j) {
        for (int l = 0; l < i; l++) {
            Device copy = chosen.get(l % best.size());
            path.add(new Device(copy.x, copy.y, copy.r, copy.n));
        }
        for (int l = j; l >= i; l--) {
            Device copy = chosen.get(l % best.size());
            path.add(new Device(copy.x, copy.y, copy.r, copy.n));
        }
        for (int l = j + 1; l < best.size(); l++) {
            Device copy = chosen.get(l % best.size());
            path.add(new Device(copy.x, copy.y, copy.r, copy.n));
        }
    }


    private static void opt() {
        boolean better = true;
        while (better) {
            better = isBetter(Meme.best);
            updateGraphics();
        }
    }

    private static boolean isBetter(ArrayList<Device> chosen) {
        boolean toReturn = false;
        for (int i = 1; i < best.size() - 1; i++) {
            for (int j = i + 1; j < devices.size(); j++) {
                ArrayList<Device> maybe = new ArrayList<>();

                optSwap(maybe, chosen, i, j);
                double afterDistance = Measurement.chromosomeFitness(maybe);

                if (afterDistance < Meme.fitness) {
                    chosen.clear();
                    chosen.addAll(maybe);
                    Meme.fitness = afterDistance;
                    toReturn = true;
                }
            }
        }
        return toReturn;
    }


    private static void fineFitting() {
        int countExit = 0;
        while (true) {
            int selectedDevice = Meme.random.nextInt(devices.size());

            Device selected = Meme.best.get(selectedDevice);
            while (selected.n == Meme.sourceIndex) {
                selectedDevice = Meme.random.nextInt(devices.size());
                selected = Meme.best.get(selectedDevice);
            }

            double lengthBefore = Meme.fitness;

            Device backup = new Device(selected.x, selected.y, selected.r, selected.n);

            double quantityX = Meme.random.nextDouble();
            move(quantityX, selected, true);
            double quantityY = Meme.random.nextDouble();
            move(quantityY, selected, false);

            if (checkBackup(backup, lengthBefore, selected)) {
                countExit = 0;
            } else {
                countExit++;
                if (countExit > 10e4)
                    break;
            }
            updateGraphics();
        }
    }

    private static void updateGraphics() {
        Meme.infoLabel.bestFitness = Meme.fitness;
        Meme.mainPanel.route = Meme.best;
        Meme.mainPanel.repaint();
        Meme.infoLabel.updateLabel();
    }

    private static boolean checkBackup(Device backup, double lengthBefore, Device selected) {
        double lengthNow = Measurement.chromosomeFitness(Meme.best);
        if (lengthNow > lengthBefore || Measurement.pointDistance(devices.get(selected.n), selected) > devices.get(selected.n).r) {
            selected.x = backup.x;
            selected.y = backup.y;
            return false;
        }
        fitness = lengthNow;
        return true;
    }

    private static void move(double quantity, Device device, boolean onX) {
        boolean side = Meme.random.nextBoolean();
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
}
