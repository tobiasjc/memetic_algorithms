package bcc.paa.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import javax.swing.JPanel;

import bcc.paa.util.Device;

public class MainPanel extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static int sourcePoint;
	public ArrayList<Device> route;
	double maxValue;
	private ArrayList<Device> points;

	public MainPanel(ArrayList<Device> points, ArrayList<Device> route, int sourcePoint, double maxValue) {
		this.points = points;
		this.route = route;
		this.maxValue = maxValue;
		this.setBackground(Color.white);
		MainPanel.sourcePoint = sourcePoint;
	}

	@Override
	public synchronized void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setStroke(new BasicStroke(2));

		// do scaled ratio
		int H = super.getHeight();
		int W = super.getWidth();
		double scaleX = (double) W / this.maxValue;
		double scaleY = (double) H / this.maxValue;
		int pointSize = 6;

		// draw graph things
		for (int idx = 0; idx < this.points.size(); idx++) {
			// get coordinates scaled
			Device d = this.points.get(idx);
			double x = d.x * scaleX;
			double y = d.y * scaleY;
			double rx = d.r * scaleX;
			double ry = d.r * scaleY;

			// draw points as a filled ellipse and radius as a line only ellipse
			if (idx == MainPanel.sourcePoint) {
				int sourceBall = 20;
				g2d.setColor(Color.green);
				g2d.fill(new Ellipse2D.Double(x - (sourceBall >> 1), y - (sourceBall >> 1), sourceBall, sourceBall));
			} else {
				g2d.setColor(Color.blue);
				g2d.fill(new Ellipse2D.Double(x - (pointSize >> 1), y - (pointSize >> 1), pointSize, pointSize));
				g2d.setColor(Color.lightGray);
				g2d.draw(new Ellipse2D.Double(x - rx, y - ry, 2 * rx, 2 * ry));
			}

			// draw the point's number
			g.setColor(Color.black);
			g2d.drawString(String.valueOf(idx), (int) x, (int) y);
		}

		// draw route things
		int size = this.route.size();
		for (int i = 0; i < size; i++) {
			// get scaled points coordinates at the center of the ellipses
			Device p1 = this.route.get(i);
			Device p2 = this.route.get((i + 1) % size);
			double x1 = p1.x * scaleX;
			double y1 = p1.y * scaleY;
			double x2 = p2.x * scaleX;
			double y2 = p2.y * scaleY;

			// draw a thick line from p1 to p2 with a small ellipse on point
			g2d.setColor(Color.red);
			g2d.setStroke(new BasicStroke(2));
			g2d.draw(new Line2D.Double(x1, y1, x2, y2));
			g2d.setColor(Color.black);
			g2d.fill(new Ellipse2D.Double(x2 - (pointSize >> 1), y2 - (pointSize >> 1), pointSize, pointSize));

			// write the number of the vectors
			String number = Integer.toString(i);
			g2d.setFont(new Font("Arial", Font.BOLD, 12));
			g2d.setColor(Color.magenta);
			g2d.drawString(number, Math.abs((float) (x1 + x2) + 10) / 2, Math.abs((float) (y1 + y2) + 30) / 2);
		}
	}
}
