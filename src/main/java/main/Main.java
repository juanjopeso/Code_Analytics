package main;

import io.javalin.Javalin;
import io.javalin.http.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import parser.JavaParserService;
import parser.JavaParserService.ClassAnalysisResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Entry point for the web-server mode.
 *
 * <p>Starts a Javalin HTTP server on port 7070 and serves:
 * <ul>
 *   <li>{@code GET  /}             → serves the frontend (index.html from resources)</li>
 *   <li>{@code POST /api/analyze}  → accepts {@code {"path":"…"}} and returns JSON report</li>
 * </ul>
 *
 * <p>Run with: {@code java -jar CodeQualityAnalyzer.jar}
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final int PORT = 7070;

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            // Serve static files from src/main/resources/public
            config.staticFiles.add("/public");
        }).start(PORT);

        LOGGER.info("CodeQualityAnalyzer running at http://localhost:" + PORT);

        app.post("/api/analyze", Main::handleAnalyze);
    }

    private static void handleAnalyze(Context ctx) {
        AnalyzeRequest req = ctx.bodyAsClass(AnalyzeRequest.class);

        if (req.path() == null || req.path().isBlank()) {
            ctx.status(400).json(new ErrorResponse("El campo 'path' es obligatorio."));
            return;
        }

        File root = new File(req.path());
        if (!root.exists()) {
            ctx.status(404).json(new ErrorResponse("Ruta no encontrada: " + req.path()));
            return;
        }

        try {
            JavaParserService parser = new JavaParserService();
            List<ClassAnalysisResult> results = new ArrayList<>();
            collectJavaFiles(root, parser, results);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            ctx.contentType("application/json").result(gson.toJson(results));

        } catch (IllegalStateException e) {
            // Missing API key etc.
            ctx.status(500).json(new ErrorResponse(e.getMessage()));
        }
    }

    /** Recursively collects and parses all {@code .java} files under {@code dir}. */
    private static void collectJavaFiles(File dir,
                                         JavaParserService parser,
                                         List<ClassAnalysisResult> results) {
        File[] children = dir.listFiles();
        if (children == null) return;

        for (File file : children) {
            if (file.isDirectory()) {
                collectJavaFiles(file, parser, results);
            } else if (file.getName().endsWith(".java")) {
                results.addAll(parser.parseFile(file.getAbsolutePath()));
            }
        }
    }

    // -------------------------------------------------------------------------
    // DTOs
    // -------------------------------------------------------------------------

    record AnalyzeRequest(String path) {}
    record ErrorResponse(String error) {}
}
