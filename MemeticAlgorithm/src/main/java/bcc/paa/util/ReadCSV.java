package bcc.paa.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class ReadCSV {
    String path;

    public ReadCSV(String path) {
        this.path = path;

    }

    public ArrayList<Device> readAll() {
        ArrayList<Device> devices = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(this.path));) {
            String line;
            line = br.readLine();
            System.out.println("Reading CSV data with format of: (" + line + ")");
            for (int n = 0; (line = br.readLine()) != null; n++) {
                putDevice(devices, line, n);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        return devices;
    }

    public ArrayList<Device> readBetween(int min, int max) {
        ArrayList<Device> devices = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(this.path));) {
            String line;
            line = br.readLine();
            System.out.println("Reading CSV data with format of: (" + line + ")");
            for (int n = 0; (n < max) && (line = br.readLine()) != null; n++) {
                if (n >= min) {
                    putDevice(devices, line, n - min);
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        return devices;
    }

    private void putDevice(ArrayList<Device> got, String line, int n) {
        String[] splitLine = line.split(",");
        double x = Double.parseDouble(splitLine[0]);
        double y = Double.parseDouble(splitLine[1]);
        double r = 10;
        got.add(new Device(x, y, r, n));
    }
}