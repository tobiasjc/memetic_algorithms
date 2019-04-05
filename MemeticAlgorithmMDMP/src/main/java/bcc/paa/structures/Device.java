package bcc.paa.structures;

public class Device implements Comparable<Device> {
	public final double r;
	public final int n;
	public double x;
	public double y;

	/**
	 * @param x
	 * @param y
	 * @param r
	 * @param n
	 */
	public Device(double x, double y, double r, int n) {
		this.x = x;
		this.y = y;
		this.r = r;
		this.n = n;
	}

	/**
	 * @param o
	 */
	public Device(Device o) {
		this.x = o.x;
		this.y = o.y;
		this.r = o.r;
		this.n = o.n;
	}

	/**
	 * @return
	 */
	public Device getNewInstanceOf() {
		return new Device(this.x, this.y, this.r, this.n);
	}

	/**
	 * @param o
	 * @return
	 */
	@Override
	public int compareTo(Device o) {
		Device other = (Device) o;
		if (other.x == this.x && other.y == this.y && this.n == other.n) {
			return 1;
		}
		return 0;
	}
}
