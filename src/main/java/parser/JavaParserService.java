package parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import metrics.LocMetric;
import metrics.CyclomaticComplexityMetric;
import metrics.MethodMetrics;

import rules.RuleEngine;
import rules.EvaluationResult;
import rules.QualityIssue;

import graph.CFGBuilder;
import graph.ControlFlowGraph;
import graph.GraphvizExporter;

import java.io.File;
import java.io.FileNotFoundException;

import ia.AIRecommendationService;
import ia.integration.OpenAIRecommendationService;

import report.JsonReportExporter;


public class JavaParserService {

    private final LocMetric locMetric = new LocMetric();
    private final CyclomaticComplexityMetric ccMetric = new CyclomaticComplexityMetric();
    private final RuleEngine ruleEngine = new RuleEngine();
    private final CFGBuilder cfgBuilder = new CFGBuilder();
    private final JsonReportExporter reportExporter = new JsonReportExporter();


    private final AIRecommendationService aiService =
            new OpenAIRecommendationService(
                    System.getenv("OPENAI_API_KEY")
            );

    public void parseFile(String path) {

        try {
            CompilationUnit cu = StaticJavaParser.parse(new File(path));

            cu.findAll(ClassOrInterfaceDeclaration.class)
                    .forEach(this::analyzeClass);

        } catch (FileNotFoundException e) {
            System.out.println(" Archivo no encontrado: " + path);
        }
    }

    private void analyzeClass(ClassOrInterfaceDeclaration clase) {

        System.out.println("\n------------------------");
        System.out.println("CLASE: " + clase.getNameAsString());
        System.out.println("--------------------------");

        int classScore = 100;
        int badMethods = 0;

        for (MethodDeclaration method : clase.getMethods()) {

            String methodName = method.getNameAsString();
            int loc = locMetric.calculate(method);
            int cc  = ccMetric.calculate(method);

            MethodMetrics metrics = new MethodMetrics(methodName, loc, cc);
            EvaluationResult result = ruleEngine.evaluate(metrics);

            System.out.println("\n-----------------------");
            System.out.println(" Método: " + methodName);
            System.out.println("   LOC: " + loc + " | CC: " + cc);
            System.out.println("   Puntaje: " + result.getScore());
            System.out.println("   Nivel: " + result.getLevel());

            // CFG
            ControlFlowGraph cfg = cfgBuilder.build(method);
            System.out.println("   CFG:");
            cfg.printIndented("      ");

            if (result.getIssues().isEmpty()) {

                System.out.println("  Sin problemas detectados");

            } else {

                badMethods++;
                classScore -= (100 - result.getScore());

                System.out.println("\n   Problemas detectados:");

                StringBuilder issuesContext = new StringBuilder();

                for (QualityIssue issue : result.getIssues()) {

                    System.out.println("     • " + issue.getType()
                            + " -> " + issue.getDescription());

                    issuesContext.append("- ")
                            .append(issue.getType())
                            .append(": ")
                            .append(issue.getDescription())
                            .append("\n");
                }

                String context =
                        "Análisis de método Java:\n" +
                        "Nombre: " + methodName + "\n" +
                        "Líneas de código: " + loc + "\n" +
                        "Complejidad ciclomática: " + cc + "\n" +
                        "Problemas detectados:\n" +
                        issuesContext +
                        "\nGenera una recomendación técnica de refactorización en español, clara y profesional. " +
                        "Máximo 100 palabras. No incluyas ejemplos largos de código.";

                String aiSuggestion = aiService.generateRecommendation(context);
                for (QualityIssue issue : result.getIssues()) {
                    issue.setSuggestion(aiSuggestion);
                }
                


                System.out.println("\n  Recomendación IA:");
                System.out.println("   ---------------------");
                System.out.println("   " + aiSuggestion);
            }

            GraphvizExporter.export(
                    cfg,
                    clase.getNameAsString() + "_" + methodName + ".dot"
            );
            
            reportExporter.addMethodReport(
                    clase.getNameAsString(),
                    metrics,
                    result
            );
        }

        // 🔥 Evaluación final de clase
        classScore = Math.max(classScore, 0);

        String classLevel =
                classScore >= 80 ? "BUENO" :
                classScore >= 50 ? "REGULAR" : "MALO";

        System.out.println("\n-------------------------");
        System.out.println(" Evaluación Final de Clase");
        System.out.println("   Puntaje: " + classScore);
        System.out.println("   Nivel: " + classLevel);

        if (badMethods >= 2) {

            System.out.println("\n GOD CLASS detectada");
            System.out.println("   Métodos problemáticos: " + badMethods);

            String classContext =
                    "Análisis de clase Java:\n" +
                    "Nombre de clase: " + clase.getNameAsString() + "\n" +
                    "Métodos problemáticos: " + badMethods + "\n" +
                    "Genera una recomendación arquitectónica en español, clara y profesional. " +
                    "Máximo 120 palabras.";

            String classAISuggestion =
                    aiService.generateRecommendation(classContext);

            System.out.println("\nRecomendación Arquitectónica IA:");
            System.out.println("------------------------");
            System.out.println(classAISuggestion);

        } else {
            System.out.println("\n Clase bien estructurada");
        }
        
        reportExporter.export("analysis_report.json");
        System.out.println("-------------------------\n");
    }
}
