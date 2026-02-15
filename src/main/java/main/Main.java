package main;

import ui.AnalyzerUI;


public class Main {

    public static void main(String[] args) {

        javax.swing.SwingUtilities.invokeLater(() -> {
            new AnalyzerUI().setVisible(true);
        });     
    }
}
