package report;

import java.io.FileWriter;
import java.io.IOException;

public class ReportTXT {

    public static void export(String content, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(content);
        } catch (IOException e) {
            System.out.println("Error escribiendo reporte");
        }
    }
}
