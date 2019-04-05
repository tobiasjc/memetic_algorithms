package bcc.paa.graphics;

import javax.swing.*;

public class MainFrame extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public double maxValue;

    public MainFrame(String title, int widht, int height, double maxValue) {
        this.setTitle(title);
        this.maxValue = maxValue;
        this.setSize(widht, height);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }

}
