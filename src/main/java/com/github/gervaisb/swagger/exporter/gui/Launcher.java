package com.github.gervaisb.swagger.exporter.gui;

import javax.swing.*;

public class Launcher {
    static {
        try {
           // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Let it be ugly
        }
    }
    public static void main(String[] args) {
        View view = new View();
        Presenter presenter = new Presenter(view);

        JFrame frame = new JFrame("Swagger Exporter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationByPlatform(true);
        frame.setContentPane(view);
        frame.pack();
        frame.setVisible(true);
    }
}
