package bcc.paa.util;

public class Device implements Comparable<Device> {
	public double x;
	public double y;
	public double r;
	public int n;

	public Device(double x, double y, double r, int n) {
		this.x = x;
		this.y = y;
		this.r = r;
		this.n = n;
	}

	@Override
	public int compareTo(Device o) {
		Device other = (Device) o;
		return (this.x == other.x && this.y == other.y) ? 1 : 0;
	}
}