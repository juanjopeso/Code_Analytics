package parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import ia.AIRecommendationService;
import ia.RecommendationBuilder;
import ia.integration.OpenAIRecommendationService;
import metrics.CyclomaticComplexityMetric;
import metrics.LocMetric;
import metrics.MethodMetrics;
import report.JsonReportExporter;
import rules.EvaluationResult;
import rules.QualityIssue;
import rules.RuleEngine;
import graph.CFGBuilder;
import graph.ControlFlowGraph;
import graph.GraphvizExporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parses Java source files, computes quality metrics, evaluates rules,
 * and (optionally) requests AI-powered refactoring recommendations.
 *
 * <p>Responsibilities kept intentionally narrow: parsing + orchestration.
 * Every heavy concern (metrics, rules, AI, reporting) lives in its own class.
 */
public class JavaParserService {

    private static final Logger LOGGER =
            Logger.getLogger(JavaParserService.class.getName());

    /** Minimum number of "bad" methods that triggers a God-Class warning. */
    private static final int GOD_CLASS_THRESHOLD = 2;

    private final LocMetric locMetric = new LocMetric();
    private final CyclomaticComplexityMetric ccMetric = new CyclomaticComplexityMetric();
    private final RuleEngine ruleEngine = new RuleEngine();
    private final CFGBuilder cfgBuilder = new CFGBuilder();
    private final JsonReportExporter reportExporter = new JsonReportExporter();
    private final AIRecommendationService aiService;

    /**
     * Creates a {@code JavaParserService} that resolves the OpenAI key from
     * the environment variable {@code OPENAI_API_KEY}.
     *
     * @throws IllegalStateException if the environment variable is not set.
     */
    public JavaParserService() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Environment variable OPENAI_API_KEY is not set.");
        }
        this.aiService = new OpenAIRecommendationService(apiKey);
    }

    /**
     * Constructor for dependency injection (useful for tests or alternative AI back-ends).
     */
    public JavaParserService(AIRecommendationService aiService) {
        this.aiService = aiService;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Parses a single {@code .java} file, analyses every class inside it,
     * and appends results to the shared report exporter.
     *
     * @param path absolute or relative path to the Java source file.
     * @return list of {@link ClassAnalysisResult} (one per class found).
     */
    public List<ClassAnalysisResult> parseFile(String path) {
        List<ClassAnalysisResult> results = new ArrayList<>();

        try {
            CompilationUnit cu = StaticJavaParser.parse(new File(path));
            cu.findAll(ClassOrInterfaceDeclaration.class)
              .stream()
              .map(this::analyzeClass)
              .forEach(results::add);

        } catch (FileNotFoundException e) {
            LOGGER.log(Level.WARNING, "File not found: {0}", path);
        }

        return results;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private ClassAnalysisResult analyzeClass(ClassOrInterfaceDeclaration clazz) {
        String className = clazz.getNameAsString();
        List<MethodAnalysisResult> methodResults = new ArrayList<>();
        int badMethodCount = 0;
        int classScore = 100;

        for (MethodDeclaration method : clazz.getMethods()) {
            MethodAnalysisResult methodResult = analyzeMethod(method);
            methodResults.add(methodResult);

            if (!methodResult.evaluation().getIssues().isEmpty()) {
                badMethodCount++;
                classScore -= (100 - methodResult.evaluation().getScore());
            }

            reportExporter.addMethodReport(className, methodResult.metrics(), methodResult.evaluation());
        }

        classScore = Math.max(classScore, 0);

        // God-Class detection
        String aiClassSuggestion = null;
        if (badMethodCount >= GOD_CLASS_THRESHOLD) {
            String prompt = buildGodClassPrompt(className, badMethodCount);
            aiClassSuggestion = aiService.generateRecommendation(prompt);
        }

        reportExporter.export("analysis_report.json");

        return new ClassAnalysisResult(
                className, classScore, badMethodCount, methodResults, aiClassSuggestion);
    }

    private MethodAnalysisResult analyzeMethod(MethodDeclaration method) {
        String methodName = method.getNameAsString();
        int loc = locMetric.calculate(method);
        int cc  = ccMetric.calculate(method);

        MethodMetrics metrics = new MethodMetrics(methodName, loc, cc);
        EvaluationResult evaluation = ruleEngine.evaluate(metrics);

        ControlFlowGraph cfg = cfgBuilder.build(method);
        GraphvizExporter.export(cfg, methodName + ".dot");

        if (!evaluation.getIssues().isEmpty()) {
            String prompt = buildMethodPrompt(metrics, evaluation.getIssues());
            String aiSuggestion = aiService.generateRecommendation(prompt);
            evaluation.getIssues().forEach(issue -> issue.setSuggestion(aiSuggestion));
        }

        return new MethodAnalysisResult(metrics, evaluation, cfg);
    }

    /** Builds the AI prompt for a problematic method. */
    private String buildMethodPrompt(MethodMetrics metrics,
                                     List<QualityIssue> issues) {
        StringBuilder issuesSummary = new StringBuilder();
        for (QualityIssue issue : issues) {
            issuesSummary.append("- ")
                         .append(issue.getType())
                         .append(": ")
                         .append(issue.getDescription())
                         .append("\n");
        }

        return "Análisis de método Java:\n"
             + "Nombre: " + metrics.getMethodName() + "\n"
             + "Líneas de código: " + metrics.getLoc() + "\n"
             + "Complejidad ciclomática: " + metrics.getCyclomaticComplexity() + "\n"
             + "Problemas detectados:\n" + issuesSummary
             + "\nGenera una recomendación técnica de refactorización en español, "
             + "clara y profesional. Máximo 100 palabras. No incluyas ejemplos largos de código.";
    }

    /** Builds the AI prompt for a God-Class. */
    private String buildGodClassPrompt(String className, int badMethodCount) {
        return "Análisis de clase Java:\n"
             + "Nombre de clase: " + className + "\n"
             + "Métodos problemáticos: " + badMethodCount + "\n"
             + "Genera una recomendación arquitectónica en español, clara y profesional. "
             + "Máximo 120 palabras.";
    }

    // -------------------------------------------------------------------------
    // Result records (plain data carriers — no logic)
    // -------------------------------------------------------------------------

    /** Immutable result for a single method analysis. */
    public record MethodAnalysisResult(
            MethodMetrics metrics,
            EvaluationResult evaluation,
            ControlFlowGraph cfg) {}

    /** Immutable result for a whole class analysis. */
    public record ClassAnalysisResult(
            String className,
            int score,
            int badMethodCount,
            List<MethodAnalysisResult> methods,
            String aiClassSuggestion) {

        public boolean isGodClass() {
            return badMethodCount >= GOD_CLASS_THRESHOLD;
        }

        public String level() {
            return score >= 80 ? "BUENO" : score >= 50 ? "REGULAR" : "MALO";
        }
    }
}
