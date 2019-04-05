package bcc.paa.structures;

public class Tour {
	public Device[] path;
	public double length;
	public int source;
	public int filled;

	/**
	 * Instantiate an empty tour.
	 *
	 * @param size the size (number of nodes) of the path
	 */
	public Tour(int size, int source) {
		this.filled = 0;
		this.source = source;
		this.path = new Device[size];
		this.length = Double.MAX_VALUE;
	}

	/**
	 * Create a tour from a device's array.
	 *
	 * @param path   is a path which will be copied into this instance
	 * @param source is the source of the path to be constructed
	 */
	public Tour(Device[] path, int source) {
		this.filled = 0;
		this.source = source;
		this.path = new Device[path.length];
		for (int i = 0; i < path.length; i++) {
			this.path[i] = new Device(path[i]);
			this.filled++;
		}
		this.length = calculateLength();
	}

	/**
	 * Create a tour from another tour.
	 *
	 * @param tour   is a tour to be constructed into this instance
	 * @param source is the source of the path to be constructed
	 */
	public Tour(Tour tour, int source) {
		this.filled = 0;
		Device[] path = tour.path;
		this.source = source;
		this.path = new Device[path.length];
		for (int i = 0; i < path.length; i++) {
			this.path[i] = path[i].getNewInstanceOf();
			this.filled++;
		}
		this.length = tour.length;
	}

	/**
	 * Set a new device on place of another one.
	 *
	 * @param i         index of the device
	 * @param newDevice new device to be placed on the path
	 */
	public void setDevice(int i, Device newDevice) {
		if (this.path[i] != null) {
			if (i == this.path.length - 1) {
				this.length -= getDeviceDistance(this.path[(i - 1) % this.path.length], this.path[i])
						+ getDeviceDistance(this.path[i], this.path[0]);
			} else if (i == 0) {
				this.length -= getDeviceDistance(this.path[this.path.length - 1], this.path[0])
						+ getDeviceDistance(this.path[0], this.path[1]);
			} else {
				this.length -= getDeviceDistance(this.path[(i - 1) % this.path.length], this.path[i])
						+ getDeviceDistance(this.path[i], this.path[(i + 1) % this.path.length]);
			}
		}

		this.path[i] = new Device(newDevice);

		if (this.path[i] != null) {
			if (i == this.path.length - 1) {
				this.length += getDeviceDistance(this.path[(i - 1) % this.path.length], this.path[i])
						+ getDeviceDistance(this.path[i], this.path[0]);
			} else if (i == 0) {
				this.length += getDeviceDistance(this.path[this.path.length - 1], this.path[0])
						+ getDeviceDistance(this.path[0], this.path[1]);
			} else {
				this.length += getDeviceDistance(this.path[(i - 1) % this.path.length], this.path[i])
						+ getDeviceDistance(this.path[i], this.path[(i + 1) % this.path.length]);
			}
		}
	}

	/**
	 * Swap devices of indexes i and j.
	 *
	 * @param i one of the devices
	 * @param j other device
	 */
	public void swapDevices(int i, int j) {
		Device aux = this.path[i].getNewInstanceOf();
		this.setDevice(i, this.path[j].getNewInstanceOf());
		this.setDevice(j, aux);
	}

	/**
	 * Add a device to the indicated index.
	 *
	 * @param i
	 * @param newDevice
	 */
	public void addDevice(int i, Device newDevice) {
		this.path[i] = newDevice.getNewInstanceOf();
		this.filled++;
		if (this.filled == this.path.length) {
			this.length = calculateLength();
		}
	}

	/**
	 * Add a device to the next possible empty slot.
	 *
	 * @param newDevice
	 */
	public void addDevice(Device newDevice) {
		this.path[this.filled++] = newDevice.getNewInstanceOf();
		if (this.filled == this.path.length) {
			this.length = calculateLength();
		}
	}

	/**
	 * Calculate the length of the complete tour.
	 *
	 * @return the length of the complete tour
	 */
	private Double calculateLength() {
		double total = 0;
		int quantity = this.path.length;

		for (int i = 0; i < quantity; i++) {
			total += getDeviceDistance(this.path[i], this.path[(i + 1) % quantity]);
		}

		return total;
	}

	public Tour getNewInstanceOf() {
		return new Tour(this, this.source);
	}

	/**
	 * @param device1 start point to measure
	 * @param device2 ending point to measure
	 * @return the distance
	 */
	private double getDeviceDistance(Device device1, Device device2) {
		return Math.sqrt(Math.pow(device1.x - device2.x, 2) + Math.pow(device1.y - device2.y, 2));
	}
}
