package bcc.paa.util;

import java.util.ArrayList;

public class Measurement {

    public static double chromosomeFitness(ArrayList<Device> path) {
        int size = path.size();
        double total = 0.0;

        for (int i = 0; i < size; i++) {
            Device d1 = path.get(i % size);
            Device d2 = path.get((i + 1) % size);
            total += pointDistance(d1, d2);
        }
        return total;
    }

    public static double pointDistance(Device d1, Device d2) {
        return Math.sqrt(Math.pow(d1.x - d2.x, 2) + Math.pow(d1.y - d2.y, 2));
    }
}
