package bcc.paa.memetic;

import java.util.Random;

import bcc.paa.graphics.InfoLabel;
import bcc.paa.graphics.MainPanel;
import bcc.paa.structures.Device;
import bcc.paa.structures.Tour;

public class MemeticAlgorithm extends Thread {

	int temperature;
	Random random;
	Device[] devices;
	int index;
	Tour best;
	boolean entities;
	private double coolingRate;
	// private EvolutionaryAlgorithm evolutionaryAlgorithm;
	private MainPanel mainPanel;
	private InfoLabel infoLabel;

	public MemeticAlgorithm(int temperature, double coolingRate, Device[] devices,
			EvolutionaryAlgorithm evolutionaryAlgorithm, MainPanel mainPanel, InfoLabel infoLabel, int seed) {
		this.mainPanel = mainPanel;
		this.infoLabel = infoLabel;
		// this.evolutionaryAlgorithm = evolutionaryAlgorithm;
		this.devices = devices;
		this.temperature = temperature;
		this.random = new Random(seed);
		this.coolingRate = coolingRate;
		this.index = 0;
		this.best = this.mainPanel.route;

	}

	public void run() {
		annealing();
		opt();
		reduce();
		update();
	}

	public double pointDistance(Device one, Device other) {
		return Math.sqrt(Math.pow(one.x - other.x, 2) + Math.pow(one.y - other.y, 2));
	}

	private void update() {
		this.mainPanel.route = this.best;
		this.infoLabel.bestFitness = this.best.length;
		this.infoLabel.updateLabel();
		this.mainPanel.repaint();
	}

	private void move(int select, double step, boolean onx, Tour best) {
		boolean up = this.random.nextBoolean();
		Device device = best.path[select].getNewInstanceOf();
		if (onx) {
			if (up) {
				device.x += step;
			} else {
				device.x -= step;
			}
		} else {
			if (up) {
				device.y += step;
			} else {
				device.y -= step;
			}
		}
		best.setDevice(select, device);
	}

	private void reduce() {
		int count = 0;
		do {
			int select = this.random.nextInt(best.filled);
			while (best.path[select].n == best.source) {
				select = this.random.nextInt(best.filled);
			}

			Device backup = best.path[select].getNewInstanceOf();

			double lengthBefore = best.length;

			double step = this.random.nextDouble();
			move(select, step, true, best);
			step = this.random.nextDouble();
			move(select, step, false, best);

			double lengthAfter = best.length;

			if (lengthAfter > lengthBefore || pointDistance(best.path[select], this.devices[backup.n]) > backup.r) {
				best.setDevice(select, backup);
				count++;
			} else {
				update();
				count = 0;
			}
		} while (count < 10e4);
	}

	private double acceptabilityFunction(double energyBefore, double energyAfter) {
		if (energyAfter < energyBefore) {
			return 1.0;
		}
		return Math.exp((energyBefore - energyAfter) / temperature);
	}

	private void annealing() {
		Tour currentSolution = new Tour(this.best, this.best.source);
		while (this.temperature > 1) {
			Tour newSolution = new Tour(currentSolution, currentSolution.source);

			int firstPosition = this.random.nextInt(currentSolution.filled);
			int secondPosition = this.random.nextInt(currentSolution.filled);

			newSolution.swapDevices(firstPosition, secondPosition);

			double energyBefore = currentSolution.length;
			double energyAfter = newSolution.length;

			if (acceptabilityFunction(energyBefore, energyAfter) > this.random.nextDouble()) {
				currentSolution = new Tour(newSolution, newSolution.source);
			}

			if (currentSolution.length < best.length) {
				update();
			}

			this.temperature *= 1 - this.coolingRate;
		}
	}

	private Tour optSwap(Tour best, int i, int k) {
		Tour newTour = new Tour(best.filled, best.source);

		for (int l = 0; l < i; l++) {
			newTour.addDevice(best.path[l].getNewInstanceOf());
		}
		for (int l = k; l >= i; l--) {
			newTour.addDevice(best.path[l].getNewInstanceOf());
		}
		for (int l = k + 1; l < best.filled; l++) {
			newTour.addDevice(best.path[l].getNewInstanceOf());
		}
		return newTour;
	}

	public void opt() {
		boolean improved = true;
		while (improved) {
			improved = false;
			for (int i = 1; i < best.filled - 1; i++) {
				for (int k = i + 1; k < best.filled; k++) {
					Tour newTour = optSwap(best, i, k);
					if (newTour.length < best.length) {
						best = new Tour(newTour, newTour.source);
						improved = true;
					}
				}
			}
			update();
		}
	}
}
