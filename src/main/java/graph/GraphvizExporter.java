package graph;

import java.io.FileWriter;
import java.io.IOException;

public class GraphvizExporter {

    public static void export(ControlFlowGraph cfg, String fileName) {

        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("digraph CFG {\n");

            for (CFGEdge edge : cfg.getEdges()) {
                writer.write(
                    "  \"" + edge.from + "\" -> \"" + edge.to + "\";\n"
                );
            }

            writer.write("}\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

