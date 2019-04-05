package bcc.paa.graphics;

import javax.swing.*;

public class MainFrame extends JFrame {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public MainFrame(String title, int width, int height) {
		this.setTitle(title);
		this.setSize(width, height);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(true);
	}

}
