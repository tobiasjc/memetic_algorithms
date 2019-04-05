package bcc.paa.memetic;

import java.util.Random;

public class PickMechanism {

	private double[] values;
	private Random random;
	private double valuesSum;

	/**
	 * @param values
	 * @param seed
	 */
	public PickMechanism(double[] values, int seed) {
		this.values = values;
		this.random = new Random(seed);
		this.valuesSum = 0;
	}

	/**
	 * @param quantity
	 * @param best
	 * @return
	 */
	int[] getRoulette(int quantity, int best) {
		int[] choices = new int[quantity];
		this.valuesSum = 0;

		for (double value : this.values) {
			this.valuesSum += value;
		}

		for (int i = 0; i < choices.length; i++) {
			double amount = this.random.nextDouble() * this.valuesSum * 2;
			int chosen = this.random.nextInt(this.values.length);

			while (true) {
				amount -= this.values[chosen];
				if (amount <= 0) {
					boolean can = true;
					for (int k = 0; k < i; k++) {
						if (choices[k] == chosen) {
							can = false;
							break;
						}
					}
					if (can || chosen != best) {
						choices[i] = chosen;
						break;
					}
					amount = this.random.nextDouble() * this.valuesSum * 2;
				}
				chosen = (chosen + 1) % this.values.length;
			}
		}
		return choices;
	}

	/**
	 * @param quantity
	 * @return
	 */
	int[] getTournament(int quantity) {
		int[] choices = new int[quantity];
		int range = this.values.length;
		int lastChosen = -1;

		for (int i = 0; i < quantity; i++) {
			int start = this.random.nextInt(range);
			int end = this.random.nextInt(range);

			while (start >= end) {
				start = this.random.nextInt(range);
				end = this.random.nextInt(range);
			}

			int chosen = start;
			while (true) {
				for (int j = start + 1; j <= end; j++) {
					if (this.values[chosen] > this.values[j]) {
						chosen = j;
					}
				}
				if (chosen != lastChosen) {
					break;
				} else {
					start = this.random.nextInt(range);
					end = this.random.nextInt(range);
					while (start >= end) {
						start = this.random.nextInt(range);
						end = this.random.nextInt(range);
					}
					chosen = start;
				}
			}

			choices[i] = chosen;
			lastChosen = chosen;
		}

		return choices;
	}
}
