package bcc.paa.memetic;

import java.util.Random;

import bcc.paa.structures.Device;
import bcc.paa.structures.Tour;

public class EntitiesManagement {
	private static double MATING_RATE = 0.1;
	private static double MUTATION_RATE = 0.01;
	private static double CHANGE_RATE = 0.02;
	public int best;
	private Tour[] entities;
	private Tour[] matingPool;
	private Device[] devices;
	private double[] entitiesValues;
	private Random random;
	private PickMechanism pickMechanism;
	private int source;

	/**
	 * @param entities
	 * @param entitiesValues
	 * @param source
	 * @param devices
	 * @param seed
	 */
	public EntitiesManagement(Tour[] entities, double[] entitiesValues, int source, Device[] devices, int seed) {
		this.entities = entities;
		this.matingPool = new Tour[(int) Math.ceil(devices.length * MATING_RATE)];
		this.devices = devices;
		this.source = source;
		this.random = new Random(seed);
		this.entitiesValues = entitiesValues;
		this.pickMechanism = new PickMechanism(entitiesValues, seed);
	}

	public Tour optSwap(Tour chosen, int i, int k) {
		Tour newTour = new Tour(chosen.filled, chosen.source);

		for (int l = 0; l < i; l++) {
			newTour.addDevice(chosen.path[l].getNewInstanceOf());
		}
		for (int l = k; l >= i; l--) {
			newTour.addDevice(chosen.path[l].getNewInstanceOf());
		}
		for (int l = k + 1; l < chosen.filled; l++) {
			newTour.addDevice(chosen.path[l].getNewInstanceOf());
		}
		return newTour;
	}

	public void memeOver() {
		int choice = this.random.nextInt(this.entities.length);
		Tour chosen = this.entities[choice];

		int i = this.random.nextInt(this.entities[this.best].path.length);
		int k = this.random.nextInt(this.entities[this.best].path.length);

		while (i >= k) {
			i = this.random.nextInt(this.entities[this.best].path.length);
			k = this.random.nextInt(this.entities[this.best].path.length);
		}

		Tour newTour = optSwap(this.entities[choice], i, k);
		if (newTour.length < chosen.length) {
			this.entities[choice] = new Tour(newTour, newTour.source);
		}
	}

	/**
	 *
	 */
	public void selection() {
		int[] choices = pickMechanism.getRoulette(this.matingPool.length, this.best);

		int position = 0;
		for (int choice : choices) {
			this.entities[choice] = new Tour(this.matingPool[position].path, this.source);
			this.entitiesValues[choice] = this.matingPool[position++].length;
		}
	}

	/**
	 *
	 */
	public void mutate() {
		int[] chosen = new int[(int) Math.ceil(this.matingPool.length * MUTATION_RATE)];

		for (int i = 0; i < chosen.length; i++) {
			chosen[i] = this.random.nextInt(this.matingPool.length);
		}
		for (int i : chosen) {
			for (int j = 0; j < (int) Math.ceil(this.devices.length * CHANGE_RATE); j++) {
				int one = this.random.nextInt(this.devices.length);
				int other = this.random.nextInt(this.devices.length);
				this.matingPool[i].swapDevices(one, other);
			}
		}
	}

	/**
	 *
	 */
	public void crossover() {
		int[] parents = pickMechanism.getTournament(this.matingPool.length * 2);

		int matingPosition = 0;
		for (int l = 0; l < matingPool.length; l++, matingPosition += 2) {
			int index1 = parents[matingPosition];
			int index2 = parents[matingPosition + 1];

			Tour parent1 = this.entities[index1];
			Tour parent2 = this.entities[index2];

			Tour offspring = new Tour(this.devices.length, this.source);

			int start = this.random.nextInt(this.devices.length);
			int end = this.random.nextInt(this.devices.length);

			while (start >= end) {
				start = this.random.nextInt(this.devices.length);
				end = this.random.nextInt(this.devices.length);
			}

			Device[] borders = new Device[this.devices.length - (end - start + 1)];

			for (int i = start; i <= end; i++) {
				offspring.addDevice(i, parent1.path[i].getNewInstanceOf());
			}

			int position = 0;
			for (Device device : parent2.path) {
				boolean can = true;
				for (int i = start; i <= end; i++) {
					if (offspring.path[i].compareTo(device) > 0) {
						can = false;
						break;
					}
				}
				if (can) {
					borders[position] = device;
					position++;
				}
			}

			for (int i = 0; i < start; i++) {
				offspring.addDevice(i, borders[i].getNewInstanceOf());
			}

			for (int i = end + 1; i < offspring.path.length; i++) {
				offspring.addDevice(i, borders[i - (end - start) - 1].getNewInstanceOf());
			}

			this.matingPool[l] = new Tour(offspring, this.source);
		}
	}

	/**
	 *
	 */
	public void getBest() {
		double length = Double.MAX_VALUE;
		int pick = -1;
		for (int i = 0; i < this.entitiesValues.length; i++) {
			if (this.entitiesValues[i] < length) {
				length = this.entitiesValues[i];
				pick = i;
			}
		}
		this.best = pick;
	}
}
