package bcc.paa.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import bcc.paa.structures.Device;

public class ManipulateData {
	/**
	 * @return
	 */
	public static Device[] readAll(String inputPath) {

		int lineCount = getDocumentLines(inputPath);
		Device[] devices = new Device[lineCount - 1];

		try (BufferedReader br = new BufferedReader(new FileReader(inputPath));) {
			String line;
			br.readLine();
			for (int n = 0; (line = br.readLine()) != null; n++) {
				devices[n] = createDevice(line, n);
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}

		return devices;
	}

	/**
	 * @param min
	 * @param max
	 * @return
	 */
	public static Device[] readBetween(String inputPath, int min, int max) {
		Device[] devices;
		int lines = getDocumentLines(inputPath);
		if (lines - 1 > (max - min)) {
			devices = new Device[max - min];
		} else {
			devices = new Device[lines - 1];
		}

		try (BufferedReader br = new BufferedReader(new FileReader(inputPath));) {
			String line;
			br.readLine();
			for (int n = 0; (n < max) && (line = br.readLine()) != null; n++) {
				if (n >= min) {
					int index = n - min;
					devices[index] = createDevice(line, index);
				}
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}

		return devices;
	}

	private static int getDocumentLines(String inputPath) {
		int lineCount = -1;
		try {
			lineCount = (int) Files.lines(Paths.get(inputPath)).count();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lineCount;
	}

	/**
	 * @param line
	 * @param n
	 * @return
	 */
	private static Device createDevice(String line, int n) {
		String[] info = line.split(",");
		double x = Double.parseDouble(info[0]);
		double y = Double.parseDouble(info[1]);
		double r = Double.parseDouble(info[2]) * 100;

		return new Device(x, y, r, n);
	}
}