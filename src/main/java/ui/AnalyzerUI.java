package ui;

import parser.JavaParserService;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class AnalyzerUI extends JFrame {

    private JTextField pathField;
    private JTextArea outputArea;

    public AnalyzerUI() {

        setTitle("Java Code Analyzer con IA");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {

        JPanel topPanel = new JPanel(new BorderLayout());

        pathField = new JTextField();
        JButton browseButton = new JButton("Seleccionar Carpeta");
        JButton analyzeButton = new JButton("Analizar");

        topPanel.add(pathField, BorderLayout.CENTER);
        topPanel.add(browseButton, BorderLayout.WEST);
        topPanel.add(analyzeButton, BorderLayout.EAST);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // 🔥 Acción botón carpeta
        browseButton.addActionListener(e -> chooseFolder());

        // 🔥 Acción botón analizar
        analyzeButton.addActionListener(e -> analyzeProject());
    }

    private void chooseFolder() {

        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = chooser.getSelectedFile();
            pathField.setText(selectedFolder.getAbsolutePath());
        }
    }

    private void analyzeProject() {

    String path = pathField.getText();

    if (path.isEmpty()) {
        JOptionPane.showMessageDialog(this,
                "Selecciona una carpeta primero.");
        return;
    }

    outputArea.setText("Analizando proyecto...\n");

    SwingWorker<Void, String> worker = new SwingWorker<>() {

        @Override
        protected Void doInBackground() {

            JavaParserService parser = new JavaParserService();
            processDirectory(new File(path), parser);

            return null;
        }

        @Override
        protected void done() {
            outputArea.append("\nAnálisis completado.\n");
            outputArea.append("Reporte generado: analysis_report.json\n");
        }
    };

    worker.execute();
}


    private void processDirectory(File dir,
                                  JavaParserService parser) {

        for (File file : dir.listFiles()) {

            if (file.isDirectory()) {
                processDirectory(file, parser);
            }

            if (file.getName().endsWith(".java")) {
                outputArea.append("Analizando: "
                        + file.getName() + "\n");
                parser.parseFile(file.getAbsolutePath());
            }
        }
    }
}
